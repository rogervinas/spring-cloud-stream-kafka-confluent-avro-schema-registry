package com.rogervinas.stream

import com.example.Sensor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.util.function.Consumer

@SpringBootApplication
class Application {
  @Bean
  fun myConsumer(): (Sensor) -> Unit = { println("Consumed $it") }
}

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}
