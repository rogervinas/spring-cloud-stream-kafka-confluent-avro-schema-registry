# Spring Cloud Stream & Kafka Schema Registry

Demo using Spring Cloud Stream & Confluent Schema Registry & Confluent Avro Serializers based on [this sample](https://github.com/spring-cloud/spring-cloud-stream-samples/tree/main/schema-registry-samples/schema-registry-confluent-avro-serializer).

## Run

* Start:
```shell
docker compose up -d

./gradlew consumer:bootRun
./gradlew producer1:bootRun
./gradlew producer2:bootRun
```

* Allow [all schema changes](https://docs.confluent.io/platform/current/schema-registry/avro.html#schema-evolution-and-compatibility):
```shell
curl -X PUT http://127.0.0.1:8081/config -d '{"compatibility": "NONE"}' -H "Content-Type:application/json"
```

* Confluent Control Center available at http://localhost:9021

* Produce v1 payload:
```shell
curl -X POST http://localhost:9001/messages
```

* Produce v2 payload:
```shell
curl -X POST http://localhost:9002/messages
```

* Alternatively you can consume with [kcat](https://docs.confluent.io/platform/current/app-development/kafkacat-usage.html):
```shell
kcat -b localhost:9092 -t sensor-topic -s avro -r http://localhost:8081
```

## Links

* [Confluent All-in-One Docker Compose](https://github.com/confluentinc/cp-all-in-one/blob/7.2.1-post/cp-all-in-one/docker-compose.yml)
* [Spring Initialzr](https://start.spring.io/#!type=gradle-project&language=kotlin&platformVersion=2.7.2&packaging=jar&jvmVersion=11&groupId=com.rogervinas.springcloudstream.kafka&artifactId=consumer&name=consumer&description=Spring%20Cloud%20Stream%20Kafka%20Consumer&packageName=com.rogervinas.springcloudstream.kafka.consumer&dependencies=cloud-stream)
* [Spring Cloud Schema Registry](https://docs.spring.io/spring-cloud-schema-registry/docs/current/reference/html/spring-cloud-schema-registry.html)




