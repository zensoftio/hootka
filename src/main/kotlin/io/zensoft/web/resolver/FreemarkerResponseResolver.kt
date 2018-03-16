package io.zensoft.web.resolver

import freemarker.template.Configuration
import freemarker.template.Version
import io.zensoft.web.properties.FreemarkerPathProperties
import io.zensoft.web.support.MimeType
import io.zensoft.web.support.ViewModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.StringWriter
import javax.annotation.PostConstruct

@Component
class FreemarkerResponseResolver(
    private val properties: FreemarkerPathProperties
): ResponseResolver {

    private lateinit var freemarkerConfig: Configuration

    override fun supportsContentType(contentType: MimeType): Boolean = MimeType.TEXT_HTML == contentType

    override fun resolveResponseBody(result: Any, handlerArgs: Array<Any?>): ByteArray {
        if (result !is String) throw IllegalArgumentException("String return type should be for html view response methods")
        val viewModel = handlerArgs.find { it != null && it::class.java.isAssignableFrom(ViewModel::class.java) } as ViewModel
        val template = freemarkerConfig.getTemplate("${properties.prefix}$result${properties.suffix}")
        val out = StringWriter()
        val attributes = viewModel.getAttributes()
        template.process(attributes, out)
        return out.toString().toByteArray()
    }

    @PostConstruct
    private fun init() {
        freemarkerConfig = Configuration(Version("2.3.23"))
        freemarkerConfig.setClassForTemplateLoading(this::class.java, "/")
    }


}