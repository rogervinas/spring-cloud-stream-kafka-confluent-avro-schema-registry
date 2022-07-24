package com.rogervinas.stream

import com.example.Sensor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
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

  private val version = "v2"
  private val unbounded: BlockingQueue<Sensor> = LinkedBlockingQueue()
  private val random = Random(System.nanoTime())

  @Bean
  fun supplier() = Supplier { unbounded.poll() }

  @RequestMapping(value = ["/messages"], method = [RequestMethod.POST])
  fun sendMessage(): String {
    unbounded.offer(randomSensor())
    return "ok, have fun with $version payload!"
  }

  private fun randomSensor() = Sensor().apply {
    this.id = randomUUID().toString() + "-$version"
    this.acceleration = random.nextFloat() * 10
    this.velocity = random.nextFloat() * 100
    this.internalTemperature = random.nextFloat() * 50
    this.externalTemperature = random.nextFloat() * 50
    this.accelerometer = (1..random.nextInt(0, 5)).map { Random.nextFloat() * 10 }
    this.magneticField = (1..random.nextInt(0, 5)).map { Random.nextFloat() }
  }
}

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}
