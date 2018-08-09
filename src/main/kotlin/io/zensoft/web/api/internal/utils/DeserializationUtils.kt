package io.zensoft.web.api.internal.utils

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.isKotlinClass
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.lang.reflect.Field
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaType

object DeserializationUtils {

    private val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    fun createBeanFromQueryString(beanClass: Class<*>, queryParams: Map<String, List<String>>): Any {
        val args = mutableMapOf<String, Any>()

        if (beanClass.isKotlinClass()) {
            val fields = getFields(beanClass)
            for (field in fields) {
                val type = field.type
                val clazz = if (type is ParameterizedTypeImpl) {
                    type.rawType as Class<*>
                } else {
                    type as Class<*>
                }
                val value = getFieldValue(field.name!!, clazz, queryParams) ?: continue
                args.put(field.name!!, value)
            }
        } else {
            val fields = getFields(beanClass)
            for (field in fields) {
                val value = getFieldValue(field.name, field.type, queryParams) ?: continue
                args.put(field.name, value)
            }
        }
        return mapper.convertValue(args, beanClass)
    }

    private fun getFields(beanClass: Class<*>): List<Field> {
        val result = mutableListOf<Field>()
        val superClass = beanClass.superclass
        result.addAll(beanClass.declaredFields)
        if (superClass != null) {
            result.addAll(getFields(superClass))
        }
        return result
    }

    private fun getFieldValue(name: String, type: Class<*>, queryParams: Map<String, List<String>>): Any? {
        val queryValues = queryParams[name] ?: return null
        val values = mutableListOf<String>()
        for (value in queryValues) {
            val actualValues = value.split(',')
            return if (type.kotlin.isSubclassOf(Iterable::class) || type.isArray) {
                values.addAll(actualValues)
                values
            } else {
                if (actualValues.size > 1) {
                    throw IllegalArgumentException("Specified more than one value for $name argument")
                }
                actualValues.first()
            }
        }
        return null
    }

}