package org.kimplify.deci.parser

import org.kimplify.deci.exception.DeciParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeciLiteralValidationTest {
    // --- validateAndNormalizeDecimalLiteral ---

    @Test
    fun `validates simple integer`() {
        assertEquals("123", validateAndNormalizeDecimalLiteral("123"))
    }

    @Test
    fun `validates simple decimal`() {
        assertEquals("123.45", validateAndNormalizeDecimalLiteral("123.45"))
    }

    @Test
    fun `validates negative value`() {
        assertEquals("-123.45", validateAndNormalizeDecimalLiteral("-123.45"))
    }

    @Test
    fun `validates positive sign value`() {
        assertEquals("123.45", validateAndNormalizeDecimalLiteral("+123.45"))
    }

    @Test
    fun `validates zero`() {
        assertEquals("0", validateAndNormalizeDecimalLiteral("0"))
    }

    @Test
    fun `trims whitespace`() {
        assertEquals("123", validateAndNormalizeDecimalLiteral("  123  "))
    }

    @Test
    fun `normalizes comma as decimal separator`() {
        assertEquals("123.45", validateAndNormalizeDecimalLiteral("123,45"))
    }

    @Test
    fun `normalizes thousands separator with dot`() {
        assertEquals("1234.56", validateAndNormalizeDecimalLiteral("1.234,56"))
    }

    @Test
    fun `normalizes thousands separator with comma`() {
        assertEquals("1234.56", validateAndNormalizeDecimalLiteral("1,234.56"))
    }

    @Test
    fun `normalizes leading decimal point`() {
        assertEquals("0.5", validateAndNormalizeDecimalLiteral(".5"))
    }

    @Test
    fun `normalizes negative leading decimal point`() {
        assertEquals("-0.5", validateAndNormalizeDecimalLiteral("-.5"))
    }

    @Test
    fun `throws on blank string`() {
        assertFailsWith<DeciParseException> {
            validateAndNormalizeDecimalLiteral("")
        }
    }

    @Test
    fun `throws on whitespace only`() {
        assertFailsWith<DeciParseException> {
            validateAndNormalizeDecimalLiteral("   ")
        }
    }

    @Test
    fun `throws on alphabetic input`() {
        assertFailsWith<DeciParseException> {
            validateAndNormalizeDecimalLiteral("abc")
        }
    }

    @Test
    fun `throws on double negative`() {
        assertFailsWith<DeciParseException> {
            validateAndNormalizeDecimalLiteral("--1")
        }
    }

    @Test
    fun `throws on lone dot`() {
        assertFailsWith<DeciParseException> {
            validateAndNormalizeDecimalLiteral(".")
        }
    }

    @Test
    fun `throws on lone comma`() {
        assertFailsWith<DeciParseException> {
            validateAndNormalizeDecimalLiteral(",")
        }
    }

    @Test
    fun `validates trailing decimal point`() {
        assertEquals("1.0", validateAndNormalizeDecimalLiteral("1.0"))
    }

    @Test
    fun `validates large number`() {
        assertEquals("9999999999.9999", validateAndNormalizeDecimalLiteral("9999999999.9999"))
    }

    // --- DECIMAL_REGEX ---

    @Test
    fun `regex matches simple integers`() {
        assertTrue(DECIMAL_REGEX.matches("0"))
        assertTrue(DECIMAL_REGEX.matches("123"))
        assertTrue(DECIMAL_REGEX.matches("-456"))
    }

    @Test
    fun `regex matches decimals`() {
        assertTrue(DECIMAL_REGEX.matches("0.1"))
        assertTrue(DECIMAL_REGEX.matches("123.456"))
        assertTrue(DECIMAL_REGEX.matches("-0.789"))
    }

    @Test
    fun `regex matches leading decimal point`() {
        assertTrue(DECIMAL_REGEX.matches(".1"))
        assertTrue(DECIMAL_REGEX.matches(",5"))
        assertTrue(DECIMAL_REGEX.matches("-.5"))
    }

    @Test
    fun `regex matches thousands separated values`() {
        assertTrue(DECIMAL_REGEX.matches("1,230.98"))
        assertTrue(DECIMAL_REGEX.matches("1.230,98"))
        assertTrue(DECIMAL_REGEX.matches("1,234,567.89"))
    }

    @Test
    fun `regex rejects empty string`() {
        assertFalse(DECIMAL_REGEX.matches(""))
    }

    @Test
    fun `regex rejects non-numeric`() {
        assertFalse(DECIMAL_REGEX.matches("abc"))
        assertFalse(DECIMAL_REGEX.matches("--1"))
    }

    @Test
    fun `regex rejects lone separators`() {
        assertFalse(DECIMAL_REGEX.matches("."))
        assertFalse(DECIMAL_REGEX.matches(","))
    }

    // --- normalizeDecimalString ---

    @Test
    fun `normalizeDecimalString with empty string`() {
        assertEquals("", "".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with whitespace only`() {
        assertEquals("0", "   ".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with plain integer`() {
        assertEquals("12", "12".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with negative integer`() {
        assertEquals("-12", "-12".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with positive sign`() {
        assertEquals("12", "+12".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with comma decimal separator`() {
        assertEquals("1.234", "1,234".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with dot decimal separator`() {
        assertEquals("1.234", "1.234".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with European format`() {
        assertEquals("1234.56", "1.234,56".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with US format`() {
        assertEquals("1234.56", "1,234.56".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with leading dot`() {
        assertEquals("0.5", ".5".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with leading comma`() {
        assertEquals("0.5", ",5".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with negative leading dot`() {
        assertEquals("-0.5", "-.5".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with lone negative sign`() {
        assertEquals("-0", "-".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with lone positive sign`() {
        assertEquals("0", "+".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString with multiple separators`() {
        assertEquals("12.3", "1,2,3".normalizeDecimalString())
        assertEquals("12.3", "1.2.3".normalizeDecimalString())
        assertEquals("12.3", "1,2.3".normalizeDecimalString())
        assertEquals("12.3", "1.2,3".normalizeDecimalString())
    }

    @Test
    fun `normalizeDecimalString trims whitespace`() {
        assertEquals("1234.56", "  1,234.56  ".normalizeDecimalString())
    }
}
