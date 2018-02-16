package io.zensoft.config

import org.mockito.Mockito

fun <T> any(clazz: Class<T>): T = Mockito.any<T>(clazz)