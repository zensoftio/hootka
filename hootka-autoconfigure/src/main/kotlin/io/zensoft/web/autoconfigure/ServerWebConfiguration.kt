package io.zensoft.web.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import freemarker.template.Template
import io.zensoft.web.api.*
import io.zensoft.web.api.internal.handler.BaseRequestProcessor
import io.zensoft.web.api.internal.http.DefaultSessionHandler
import io.zensoft.web.api.internal.http.InMemorySessionStorage
import io.zensoft.web.api.internal.mapper.*
import io.zensoft.web.api.internal.provider.*
import io.zensoft.web.api.internal.resource.ClasspathResourceHandler
import io.zensoft.web.api.internal.response.FileResponseResolver
import io.zensoft.web.api.internal.response.FreemarkerResponseResolver
import io.zensoft.web.api.internal.response.JsonResponseResolver
import io.zensoft.web.api.internal.security.DefaultRememberMeService
import io.zensoft.web.api.internal.security.DefaultSecurityProvider
import io.zensoft.web.api.internal.server.HttpChannelInitializer
import io.zensoft.web.api.internal.server.HttpControllerHandler
import io.zensoft.web.api.internal.server.HttpServer
import io.zensoft.web.api.internal.validation.DefaultValidationProvider
import io.zensoft.web.api.model.SimpleAuthenticationDetails
import io.zensoft.web.autoconfigure.property.FreemarkerPathProperties
import io.zensoft.web.autoconfigure.property.WebConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(WebConfig::class, FreemarkerPathProperties::class)
class ServerWebConfiguration(
    private val freemarkerPathProperties: FreemarkerPathProperties,
    private val applicationContext: ApplicationContext,
    private val webConfig: WebConfig
) {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper::class)
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerModule(JavaTimeModule())
    }

    @Bean
    @ConditionalOnMissingBean(freemarker.template.Configuration::class)
    fun freemarkerConfiguration(): freemarker.template.Configuration {
        val configuration = object : freemarker.template.Configuration(freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS) {
            override fun getTemplate(name: String?): Template {
                return super.getTemplate("$name${freemarkerPathProperties.suffix}")
            }
        }
        configuration.setClassLoaderForTemplateLoading(this.javaClass.classLoader, freemarkerPathProperties.prefix)
        return configuration
    }

    // Session

    @Bean
    fun sessionStorage(): SessionStorage
        = InMemorySessionStorage(webConfig.session.cookieName, webConfig.session.cookieMaxAge)

    @Bean
    fun sessionHandler(): SessionHandler
        = DefaultSessionHandler(sessionStorage(), webConfig.session.cookieName)

    // Security
    // TODO make it optional

    @Bean
    @ConditionalOnProperty()
    fun userDetailsService(): UserDetailsService = object: UserDetailsService {
        override fun findUserDetailsByUsername(value: String): UserDetails? {
            return null
        }
    }

    @Bean
    fun defaultRememberMeService(): DefaultRememberMeService
        = DefaultRememberMeService(
            webConfig.security.rememberMeTokenName,
            webConfig.security.rememberMeTokenMaxAge,
            webConfig.security.rememberMeSalt,
            userDetailsService()
        )

    // TODO fun securityExpressionExecutor(): SecurityExpressionExecutor = SecurityExpressionExecutor()

    @Bean
    fun securityProvider(): SecurityProvider<SimpleAuthenticationDetails>
        = DefaultSecurityProvider(sessionStorage(), userDetailsService())

    // Request Processor

    @Bean
    fun requestProcessor(): BaseRequestProcessor
        = BaseRequestProcessor(
            methodHandlerProvider(),
            exceptionHandlerProvider(),
            sessionHandler(),
            handlerParameterMapperProvider(),
            responseResolverProvider(),
            staticResourcesProvider()
        )

    // Server

    @Bean
    fun httpControllerHandler(): HttpControllerHandler
        = HttpControllerHandler(requestProcessor())

    @Bean
    fun httpChannelInitializer(): HttpChannelInitializer
        = HttpChannelInitializer(httpControllerHandler())

    @Bean
    fun httpServer(): HttpServer
        = HttpServer(webConfig.port, httpChannelInitializer())

    // Request Mappers

    @Bean
    fun modelAttributeMapper(): ModelAttributeMapper
        = ModelAttributeMapper()

    @Bean
    fun nettyMultipartFileMapper(): NettyMultipartFileMapper
        = NettyMultipartFileMapper()

    @Bean
    fun nettyMultipartObjectMapper(): NettyMultipartObjectMapper
        = NettyMultipartObjectMapper()

    @Bean
    fun pathVariableMapper(): PathVariableMapper
        = PathVariableMapper()

    @Bean
    fun principalMapper(): PrincipalMapper
        = PrincipalMapper(securityProvider())

    @Bean
    fun requestBodyMapper(): RequestBodyMapper
        = RequestBodyMapper(objectMapper())

    @Bean
    fun requestParameterMapper(): RequestParameterMapper
        = RequestParameterMapper()

    @Bean
    fun validMapper(): ValidMapper
        = ValidMapper()

    // Response Resolvers

    @Bean
    fun fileResponseResolver(): FileResponseResolver
        = FileResponseResolver()

    @Bean
    fun freemarkerResponseResolver(): FreemarkerResponseResolver
        = FreemarkerResponseResolver(freemarkerConfiguration())

    @Bean
    fun jsonResponseResolver(): JsonResponseResolver
        = JsonResponseResolver(objectMapper())

    // Static Resource Handlers

    @Bean
    fun classpathResourceHandler(): StaticResourceHandler
        = ClasspathResourceHandler("/**", "static")

    // Validation

    @Bean
    fun defaultValidationProvider(): ValidationProvider
        = DefaultValidationProvider()

    // Providers

    @Bean
    fun staticResourcesProvider(): StaticResourcesProvider
        = StaticResourcesProvider(applicationContext, webConfig.static.cachedResourceExpiry)

    @Bean
    fun responseResolverProvider(): ResponseResolverProvider
        = ResponseResolverProvider(applicationContext)

    @Bean
    fun handlerParameterMapperProvider(): HandlerParameterMapperProvider
        = HandlerParameterMapperProvider(applicationContext, defaultValidationProvider())

    @Bean
    fun methodHandlerProvider(): MethodHandlerProvider
        = MethodHandlerProvider(applicationContext, handlerParameterMapperProvider())

    @Bean
    fun exceptionHandlerProvider(): ExceptionHandlerProvider
        = ExceptionHandlerProvider(applicationContext, handlerParameterMapperProvider())
}