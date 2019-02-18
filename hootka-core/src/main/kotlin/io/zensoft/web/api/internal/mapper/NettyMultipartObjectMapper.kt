package io.zensoft.web.api.internal.mapper

import io.netty.buffer.ByteBufInputStream
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.multipart.Attribute
import io.netty.handler.codec.http.multipart.FileUpload
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import io.netty.handler.codec.http.multipart.InterfaceHttpData
import io.zensoft.web.annotation.MultipartObject
import io.zensoft.web.api.HttpRequestMapper
import io.zensoft.web.api.internal.support.HandlerMethodParameter
import io.zensoft.web.api.internal.support.HttpHandlerMetaInfo
import io.zensoft.web.api.internal.support.RequestContext
import io.zensoft.web.api.internal.utils.NumberUtils
import io.zensoft.web.api.model.InMemoryFile
import org.springframework.stereotype.Component
import javax.validation.Valid
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaType

@Component
class NettyMultipartObjectMapper : HttpRequestMapper {

    override fun supportsAnnotation(annotations: List<Annotation>): Boolean {
        return annotations.find { it is MultipartObject } != null
    }

    override fun createValue(parameter: HandlerMethodParameter, context: RequestContext, handlerMethod: HttpHandlerMetaInfo): Any? {
        val multipartContent = HttpPostRequestDecoder(context.request.getWrappedRequest() as FullHttpRequest)
        val annotation = parameter.annotation!! as MultipartObject
        val ctor = parameter.clazz.kotlin.primaryConstructor!!
        val args = mutableListOf<Any?>()
        for (field in ctor.parameters) {
            val content = multipartContent.getBodyHttpData(field.name)
            if (null == content) {
                args.add(null)
                continue
            }
            args.add(createFieldValue(content, annotation, field))
        }
        val javaCtor = ctor.javaConstructor
        return javaCtor!!.newInstance(*args.toTypedArray())
    }

    override fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter {
        val annotation = annotations.find { it is MultipartObject }
        val validationRequired = annotations.find { it is Valid } != null
        return HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>,
            parameter.type.isMarkedNullable, annotation, validationRequired)
    }

    private fun extractExtension(fileName: String): String = fileName.substring(fileName.lastIndexOf('.') + 1)

    private fun createFieldValue(data: InterfaceHttpData, annotation: MultipartObject, field: KParameter): Any {
        return when (data.httpDataType) {
            InterfaceHttpData.HttpDataType.FileUpload -> {
                val fileUpload = data as FileUpload
                val extension = extractExtension(fileUpload.filename)
                if (annotation.acceptExtensions.isNotEmpty() && !annotation.acceptExtensions.contains(extension)) {
                    throw IllegalArgumentException("Unsupported file type with extension $extension")
                }
                InMemoryFile(fileUpload.filename, ByteBufInputStream(fileUpload.byteBuf))
            }
            InterfaceHttpData.HttpDataType.Attribute -> {
                val attribute = data as Attribute
                val parameterType = field.type.javaType as Class<*>
                if (parameterType.isEnum) {
                    parameterType.getDeclaredMethod("valueOf", String::class.java).invoke(null, attribute.value.toUpperCase())
                } else {
                    NumberUtils.parseNumber(attribute.value, parameterType)
                }
            }
            else -> throw IllegalStateException("Unprocessed data type")
        }
    }

}