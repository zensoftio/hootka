package io.zensoft.sample.domain

import io.zensoft.hootka.api.model.InMemoryFile

data class MultiObject(
    val file: InMemoryFile,
    val description: String
)