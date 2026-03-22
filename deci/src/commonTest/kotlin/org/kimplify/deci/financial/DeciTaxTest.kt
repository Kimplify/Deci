package org.kimplify.deci.financial

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext
import kotlin.test.Test
import kotlin.test.assertEquals

class DeciTaxTest {
    @Test
    fun `taxAmount calculates correctly`() {
        assertEquals(Deci("8.00"), Deci("100").taxAmount(Deci("0.08")))
    }

    @Test
    fun `withTax adds tax to amount`() {
        assertEquals(Deci("108.00"), Deci("100").withTax(Deci("0.08")))
    }

    @Test
    fun `preTax extracts pre-tax amount`() {
        assertEquals(Deci("100.00"), Deci("108").preTax(Deci("0.08")))
    }

    @Test
    fun `withDiscount applies discount`() {
        assertEquals(Deci("80.00"), Deci("100").withDiscount(Deci("0.20")))
    }

    @Test
    fun `withDiscount with custom context`() {
        val ctx = DeciContext.CURRENCY_USD
        assertEquals(Deci("90.00"), Deci("100").withDiscount(Deci("0.10"), ctx))
    }
}
