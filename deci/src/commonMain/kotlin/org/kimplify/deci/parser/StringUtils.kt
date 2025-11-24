package org.kimplify.deci.parser

/**
 * Normalizes a decimal-like string into a standard form:
 *
 * - Uses '.' as the decimal separator
 * - Removes thousand/grouping separators ('.' or ',') from the integer part
 * - Respects leading '+' or '-' sign
 * - Interprets the *last* '.' or ',' as the decimal separator, when present
 *
 * Examples:
 *  "1.234,56"  -> "1234.56"
 *  "1,234.56"  -> "1234.56"
 *  "-2,5"      -> "-2.5"
 *  ".5"        -> "0.5"
 *  "   -"      -> "-0"
 */
internal fun String.normalizeDecimalString(): String {
    if (isEmpty()) return this

    val trimmed = trim()
    val isNegative = trimmed.startsWith('-')
    val hasSign = isNegative || trimmed.startsWith('+')

    val unsigned = if (hasSign) trimmed.drop(1) else trimmed

    if (unsigned.isEmpty()) return if (isNegative) "-0" else "0"

    val lastCommaIndex = unsigned.lastIndexOf(',')
    val lastDotIndex = unsigned.lastIndexOf('.')

    val hasComma = lastCommaIndex >= 0
    val hasDot = lastDotIndex >= 0

    val normalized = when {
        !hasComma && !hasDot ->
            normalizeInteger(unsigned)

        hasComma && !hasDot ->
            normalizeWithDecimal(unsigned, lastCommaIndex)

        !hasComma && hasDot ->
            normalizeWithDecimal(unsigned, lastDotIndex)

        else -> {
            val lastSeparatorIndex = maxOf(lastCommaIndex, lastDotIndex)
            normalizeWithDecimal(unsigned, lastSeparatorIndex)
        }
    }

    return if (isNegative) "-$normalized" else normalized
}

/**
 * Normalizes a string that should be treated as an integer:
 * removes any '.' or ',' characters.
 */
private fun normalizeInteger(raw: String): String =
    raw.filterNot { it == '.' || it == ',' }

/**
 * Normalizes a string that has a decimal separator at [decimalIndex].
 *
 * - Everything before the separator is treated as integer part,
 *   and all '.' / ',' are removed from it (grouping separators).
 * - Everything after the separator is the decimal part (kept as-is).
 * - Ensures there is at least "0" before the decimal point.
 */
private fun normalizeWithDecimal(raw: String, decimalIndex: Int): String {
    val integerPartRaw = raw.substring(0, decimalIndex)
    val decimalPart = raw.substring(decimalIndex + 1)

    val integerNormalized = integerPartRaw.filterNot { it == '.' || it == ',' }
    val safeInteger = if (integerNormalized.isEmpty()) "0" else integerNormalized

    return "$safeInteger.$decimalPart"
}
