package io.zensoft.web.mapper

import io.netty.buffer.ByteBufInputStream
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.multipart.Attribute
import io.netty.handler.codec.http.multipart.FileUpload
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import io.netty.handler.codec.http.multipart.InterfaceHttpData
import io.zensoft.web.annotation.MultipartFile
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import io.zensoft.web.support.InMemoryFile
import org.springframework.stereotype.Component
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

@Component
class MultipartFileMapper : HttpRequestMapper {

    override fun supportsAnnotation(annotation: Annotation): Boolean = annotation is MultipartFile

    override fun mapValue(parameter: HandlerMethodParameter, request: FullHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any {
        val multipartContent = HttpPostRequestDecoder(request)
        val files = mutableListOf<InMemoryFile>()
        while (multipartContent.hasNext()) {
            val content = multipartContent.next()
            when(content.httpDataType) {
                InterfaceHttpData.HttpDataType.FileUpload -> {
                    val fileUpload = content as FileUpload
                    files.add(InMemoryFile(fileUpload.filename, ByteBufInputStream(fileUpload.byteBuf)))
                }
                else -> throw IllegalStateException("Unprocessed data type")
            }

        }
        return files.first()
    }

    override fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter {
        val annotation = annotations.find { it is MultipartFile }
        return HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>,
            parameter.type.isMarkedNullable, annotation)
    }


}