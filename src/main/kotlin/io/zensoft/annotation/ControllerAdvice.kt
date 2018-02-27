package io.zensoft.annotation

import org.springframework.stereotype.Component

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Component
annotation class ControllerAdvice