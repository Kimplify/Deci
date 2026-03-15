package org.kimplify.deci

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object DeciSerializer : KSerializer<Deci> {
    override val descriptor = PrimitiveSerialDescriptor("Deci", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Deci) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Deci {
        val value = decoder.decodeString()
        return try {
            Deci(value)
        } catch (e: Exception) {
            throw SerializationException("Invalid Deci value: '$value'", e)
        }
    }
}

