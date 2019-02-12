package io.zensoft.web.annotation

import org.springframework.stereotype.Controller

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Controller
annotation class Controller