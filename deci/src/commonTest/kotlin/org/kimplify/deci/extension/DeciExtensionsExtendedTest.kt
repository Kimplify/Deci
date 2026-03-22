package org.kimplify.deci.extension

import org.kimplify.deci.Deci
import org.kimplify.deci.exception.DeciOverflowException
import org.kimplify.deci.exception.DeciParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeciExtensionsExtendedTest {
    // --- sumDeci extended ---

    @Test
    fun `sumDeci of empty list returns ZERO`() {
        assertEquals(Deci.ZERO, emptyList<Deci>().sumDeci())
    }

    @Test
    fun `sumDeci of single element returns that element`() {
        assertEquals(Deci("42"), listOf(Deci("42")).sumDeci())
    }

    @Test
    fun `sumDeci of all positive values`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        assertEquals(Deci("6"), values.sumDeci())
    }

    @Test
    fun `sumDeci of mixed positive and negative`() {
        val values = listOf(Deci("10"), Deci("-3"), Deci("5.5"), Deci("-2.5"))
        assertEquals(Deci("10"), values.sumDeci())
    }

    @Test
    fun `sumDeci preserves full precision`() {
        val values = listOf(Deci("0.1"), Deci("0.2"), Deci("0.3"))
        assertEquals(Deci("0.6"), values.sumDeci())
    }

    @Test
    fun `sumDeci of all zeros returns zero`() {
        val values = listOf(Deci.ZERO, Deci.ZERO, Deci.ZERO)
        assertEquals(Deci.ZERO, values.sumDeci())
    }

    @Test
    fun `sumDeci handles large values`() {
        val values = listOf(Deci("999999999"), Deci("1"))
        assertEquals(Deci("1000000000"), values.sumDeci())
    }

    // --- toLong extended ---

    @Test
    fun `toLong for integer value`() {
        assertEquals(42L, Deci("42").toLong())
    }

    @Test
    fun `toLong truncates positive fractional part`() {
        assertEquals(3L, Deci("3.99").toLong())
    }

    @Test
    fun `toLong truncates negative fractional part toward zero`() {
        assertEquals(-3L, Deci("-3.99").toLong())
    }

    @Test
    fun `toLong for zero`() {
        assertEquals(0L, Deci("0").toLong())
    }

    @Test
    fun `toLong for negative integer`() {
        assertEquals(-100L, Deci("-100").toLong())
    }

    @Test
    fun `toLong for max long value`() {
        assertEquals(Long.MAX_VALUE, Deci(Long.MAX_VALUE.toString()).toLong())
    }

    @Test
    fun `toLong for min long value`() {
        assertEquals(Long.MIN_VALUE, Deci(Long.MIN_VALUE.toString()).toLong())
    }

    // --- toLongOrNull extended ---

    @Test
    fun `toLongOrNull returns long for valid integer`() {
        assertEquals(100L, Deci("100").toLongOrNull())
    }

    @Test
    fun `toLongOrNull returns null for value exceeding Long range`() {
        assertNull(Deci("99999999999999999999").toLongOrNull())
    }

    @Test
    fun `toLongOrNull truncates fractional part`() {
        assertEquals(5L, Deci("5.9").toLongOrNull())
    }

    @Test
    fun `toLongOrNull returns zero for zero`() {
        assertEquals(0L, Deci("0").toLongOrNull())
    }

    @Test
    fun `toLongOrNull handles negative`() {
        assertEquals(-42L, Deci("-42.5").toLongOrNull())
    }

    // --- toLongExact extended ---

    @Test
    fun `toLongExact succeeds for valid integer`() {
        assertEquals(42L, Deci("42").toLongExact())
    }

    @Test
    fun `toLongExact throws for overflow`() {
        assertFailsWith<DeciOverflowException> {
            Deci("99999999999999999999").toLongExact()
        }
    }

    @Test
    fun `toLongExact truncates fractional part`() {
        assertEquals(10L, Deci("10.999").toLongExact())
    }

    // --- scale extended ---

    @Test
    fun `scale of integer is zero`() {
        assertEquals(0, Deci("123").scale())
    }

    @Test
    fun `scale of one decimal place`() {
        assertEquals(1, Deci("1.5").scale())
    }

    @Test
    fun `scale of many decimal places`() {
        assertEquals(6, Deci("1.123456").scale())
    }

    @Test
    fun `scale of zero`() {
        assertEquals(0, Deci("0").scale())
    }

    @Test
    fun `scale of trailing zeros`() {
        val result = Deci("1.50").scale()
        assertTrue(result >= 1)
    }

    // --- precision extended ---

    @Test
    fun `precision of single digit`() {
        assertEquals(1, Deci("5").precision())
    }

    @Test
    fun `precision of large integer`() {
        assertEquals(12, Deci("123456789012").precision())
    }

    @Test
    fun `precision of decimal value`() {
        assertEquals(4, Deci("0.001").precision())
    }

    @Test
    fun `precision of negative value counts only digits`() {
        assertEquals(3, Deci("-123").precision())
    }

    @Test
    fun `precision of zero`() {
        assertEquals(1, Deci("0").precision())
    }

    // --- toDeci conversion extensions ---

    @Test
    fun `Int toDeci positive`() {
        assertEquals(Deci("42"), 42.toDeci())
    }

    @Test
    fun `Int toDeci zero`() {
        assertEquals(Deci.ZERO, 0.toDeci())
    }

    @Test
    fun `Int toDeci negative`() {
        assertEquals(Deci("-7"), (-7).toDeci())
    }

    @Test
    fun `Int toDeci max value`() {
        assertEquals(Deci(Int.MAX_VALUE.toString()), Int.MAX_VALUE.toDeci())
    }

    @Test
    fun `Int toDeci min value`() {
        assertEquals(Deci(Int.MIN_VALUE.toString()), Int.MIN_VALUE.toDeci())
    }

    @Test
    fun `Long toDeci positive`() {
        assertEquals(Deci("9999999999"), 9999999999L.toDeci())
    }

    @Test
    fun `Long toDeci zero`() {
        assertEquals(Deci.ZERO, 0L.toDeci())
    }

    @Test
    fun `Long toDeci negative`() {
        assertEquals(Deci("-1000"), (-1000L).toDeci())
    }

    @Test
    fun `String toDeci valid input`() {
        assertEquals(Deci("1.23"), "1.23".toDeci())
    }

    @Test
    fun `String toDeci throws on invalid input`() {
        assertFailsWith<DeciParseException> {
            "not-a-number".toDeci()
        }
    }

    @Test
    fun `String toDeci throws on empty string`() {
        assertFailsWith<DeciParseException> {
            "".toDeci()
        }
    }

    @Test
    fun `String toDeci with comma separator`() {
        assertEquals(Deci("1.23"), "1,23".toDeci())
    }

    @Test
    fun `String toDeciOrNull returns Deci for valid input`() {
        assertEquals(Deci("1.23"), "1.23".toDeciOrNull())
    }

    @Test
    fun `String toDeciOrNull returns null for invalid input`() {
        assertNull("not-a-number".toDeciOrNull())
    }

    @Test
    fun `String toDeciOrNull returns null for empty string`() {
        assertNull("".toDeciOrNull())
    }

    @Test
    fun `Double toDeci exact value`() {
        assertEquals(Deci("1.5"), 1.5.toDeci())
    }

    @Test
    fun `Double toDeci zero`() {
        assertEquals(Deci.ZERO, 0.0.toDeci())
    }

    @Test
    fun `Double toDeci negative`() {
        assertEquals(Deci("-2.5"), (-2.5).toDeci())
    }

    @Test
    fun `orZero returns value when non-null`() {
        val value: Deci? = Deci("5")
        assertEquals(Deci("5"), value.orZero())
    }

    @Test
    fun `orZero returns ZERO when null`() {
        val value: Deci? = null
        assertEquals(Deci.ZERO, value.orZero())
    }

    @Test
    fun `orOne returns ONE when null`() {
        val value: Deci? = null
        assertEquals(Deci.ONE, value.orOne())
    }

    @Test
    fun `orDefault returns default when null`() {
        val value: Deci? = null
        assertEquals(Deci("99"), value.orDefault(Deci("99")))
    }

    @Test
    fun `toInt converts correctly`() {
        assertEquals(42, Deci("42").toInt())
        assertEquals(-7, Deci("-7").toInt())
        assertEquals(0, Deci("0.99").toInt())
    }

    @Test
    fun `toIntOrNull returns null for overflow`() {
        assertNull(Deci("99999999999").toIntOrNull())
        assertEquals(42, Deci("42").toIntOrNull())
    }

    @Test
    fun `toFloat converts correctly`() {
        assertEquals(1.5f, Deci("1.5").toFloat())
    }
}
