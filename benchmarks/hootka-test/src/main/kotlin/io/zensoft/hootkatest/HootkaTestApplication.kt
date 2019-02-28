package io.zensoft.hootkatest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HootkaTestApplication

fun main(args: Array<String>) {
    runApplication<HootkaTestApplication>(*args)
}
