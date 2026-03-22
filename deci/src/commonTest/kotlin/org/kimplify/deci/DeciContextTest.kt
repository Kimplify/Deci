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

    @Test
    fun `CURRENCY_EUR has 2 decimal places with HALF_EVEN`() {
        assertEquals(2, DeciContext.CURRENCY_EUR.precision)
        assertEquals(RoundingMode.HALF_EVEN, DeciContext.CURRENCY_EUR.roundingMode)
    }

    @Test
    fun `CURRENCY_JPY has 0 decimal places`() {
        assertEquals(0, DeciContext.CURRENCY_JPY.precision)
    }

    @Test
    fun `CURRENCY_BTC has 8 decimal places`() {
        assertEquals(8, DeciContext.CURRENCY_BTC.precision)
    }

    @Test
    fun `forCurrency returns correct context`() {
        assertEquals(DeciContext.CURRENCY_USD, DeciContext.forCurrency("USD"))
        assertEquals(DeciContext.CURRENCY_EUR, DeciContext.forCurrency("EUR"))
        assertEquals(DeciContext.CURRENCY_JPY, DeciContext.forCurrency("JPY"))
        assertEquals(DeciContext.CURRENCY_BTC, DeciContext.forCurrency("BTC"))
        assertEquals(DeciContext.CURRENCY_USD, DeciContext.forCurrency("UNKNOWN"))
    }
}
