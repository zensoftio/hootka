package io.zensoft.web.api.exceptions

class InvalidRememberMeTokenException: AuthenticationFailedException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}