import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "2.7.2" apply false
  id("io.spring.dependency-management") version "1.0.12.RELEASE"
  id("com.github.davidmc24.gradle.plugin.avro") version "1.3.0"
  id("org.jetbrains.kotlin.jvm") version "1.6.21"
  id("org.jetbrains.kotlin.plugin.spring") version "1.6.21"
}

group = "com.rogervinas"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

allprojects {
  repositories {
    mavenCentral()
    maven {
      url = uri("https://packages.confluent.io/maven")
    }
  }
}

subprojects {
  apply(plugin = "org.springframework.boot")
  apply(plugin = "io.spring.dependency-management")
  apply(plugin = "com.github.davidmc24.gradle.plugin.avro")
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "org.jetbrains.kotlin.plugin.spring")

  extra["springCloudVersion"] = "2021.0.3"

  dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")

    implementation("io.confluent:kafka-avro-serializer:5.3.0")
    implementation("org.apache.avro:avro:1.11.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
  }

  dependencyManagement {
    imports {
      mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      jvmTarget = "11"
    }
  }

  tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
      events(PASSED, SKIPPED, FAILED)
      exceptionFormat = FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true
    }
  }
}
