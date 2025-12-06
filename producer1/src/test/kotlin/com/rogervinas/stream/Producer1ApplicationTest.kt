package com.rogervinas.stream

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.wait.strategy.Wait.forListeningPort
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.time.Duration
import java.util.Properties
import java.util.function.Consumer
import kotlin.random.Random

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(RandomTestConfiguration::class)
@Testcontainers
class Producer1ApplicationTest {

  @LocalServerPort
  private var serverPort: Int = 0

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

@Test
fun `should produce sensor v1 message`() {
  KafkaConsumer<String, GenericRecord>(consumerProperties()).use { consumer ->
    // Subscribe to topic
    consumer.subscribe(listOf(SENSOR_TOPIC))

    // Consume previous messages (just in case)
    consumer.poll(TIMEOUT)

    // Produce one message
    WebTestClient
      .bindToServer()
      .baseUrl("http://localhost:$serverPort")
      .build()
      .post().uri("/messages").exchange()
      .expectStatus().isOk
      .expectBody(String::class.java).isEqualTo("ok, have fun with v1 payload!")

    // Consume message
    assertThat(consumer.poll(TIMEOUT)).singleElement().satisfies(Consumer { record ->
      val value = record.value()
      assertThat(value["id"]).isEqualTo("2376-v1")
      assertThat(value["temperature"]).isEqualTo(33.067642f)
      assertThat(value["acceleration"]).isEqualTo(3.2810485f)
      assertThat(value["velocity"]).isEqualTo(84.885544f)
    })
  }
}

  private fun consumerProperties() = Properties().apply {
    this[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:$BROKER_PORT"
    this[ConsumerConfig.GROUP_ID_CONFIG] = "group1"
    this[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"
    this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = "io.confluent.kafka.serializers.KafkaAvroDeserializer"
    this["schema.registry.url"] = "http://localhost:$SCHEMA_REGISTRY_PORT"
    this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
  }
}

@Configuration
class RandomTestConfiguration {

  @Bean @Primary
  fun randomTest() = Random(0)
}
