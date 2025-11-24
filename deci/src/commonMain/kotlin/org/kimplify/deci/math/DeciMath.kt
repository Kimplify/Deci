package org.kimplify.deci.math

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciConstants
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.extension.toLong


/**
 * Basic mathematical operations for Deci.
 */

/**
 * Calculates the square root of this [Deci] using Newton's method.
 * 
 * @param precision Number of decimal places for precision (default: 10)
 * @return Square root of this value
 * @throws IllegalArgumentException if this value is negative
 */
fun Deci.sqrt(precision: Int = 10): Deci {
    require(!this.isNegative()) { "Cannot calculate square root of negative number: $this" }
    
    if (this.isZero()) return Deci.ZERO
    if (this == Deci.ONE) return Deci.ONE
    
    var x = this / DeciConstants.TWO
    var prevX: Deci
    
    repeat(50) { // Max iterations to prevent infinite loops
        prevX = x
        x = (x + this / x) / DeciConstants.TWO
        
        // Check for convergence
        val diff = (x - prevX).abs()
        if (diff.setScale(precision + 2, RoundingMode.HALF_UP).isZero()) {
            return@repeat
        }
    }
    
    return x.setScale(precision, RoundingMode.HALF_UP)
}

/**
 * Calculates this value raised to the given [Deci] power.
 * Limited implementation for positive bases and integer-like exponents.
 * 
 * @param exponent The exponent
 * @return This value raised to the exponent
 */
fun Deci.pow(exponent: Deci): Deci {
    return when {
        exponent.isZero() -> Deci.ONE
        exponent == Deci.ONE -> this
        else -> {
            val exp = exponent.toLong()
            require(Deci(exp) == exponent) { "Exponent must be an integer: $exponent" }

            if (exp < 0) {
                Deci.ONE / powPositive(-exp.toInt())
            } else {
                powPositive(exp.toInt())
            }
        }
    }
}

private fun Deci.powPositive(exponent: Int): Deci {
    var result = Deci.ONE
    var base = this
    var exp = exponent

    // Fast exponentiation to avoid recursion
    while (exp > 0) {
        if (exp and 1 == 1) {
            result *= base
        }
        base *= base
        exp = exp ushr 1
    }

    return result
}

/**
 * Calculates the modulo operation.
 * 
 * @param divisor The divisor
 * @return The remainder after division
 */
fun Deci.mod(divisor: Deci): Deci {
    require(!divisor.isZero()) { "Division by zero in modulo operation" }
    
    val quotient = (this / divisor).setScale(0, RoundingMode.DOWN)
    return this - (quotient * divisor)
}

/**
 * Calculates the remainder after division (different from mod for negative numbers).
 * 
 * @param divisor The divisor
 * @return The remainder
 */
fun Deci.remainder(divisor: Deci): Deci {
    require(!divisor.isZero()) { "Division by zero in remainder operation" }
    
    val quotient = (this / divisor).setScale(0, RoundingMode.HALF_UP)
    return this - (quotient * divisor)
}

/**
 * Rounds this value to the nearest multiple of the given value.
 * 
 * @param multiple The multiple to round to
 * @return Value rounded to nearest multiple
 */
fun Deci.roundToNearest(multiple: Deci): Deci {
    require(!multiple.isZero()) { "Cannot round to nearest zero" }
    
    val quotient = (this / multiple).setScale(0, RoundingMode.HALF_UP)
    return quotient * multiple
}

/**
 * Rounds this value to the specified number of significant digits.
 * 
 * @param digits Number of significant digits
 * @return Value rounded to significant digits
 */
fun Deci.roundToSignificantDigits(digits: Int): Deci {
    require(digits > 0) { "Number of significant digits must be positive: $digits" }
    
    if (this.isZero()) return Deci.ZERO
    
    val str = this.abs().toString()
    val decimalIndex = str.indexOf('.')
    val firstDigitIndex = str.indexOfFirst { it != '0' && it != '.' }
    
    return if (decimalIndex == -1 || firstDigitIndex < decimalIndex) {
        // Integer or first significant digit is before decimal point
        val scale = maxOf(0, digits - (decimalIndex - firstDigitIndex))
        this.setScale(scale, RoundingMode.HALF_UP)
    } else {
        // First significant digit is after decimal point
        val scale = digits + (firstDigitIndex - decimalIndex - 1)
        this.setScale(scale, RoundingMode.HALF_UP)
    }
}
