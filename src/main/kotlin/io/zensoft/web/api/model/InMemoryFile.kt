package io.zensoft.web.api.model

import java.io.InputStream

class InMemoryFile(
    val name: String,
    val stream: InputStream
)