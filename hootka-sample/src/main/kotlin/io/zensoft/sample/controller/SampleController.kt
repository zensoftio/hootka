package io.zensoft.sample.controller

import io.zensoft.web.annotation.Controller
import io.zensoft.web.annotation.RequestMapping

@Controller
class SampleController {

    @RequestMapping(["/api/greet"])
    fun greet(): String {
        return "Greetings!!!"
    }

}