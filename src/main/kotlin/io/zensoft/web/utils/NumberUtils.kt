package io.zensoft.web.utils

import java.math.BigDecimal

object NumberUtils {

    fun parseNumber(value: String, type: Class<*>): Any {
        return when (type) {
            String::class.java -> value
            Int::class.java -> value.toInt()
            Long::class.java -> value.toLong()
            BigDecimal::class.java -> value.toBigDecimal()
            Float::class.java -> value.toFloat()
            Double::class.java -> value.toDouble()
            Byte::class.java -> value.toByte()
            Short::class.java -> value.toShort()
            else -> throw IllegalArgumentException("Wrong method parameter specified: ${type.name}")
        }
    }

}