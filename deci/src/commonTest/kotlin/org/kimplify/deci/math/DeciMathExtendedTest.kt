package org.kimplify.deci.math

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.exception.DeciArithmeticException
import org.kimplify.deci.exception.DeciDivisionByZeroException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DeciMathExtendedTest {
    // --- sqrt extended ---

    @Test
    fun `sqrt of 2 with various precisions`() {
        assertEquals(Deci("1.41"), Deci("2").sqrt(2))
        assertEquals(Deci("1.41421"), Deci("2").sqrt(5))
    }

    @Test
    fun `sqrt of perfect squares`() {
        assertEquals(Deci("2"), Deci("4").sqrt(0))
        assertEquals(Deci("5"), Deci("25").sqrt(0))
        assertEquals(Deci("10"), Deci("100").sqrt(0))
    }

    @Test
    fun `sqrt of large number`() {
        val result = Deci("10000").sqrt(0)
        assertEquals(Deci("100"), result)
    }

    @Test
    fun `sqrt of small decimal`() {
        val result = Deci("0.25").sqrt(2)
        assertEquals(Deci("0.50"), result)
    }

    @Test
    fun `sqrt of zero returns zero`() {
        assertEquals(Deci.ZERO, Deci.ZERO.sqrt())
    }

    @Test
    fun `sqrt of one returns one`() {
        assertEquals(Deci.ONE, Deci.ONE.sqrt())
    }

    @Test
    fun `sqrt of negative throws`() {
        assertFailsWith<DeciArithmeticException> {
            Deci("-4").sqrt()
        }
    }

    // --- pow extended ---

    @Test
    fun `pow zero exponent returns one`() {
        assertEquals(Deci.ONE, Deci("999").pow(Deci.ZERO))
    }

    @Test
    fun `pow exponent one returns base`() {
        assertEquals(Deci("42"), Deci("42").pow(Deci.ONE))
    }

    @Test
    fun `pow positive integer exponents`() {
        assertEquals(Deci("8"), Deci("2").pow(Deci("3")))
        assertEquals(Deci("16"), Deci("2").pow(Deci("4")))
        assertEquals(Deci("27"), Deci("3").pow(Deci("3")))
        assertEquals(Deci("1000"), Deci("10").pow(Deci("3")))
    }

    @Test
    fun `pow negative exponent`() {
        val context = DeciContext(precision = 4, roundingMode = RoundingMode.HALF_UP)
        val result = Deci("2").pow(Deci("-2"), context)
        assertEquals(Deci("0.25"), result)
    }

    @Test
    fun `pow negative exponent with default context`() {
        val result = Deci("2").pow(Deci("-1"))
        assertEquals(Deci("0.5"), result)
    }

    @Test
    fun `pow with zero base and positive exponent`() {
        assertEquals(Deci("0"), Deci("0").pow(Deci("5")))
    }

    @Test
    fun `pow with one base returns one`() {
        assertEquals(Deci.ONE, Deci.ONE.pow(Deci("100")))
    }

    @Test
    fun `pow requires integer exponent`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("2").pow(Deci("1.5"))
        }
    }

    @Test
    fun `pow with negative base and odd exponent`() {
        assertEquals(Deci("-8"), Deci("-2").pow(Deci("3")))
    }

    @Test
    fun `pow with negative base and even exponent`() {
        assertEquals(Deci("4"), Deci("-2").pow(Deci("2")))
    }

    // --- mod extended ---

    @Test
    fun `mod basic cases`() {
        assertEquals(Deci("1"), Deci("7").mod(Deci("3")))
        assertEquals(Deci("0"), Deci("6").mod(Deci("3")))
        assertEquals(Deci("2"), Deci("5").mod(Deci("3")))
    }

    @Test
    fun `mod with decimal values`() {
        val result = Deci("5.5").mod(Deci("2"))
        assertEquals(Deci("1.5"), result)
    }

    @Test
    fun `mod with negative dividend`() {
        val result = Deci("-7").mod(Deci("3"))
        assertEquals(Deci("-1"), result)
    }

    @Test
    fun `mod throws on zero divisor`() {
        assertFailsWith<DeciDivisionByZeroException> {
            Deci("5").mod(Deci.ZERO)
        }
    }

    @Test
    fun `mod when dividend is smaller than divisor`() {
        assertEquals(Deci("2"), Deci("2").mod(Deci("5")))
    }

    @Test
    fun `mod when dividend equals divisor`() {
        assertEquals(Deci("0"), Deci("5").mod(Deci("5")))
    }

    // --- remainder extended ---

    @Test
    fun `remainder basic cases`() {
        assertEquals(Deci("0"), Deci("6").remainder(Deci("3")))
    }

    @Test
    fun `remainder throws on zero divisor`() {
        assertFailsWith<DeciDivisionByZeroException> {
            Deci("5").remainder(Deci.ZERO)
        }
    }

    @Test
    fun `remainder with decimal values`() {
        val result = Deci("5.5").remainder(Deci("2"))
        assertEquals(Deci("1.5"), result)
    }

    @Test
    fun `remainder truncates quotient toward zero`() {
        assertEquals(Deci("2"), Deci("5").remainder(Deci("3")))
        assertEquals(Deci("-2"), Deci("-5").remainder(Deci("3")))
        assertEquals(Deci("3"), Deci("7").remainder(Deci("4")))
        assertEquals(Deci("1"), Deci("10").remainder(Deci("3")))
    }

    // --- roundToNearest extended ---

    @Test
    fun `roundToNearest rounds to nearest 5`() {
        assertEquals(Deci("5"), Deci("4.7").roundToNearest(Deci("5")))
        assertEquals(Deci("5"), Deci("3").roundToNearest(Deci("5")))
        assertEquals(Deci("10"), Deci("8").roundToNearest(Deci("5")))
    }

    @Test
    fun `roundToNearest rounds to nearest 0 point 5`() {
        assertEquals(Deci("0.5"), Deci("0.37").roundToNearest(Deci("0.5")))
        assertEquals(Deci("1.0"), Deci("0.8").roundToNearest(Deci("0.5")))
    }

    @Test
    fun `roundToNearest rounds to nearest 10`() {
        assertEquals(Deci("10"), Deci("12").roundToNearest(Deci("10")))
        assertEquals(Deci("20"), Deci("16").roundToNearest(Deci("10")))
    }

    @Test
    fun `roundToNearest throws on zero multiple`() {
        assertFailsWith<DeciDivisionByZeroException> {
            Deci("5").roundToNearest(Deci.ZERO)
        }
    }

    @Test
    fun `roundToNearest with exact multiple returns unchanged`() {
        assertEquals(Deci("10"), Deci("10").roundToNearest(Deci("5")))
    }

    @Test
    fun `roundToNearest with negative value`() {
        assertEquals(Deci("-10"), Deci("-12").roundToNearest(Deci("5")))
    }

    @Test
    fun `roundToNearest with nearest 0 point 25`() {
        assertEquals(Deci("0.25"), Deci("0.3").roundToNearest(Deci("0.25")))
        assertEquals(Deci("0.50"), Deci("0.4").roundToNearest(Deci("0.25")))
    }

    // --- roundToSignificantDigits extended ---

    @Test
    fun `roundToSignificantDigits with zero returns zero`() {
        assertEquals(Deci.ZERO, Deci.ZERO.roundToSignificantDigits(3))
    }

    @Test
    fun `roundToSignificantDigits with integers`() {
        assertEquals(Deci("123"), Deci("123.456").roundToSignificantDigits(3))
        assertEquals(Deci("1200"), Deci("1234").roundToSignificantDigits(2))
    }

    @Test
    fun `roundToSignificantDigits with small decimals`() {
        assertEquals(Deci("0.00123"), Deci("0.001234").roundToSignificantDigits(3))
    }

    @Test
    fun `roundToSignificantDigits with 1 digit`() {
        assertEquals(Deci("100"), Deci("123").roundToSignificantDigits(1))
    }

    @Test
    fun `roundToSignificantDigits throws for zero digits`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("123").roundToSignificantDigits(0)
        }
    }

    @Test
    fun `roundToSignificantDigits throws for negative digits`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("123").roundToSignificantDigits(-1)
        }
    }

    @Test
    fun `roundToSignificantDigits preserves more digits than available`() {
        val result = Deci("12").roundToSignificantDigits(5)
        assertTrue(result.toString().startsWith("12"))
    }

    @Test
    fun `roundToSignificantDigits with negative value`() {
        val result = Deci("-123.456").roundToSignificantDigits(3)
        assertEquals(Deci("-123"), result)
    }
}
