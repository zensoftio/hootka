package io.zensoft.hootka.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import freemarker.template.Template
import io.zensoft.hootka.api.*
import io.zensoft.hootka.api.internal.handler.BaseRequestProcessor
import io.zensoft.hootka.api.internal.http.DefaultSessionHandler
import io.zensoft.hootka.api.internal.http.InMemorySessionStorage
import io.zensoft.hootka.api.internal.mapper.*
import io.zensoft.hootka.api.internal.provider.*
import io.zensoft.hootka.api.internal.resource.ClasspathResourceHandler
import io.zensoft.hootka.api.internal.response.FileResponseResolver
import io.zensoft.hootka.api.internal.response.FreemarkerResponseResolver
import io.zensoft.hootka.api.internal.response.JsonResponseResolver
import io.zensoft.hootka.api.internal.security.DefaultRememberMeService
import io.zensoft.hootka.api.internal.security.DefaultSecurityProvider
import io.zensoft.hootka.api.internal.security.SecurityExpressionExecutor
import io.zensoft.hootka.api.internal.server.HttpChannelInitializer
import io.zensoft.hootka.api.internal.server.HttpControllerHandler
import io.zensoft.hootka.api.internal.server.HttpServer
import io.zensoft.hootka.api.internal.validation.DefaultValidationProvider
import io.zensoft.hootka.api.model.SimpleAuthenticationDetails
import io.zensoft.hootka.autoconfigure.property.WebConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(WebConfig::class)
class ServerWebConfiguration(
    private val applicationContext: ApplicationContext,
    private val webConfig: WebConfig
) {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper::class)
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerModule(JavaTimeModule()).registerKotlinModule()
    }

    @Bean
    @ConditionalOnMissingBean(freemarker.template.Configuration::class)
    fun freemarkerConfiguration(): freemarker.template.Configuration {
        val configuration = object : freemarker.template.Configuration(freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS) {
            override fun getTemplate(name: String?): Template {
                return super.getTemplate("$name${webConfig.freemarker.suffix}")
            }
        }
        configuration.setClassLoaderForTemplateLoading(this.javaClass.classLoader, webConfig.freemarker.prefix)
        return configuration
    }

    // Session

    @Bean
    @ConditionalOnMissingBean(SessionStorage::class)
    fun sessionStorage(): SessionStorage
        = InMemorySessionStorage(webConfig.session.cookieName, webConfig.session.cookieMaxAge)

    @Bean
    @ConditionalOnMissingBean(SessionHandler::class)
    fun sessionHandler(): SessionHandler
        = DefaultSessionHandler(sessionStorage(), webConfig.session.cookieName)

    // Security

    @Bean
    @ConditionalOnBean(UserDetailsService::class)
    fun rememberMeService(): RememberMeService
        = DefaultRememberMeService(
        webConfig.security.rememberMeTokenName,
        webConfig.security.rememberMeTokenMaxAge,
        webConfig.security.rememberMeSalt,
        applicationContext.getBean(UserDetailsService::class.java)
    )

    @Bean
    @ConditionalOnBean(UserDetailsService::class)
    fun securityProvider(): SecurityProvider<SimpleAuthenticationDetails>
        = DefaultSecurityProvider(sessionStorage(), applicationContext.getBean(UserDetailsService::class.java), rememberMeService())

    @Bean
    @ConditionalOnBean(SecurityProvider::class, SecurityExpressionInitializer::class)
    fun securityExpressionExecutor(): SecurityExpressionExecutor
        = SecurityExpressionExecutor(securityProvider(), applicationContext.getBean(SecurityExpressionInitializer::class.java))

    // Request Processor

    @Bean
    @ConditionalOnMissingBean(BaseRequestProcessor::class)
    fun requestProcessor(): BaseRequestProcessor = BaseRequestProcessor(
        methodHandlerProvider(),
        exceptionHandlerProvider(),
        sessionHandler(),
        handlerParameterMapperProvider(),
        responseResolverProvider(),
        staticResourcesProvider()
    )

    // Server

    @Bean
    @ConditionalOnMissingBean(HttpControllerHandler::class)
    fun httpControllerHandler(): HttpControllerHandler = HttpControllerHandler(requestProcessor())

    @Bean
    @ConditionalOnMissingBean(HttpChannelInitializer::class)
    fun httpChannelInitializer(): HttpChannelInitializer = HttpChannelInitializer(httpControllerHandler())

    @Bean
    @ConditionalOnMissingBean(HttpServer::class)
    fun httpServer(): HttpServer = HttpServer(webConfig.port, httpChannelInitializer())

    // Request Mappers

    @Bean
    @ConditionalOnMissingBean(ModelAttributeMapper::class)
    fun modelAttributeMapper(): ModelAttributeMapper = ModelAttributeMapper()

    @Bean
    @ConditionalOnMissingBean(NettyMultipartFileMapper::class)
    fun nettyMultipartFileMapper(): NettyMultipartFileMapper = NettyMultipartFileMapper()

    @Bean
    @ConditionalOnMissingBean(NettyMultipartObjectMapper::class)
    fun nettyMultipartObjectMapper(): NettyMultipartObjectMapper = NettyMultipartObjectMapper()

    @Bean
    @ConditionalOnMissingBean(PathVariableMapper::class)
    fun pathVariableMapper(): PathVariableMapper = PathVariableMapper()

    @Bean
    @ConditionalOnBean(SecurityProvider::class)
    fun principalMapper(): PrincipalMapper = PrincipalMapper(securityProvider())

    @Bean
    @ConditionalOnMissingBean(RequestBodyMapper::class)
    fun requestBodyMapper(): RequestBodyMapper = RequestBodyMapper(objectMapper())

    @Bean
    @ConditionalOnMissingBean(RequestParameterMapper::class)
    fun requestParameterMapper(): RequestParameterMapper = RequestParameterMapper()

    @Bean
    @ConditionalOnMissingBean(ValidMapper::class)
    fun validMapper(): ValidMapper = ValidMapper()

    // Response Resolvers

    @Bean
    @ConditionalOnMissingBean(FileResponseResolver::class)
    fun fileResponseResolver(): FileResponseResolver = FileResponseResolver()

    @Bean
    @ConditionalOnMissingBean(FreemarkerResponseResolver::class)
    fun freemarkerResponseResolver(): FreemarkerResponseResolver = FreemarkerResponseResolver(freemarkerConfiguration())

    @Bean
    @ConditionalOnMissingBean(JsonResponseResolver::class)
    fun jsonResponseResolver(): JsonResponseResolver = JsonResponseResolver(objectMapper())

    // Static Resource Handlers

    @Bean
    @ConditionalOnMissingBean(StaticResourceHandler::class)
    fun classpathResourceHandler(): StaticResourceHandler
        = ClasspathResourceHandler("/**", "static")

    // Validation

    @Bean
    @ConditionalOnMissingBean(ValidationProvider::class)
    fun defaultValidationProvider(): ValidationProvider = DefaultValidationProvider()

    // Providers

    @Bean
    @ConditionalOnMissingBean(StaticResourcesProvider::class)
    fun staticResourcesProvider(): StaticResourcesProvider
        = StaticResourcesProvider(applicationContext, webConfig.static.cachedResourceExpiry)

    @Bean
    @ConditionalOnMissingBean(ResponseResolverProvider::class)
    fun responseResolverProvider(): ResponseResolverProvider = ResponseResolverProvider(applicationContext)

    @Bean
    @ConditionalOnMissingBean(HandlerParameterMapperProvider::class)
    fun handlerParameterMapperProvider(): HandlerParameterMapperProvider
        = HandlerParameterMapperProvider(applicationContext, defaultValidationProvider())

    @Bean
    @ConditionalOnMissingBean(MethodHandlerProvider::class)
    fun methodHandlerProvider(): MethodHandlerProvider
        = MethodHandlerProvider(applicationContext, handlerParameterMapperProvider())

    @Bean
    @ConditionalOnMissingBean(ExceptionHandlerProvider::class)
    fun exceptionHandlerProvider(): ExceptionHandlerProvider
        = ExceptionHandlerProvider(applicationContext, handlerParameterMapperProvider())
}