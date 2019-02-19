package io.zensoft.web.api.internal.response

import freemarker.template.Configuration
import io.zensoft.web.api.HttpResponseResolver
import io.zensoft.web.api.WrappedHttpResponse
import io.zensoft.web.api.model.MimeType
import io.zensoft.web.api.model.ViewModel
import java.io.StringWriter

class FreemarkerResponseResolver(
    private var freemarkerConfig: Configuration
) : HttpResponseResolver {

    override fun supportsContentType(contentType: MimeType): Boolean = MimeType.TEXT_HTML == contentType

    override fun resolveResponseBody(result: Any, handlerArgs: Array<Any?>, response: WrappedHttpResponse): ByteArray {
        if (result !is String) throw IllegalArgumentException("String return type should be for html view response methods")
        val viewModel = handlerArgs.find { it != null && it is ViewModel } as? ViewModel ?: ViewModel()
        val template = freemarkerConfig.getTemplate(result)
        val out = StringWriter()
        val attributes = viewModel.getAttributes()
        template.process(attributes, out)
        return out.toString().toByteArray()
    }

}