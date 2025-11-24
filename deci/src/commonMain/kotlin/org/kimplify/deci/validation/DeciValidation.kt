package org.kimplify.deci.validation

import org.kimplify.deci.Deci
import org.kimplify.deci.extension.toLong
import org.kimplify.deci.parser.DECIMAL_REGEX

/**
 * Validation and safety utilities for Deci.
 */

/**
 * Checks if this string represents a valid Deci value.
 * 
 * @return True if the string can be parsed as a Deci, false otherwise
 */
fun String.isValidDeci(): Boolean {
    if (this.isBlank()) return false
    val sanitized = this.trim().replace(',', '.')
    return DECIMAL_REGEX.matches(sanitized)
}

/**
 * Converts this string to a Deci with error handling.
 * 
 * @return Result containing the Deci value or an exception
 */
fun String.toDeciOrError(): Result<Deci> {
    return runCatching { Deci(this) }
}

/**
 * Checks if this Deci is within the specified range (inclusive).
 * 
 * @param min Minimum value (inclusive)
 * @param max Maximum value (inclusive)
 * @return True if this value is within the range
 */
fun Deci.isInRange(min: Deci, max: Deci): Boolean {
    require(min <= max) { "Min value ($min) must be less than or equal to max value ($max)" }
    return this >= min && this <= max
}

/**
 * Clamps this Deci value to the specified range.
 * 
 * @param min Minimum value
 * @param max Maximum value
 * @return This value clamped to the range [min, max]
 */
fun Deci.clamp(min: Deci, max: Deci): Deci {
    require(min <= max) { "Min value ($min) must be less than or equal to max value ($max)" }
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

/**
 * Checks if this Deci represents a whole number (no fractional part).
 * 
 * @return True if this is a whole number
 */
fun Deci.isWhole(): Boolean {
    val str = this.toString()
    return !str.contains('.') || str.endsWith(".0") || str.substringAfter('.').all { it == '0' }
}

/**
 * Checks if this Deci represents an even number (for whole numbers only).
 * 
 * @return True if this is an even whole number
 * @throws IllegalArgumentException if this is not a whole number
 */
fun Deci.isEven(): Boolean {
    require(isWhole()) { "Value must be a whole number: $this" }
    return toLong() % 2 == 0L
}

/**
 * Checks if this Deci represents an odd number (for whole numbers only).
 * 
 * @return True if this is an odd whole number
 * @throws IllegalArgumentException if this is not a whole number
 */
fun Deci.isOdd(): Boolean {
    require(isWhole()) { "Value must be a whole number: $this" }
    return toLong() % 2 != 0L
}

/**
 * Safely divides this Deci by another, returning a default value if division by zero.
 * 
 * @param divisor The divisor
 * @param default Default value to return if divisor is zero (default: Deci.ZERO)
 * @return The division result or default value
 */
fun Deci.safeDivide(divisor: Deci, default: Deci = Deci.ZERO): Deci {
    return if (divisor.isZero()) default else this / divisor
}

/**
 * Checks if this Deci has the specified number of decimal places or fewer.
 * 
 * @param maxDecimalPlaces Maximum number of decimal places allowed
 * @return True if decimal places are within limit
 */
fun Deci.hasValidDecimalPlaces(maxDecimalPlaces: Int): Boolean {
    require(maxDecimalPlaces >= 0) { "Max decimal places must be non-negative: $maxDecimalPlaces" }
    
    val str = this.toString()
    val decimalIndex = str.indexOf('.')
    
    return if (decimalIndex == -1) {
        true // No decimal part
    } else {
        val decimalPart = str.substring(decimalIndex + 1)
        decimalPart.length <= maxDecimalPlaces
    }
}

/**
 * Validates that this Deci meets financial precision requirements.
 * 
 * @param currency Currency code for validation rules (default: "USD")
 * @return True if the value meets currency precision requirements
 */
fun Deci.isValidCurrencyAmount(currency: String = "USD"): Boolean {
    return when (currency.uppercase()) {
        "USD", "EUR", "GBP", "CAD", "AUD" -> hasValidDecimalPlaces(2)
        "JPY", "KRW" -> isWhole() // These currencies typically don't use decimal places
        "BTC" -> hasValidDecimalPlaces(8) // Bitcoin precision
        else -> hasValidDecimalPlaces(2) // Default to 2 decimal places
    }
}

/**
 * Validates that this Deci is a valid percentage (0-100).
 * 
 * @param allowNegative Whether negative percentages are allowed (default: false)
 * @param allowOver100 Whether percentages over 100 are allowed (default: false)
 * @return True if this is a valid percentage
 */
fun Deci.isValidPercentage(allowNegative: Boolean = false, allowOver100: Boolean = false): Boolean {
    val min = if (allowNegative) Deci("-100") else Deci.ZERO
    val max = if (allowOver100) Deci("1000") else Deci("100") // Reasonable upper limit
    return isInRange(min, max)
}

/**
 * Validates that this Deci is a positive number.
 * 
 * @return True if this value is positive (> 0)
 */
fun Deci.isPositiveStrict(): Boolean = this > Deci.ZERO

/**
 * Validates that this Deci is non-negative (>= 0).
 * 
 * @return True if this value is non-negative
 */
fun Deci.isNonNegative(): Boolean = this >= Deci.ZERO

/**
 * Validates that this Deci represents a valid tax rate.
 * 
 * @return True if this is a valid tax rate (0-1 as decimal)
 */
fun Deci.isValidTaxRate(): Boolean = isInRange(Deci.ZERO, Deci.ONE)

/**
 * Validates that this Deci represents a valid interest rate.
 * 
 * @param maxRate Maximum allowed rate (default: 1.0 for 100%)
 * @return True if this is a valid interest rate
 */
fun Deci.isValidInterestRate(maxRate: Deci = Deci.ONE): Boolean {
    return isInRange(Deci.ZERO, maxRate)
}

/**
 * Checks if this Deci is "approximately equal" to another within a tolerance.
 * 
 * @param other The other value to compare with
 * @param tolerance The tolerance for comparison (default: 0.000001)
 * @return True if values are approximately equal
 */
fun Deci.isApproximatelyEqual(other: Deci, tolerance: Deci = Deci("0.000001")): Boolean {
    return (this - other).abs() <= tolerance
}

/**
 * Validates input for form fields with specific requirements.
 * 
 * @param minValue Minimum allowed value (optional)
 * @param maxValue Maximum allowed value (optional)
 * @param maxDecimalPlaces Maximum decimal places allowed (optional)
 * @param mustBePositive Whether value must be positive (default: false)
 * @return Validation result with error message if invalid
 */
data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)

fun Deci.validateForForm(
    minValue: Deci? = null,
    maxValue: Deci? = null,
    maxDecimalPlaces: Int? = null,
    mustBePositive: Boolean = false
): ValidationResult {
    
    if (mustBePositive && !isPositiveStrict()) {
        return ValidationResult(
            false,
            "Value must be positive"
        )
    }
    
    minValue?.let { min ->
        if (this < min) {
            return ValidationResult(
                false,
                "Value must be at least $min"
            )
        }
    }
    
    maxValue?.let { max ->
        if (this > max) {
            return ValidationResult(
                false,
                "Value must be at most $max"
            )
        }
    }
    
    maxDecimalPlaces?.let { places ->
        if (!hasValidDecimalPlaces(places)) {
            return ValidationResult(
                false,
                "Value can have at most $places decimal places"
            )
        }
    }
    
    return ValidationResult(true)
}