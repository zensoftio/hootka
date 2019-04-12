package io.zensoft.hootka.api.model

import java.io.InputStream

class InMemoryFile(
    val name: String,
    val stream: InputStream
)