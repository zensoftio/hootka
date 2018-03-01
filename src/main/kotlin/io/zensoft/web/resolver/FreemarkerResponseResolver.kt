package io.zensoft.web.resolver

import freemarker.template.Configuration
import freemarker.template.Version
import io.zensoft.web.support.MimeType
import io.zensoft.web.support.ViewModel
import org.springframework.stereotype.Component
import java.io.StringWriter
import javax.annotation.PostConstruct

@Component
class FreemarkerResponseResolver(
//    @Value("\${freemarker.path.prefix}") private val pathPrefix: String,
//    @Value("\${freemarker.path.suffix}") private val pathSuffix: String
): ResponseResolver {

    private val pathPrefix: String = "templates/"
    private val pathSuffix: String = ".ftl"

    private lateinit var freemarkerConfig: Configuration

    override fun supportsContentType(contentType: MimeType): Boolean = MimeType.TEXT_HTML == contentType

    override fun resolveResponseBody(result: Any, handlerArgs: Array<Any>): String {
        if (result !is String) throw IllegalArgumentException("String return type should be for html view response methods")
        val viewModel = handlerArgs.find { it::class.java.isAssignableFrom(ViewModel::class.java) } as ViewModel
        val template = freemarkerConfig.getTemplate("$pathPrefix${result as String}$pathSuffix")
        val out = StringWriter()
        val attributes = viewModel.getAttributes()
        template.process(attributes, out)
        return out.toString()
    }

    @PostConstruct
    private fun init() {
        freemarkerConfig = Configuration(Version("2.3.23"))
        freemarkerConfig.setClassForTemplateLoading(this::class.java, "/")
    }


}