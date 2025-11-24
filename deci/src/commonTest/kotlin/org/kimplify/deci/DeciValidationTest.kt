package org.kimplify.deci

import org.kimplify.deci.validation.clamp
import org.kimplify.deci.validation.hasValidDecimalPlaces
import org.kimplify.deci.validation.isEven
import org.kimplify.deci.validation.isInRange
import org.kimplify.deci.validation.isOdd
import org.kimplify.deci.validation.isValidCurrencyAmount
import org.kimplify.deci.validation.isValidDeci
import org.kimplify.deci.validation.isValidPercentage
import org.kimplify.deci.validation.isWhole
import org.kimplify.deci.validation.safeDivide
import org.kimplify.deci.validation.toDeciOrError
import org.kimplify.deci.validation.validateForForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeciValidationTest {

    @Test
    fun `isValidDeci validates strings correctly`() {
        assertTrue("123.45".isValidDeci())
        assertTrue("123,45".isValidDeci())
        assertTrue("-123.45".isValidDeci())
        assertTrue(".45".isValidDeci())
        assertTrue("123.".isValidDeci())

        assertFalse("".isValidDeci())
        assertFalse("abc".isValidDeci())
        assertFalse("12.34.56".isValidDeci())
    }

    @Test
    fun `toDeciOrError returns correct results`() {
        val validResult = "123.45".toDeciOrError()
        assertTrue(validResult.isSuccess)
        assertEquals(Deci("123.45"), validResult.getOrNull())

        val invalidResult = "abc".toDeciOrError()
        assertTrue(invalidResult.isFailure)
    }

    @Test
    fun `isInRange validates range correctly`() {
        val value = Deci("50")
        assertTrue(value.isInRange(Deci("0"), Deci("100")))
        assertFalse(value.isInRange(Deci("60"), Deci("100")))
        assertFalse(value.isInRange(Deci("0"), Deci("40")))
    }

    @Test
    fun `clamp constrains value to range`() {
        assertEquals(Deci("10"), Deci("5").clamp(Deci("10"), Deci("20")))
        assertEquals(Deci("20"), Deci("25").clamp(Deci("10"), Deci("20")))
        assertEquals(Deci("15"), Deci("15").clamp(Deci("10"), Deci("20")))
    }

    @Test
    fun `isWhole identifies whole numbers correctly`() {
        assertTrue(Deci("123").isWhole())
        assertTrue(Deci("123.0").isWhole())
        assertTrue(Deci("123.00").isWhole())
        assertFalse(Deci("123.45").isWhole())
    }

    @Test
    fun `isEven and isOdd work correctly`() {
        assertTrue(Deci("4").isEven())
        assertFalse(Deci("5").isEven())
        assertTrue(Deci("5").isOdd())
        assertFalse(Deci("4").isOdd())
    }

    @Test
    fun `safeDivide returns default for zero divisor`() {
        assertEquals(Deci.ZERO, Deci("10").safeDivide(Deci.ZERO))
        assertEquals(Deci("5"), Deci("10").safeDivide(Deci("2")))
        assertEquals(Deci("-1"), Deci("10").safeDivide(Deci.ZERO, Deci("-1")))
    }

    @Test
    fun `hasValidDecimalPlaces checks decimal places correctly`() {
        assertTrue(Deci("123.45").hasValidDecimalPlaces(2))
        assertTrue(Deci("123.4").hasValidDecimalPlaces(2))
        assertTrue(Deci("123").hasValidDecimalPlaces(2))
        assertFalse(Deci("123.456").hasValidDecimalPlaces(2))
    }

    @Test
    fun `isValidCurrencyAmount validates currency amounts`() {
        assertTrue(Deci("123.45").isValidCurrencyAmount("USD"))
        assertTrue(Deci("123").isValidCurrencyAmount("JPY"))
        assertFalse(Deci("123.456").isValidCurrencyAmount("USD"))
        assertFalse(Deci("123.45").isValidCurrencyAmount("JPY"))
    }

    @Test
    fun `isValidPercentage validates percentages`() {
        assertTrue(Deci("50").isValidPercentage())
        assertTrue(Deci("0").isValidPercentage())
        assertTrue(Deci("100").isValidPercentage())
        assertFalse(Deci("-10").isValidPercentage())
        assertFalse(Deci("150").isValidPercentage())

        assertTrue(Deci("-10").isValidPercentage(allowNegative = true))
        assertTrue(Deci("150").isValidPercentage(allowOver100 = true))
    }

    @Test
    fun `validateForForm returns correct validation results`() {
        val value = Deci("50")

        val validResult = value.validateForForm(
            minValue = Deci("0"),
            maxValue = Deci("100"),
            maxDecimalPlaces = 2,
            mustBePositive = true
        )
        assertTrue(validResult.isValid)

        val invalidResult = Deci("-10").validateForForm(mustBePositive = true)
        assertFalse(invalidResult.isValid)
        assertEquals("Value must be positive", invalidResult.errorMessage)
    }
}
