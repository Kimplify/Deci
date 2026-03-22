package org.kimplify.deci.validation

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext
import org.kimplify.deci.RoundingMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeciValidationExtendedTest {
    // --- isValidDeci extended ---

    @Test
    fun `isValidDeci accepts valid integers`() {
        assertTrue("123".isValidDeci())
        assertTrue("-456".isValidDeci())
        assertTrue("0".isValidDeci())
    }

    @Test
    fun `isValidDeci accepts valid decimals`() {
        assertTrue("123.45".isValidDeci())
        assertTrue("-0.789".isValidDeci())
        assertTrue(".45".isValidDeci())
    }

    @Test
    fun `isValidDeci accepts comma-separated values`() {
        assertTrue("123,45".isValidDeci())
        assertTrue("1,234.56".isValidDeci())
    }

    @Test
    fun `isValidDeci rejects blank strings`() {
        assertFalse("".isValidDeci())
        assertFalse("   ".isValidDeci())
    }

    @Test
    fun `isValidDeci rejects non-numeric`() {
        assertFalse("abc".isValidDeci())
        assertFalse("12a34".isValidDeci())
    }

    // --- toDeciOrError extended ---

    @Test
    fun `toDeciOrError success for valid input`() {
        val result = "123.45".toDeciOrError()
        assertTrue(result.isSuccess)
        assertEquals(Deci("123.45"), result.getOrNull())
    }

    @Test
    fun `toDeciOrError failure for invalid input`() {
        val result = "abc".toDeciOrError()
        assertTrue(result.isFailure)
    }

    @Test
    fun `toDeciOrError failure for empty string`() {
        val result = "".toDeciOrError()
        assertTrue(result.isFailure)
    }

    @Test
    fun `toDeciOrError success for negative value`() {
        val result = "-42.5".toDeciOrError()
        assertTrue(result.isSuccess)
        assertEquals(Deci("-42.5"), result.getOrNull())
    }

    // --- isInRange extended ---

    @Test
    fun `isInRange within range`() {
        assertTrue(Deci("50").isInRange(Deci("0"), Deci("100")))
    }

    @Test
    fun `isInRange at min boundary`() {
        assertTrue(Deci("0").isInRange(Deci("0"), Deci("100")))
    }

    @Test
    fun `isInRange at max boundary`() {
        assertTrue(Deci("100").isInRange(Deci("0"), Deci("100")))
    }

    @Test
    fun `isInRange below min`() {
        assertFalse(Deci("-1").isInRange(Deci("0"), Deci("100")))
    }

    @Test
    fun `isInRange above max`() {
        assertFalse(Deci("101").isInRange(Deci("0"), Deci("100")))
    }

    @Test
    fun `isInRange with equal min and max`() {
        assertTrue(Deci("5").isInRange(Deci("5"), Deci("5")))
        assertFalse(Deci("6").isInRange(Deci("5"), Deci("5")))
    }

    @Test
    fun `isInRange throws when min greater than max`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("5").isInRange(Deci("10"), Deci("1"))
        }
    }

    @Test
    fun `isInRange with negative range`() {
        assertTrue(Deci("-5").isInRange(Deci("-10"), Deci("0")))
    }

    // --- clamp extended ---

    @Test
    fun `clamp value within range returns unchanged`() {
        assertEquals(Deci("15"), Deci("15").clamp(Deci("10"), Deci("20")))
    }

    @Test
    fun `clamp value below min returns min`() {
        assertEquals(Deci("10"), Deci("5").clamp(Deci("10"), Deci("20")))
    }

    @Test
    fun `clamp value above max returns max`() {
        assertEquals(Deci("20"), Deci("25").clamp(Deci("10"), Deci("20")))
    }

    @Test
    fun `clamp at min boundary`() {
        assertEquals(Deci("10"), Deci("10").clamp(Deci("10"), Deci("20")))
    }

    @Test
    fun `clamp at max boundary`() {
        assertEquals(Deci("20"), Deci("20").clamp(Deci("10"), Deci("20")))
    }

    @Test
    fun `clamp throws when min greater than max`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("5").clamp(Deci("20"), Deci("10"))
        }
    }

    @Test
    fun `clamp with negative range`() {
        assertEquals(Deci("-5"), Deci("-5").clamp(Deci("-10"), Deci("0")))
        assertEquals(Deci("-10"), Deci("-15").clamp(Deci("-10"), Deci("0")))
    }

    // --- isWhole extended ---

    @Test
    fun `isWhole for integer`() {
        assertTrue(Deci("123").isWhole())
    }

    @Test
    fun `isWhole for value with zero fractional part`() {
        assertTrue(Deci("123.0").isWhole())
        assertTrue(Deci("123.00").isWhole())
        assertTrue(Deci("123.000").isWhole())
    }

    @Test
    fun `isWhole for fractional value`() {
        assertFalse(Deci("123.45").isWhole())
        assertFalse(Deci("0.1").isWhole())
    }

    @Test
    fun `isWhole for zero`() {
        assertTrue(Deci("0").isWhole())
    }

    @Test
    fun `isWhole for negative integer`() {
        assertTrue(Deci("-42").isWhole())
    }

    @Test
    fun `isWhole for negative fractional`() {
        assertFalse(Deci("-42.5").isWhole())
    }

    // --- isEven extended ---

    @Test
    fun `isEven for even numbers`() {
        assertTrue(Deci("0").isEven())
        assertTrue(Deci("2").isEven())
        assertTrue(Deci("4").isEven())
        assertTrue(Deci("-6").isEven())
        assertTrue(Deci("100").isEven())
    }

    @Test
    fun `isEven for odd numbers`() {
        assertFalse(Deci("1").isEven())
        assertFalse(Deci("3").isEven())
        assertFalse(Deci("-5").isEven())
    }

    @Test
    fun `isEven throws for non-whole number`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("1.5").isEven()
        }
    }

    // --- isOdd extended ---

    @Test
    fun `isOdd for odd numbers`() {
        assertTrue(Deci("1").isOdd())
        assertTrue(Deci("3").isOdd())
        assertTrue(Deci("-5").isOdd())
        assertTrue(Deci("99").isOdd())
    }

    @Test
    fun `isOdd for even numbers`() {
        assertFalse(Deci("0").isOdd())
        assertFalse(Deci("2").isOdd())
        assertFalse(Deci("-4").isOdd())
    }

    @Test
    fun `isOdd throws for non-whole number`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("2.5").isOdd()
        }
    }

    // --- safeDivide extended ---

    @Test
    fun `safeDivide with normal division`() {
        assertEquals(Deci("5"), Deci("10").safeDivide(Deci("2")))
    }

    @Test
    fun `safeDivide with zero divisor returns default zero`() {
        assertEquals(Deci.ZERO, Deci("10").safeDivide(Deci.ZERO))
    }

    @Test
    fun `safeDivide with zero divisor returns custom default`() {
        assertEquals(Deci("-1"), Deci("10").safeDivide(Deci.ZERO, Deci("-1")))
    }

    @Test
    fun `safeDivide with explicit context`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        assertEquals(Deci("3.33"), Deci("10").safeDivide(Deci("3"), context = context))
    }

    @Test
    fun `safeDivide with BANKING context`() {
        val result = Deci("1").safeDivide(Deci("4"), context = DeciContext.BANKING)
        assertEquals(Deci("0.25"), result)
    }

    // --- hasValidDecimalPlaces extended ---

    @Test
    fun `hasValidDecimalPlaces with fewer places`() {
        assertTrue(Deci("123.4").hasValidDecimalPlaces(2))
    }

    @Test
    fun `hasValidDecimalPlaces with exact places`() {
        assertTrue(Deci("123.45").hasValidDecimalPlaces(2))
    }

    @Test
    fun `hasValidDecimalPlaces with more places`() {
        assertFalse(Deci("123.456").hasValidDecimalPlaces(2))
    }

    @Test
    fun `hasValidDecimalPlaces with no decimal part`() {
        assertTrue(Deci("123").hasValidDecimalPlaces(0))
    }

    @Test
    fun `hasValidDecimalPlaces with zero max places and decimal value`() {
        assertFalse(Deci("123.4").hasValidDecimalPlaces(0))
    }

    @Test
    fun `hasValidDecimalPlaces throws for negative max`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("123").hasValidDecimalPlaces(-1)
        }
    }

    // --- isValidCurrencyAmount extended ---

    @Test
    fun `isValidCurrencyAmount USD accepts 2 decimal places`() {
        assertTrue(Deci("123.45").isValidCurrencyAmount("USD"))
        assertFalse(Deci("123.456").isValidCurrencyAmount("USD"))
    }

    @Test
    fun `isValidCurrencyAmount EUR accepts 2 decimal places`() {
        assertTrue(Deci("123.45").isValidCurrencyAmount("EUR"))
    }

    @Test
    fun `isValidCurrencyAmount GBP accepts 2 decimal places`() {
        assertTrue(Deci("123.45").isValidCurrencyAmount("GBP"))
    }

    @Test
    fun `isValidCurrencyAmount JPY requires whole numbers`() {
        assertTrue(Deci("123").isValidCurrencyAmount("JPY"))
        assertFalse(Deci("123.45").isValidCurrencyAmount("JPY"))
    }

    @Test
    fun `isValidCurrencyAmount KRW requires whole numbers`() {
        assertTrue(Deci("123").isValidCurrencyAmount("KRW"))
        assertFalse(Deci("123.1").isValidCurrencyAmount("KRW"))
    }

    @Test
    fun `isValidCurrencyAmount BTC accepts 8 decimal places`() {
        assertTrue(Deci("0.12345678").isValidCurrencyAmount("BTC"))
        assertFalse(Deci("0.123456789").isValidCurrencyAmount("BTC"))
    }

    @Test
    fun `isValidCurrencyAmount unknown currency defaults to 2 decimal places`() {
        assertTrue(Deci("123.45").isValidCurrencyAmount("XYZ"))
        assertFalse(Deci("123.456").isValidCurrencyAmount("XYZ"))
    }

    @Test
    fun `isValidCurrencyAmount is case insensitive`() {
        assertTrue(Deci("123.45").isValidCurrencyAmount("usd"))
        assertTrue(Deci("123").isValidCurrencyAmount("jpy"))
    }

    // --- isValidPercentage extended ---

    @Test
    fun `isValidPercentage default accepts 0 to 100`() {
        assertTrue(Deci("0").isValidPercentage())
        assertTrue(Deci("50").isValidPercentage())
        assertTrue(Deci("100").isValidPercentage())
        assertFalse(Deci("-1").isValidPercentage())
        assertFalse(Deci("101").isValidPercentage())
    }

    @Test
    fun `isValidPercentage with allowNegative`() {
        assertTrue(Deci("-50").isValidPercentage(allowNegative = true))
        assertTrue(Deci("-100").isValidPercentage(allowNegative = true))
        assertFalse(Deci("-101").isValidPercentage(allowNegative = true))
    }

    @Test
    fun `isValidPercentage with allowOver100`() {
        assertTrue(Deci("150").isValidPercentage(allowOver100 = true))
        assertTrue(Deci("1000").isValidPercentage(allowOver100 = true))
        assertFalse(Deci("1001").isValidPercentage(allowOver100 = true))
    }

    @Test
    fun `isValidPercentage with both flags`() {
        assertTrue(Deci("-50").isValidPercentage(allowNegative = true, allowOver100 = true))
        assertTrue(Deci("500").isValidPercentage(allowNegative = true, allowOver100 = true))
    }

    // --- isPositiveStrict extended ---

    @Test
    fun `isPositiveStrict for positive values`() {
        assertTrue(Deci("1").isPositiveStrict())
        assertTrue(Deci("0.001").isPositiveStrict())
    }

    @Test
    fun `isPositiveStrict for zero`() {
        assertFalse(Deci("0").isPositiveStrict())
    }

    @Test
    fun `isPositiveStrict for negative values`() {
        assertFalse(Deci("-1").isPositiveStrict())
        assertFalse(Deci("-0.001").isPositiveStrict())
    }

    // --- isNonNegative extended ---

    @Test
    fun `isNonNegative for positive values`() {
        assertTrue(Deci("1").isNonNegative())
    }

    @Test
    fun `isNonNegative for zero`() {
        assertTrue(Deci("0").isNonNegative())
    }

    @Test
    fun `isNonNegative for negative values`() {
        assertFalse(Deci("-1").isNonNegative())
        assertFalse(Deci("-0.001").isNonNegative())
    }

    // --- isValidTaxRate extended ---

    @Test
    fun `isValidTaxRate accepts 0 to 1`() {
        assertTrue(Deci("0").isValidTaxRate())
        assertTrue(Deci("0.5").isValidTaxRate())
        assertTrue(Deci("1").isValidTaxRate())
    }

    @Test
    fun `isValidTaxRate rejects outside range`() {
        assertFalse(Deci("-0.01").isValidTaxRate())
        assertFalse(Deci("1.01").isValidTaxRate())
    }

    // --- isValidInterestRate extended ---

    @Test
    fun `isValidInterestRate with default max`() {
        assertTrue(Deci("0").isValidInterestRate())
        assertTrue(Deci("0.5").isValidInterestRate())
        assertTrue(Deci("1").isValidInterestRate())
        assertFalse(Deci("1.01").isValidInterestRate())
    }

    @Test
    fun `isValidInterestRate with custom max`() {
        assertTrue(Deci("0.5").isValidInterestRate(maxRate = Deci("0.5")))
        assertFalse(Deci("0.51").isValidInterestRate(maxRate = Deci("0.5")))
    }

    @Test
    fun `isValidInterestRate rejects negative`() {
        assertFalse(Deci("-0.01").isValidInterestRate())
    }

    // --- isApproximatelyEqual extended ---

    @Test
    fun `isApproximatelyEqual for equal values`() {
        assertTrue(Deci("1").isApproximatelyEqual(Deci("1")))
    }

    @Test
    fun `isApproximatelyEqual within default tolerance`() {
        assertTrue(Deci("1").isApproximatelyEqual(Deci("1.0000001")))
    }

    @Test
    fun `isApproximatelyEqual outside default tolerance`() {
        assertFalse(Deci("1").isApproximatelyEqual(Deci("1.001")))
    }

    @Test
    fun `isApproximatelyEqual with custom tolerance`() {
        assertTrue(Deci("1").isApproximatelyEqual(Deci("1.5"), tolerance = Deci("1")))
        assertFalse(Deci("1").isApproximatelyEqual(Deci("3"), tolerance = Deci("1")))
    }

    @Test
    fun `isApproximatelyEqual handles negative values`() {
        assertTrue(Deci("-1").isApproximatelyEqual(Deci("-1.0000001")))
    }

    @Test
    fun `isApproximatelyEqual symmetric`() {
        val a = Deci("1")
        val b = Deci("1.0000001")
        assertEquals(a.isApproximatelyEqual(b), b.isApproximatelyEqual(a))
    }

    // --- validateForForm extended ---

    @Test
    fun `validateForForm all valid`() {
        val result =
            Deci("50").validateForForm(
                minValue = Deci("0"),
                maxValue = Deci("100"),
                maxDecimalPlaces = 2,
                mustBePositive = true,
            )
        assertTrue(result.isValid)
    }

    @Test
    fun `validateForForm fails mustBePositive`() {
        val result = Deci("-10").validateForForm(mustBePositive = true)
        assertFalse(result.isValid)
        assertEquals("Value must be positive", result.errorMessage)
    }

    @Test
    fun `validateForForm fails mustBePositive for zero`() {
        val result = Deci("0").validateForForm(mustBePositive = true)
        assertFalse(result.isValid)
    }

    @Test
    fun `validateForForm fails minValue`() {
        val result = Deci("5").validateForForm(minValue = Deci("10"))
        assertFalse(result.isValid)
        assertEquals("Value must be at least 10", result.errorMessage)
    }

    @Test
    fun `validateForForm fails maxValue`() {
        val result = Deci("150").validateForForm(maxValue = Deci("100"))
        assertFalse(result.isValid)
        assertEquals("Value must be at most 100", result.errorMessage)
    }

    @Test
    fun `validateForForm fails maxDecimalPlaces`() {
        val result = Deci("1.234").validateForForm(maxDecimalPlaces = 2)
        assertFalse(result.isValid)
        assertEquals("Value can have at most 2 decimal places", result.errorMessage)
    }

    @Test
    fun `validateForForm with no constraints passes`() {
        val result = Deci("-999.99999").validateForForm()
        assertTrue(result.isValid)
    }

    @Test
    fun `validateForForm checks mustBePositive before min`() {
        val result = Deci("-5").validateForForm(minValue = Deci("-10"), mustBePositive = true)
        assertFalse(result.isValid)
        assertEquals("Value must be positive", result.errorMessage)
    }

    @Test
    fun `validateForForm at boundary values`() {
        val result = Deci("10").validateForForm(minValue = Deci("10"), maxValue = Deci("10"))
        assertTrue(result.isValid)
    }

    // --- ValidationResult ---

    @Test
    fun `ValidationResult valid has null error`() {
        val result = ValidationResult(true)
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun `ValidationResult invalid has error message`() {
        val result = ValidationResult(false, "some error")
        assertFalse(result.isValid)
        assertEquals("some error", result.errorMessage)
    }
}
