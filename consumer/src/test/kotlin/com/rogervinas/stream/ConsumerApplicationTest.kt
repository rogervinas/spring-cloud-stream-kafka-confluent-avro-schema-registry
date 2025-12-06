package com.rogervinas.stream

import com.example.Sensor
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Test
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.wait.strategy.Wait.forListeningPort
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.time.Duration
import java.util.Properties
import java.util.UUID


@SpringBootTest
@Testcontainers
class ConsumerApplicationTest {

  companion object {
    private const val BROKER = "broker"
    private const val BROKER_PORT = 9092
    private const val SCHEMA_REGISTRY = "schema-registry"
    private const val SCHEMA_REGISTRY_PORT = 8081
    private const val SENSOR_TOPIC = "sensor-topic"

    private val TIMEOUT = Duration.ofSeconds(5)

    @Container
    val container = ComposeContainer(File("../docker-compose.yml"))
      .withExposedService(BROKER, BROKER_PORT, forListeningPort())
      .withExposedService(SCHEMA_REGISTRY, SCHEMA_REGISTRY_PORT, forListeningPort())
  }

  @MockitoBean(name = "myConsumer")
  private lateinit var myConsumer: (Sensor) -> Unit

  @Test
  fun `should consume sensor v1 message`() {
    val id = UUID.randomUUID().toString()
    val temperature = 34.98f
    val acceleration = 9.81f
    val velocity = 15.73f

    val recordV1 = createRecord(
      schema = """
      {
        "namespace" : "com.example",
        "type" : "record",
        "name" : "Sensor",
        "fields" : [
          {"name":"id","type":"string"},
          {"name":"temperature", "type":"float", "default":0.0},
          {"name":"acceleration", "type":"float","default":0.0},
          {"name":"velocity","type":"float","default":0.0}
        ]
      }
    """.trimIndent()
    ).apply {
      put("id", id)
      put("temperature", temperature)
      put("acceleration", acceleration)
      put("velocity", velocity)
    }

    produceRecord(id, recordV1)

    verify(myConsumer, timeout(TIMEOUT.toMillis()))
      .invoke(Sensor(id, temperature, 0f, acceleration, velocity))
  }

  @Test
  fun `should consume sensor v2 message`() {
    val id = UUID.randomUUID().toString()
    val internalTemperature = 34.98f
    val externalTemperature = 54.16f
    val acceleration = 9.81f
    val velocity = 15.73f

    val recordV2 = createRecord(
      schema = """
      {
        "namespace" : "com.example",
        "type" : "record",
        "name" : "Sensor",
        "fields" : [
          {"name":"id","type":"string"},
          {"name":"internalTemperature", "type":"float", "default":0.0},
          {"name":"externalTemperature", "type":"float", "default":0.0},
          {"name":"acceleration", "type":"float","default":0.0},
          {"name":"velocity","type":"float","default":0.0},
          {"name":"accelerometer","type":["null",{"type":"array","items":"float"}]},
          {"name":"magneticField","type":["null",{"type":"array","items":"float"}]}
        ]
      }
    """.trimIndent()
    ).apply {
      put("id", id)
      put("internalTemperature", internalTemperature)
      put("externalTemperature", externalTemperature)
      put("acceleration", acceleration)
      put("velocity", velocity)
      put("accelerometer", listOf(1.1f, 2.2f, 3.3f))
      put("magneticField", listOf(4.4f, 5.5f, 6.6f))
    }

    produceRecord(id, recordV2)

    verify(myConsumer, timeout(TIMEOUT.toMillis()))
      .invoke(Sensor(id, internalTemperature, externalTemperature, acceleration, velocity))
  }

  private fun createRecord(schema: String): GenericRecord {
    val parser = Schema.Parser()
    return GenericData.Record(parser.parse(schema))
  }

  private fun produceRecord(key: String, record: GenericRecord) {
    val producerProperties = Properties().apply {
      this[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:$BROKER_PORT"
      this[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
      this[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
      this["schema.registry.url"] = "http://localhost:$SCHEMA_REGISTRY_PORT"
    }

    KafkaProducer<String, GenericRecord>(producerProperties).use { producer ->
      producer.send(ProducerRecord(SENSOR_TOPIC, key, record))
    }
  }
}
