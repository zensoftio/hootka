package io.zensoft.web.api.exceptions

class PreconditionNotSatisfiedException(
    message: String,
    val viewLogin: Boolean = false
) : RuntimeException(message)