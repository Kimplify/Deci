package org.kimplify.deci.formatting

import org.kimplify.deci.Deci
import org.kimplify.deci.exception.DeciFormatException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeciFormattingTest {

    // --- formatCurrency ---

    @Test
    fun `formatCurrency with default parameters`() {
        assertEquals("$1,234.57", Deci("1234.567").formatCurrency())
    }

    @Test
    fun `formatCurrency rounds to two decimal places by default`() {
        assertEquals("$1.24", Deci("1.235").formatCurrency())
    }

    @Test
    fun `formatCurrency pads to two decimal places`() {
        assertEquals("$1.00", Deci("1").formatCurrency())
    }

    @Test
    fun `formatCurrency with custom symbol`() {
        assertEquals("€1,234.57", Deci("1234.567").formatCurrency(currencySymbol = "€"))
    }

    @Test
    fun `formatCurrency with custom scale`() {
        assertEquals("$1,234.5670", Deci("1234.567").formatCurrency(scale = 4))
    }

    @Test
    fun `formatCurrency with custom thousands separator`() {
        assertEquals("$1.234.57", Deci("1234.567").formatCurrency(thousandsSeparator = "."))
    }

    @Test
    fun `formatCurrency with negative value`() {
        assertEquals("-$1,234.57", Deci("-1234.567").formatCurrency())
    }

    @Test
    fun `formatCurrency with zero`() {
        assertEquals("$0.00", Deci("0").formatCurrency())
    }

    @Test
    fun `formatCurrency with large number`() {
        assertEquals("$1,000,000.00", Deci("1000000").formatCurrency())
    }

    @Test
    fun `formatCurrency with small value`() {
        assertEquals("$0.01", Deci("0.01").formatCurrency())
    }

    @Test
    fun `formatCurrency with negative zero-point value`() {
        assertEquals("-$0.50", Deci("-0.5").formatCurrency())
    }

    @Test
    fun `formatCurrency with zero scale shows integer`() {
        assertEquals("$1,235", Deci("1234.567").formatCurrency(scale = 0))
    }

    // --- formatWithThousandsSeparator ---

    @Test
    fun `formatWithThousandsSeparator with default separator`() {
        assertEquals("1,234,567", Deci("1234567").formatWithThousandsSeparator())
    }

    @Test
    fun `formatWithThousandsSeparator with custom separator`() {
        assertEquals("1.234.567", Deci("1234567").formatWithThousandsSeparator("."))
    }

    @Test
    fun `formatWithThousandsSeparator preserves decimal part`() {
        assertEquals("1,234.56", Deci("1234.56").formatWithThousandsSeparator())
    }

    @Test
    fun `formatWithThousandsSeparator with value less than 1000`() {
        assertEquals("123", Deci("123").formatWithThousandsSeparator())
    }

    @Test
    fun `formatWithThousandsSeparator with negative value`() {
        assertEquals("-1,234", Deci("-1234").formatWithThousandsSeparator())
    }

    @Test
    fun `formatWithThousandsSeparator with zero`() {
        assertEquals("0", Deci("0").formatWithThousandsSeparator())
    }

    @Test
    fun `formatWithThousandsSeparator with negative and decimal`() {
        assertEquals("-1,234.56", Deci("-1234.56").formatWithThousandsSeparator())
    }

    @Test
    fun `formatWithThousandsSeparator single digit`() {
        assertEquals("5", Deci("5").formatWithThousandsSeparator())
    }

    @Test
    fun `formatWithThousandsSeparator exactly 1000`() {
        assertEquals("1,000", Deci("1000").formatWithThousandsSeparator())
    }

    // --- formatAsPercentage ---

    @Test
    fun `formatAsPercentage with default parameters`() {
        assertEquals("50.0%", Deci("0.5").formatAsPercentage())
    }

    @Test
    fun `formatAsPercentage with full value`() {
        assertEquals("100.0%", Deci("1").formatAsPercentage())
    }

    @Test
    fun `formatAsPercentage with zero`() {
        assertEquals("0.0%", Deci("0").formatAsPercentage())
    }

    @Test
    fun `formatAsPercentage with custom scale`() {
        assertEquals("33.33%", Deci("0.3333").formatAsPercentage(scale = 2))
    }

    @Test
    fun `formatAsPercentage with zero scale`() {
        assertEquals("50%", Deci("0.5").formatAsPercentage(scale = 0))
    }

    @Test
    fun `formatAsPercentage with custom symbol`() {
        assertEquals("50.0 percent", Deci("0.5").formatAsPercentage(symbol = " percent"))
    }

    @Test
    fun `formatAsPercentage with negative value`() {
        assertEquals("-25.0%", Deci("-0.25").formatAsPercentage())
    }

    @Test
    fun `formatAsPercentage over 100 percent`() {
        assertEquals("150.0%", Deci("1.5").formatAsPercentage())
    }

    @Test
    fun `formatAsPercentage small fraction`() {
        assertEquals("0.1%", Deci("0.001").formatAsPercentage())
    }

    // --- toScientificNotation ---

    @Test
    fun `toScientificNotation with default precision`() {
        assertEquals("1.234500E+4", Deci("12345").toScientificNotation())
    }

    @Test
    fun `toScientificNotation with small number`() {
        assertEquals("7.890000E-3", Deci("0.00789").toScientificNotation())
    }

    @Test
    fun `toScientificNotation with zero`() {
        assertEquals("0.0E+0", Deci("0").toScientificNotation())
    }

    @Test
    fun `toScientificNotation with one`() {
        assertEquals("1E+0", Deci("1").toScientificNotation())
    }

    @Test
    fun `toScientificNotation with negative value`() {
        assertEquals("-1.234500E+4", Deci("-12345").toScientificNotation())
    }

    @Test
    fun `toScientificNotation with custom precision`() {
        assertEquals("1.23E+4", Deci("12345").toScientificNotation(precision = 2))
    }

    @Test
    fun `toScientificNotation with precision zero`() {
        assertEquals("1E+4", Deci("12345").toScientificNotation(precision = 0))
    }

    @Test
    fun `toScientificNotation with value between 1 and 10`() {
        assertEquals("5.500000E+0", Deci("5.5").toScientificNotation())
    }

    @Test
    fun `toScientificNotation with very small number`() {
        assertEquals("1E-6", Deci("0.000001").toScientificNotation())
    }

    @Test
    fun `toScientificNotation with exactly 10`() {
        assertEquals("1.000000E+1", Deci("10").toScientificNotation())
    }

    // --- format ---

    @Test
    fun `format with pattern 0 dot 00`() {
        assertEquals("1234.57", Deci("1234.567").format("0.00"))
    }

    @Test
    fun `format with pattern hash comma hash hash 0 dot 00`() {
        assertEquals("1,234.57", Deci("1234.567").format("#,##0.00"))
    }

    @Test
    fun `format with pattern 0 dot 0000`() {
        assertEquals("1234.5670", Deci("1234.567").format("0.0000"))
    }

    @Test
    fun `format with pattern hash comma hash hash 0`() {
        assertEquals("1,235", Deci("1234.567").format("#,##0"))
    }

    @Test
    fun `format throws on unsupported pattern`() {
        assertFailsWith<DeciFormatException> {
            Deci("1234.567").format("unknown")
        }
    }

    @Test
    fun `format 0 dot 00 with integer value`() {
        assertEquals("100.00", Deci("100").format("0.00"))
    }

    @Test
    fun `format hash comma hash hash 0 with large number`() {
        assertEquals("1,000,000", Deci("1000000").format("#,##0"))
    }

    @Test
    fun `format 0 dot 00 with negative value`() {
        assertEquals("-1234.57", Deci("-1234.567").format("0.00"))
    }

    @Test
    fun `format hash comma hash hash 0 dot 00 with small value`() {
        assertEquals("0.50", Deci("0.5").format("#,##0.00"))
    }

    // --- toWords ---

    @Test
    fun `toWords zero`() {
        assertEquals("zero", Deci("0").toWords())
    }

    @Test
    fun `toWords single digit`() {
        assertEquals("one", Deci("1").toWords())
        assertEquals("nine", Deci("9").toWords())
    }

    @Test
    fun `toWords teens`() {
        assertEquals("ten", Deci("10").toWords())
        assertEquals("eleven", Deci("11").toWords())
        assertEquals("twelve", Deci("12").toWords())
        assertEquals("thirteen", Deci("13").toWords())
        assertEquals("nineteen", Deci("19").toWords())
    }

    @Test
    fun `toWords tens`() {
        assertEquals("twenty", Deci("20").toWords())
        assertEquals("thirty", Deci("30").toWords())
        assertEquals("forty two", Deci("42").toWords())
        assertEquals("ninety nine", Deci("99").toWords())
    }

    @Test
    fun `toWords hundreds`() {
        assertEquals("one hundred", Deci("100").toWords())
        assertEquals("two hundred fifty six", Deci("256").toWords())
        assertEquals("nine hundred ninety nine", Deci("999").toWords())
    }

    @Test
    fun `toWords negative value`() {
        assertEquals("negative forty two", Deci("-42").toWords())
    }

    @Test
    fun `toWords number too large`() {
        assertEquals("number too large", Deci("1000").toWords())
    }

    @Test
    fun `toWords ignores fractional part`() {
        assertEquals("forty two", Deci("42.99").toWords())
    }

    // --- pad ---

    @Test
    fun `pad left with spaces by default`() {
        assertEquals("   123", Deci("123").pad(6))
    }

    @Test
    fun `pad right with spaces`() {
        assertEquals("123   ", Deci("123").pad(6, padLeft = false))
    }

    @Test
    fun `pad left with custom char`() {
        assertEquals("000123", Deci("123").pad(6, padChar = '0'))
    }

    @Test
    fun `pad right with custom char`() {
        assertEquals("123000", Deci("123").pad(6, padChar = '0', padLeft = false))
    }

    @Test
    fun `pad when string is already wider than width`() {
        assertEquals("12345", Deci("12345").pad(3))
    }

    @Test
    fun `pad with decimal value`() {
        assertEquals("  1.5", Deci("1.5").pad(5))
    }

    @Test
    fun `pad with negative value`() {
        assertEquals("  -42", Deci("-42").pad(5))
    }

    @Test
    fun `pad with exact width returns unchanged`() {
        assertEquals("123", Deci("123").pad(3))
    }
}
