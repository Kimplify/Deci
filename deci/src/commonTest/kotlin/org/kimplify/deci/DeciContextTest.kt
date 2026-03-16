package org.kimplify.deci

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeciContextTest {
    @Test
    fun `DEFAULT has expected values`() {
        assertEquals(20, DeciContext.DEFAULT.precision)
        assertEquals(RoundingMode.HALF_UP, DeciContext.DEFAULT.roundingMode)
    }

    @Test
    fun `CURRENCY_USD has expected values`() {
        assertEquals(2, DeciContext.CURRENCY_USD.precision)
        assertEquals(RoundingMode.HALF_UP, DeciContext.CURRENCY_USD.roundingMode)
    }

    @Test
    fun `BANKING has expected values`() {
        assertEquals(2, DeciContext.BANKING.precision)
        assertEquals(RoundingMode.HALF_EVEN, DeciContext.BANKING.roundingMode)
    }

    @Test
    fun `rejects negative precision`() {
        assertFailsWith<IllegalArgumentException> {
            DeciContext(precision = -1, roundingMode = RoundingMode.HALF_UP)
        }
    }

    @Test
    fun `data class equality`() {
        val a = DeciContext(2, RoundingMode.HALF_UP)
        val b = DeciContext(2, RoundingMode.HALF_UP)
        assertEquals(a, b)
    }

    @Test
    fun `divide with DeciContext uses context scale and rounding`() {
        val ctx = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = Deci("1").divide(Deci("3"), ctx)
        assertEquals(Deci("0.33"), result)
    }

    @Test
    fun `divide with CURRENCY_USD context`() {
        val result = Deci("10").divide(Deci("3"), DeciContext.CURRENCY_USD)
        assertEquals(Deci("3.33"), result)
    }

    @Test
    fun `divide with BANKING context uses HALF_EVEN`() {
        val result = Deci("1").divide(Deci("4"), DeciContext.BANKING)
        assertEquals(Deci("0.25"), result)
    }
}
