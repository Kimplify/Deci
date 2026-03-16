package org.kimplify.deci

import org.kimplify.deci.exception.DeciArithmeticException
import org.kimplify.deci.exception.DeciDivisionByZeroException
import org.kimplify.deci.exception.DeciException
import org.kimplify.deci.exception.DeciFormatException
import org.kimplify.deci.exception.DeciOverflowException
import org.kimplify.deci.exception.DeciParseException
import org.kimplify.deci.exception.DeciScaleException
import org.kimplify.deci.exception.DeciSerializationException
import org.kimplify.deci.extension.toLongExact
import org.kimplify.deci.formatting.format
import org.kimplify.deci.math.mod
import org.kimplify.deci.math.remainder
import org.kimplify.deci.math.roundToNearest
import org.kimplify.deci.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class DeciExceptionTest {
    @Test
    fun `DeciParseException is catchable as DeciException`() {
        val ex = assertFailsWith<DeciException> { Deci("not-a-number") }
        assertIs<DeciParseException>(ex)
    }

    @Test
    fun `DeciDivisionByZeroException is catchable as DeciArithmeticException`() {
        val ex = assertFailsWith<DeciArithmeticException> { Deci("1") / Deci("0") }
        assertIs<DeciDivisionByZeroException>(ex)
    }

    @Test
    fun `DeciDivisionByZeroException is catchable as DeciException`() {
        assertFailsWith<DeciException> { Deci("1") / Deci("0") }
    }

    @Test
    fun `DeciOverflowException is catchable as DeciArithmeticException`() {
        val huge = Deci("999999999999999999999")
        val ex = assertFailsWith<DeciArithmeticException> { huge.toLongExact() }
        assertIs<DeciOverflowException>(ex)
    }

    @Test
    fun `DeciScaleException is catchable as DeciException`() {
        assertFailsWith<DeciException> {
            Deci("1.23").setScale(-1, RoundingMode.DOWN)
        }
    }

    @Test
    fun `DeciFormatException is catchable as DeciException`() {
        assertFailsWith<DeciException> {
            Deci("1.23").format("invalid-pattern")
        }
    }

    @Test
    fun `DeciParseException carries rawValue`() {
        val ex = assertFailsWith<DeciParseException> { Deci("abc") }
        assertEquals("abc", ex.rawValue)
    }

    @Test
    fun `DeciParseException carries rawValue for blank input`() {
        val ex = assertFailsWith<DeciParseException> { Deci("") }
        assertEquals("", ex.rawValue)
    }

    @Test
    fun `DeciOverflowException carries value`() {
        val huge = Deci("999999999999999999999")
        val ex = assertFailsWith<DeciOverflowException> { huge.toLongExact() }
        assertEquals("999999999999999999999", ex.value)
    }

    @Test
    fun `DeciScaleException carries scale`() {
        val ex =
            assertFailsWith<DeciScaleException> {
                Deci("1").divide(Deci("2"), scale = -3, roundingMode = RoundingMode.UP)
            }
        assertEquals(-3, ex.scale)
    }

    @Test
    fun `DeciFormatException carries pattern`() {
        val ex =
            assertFailsWith<DeciFormatException> {
                Deci("1").format("xxx")
            }
        assertEquals("xxx", ex.pattern)
    }

    @Test
    fun `DeciSerializationException wraps DeciParseException cause`() {
        val parseEx = DeciParseException("bad")
        val serEx = DeciSerializationException(rawValue = "bad", cause = parseEx)
        assertEquals("bad", serEx.rawValue)
        assertNotNull(serEx.cause)
        assertIs<DeciParseException>(serEx.cause)
    }

    @Test
    fun `sqrt of negative throws DeciArithmeticException`() {
        assertFailsWith<DeciArithmeticException> { Deci("-4").sqrt() }
    }

    @Test
    fun `mod by zero throws DeciDivisionByZeroException`() {
        assertFailsWith<DeciDivisionByZeroException> { Deci("5").mod(Deci.ZERO) }
    }

    @Test
    fun `remainder by zero throws DeciDivisionByZeroException`() {
        assertFailsWith<DeciDivisionByZeroException> { Deci("5").remainder(Deci.ZERO) }
    }

    @Test
    fun `roundToNearest zero throws DeciDivisionByZeroException`() {
        assertFailsWith<DeciDivisionByZeroException> { Deci("5").roundToNearest(Deci.ZERO) }
    }

    @Test
    fun `divide function with zero divisor throws DeciDivisionByZeroException`() {
        assertFailsWith<DeciDivisionByZeroException> {
            Deci("10").divide(Deci("0"), scale = 2, roundingMode = RoundingMode.HALF_UP)
        }
    }

    @Test
    fun `divide function with negative scale throws DeciScaleException`() {
        assertFailsWith<DeciScaleException> {
            Deci("10").divide(Deci("3"), scale = -1, roundingMode = RoundingMode.HALF_UP)
        }
    }
}
