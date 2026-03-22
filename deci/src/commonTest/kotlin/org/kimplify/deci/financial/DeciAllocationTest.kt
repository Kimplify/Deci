package org.kimplify.deci.financial

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.extension.sumDeci
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeciAllocationTest {
    @Test
    fun `allocate splits evenly when divisible`() {
        val result = Deci("100").allocate(4, DeciContext.CURRENCY_USD)
        assertEquals(4, result.size)
        result.forEach { assertEquals(Deci("25.00"), it) }
        assertEquals(Deci("100"), result.sumDeci().setScale(2, RoundingMode.HALF_UP))
    }

    @Test
    fun `allocate distributes remainder`() {
        val result = Deci("100").allocate(3, DeciContext.CURRENCY_USD)
        assertEquals(3, result.size)
        assertEquals(Deci("100"), result.sumDeci().setScale(2, RoundingMode.HALF_UP))
    }

    @Test
    fun `allocate sum invariant`() {
        val amount = Deci("99.99")
        val result = amount.allocate(7, DeciContext.CURRENCY_USD)
        assertEquals(amount, result.sumDeci().setScale(2, RoundingMode.HALF_UP))
    }

    @Test
    fun `allocate throws for zero parts`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("100").allocate(0)
        }
    }

    @Test
    fun `allocateByRatios splits proportionally`() {
        val result =
            Deci("100").allocateByRatios(
                listOf(Deci("2"), Deci("3"), Deci("5")),
                DeciContext.CURRENCY_USD,
            )
        assertEquals(3, result.size)
        assertEquals(Deci("100"), result.sumDeci().setScale(2, RoundingMode.HALF_UP))
    }

    @Test
    fun `allocateByRatios sum invariant`() {
        val result =
            Deci("1000").allocateByRatios(
                listOf(Deci("1"), Deci("1"), Deci("1")),
                DeciContext.CURRENCY_USD,
            )
        assertEquals(Deci("1000"), result.sumDeci().setScale(2, RoundingMode.HALF_UP))
    }
}
