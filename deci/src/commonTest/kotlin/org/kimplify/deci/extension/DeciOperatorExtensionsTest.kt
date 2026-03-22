package org.kimplify.deci.extension

import org.kimplify.deci.Deci
import kotlin.test.Test
import kotlin.test.assertEquals

class DeciOperatorExtensionsTest {
    @Test
    fun `Deci plus Int`() {
        assertEquals(Deci("15"), Deci("10") + 5)
    }

    @Test
    fun `Deci minus Int`() {
        assertEquals(Deci("5"), Deci("10") - 5)
    }

    @Test
    fun `Deci times Int`() {
        assertEquals(Deci("50"), Deci("10") * 5)
    }

    @Test
    fun `Deci plus Long`() {
        assertEquals(Deci("15"), Deci("10") + 5L)
    }

    @Test
    fun `Deci minus Long`() {
        assertEquals(Deci("5"), Deci("10") - 5L)
    }

    @Test
    fun `Deci times Long`() {
        assertEquals(Deci("50"), Deci("10") * 5L)
    }

    @Test
    fun `Int times Deci`() {
        assertEquals(Deci("50"), 5 * Deci("10"))
    }

    @Test
    fun `Long times Deci`() {
        assertEquals(Deci("50"), 5L * Deci("10"))
    }

    @Test
    fun `d factory from String`() {
        assertEquals(Deci("19.99"), d("19.99"))
    }

    @Test
    fun `d factory from Int`() {
        assertEquals(Deci("42"), d(42))
    }

    @Test
    fun `d factory from Long`() {
        assertEquals(Deci("100"), d(100L))
    }
}
