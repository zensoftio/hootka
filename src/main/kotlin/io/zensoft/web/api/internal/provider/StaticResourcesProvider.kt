package io.zensoft.web.api.internal.provider

import io.zensoft.web.api.StaticResourceHandler
import io.zensoft.web.api.WrappedHttpRequest
import io.zensoft.web.api.WrappedHttpResponse
import io.zensoft.web.api.internal.utils.ResourceMimeTypeUtils
import io.zensoft.web.api.model.HttpMethod
import io.zensoft.web.api.model.HttpStatus
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import javax.annotation.PostConstruct

@Component
class StaticResourcesProvider(
    private val applicationContext: ApplicationContext,
    @Value("\${app.static.cache.max-age:28800}") private val resourceMaxAge: Long
) {

    companion object {
        private const val CACHE_CONTROL_HEADER = "Cache-Control"
    }

    private val pathMatcher = AntPathMatcher()
    private val resourceProviders = HashMap<String, StaticResourceHandler>()

    fun handleStaticResource(request: WrappedHttpRequest, response: WrappedHttpResponse): Boolean {
        if (HttpMethod.GET == request.getMethod()) {
            val path = request.getPath()
            val resourceHandler = resourceProviders.entries.find { pathMatcher.match(it.key, path) }?.value
            if (resourceHandler != null) {
                val resourcePath = pathMatcher.extractPathWithinPattern(resourceHandler.getPath(), path)
                val resultFile = resourceHandler.findResource(resourcePath)
                if (resultFile != null) {
                    val responseBody = IOUtils.toByteArray(resultFile)
                    response.mutate(HttpStatus.OK, ResourceMimeTypeUtils.resolveMimeType(resourcePath), responseBody)
                    if (resourceHandler.isCacheable()) {
                        response.setHeader(CACHE_CONTROL_HEADER, "max-age=$resourceMaxAge")
                    }
                    return true
                }
            }
        }
        return false
    }

    @PostConstruct
    private fun init() {
        val resourceHandlers = applicationContext.getBeansOfType(StaticResourceHandler::class.java).values
        resourceHandlers.forEach {
            val path = it.getPath()
            if (!resourceProviders.containsKey(path)) {
                resourceProviders[path] = it
            } else {
                throw IllegalStateException("There is two mappings on static resources with path: $path")
            }
        }
    }

}