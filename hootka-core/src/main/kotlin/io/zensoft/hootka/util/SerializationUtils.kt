package io.zensoft.hootka.util

import java.io.*

object SerializationUtils {

    fun serialize(obj: Serializable): ByteArray {
        val out = ByteArrayOutputStream(512)
        ObjectOutputStream(out).writeObject(obj)
        return out.toByteArray()
    }

    fun deserialize(bytes: ByteArray): Serializable = ObjectInputStream(ByteArrayInputStream(bytes)).readObject() as Serializable
}