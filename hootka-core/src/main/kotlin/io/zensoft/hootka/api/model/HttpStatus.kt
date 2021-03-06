package io.zensoft.hootka.api.model

import io.netty.handler.codec.http.HttpResponseStatus

enum class HttpStatus(
    val value: HttpResponseStatus
) {
    OK(HttpResponseStatus.OK),
    FOUND(HttpResponseStatus.FOUND),
    NOT_FOUND(HttpResponseStatus.NOT_FOUND),
    BAD_REQUEST(HttpResponseStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED(HttpResponseStatus.METHOD_NOT_ALLOWED),
    INTERNAL_SERVER_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR),
    FORBIDDEN(HttpResponseStatus.FORBIDDEN),
    UNAUTHORIZED(HttpResponseStatus.UNAUTHORIZED),
    CONFLICT(HttpResponseStatus.CONFLICT),
    MOVED_PERMANENTLY(HttpResponseStatus.MOVED_PERMANENTLY)
}