package org.kimplify.deci

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.kimplify.deci.exception.DeciParseException
import org.kimplify.deci.exception.DeciSerializationException

/**
 * A [KSerializer] for [Deci] that serializes values as JSON **strings** to preserve
 * trailing zeros and avoid lossy float coercion by JSON parsers.
 *
 * For example, `Deci("1.50")` is serialized as the JSON string `"1.50"`, not the
 * JSON number `1.5`.
 *
 * Serialization uses [Deci.toString], which never produces scientific notation.
 * Deserialization parses the decoded string via the [Deci] string constructor.
 *
 * @throws [org.kimplify.deci.exception.DeciSerializationException] if the decoded string
 *         is not a valid decimal literal.
 */
object DeciSerializer : KSerializer<Deci> {
    override val descriptor = PrimitiveSerialDescriptor("Deci", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Deci,
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Deci {
        val value = decoder.decodeString()
        return try {
            Deci(value)
        } catch (e: DeciParseException) {
            throw DeciSerializationException(rawValue = value, cause = e)
        }
    }
}
