package org.kimplify.deci.formatting

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciConstants
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.exception.DeciFormatException

/**
 * Formatting and display utilities for Deci.
 */

/**
 * Formats this Deci as a currency string.
 *
 * @param currencySymbol The currency symbol to use (default: "$")
 * @param scale Number of decimal places (default: 2)
 * @param thousandsSeparator Thousands separator (default: ",")
 * @return Formatted currency string
 */
fun Deci.formatCurrency(
    currencySymbol: String = "$",
    scale: Int = 2,
    thousandsSeparator: String = ",",
): String {
    val rounded = this.setScale(scale, RoundingMode.HALF_UP)
    val formatted = rounded.formatWithThousandsSeparator(thousandsSeparator)
    return if (this.isNegative()) {
        "-$currencySymbol${formatted.substring(1)}"
    } else {
        "$currencySymbol$formatted"
    }
}

/**
 * Formats this Deci with thousands separators.
 *
 * @param separator The thousands separator (default: ",")
 * @return Formatted string with thousands separators
 */
fun Deci.formatWithThousandsSeparator(separator: String = ","): String {
    val str = this.toString()
    val parts = str.split(".")
    val integerPart = parts[0]
    val decimalPart = if (parts.size > 1) ".${parts[1]}" else ""

    val isNegative = integerPart.startsWith("-")
    val digits = if (isNegative) integerPart.substring(1) else integerPart

    val formattedInteger =
        digits.reversed()
            .chunked(3)
            .joinToString(separator)
            .reversed()

    val result = if (isNegative) "-$formattedInteger" else formattedInteger
    return result + decimalPart
}

/**
 * Formats this Deci as a percentage string.
 *
 * @param scale Number of decimal places (default: 1)
 * @param symbol The percentage symbol (default: "%")
 * @return Formatted percentage string
 */
fun Deci.formatAsPercentage(
    scale: Int = 1,
    symbol: String = "%",
): String {
    val percentage = this * DeciConstants.HUNDRED
    val rounded = percentage.setScale(scale, RoundingMode.HALF_UP)
    return "${rounded}$symbol"
}

/**
 * Formats this [Deci] in scientific (exponential) notation.
 *
 * The mantissa is normalized so that it has exactly one digit before the decimal point,
 * followed by [precision] digits after it. No rounding mode parameter is needed because
 * the mantissa digits are simply truncated/padded from the significant-digit string.
 *
 * Examples:
 * ```
 * Deci("12345").toScientificNotation()   // "1.234500E+4"
 * Deci("0.00789").toScientificNotation() // "7.890000E-3"
 * ```
 *
 * @param precision the number of digits after the decimal point in the mantissa (default: 6).
 * @return the formatted scientific-notation string.
 */
fun Deci.toScientificNotation(precision: Int = 6): String {
    if (this.isZero()) return "0.0E+0"

    val str = this.abs().toString()
    val allDigits = str.filter { it.isDigit() }
    val firstNonZeroIdx = allDigits.indexOfFirst { it != '0' }
    if (firstNonZeroIdx == -1) return "0.0E+0"

    val significantDigits = allDigits.substring(firstNonZeroIdx)
    val decimalIndex = str.indexOf('.')
    val firstNonZeroInStr = str.indexOfFirst { it.isDigit() && it != '0' }

    val exponent =
        if (decimalIndex == -1) {
            str.length - firstNonZeroInStr - 1
        } else if (firstNonZeroInStr < decimalIndex) {
            decimalIndex - firstNonZeroInStr - 1
        } else {
            decimalIndex - firstNonZeroInStr
        }

    val mantissa =
        if (precision > 0 && significantDigits.length > 1) {
            significantDigits[0] + "." + significantDigits.substring(1).take(precision).padEnd(precision, '0')
        } else {
            significantDigits[0].toString()
        }

    val sign = if (this.isNegative()) "-" else ""
    val expSign = if (exponent >= 0) "+" else ""

    return "$sign${mantissa}E$expSign$exponent"
}

/**
 * Formats this [Deci] according to the given number [pattern].
 *
 * The value is rounded using [RoundingMode.HALF_UP] to the number of decimal places
 * implied by the pattern. Supported patterns:
 *
 * | Pattern | Description | Example (`Deci("1234.567")`) |
 * |---|---|---|
 * | `"0.00"` | Two decimal places, no grouping | `"1234.57"` |
 * | `"#,##0.00"` | Two decimal places with thousands separator | `"1,234.57"` |
 * | `"0.0000"` | Four decimal places, no grouping | `"1234.5670"` |
 * | `"#,##0"` | Integer with thousands separator | `"1,235"` |
 *
 * @param pattern the format pattern string.
 * @return the formatted string.
 * @throws [org.kimplify.deci.exception.DeciFormatException] if [pattern] is not a recognized format.
 */
fun Deci.format(pattern: String): String {
    return when (pattern) {
        "0.00" -> this.setScale(2, RoundingMode.HALF_UP).toString()
        "#,##0.00" -> this.setScale(2, RoundingMode.HALF_UP).formatWithThousandsSeparator()
        "0.0000" -> this.setScale(4, RoundingMode.HALF_UP).toString()
        "#,##0" -> this.setScale(0, RoundingMode.HALF_UP).formatWithThousandsSeparator()
        else -> throw DeciFormatException(pattern = pattern)
    }
}

/**
 * Converts this Deci to words (English).
 * Limited implementation for common cases.
 *
 * @return Number as words
 */
fun Deci.toWords(): String {
    if (this.isZero()) return "zero"

    val isNegative = this.isNegative()
    val abs = this.abs()
    val parts = abs.toString().split(".")
    val integerPart = parts[0].toLongOrNull() ?: return "number too large"

    val result =
        convertIntegerToWords(integerPart)

    return if (isNegative) "negative $result" else result
}

private fun convertIntegerToWords(number: Long): String {
    if (number == 0L) return "zero"

    val ones = arrayOf("", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
    val teens =
        arrayOf("ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen")
    val tens = arrayOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")

    return when {
        number < 10 -> ones[number.toInt()]
        number < 20 -> teens[(number - 10).toInt()]
        number < 100 -> {
            val ten = (number / 10).toInt()
            val one = (number % 10).toInt()
            tens[ten] + if (one > 0) " ${ones[one]}" else ""
        }
        number < 1000 -> {
            val hundred = (number / 100).toInt()
            val remainder = number % 100
            ones[hundred] + " hundred" +
                if (remainder > 0) {
                    " ${
                        convertIntegerToWords(
                            remainder,
                        )
                    }"
                } else {
                    ""
                }
        }
        else -> "number too large"
    }
}

/**
 * Pads this Deci string representation to the specified width.
 *
 * @param width Total width of the result string
 * @param padChar Character to use for padding (default: space)
 * @param padLeft True to pad on the left, false for right (default: true)
 * @return Padded string
 */
fun Deci.pad(
    width: Int,
    padChar: Char = ' ',
    padLeft: Boolean = true,
): String {
    val str = this.toString()
    return if (padLeft) {
        str.padStart(width, padChar)
    } else {
        str.padEnd(width, padChar)
    }
}
