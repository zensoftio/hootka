package io.zensoft.hootka.api.exceptions

class PreconditionNotSatisfiedException(
    message: String,
    val viewLogin: Boolean = false
) : RuntimeException(message)