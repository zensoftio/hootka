package io.zensoft.web.api.internal.utils

import java.math.BigDecimal

object NumberUtils {

    fun parseNumber(value: String, type: Class<*>): Any {
        return when (type.kotlin) {
            String::class -> value
            Int::class -> value.toInt()
            Long::class -> value.toLong()
            BigDecimal::class -> value.toBigDecimal()
            Float::class -> value.toFloat()
            Double::class -> value.toDouble()
            Byte::class -> value.toByte()
            Short::class -> value.toShort()
            else -> throw IllegalArgumentException("Wrong method parameter specified: ${type.name}")
        }
    }

}