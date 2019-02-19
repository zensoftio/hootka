package io.zensoft.web.api.internal.mapper

import io.netty.buffer.ByteBufInputStream
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.multipart.FileUpload
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import io.netty.handler.codec.http.multipart.InterfaceHttpData
import io.zensoft.web.annotation.MultipartFile
import io.zensoft.web.api.HttpRequestMapper
import io.zensoft.web.api.internal.support.HandlerMethodParameter
import io.zensoft.web.api.internal.support.HttpHandlerMetaInfo
import io.zensoft.web.api.internal.support.RequestContext
import io.zensoft.web.api.model.InMemoryFile
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

class NettyMultipartFileMapper : HttpRequestMapper {

    override fun supportsAnnotation(annotations: List<Annotation>): Boolean {
        return annotations.find { it is MultipartFile } != null
    }

    override fun createValue(parameter: HandlerMethodParameter, context: RequestContext, handlerMethod: HttpHandlerMetaInfo): Any {
        val multipartContent = HttpPostRequestDecoder(context.request.getWrappedRequest() as FullHttpRequest)
        val files = mutableListOf<InMemoryFile>()
        val annotation = parameter.annotation!! as MultipartFile
        while (multipartContent.hasNext()) {
            val content = multipartContent.next()
            when (content.httpDataType) {
                InterfaceHttpData.HttpDataType.FileUpload -> {
                    val fileUpload = content as FileUpload
                    val extension = extractExtension(fileUpload.filename)
                    if (annotation.acceptExtensions.isNotEmpty() && !annotation.acceptExtensions.contains(extension)) {
                        throw IllegalArgumentException("Unsupported file type with extension $extension")
                    }
                    files.add(InMemoryFile(fileUpload.filename, ByteBufInputStream(fileUpload.byteBuf)))
                }
                else -> throw IllegalStateException("Unprocessed data type")
            }

        }
        return if (parameter.clazz.isArray) {
            return files.toTypedArray()
        } else {
            files.first()
        }
    }

    override fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter {
        val annotation = annotations.find { it is MultipartFile }
        return HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>,
            parameter.type.isMarkedNullable, annotation)
    }

    private fun extractExtension(fileName: String): String = fileName.substring(fileName.lastIndexOf('.') + 1)


}