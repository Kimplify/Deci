package org.kimplify.deci.financial

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.exception.DeciDivisionByZeroException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeciPercentageTest {
    @Test
    fun `percentageChangeTo calculates increase`() {
        val result = Deci("100").percentageChangeTo(Deci("125"), DeciContext(2, RoundingMode.HALF_UP))
        assertEquals(Deci("25.00"), result.setScale(2, RoundingMode.HALF_UP))
    }

    @Test
    fun `percentageChangeTo calculates decrease`() {
        val result = Deci("100").percentageChangeTo(Deci("80"), DeciContext(2, RoundingMode.HALF_UP))
        assertEquals(Deci("-20.00"), result.setScale(2, RoundingMode.HALF_UP))
    }

    @Test
    fun `percentageChangeTo throws for zero base`() {
        assertFailsWith<DeciDivisionByZeroException> {
            Deci.ZERO.percentageChangeTo(Deci("100"))
        }
    }

    @Test
    fun `grossMargin calculation`() {
        val result = grossMargin(Deci("200"), Deci("120"), DeciContext(2, RoundingMode.HALF_UP))
        assertEquals(Deci("40.00"), result.setScale(2, RoundingMode.HALF_UP))
    }

    @Test
    fun `markup calculation`() {
        val result = markup(Deci("150"), Deci("100"), DeciContext(2, RoundingMode.HALF_UP))
        assertEquals(Deci("50.00"), result.setScale(2, RoundingMode.HALF_UP))
    }
}
