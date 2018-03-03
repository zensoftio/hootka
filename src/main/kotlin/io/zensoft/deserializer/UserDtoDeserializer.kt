package io.zensoft.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.zensoft.domain.UserDto

class UserDtoDeserializer: StdDeserializer<UserDto>(UserDto::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UserDto {
        var firstName: String? = null
        var lastName: String? = null
        var email: String? = null
        while (p.nextToken() != JsonToken.END_OBJECT) {
            val fieldName = p.currentName
            if ("firstName" == fieldName) {
                firstName = p.text
            }
            if ("lastName" == fieldName) {
                lastName = p.text
            }
            if ("email" == fieldName) {
                email = p.text
            }
        }
        p.close()
        return UserDto(firstName!!, lastName!!, email!!)
    }

}