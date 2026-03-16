package org.kimplify.deci

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.kimplify.deci.exception.DeciSerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Serialization contract tests per CLAUDE.md:
 * - Deci serializes as a JSON string (not a JSON number)
 * - Trailing zeros are preserved when set via setScale
 * - No scientific notation in serialized form
 * - Invalid strings are rejected during deserialization
 */
class DeciSerializationTest {
    private val json = Json { encodeDefaults = true }

    // ========== Format ==========

    @Test
    fun `serializes as JSON string not JSON number`() {
        val encoded = json.encodeToString(Deci.serializer(), Deci("123.45"))
        assertTrue(
            encoded.startsWith("\"") && encoded.endsWith("\""),
            "Must serialize as JSON string, got: $encoded",
        )
    }

    @Test
    fun `serialized value is the plain string representation`() {
        val encoded = json.encodeToString(Deci.serializer(), Deci("123.45"))
        assertEquals("\"123.45\"", encoded)
    }

    // ========== Trailing zeros ==========

    @Test
    fun `setScale value round-trips correctly through serialization`() {
        val d = Deci("1.5").setScale(2, RoundingMode.DOWN)
        val encoded = json.encodeToString(Deci.serializer(), d)
        val restored = json.decodeFromString(Deci.serializer(), encoded)
        assertEquals(d, restored, "Value must survive round-trip")
    }

    @Test
    fun `integer 100 does not become scientific notation`() {
        val encoded = json.encodeToString(Deci.serializer(), Deci("100"))
        assertEquals("\"100\"", encoded)
        assertFalse(
            encoded.contains("E", ignoreCase = true),
            "Must not use scientific notation: $encoded",
        )
    }

    // ========== No scientific notation ==========

    @Test
    fun `large number does not use scientific notation`() {
        val large = Deci("10000000000000000000")
        val encoded = json.encodeToString(Deci.serializer(), large)
        assertFalse(
            encoded.contains("E", ignoreCase = true),
            "Large number must not use scientific notation: $encoded",
        )
    }

    @Test
    fun `moderately small number does not use scientific notation`() {
        val small = Deci("0.001")
        val encoded = json.encodeToString(Deci.serializer(), small)
        assertFalse(
            encoded.contains("E", ignoreCase = true),
            "Moderately small number must not use scientific notation: $encoded",
        )
    }

    // ========== Round-trip ==========

    @Test
    fun `round-trip preserves exact value`() {
        val cases = listOf("0", "1", "-1", "123.456", "0.001", "999999999999", "-0.1")
        for (s in cases) {
            val d = Deci(s)
            val encoded = json.encodeToString(Deci.serializer(), d)
            val restored = json.decodeFromString(Deci.serializer(), encoded)
            assertEquals(d, restored, "Round-trip failed for $s")
            assertEquals(s, restored.toString(), "String representation changed for $s")
        }
    }

    @Test
    fun `round-trip preserves value from setScale`() {
        val d = Deci("2").setScale(3, RoundingMode.DOWN)
        val encoded = json.encodeToString(Deci.serializer(), d)
        val restored = json.decodeFromString(Deci.serializer(), encoded)
        assertEquals(d, restored, "Value must survive round-trip after setScale")
    }

    @Test
    fun `round-trip with negative zero`() {
        val d = Deci("-0")
        val encoded = json.encodeToString(Deci.serializer(), d)
        val restored = json.decodeFromString(Deci.serializer(), encoded)
        assertEquals(Deci.ZERO, restored)
    }

    // ========== Invalid input rejection ==========

    @Test
    fun `deserialization rejects non-numeric strings`() {
        val invalidInputs = listOf("\"foo\"", "\"abc\"", "\"\"", "\"1.2.3\"", "\"--1\"")
        for (input in invalidInputs) {
            assertFailsWith<DeciSerializationException>(
                "Expected DeciSerializationException for input: $input",
            ) {
                json.decodeFromString(Deci.serializer(), input)
            }
        }
    }

    @Test
    fun `deserialization rejects bare JSON number`() {
        // PrimitiveKind.STRING descriptor should reject unquoted number tokens
        assertFailsWith<SerializationException> {
            json.decodeFromString(Deci.serializer(), "123.45")
        }
    }

    @Test
    fun `deserialization rejects NaN and Infinity strings`() {
        val special = listOf("\"NaN\"", "\"Infinity\"", "\"-Infinity\"")
        for (input in special) {
            assertFailsWith<DeciSerializationException>(
                "Expected DeciSerializationException for input: $input",
            ) {
                json.decodeFromString(Deci.serializer(), input)
            }
        }
    }

    // ========== Valid edge cases ==========

    @Test
    fun `deserializes valid numeric strings`() {
        val validCases =
            mapOf(
                "\"0\"" to Deci("0"),
                "\"123\"" to Deci("123"),
                "\"-456.789\"" to Deci("-456.789"),
                "\"0.001\"" to Deci("0.001"),
            )
        for ((input, expected) in validCases) {
            val result = json.decodeFromString(Deci.serializer(), input)
            assertEquals(expected, result, "Failed for input: $input")
        }
    }
}
