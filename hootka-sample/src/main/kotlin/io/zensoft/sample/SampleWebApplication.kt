package io.zensoft.sample

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class SampleWebApplication

fun main(args: Array<String>) {
    SpringApplication.run(SampleWebApplication::class.java, *args)
}