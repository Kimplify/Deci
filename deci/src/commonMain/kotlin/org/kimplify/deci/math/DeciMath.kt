package org.kimplify.deci.math

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciConstants
import org.kimplify.deci.DeciContext
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.exception.DeciArithmeticException
import org.kimplify.deci.exception.DeciDivisionByZeroException
import org.kimplify.deci.extension.toLong

/**
 * Basic mathematical operations for Deci.
 */

/**
 * Calculates the square root of this [Deci] using Newton's method.
 *
 * Intermediate divisions use an internal [DeciContext] with extra precision
 * ([precision] + 5 digits, [RoundingMode.HALF_UP]) for convergence accuracy.
 * The final result is trimmed to the requested [precision].
 *
 * @param precision Number of decimal places for precision (default: 10).
 * @return Square root of this value.
 * @throws DeciArithmeticException if this value is negative.
 */
fun Deci.sqrt(precision: Int = 10): Deci {
    if (this.isNegative()) throw DeciArithmeticException("Cannot calculate square root of negative number: $this")

    if (this.isZero()) return Deci.ZERO
    if (this == Deci.ONE) return Deci.ONE

    val internalContext = DeciContext(precision + 5, RoundingMode.HALF_UP)

    var x = this.divide(DeciConstants.TWO, internalContext)
    var prevX: Deci

    repeat(50) {
        prevX = x
        x = (x + this.divide(x, internalContext)).divide(DeciConstants.TWO, internalContext)

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
 * For negative exponents, division is performed using the supplied [context]
 * for scale and rounding. By default, [DeciContext.DEFAULT] (20 fractional
 * digits, [RoundingMode.HALF_UP]) is used.
 *
 * @param exponent The exponent.
 * @param context The [DeciContext] controlling precision and rounding when
 *                the exponent is negative (requires division by the result).
 * @return This value raised to the exponent.
 */
fun Deci.pow(
    exponent: Deci,
    context: DeciContext = DeciContext.DEFAULT,
): Deci {
    return when {
        exponent.isZero() -> Deci.ONE
        exponent == Deci.ONE -> this
        else -> {
            val exp = exponent.toLong()
            require(Deci(exp) == exponent) { "Exponent must be an integer: $exponent" }
            require(exp >= Int.MIN_VALUE && exp <= Int.MAX_VALUE) {
                "Exponent $exp is outside supported range"
            }

            if (exp < 0) {
                require(exp != Long.MIN_VALUE) { "Exponent cannot be Long.MIN_VALUE" }
                Deci.ONE.divide(powPositive((-exp).toInt()), context)
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
    if (divisor.isZero()) throw DeciDivisionByZeroException("Cannot compute modulo: divisor is zero")

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
    if (divisor.isZero()) throw DeciDivisionByZeroException("Cannot compute remainder: divisor is zero")

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
    if (multiple.isZero()) throw DeciDivisionByZeroException("Cannot round to nearest zero")

    val quotient = (this / multiple).setScale(0, RoundingMode.HALF_UP)
    return quotient * multiple
}

/**
 * Rounds this value to the given number of significant digits.
 *
 * Rounding is performed with [RoundingMode.HALF_UP]. For values whose magnitude
 * requires scaling beyond the decimal point, intermediate division and multiplication
 * by powers of ten are used with an internal [DeciContext] (20 fractional digits,
 * [RoundingMode.HALF_UP]).
 *
 * @param digits the number of significant digits to retain. Must be positive.
 * @return a new [Deci] rounded to the specified number of significant digits.
 * @throws IllegalArgumentException if [digits] is not positive.
 */
fun Deci.roundToSignificantDigits(digits: Int): Deci {
    require(digits > 0) { "Number of significant digits must be positive: $digits" }

    if (this.isZero()) return Deci.ZERO

    val absValue = this.abs()
    val str = absValue.toString()

    val firstSigDigitIndex = str.indexOfFirst { it.isDigit() && it != '0' }
    if (firstSigDigitIndex == -1) return Deci.ZERO

    val decimalIndex = str.indexOf('.')

    val magnitude =
        if (decimalIndex == -1) {
            str.length - firstSigDigitIndex - 1
        } else if (firstSigDigitIndex < decimalIndex) {
            decimalIndex - firstSigDigitIndex - 1
        } else {
            -(firstSigDigitIndex - decimalIndex)
        }

    val targetScale = -(magnitude - digits + 1)

    return if (targetScale >= 0) {
        this.setScale(targetScale, RoundingMode.HALF_UP)
    } else {
        val factor = Deci.TEN.pow(Deci(-targetScale))
        val divided = this.divide(factor, DeciContext(20, RoundingMode.HALF_UP))
        val rounded = divided.setScale(0, RoundingMode.HALF_UP)
        rounded * factor
    }
}
