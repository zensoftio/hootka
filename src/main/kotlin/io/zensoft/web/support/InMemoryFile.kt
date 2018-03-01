package io.zensoft.web.support

import java.io.InputStream

class InMemoryFile(
    val name: String,
    val stream: InputStream
)