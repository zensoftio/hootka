package io.zensoft.webfluxtest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebfluxTestApplication

fun main(args: Array<String>) {
    runApplication<WebfluxTestApplication>(*args)
}
