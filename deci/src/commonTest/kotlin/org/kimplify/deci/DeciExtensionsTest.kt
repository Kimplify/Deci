package org.kimplify.deci

import org.kimplify.deci.exception.DeciOverflowException
import org.kimplify.deci.exception.DeciParseException
import org.kimplify.deci.extension.precision
import org.kimplify.deci.extension.scale
import org.kimplify.deci.extension.sumDeci
import org.kimplify.deci.extension.toDeci
import org.kimplify.deci.extension.toDeciOrNull
import org.kimplify.deci.extension.toLong
import org.kimplify.deci.extension.toLongExact
import org.kimplify.deci.extension.toLongOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DeciExtensionsTest {
    // -- Int.toDeci --

    @Test
    fun `Int toDeci creates correct value`() {
        assertEquals(Deci("42"), 42.toDeci())
    }

    @Test
    fun `Int toDeci handles zero`() {
        assertEquals(Deci.ZERO, 0.toDeci())
    }

    @Test
    fun `Int toDeci handles negative`() {
        assertEquals(Deci("-7"), (-7).toDeci())
    }

    // -- Long.toDeci --

    @Test
    fun `Long toDeci creates correct value`() {
        assertEquals(Deci("9999999999"), 9999999999L.toDeci())
    }

    @Test
    fun `Long toDeci handles zero`() {
        assertEquals(Deci.ZERO, 0L.toDeci())
    }

    // -- String.toDeci --

    @Test
    fun `String toDeci creates correct value`() {
        assertEquals(Deci("1.23"), "1.23".toDeci())
    }

    @Test
    fun `String toDeci throws on invalid input`() {
        assertFailsWith<DeciParseException> {
            "not-a-number".toDeci()
        }
    }

    // -- String.toDeciOrNull --

    @Test
    fun `String toDeciOrNull returns Deci for valid input`() {
        val result = "1.23".toDeciOrNull()
        assertNotNull(result)
        assertEquals(Deci("1.23"), result)
    }

    @Test
    fun `String toDeciOrNull returns null for invalid input`() {
        assertNull("not-a-number".toDeciOrNull())
    }

    @Test
    fun `String toDeciOrNull returns null for empty string`() {
        assertNull("".toDeciOrNull())
    }

    // -- Double.toDeci --

    @Test
    fun `Double toDeci creates correct value for exact doubles`() {
        assertEquals(Deci("1.5"), 1.5.toDeci())
    }

    @Test
    fun `Double toDeci handles zero`() {
        assertEquals(Deci.ZERO, 0.0.toDeci())
    }

    @Test
    fun `Double toDeci handles negative`() {
        assertEquals(Deci("-2.5"), (-2.5).toDeci())
    }

    // -- toLong / toLongOrNull / toLongExact --

    @Test
    fun `toLong for exact integer`() {
        assertEquals(42L, Deci("42").toLong())
    }

    @Test
    fun `toLong truncates fractional part`() {
        assertEquals(3L, Deci("3.99").toLong())
    }

    @Test
    fun `toLongOrNull returns null for non-representable value`() {
        // A value far beyond Long range
        assertNull(Deci("99999999999999999999").toLongOrNull())
    }

    @Test
    fun `toLongOrNull returns long for valid integer`() {
        assertEquals(100L, Deci("100").toLongOrNull())
    }

    @Test
    fun `toLongExact throws for non-representable value`() {
        assertFailsWith<DeciOverflowException> {
            Deci("99999999999999999999").toLongExact()
        }
    }

    // -- scale and precision --

    @Test
    fun `scale of integer is zero`() {
        assertEquals(0, Deci("123").scale())
    }

    @Test
    fun `scale of fractional value`() {
        assertEquals(2, Deci("1.25").scale())
        assertEquals(3, Deci("0.125").scale())
        assertEquals(1, Deci("1.5").scale())
    }

    @Test
    fun `precision of large number`() {
        assertEquals(12, Deci("123456789012").precision())
    }

    @Test
    fun `precision of small decimal`() {
        assertEquals(4, Deci("0.001").precision())
    }

    // -- sumDeci --

    @Test
    fun `sumDeci of empty list is ZERO`() {
        assertEquals(Deci.ZERO, emptyList<Deci>().sumDeci())
    }

    @Test
    fun `sumDeci of mixed positive and negative`() {
        val values = listOf(Deci("10"), Deci("-3"), Deci("5.5"), Deci("-2.5"))
        assertEquals(Deci("10"), values.sumDeci())
    }

    @Test
    fun `sumDeci of single element`() {
        assertEquals(Deci("42"), listOf(Deci("42")).sumDeci())
    }

    // -- String.toDeci edge cases --

    @Test
    fun `String toDeci with comma separator`() {
        assertEquals(Deci("1.23"), "1,23".toDeci())
    }

    @Test
    fun `String toDeciOrNull with comma separator`() {
        val result = "1,23".toDeciOrNull()
        assertNotNull(result)
        assertEquals(Deci("1.23"), result)
    }
}
