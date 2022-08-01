package com.rogervinas.stream

import com.example.Sensor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.UUID.randomUUID
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.function.Supplier
import kotlin.random.Random

@SpringBootApplication
@RestController
class Application {

  private val version = "v1"
  private val unbounded: BlockingQueue<Sensor> = LinkedBlockingQueue()
  @Autowired private lateinit var random: Random

  @Bean
  fun supplier() = Supplier { unbounded.poll() }

  @RequestMapping(value = ["/messages"], method = [RequestMethod.POST])
  fun sendMessage(): String {
    unbounded.offer(randomSensor())
    return "ok, have fun with $version payload!"
  }

  private fun randomSensor() = Sensor().apply {
    this.id = random.nextInt(1000, 9999).toString() + "-$version"
    this.acceleration = random.nextFloat() * 10
    this.velocity = random.nextFloat() * 100
    this.temperature = random.nextFloat() * 50
  }
}

@Configuration
class RandomConfiguration {
  @Bean
  fun random() = Random(System.nanoTime())
}

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}
