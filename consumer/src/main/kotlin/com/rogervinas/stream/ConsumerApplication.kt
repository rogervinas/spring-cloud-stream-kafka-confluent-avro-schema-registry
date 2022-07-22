package com.rogervinas.stream

import com.example.Sensor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.util.function.Consumer

@SpringBootApplication
class Application {
  @Bean
  fun process() = Consumer { input: Sensor -> println("Consumed $input") }
}

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}
