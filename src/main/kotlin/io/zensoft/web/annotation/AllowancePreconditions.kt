package io.zensoft.web.annotation

import org.springframework.stereotype.Component

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Component
annotation class AllowancePreconditions(
    val name: String
)