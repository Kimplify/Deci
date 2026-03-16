package org.kimplify.deci

import org.kimplify.deci.exception.DeciDivisionByZeroException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeciOperatorTest {
    // -- unaryMinus --

    @Test
    fun `unaryMinus negates positive value`() {
        assertEquals(Deci("-5"), -Deci("5"))
    }

    @Test
    fun `unaryMinus negates negative value`() {
        assertEquals(Deci("5"), -Deci("-5"))
    }

    @Test
    fun `unaryMinus of zero is zero`() {
        assertEquals(Deci.ZERO, -Deci.ZERO)
    }

    @Test
    fun `double unaryMinus returns original`() {
        val d = Deci("123.456")
        assertEquals(d, -(-d))
    }

    @Test
    fun `unaryMinus preserves scale`() {
        assertEquals(Deci("-1.50"), -Deci("1.50"))
    }

    // -- rem --

    @Test
    fun `rem returns correct remainder`() {
        assertEquals(Deci("1"), Deci("7") % Deci("3"))
        assertEquals(Deci("2"), Deci("5") % Deci("3"))
        assertEquals(Deci.ZERO, Deci("6") % Deci("3"))
    }

    @Test
    fun `rem with negative dividend`() {
        assertEquals(Deci("-1"), Deci("-7") % Deci("3"))
    }

    @Test
    fun `rem with negative divisor`() {
        assertEquals(Deci("1"), Deci("7") % Deci("-3"))
    }

    @Test
    fun `rem with fractional values`() {
        assertEquals(Deci("0.1"), Deci("10.1") % Deci("5"))
    }

    @Test
    fun `rem throws on zero divisor`() {
        assertFailsWith<DeciDivisionByZeroException> {
            Deci("5") % Deci.ZERO
        }
    }

    @Test
    fun `rem with both negative`() {
        assertEquals(Deci("-1"), Deci("-7") % Deci("-3"))
    }

    @Test
    fun `rem with small decimals`() {
        assertEquals(Deci("0.1"), Deci("1.1") % Deci("0.5"))
    }

    // -- Comparison operators --

    @Test
    fun `less than operator`() {
        assertTrue(Deci("1") < Deci("2"))
        assertFalse(Deci("2") < Deci("1"))
        assertFalse(Deci("1") < Deci("1"))
    }

    @Test
    fun `greater than operator`() {
        assertTrue(Deci("2") > Deci("1"))
        assertFalse(Deci("1") > Deci("2"))
        assertFalse(Deci("1") > Deci("1"))
    }

    @Test
    fun `less than or equal at boundary`() {
        assertTrue(Deci("1") <= Deci("1"))
        assertTrue(Deci("1") <= Deci("2"))
        assertFalse(Deci("2") <= Deci("1"))
    }

    @Test
    fun `greater than or equal at boundary`() {
        assertTrue(Deci("1") >= Deci("1"))
        assertTrue(Deci("2") >= Deci("1"))
        assertFalse(Deci("1") >= Deci("2"))
    }

    @Test
    fun `comparison with negative values`() {
        assertTrue(Deci("-5") < Deci("0"))
        assertTrue(Deci("-1") > Deci("-2"))
        assertTrue(Deci("-1") >= Deci("-1"))
    }

    @Test
    fun `comparison with different scales is value-based`() {
        assertTrue(Deci("1.0") <= Deci("1.00"))
        assertTrue(Deci("1.0") >= Deci("1.00"))
        assertFalse(Deci("1.0") < Deci("1.00"))
        assertFalse(Deci("1.0") > Deci("1.00"))
    }
}
