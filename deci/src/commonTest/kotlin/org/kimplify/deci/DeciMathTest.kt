package org.kimplify.deci

import org.kimplify.deci.exception.DeciArithmeticException
import org.kimplify.deci.exception.DeciDivisionByZeroException
import org.kimplify.deci.math.mod
import org.kimplify.deci.math.pow
import org.kimplify.deci.math.remainder
import org.kimplify.deci.math.roundToNearest
import org.kimplify.deci.math.roundToSignificantDigits
import org.kimplify.deci.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeciMathTest {
    @Test
    fun `sqrt calculates square root correctly`() {
        assertEquals(Deci("2"), Deci("4").sqrt(0))
        assertEquals(Deci("3"), Deci("9").sqrt(0))
        assertEquals(Deci("1.414"), Deci("2").sqrt(3))
        assertEquals(Deci.ZERO, Deci.ZERO.sqrt())
        assertEquals(Deci.ONE, Deci.ONE.sqrt())
    }

    @Test
    fun `sqrt throws for negative numbers`() {
        assertFailsWith<DeciArithmeticException> {
            Deci("-1").sqrt()
        }
    }

    @Test
    fun `pow with Deci exponent works for simple cases`() {
        assertEquals(Deci("8"), Deci("2").pow(Deci("3")))
        assertEquals(Deci.ONE, Deci("5").pow(Deci.ZERO))
        assertEquals(Deci("5"), Deci("5").pow(Deci.ONE))
    }

    @Test
    fun `mod operation works correctly`() {
        assertEquals(Deci("1"), Deci("7").mod(Deci("3")))
        assertEquals(Deci("2"), Deci("5").mod(Deci("3")))
        assertEquals(Deci.ZERO, Deci("6").mod(Deci("3")))
    }

    @Test
    fun `mod throws for zero divisor`() {
        assertFailsWith<DeciDivisionByZeroException> {
            Deci("5").mod(Deci.ZERO)
        }
    }

    @Test
    fun `roundToNearest works correctly`() {
        assertEquals(Deci("5"), Deci("4.7").roundToNearest(Deci("5")))
        assertEquals(Deci("0.5"), Deci("0.37").roundToNearest(Deci("0.5")))
        assertEquals(Deci("10"), Deci("12").roundToNearest(Deci("10")))
    }

    @Test
    fun `roundToSignificantDigits works correctly`() {
        assertEquals(Deci("123"), Deci("123.456").roundToSignificantDigits(3))
        assertEquals(Deci("0.00123"), Deci("0.001234").roundToSignificantDigits(3))
        assertEquals(Deci.ZERO, Deci.ZERO.roundToSignificantDigits(3))
    }

    @Test
    fun `roundToSignificantDigits requires positive digits`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("123").roundToSignificantDigits(0)
        }
    }

    @Test
    fun `pow with negative exponent uses explicit context`() {
        val result = Deci("2").pow(Deci("-3"), DeciContext(2, RoundingMode.HALF_UP))
        assertEquals(Deci("0.13"), result)
    }

    @Test
    fun `pow with negative exponent uses default context`() {
        val result = Deci("2").pow(Deci("-3"))
        assertEquals(Deci("0.125"), result)
    }

    @Test
    fun `pow with BANKING context rounds with HALF_EVEN`() {
        val result = Deci("2").pow(Deci("-1"), DeciContext.BANKING)
        assertEquals(Deci("0.50"), result)
    }

    @Test
    fun `remainder works correctly`() {
        assertEquals(Deci.ZERO, Deci("6").remainder(Deci("3")))
    }

    @Test
    fun `remainder throws for zero divisor`() {
        assertFailsWith<DeciDivisionByZeroException> {
            Deci("5").remainder(Deci.ZERO)
        }
    }
}
