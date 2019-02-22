package io.zensoft.tomcattest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TomcattestApplication

fun main(args: Array<String>) {
    runApplication<TomcattestApplication>(*args)
}