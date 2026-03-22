package org.kimplify.deci.extension

import org.kimplify.deci.Deci
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.exception.DeciOverflowException

/**
 * Returns the sum of all [Deci] values in the iterable.
 *
 * No rounding is applied; the result retains full precision. Returns [Deci.ZERO] for empty iterables.
 */
fun Iterable<Deci>.sumDeci(): Deci =
    this.fold(Deci.ZERO) { accumulated, value ->
        accumulated + value
    }

/**
 * Converts this [Deci] to a [Long] by discarding the fractional part.
 *
 * This is equivalent to [toLongExact] — the fractional part is truncated using
 * [RoundingMode.DOWN] and the integer value must fit in a [Long].
 *
 * @throws DeciOverflowException if the integer part is outside the [Long] range.
 */
fun Deci.toLong(): Long = toLongExact()

/**
 * Converts this [Deci] to a [Long], or returns `null` if the integer part is outside the [Long] range.
 *
 * The fractional part is truncated using [RoundingMode.DOWN] (i.e. toward zero) before conversion.
 */
fun Deci.toLongOrNull(): Long? {
    val truncated = this.setScale(0, RoundingMode.DOWN)
    val str = truncated.toPlainString()
    return str.toLongOrNull()
}

/**
 * Converts this [Deci] to a [Long], truncating the fractional part toward zero.
 *
 * The fractional part is discarded using [RoundingMode.DOWN] before conversion.
 *
 * @return the integer part of this value as a [Long].
 * @throws DeciOverflowException if the integer part is outside the [Long] range.
 */
fun Deci.toLongExact(): Long =
    toLongOrNull()
        ?: throw DeciOverflowException(value = this.toString())

/**
 * Returns the number of digits to the right of the decimal separator in the canonical string form.
 *
 * @return the scale (number of fractional digits), or `0` if there is no decimal separator.
 */
fun Deci.scale(): Int {
    val text = toPlainString()
    val separatorIndex = text.indexOf('.')
    if (separatorIndex < 0) return 0
    return text.length - separatorIndex - 1
}

/**
 * Returns the count of significant digits (excluding the sign and decimal separator).
 *
 * @return the total number of digit characters in the canonical string representation.
 */
fun Deci.precision(): Int {
    val text = toPlainString()
    return text.count { it.isDigit() }
}

/** Converts this [Int] to a [Deci]. */
fun Int.toDeci(): Deci = Deci(this)

/** Converts this [Long] to a [Deci]. */
fun Long.toDeci(): Deci = Deci(this)

/**
 * Parses this [String] as a [Deci].
 *
 * @throws [org.kimplify.deci.exception.DeciParseException] if this string is not a valid decimal literal.
 */
fun String.toDeci(): Deci = Deci(this)

/**
 * Parses this [String] as a [Deci], returning `null` if the string is not a valid decimal literal.
 */
fun String.toDeciOrNull(): Deci? = Deci.fromStringOrNull(this)

/**
 * Converts this [Double] to a [Deci].
 *
 * **Warning:** This conversion first converts the [Double] to its [String] representation
 * via [Double.toString], which may introduce floating-point artifacts.
 * For example, `0.1.toDeci()` may produce `"0.1"` but `0.1 + 0.2` yields
 * `0.30000000000000004` as a Double. Prefer constructing [Deci] from a [String] literal
 * whenever exact representation is required.
 */
fun Double.toDeci(): Deci = Deci(this)

/** Returns this Deci if non-null, or [Deci.ZERO] otherwise. */
fun Deci?.orZero(): Deci = this ?: Deci.ZERO

/** Returns this Deci if non-null, or [Deci.ONE] otherwise. */
fun Deci?.orOne(): Deci = this ?: Deci.ONE

/** Returns this Deci if non-null, or [default] otherwise. */
fun Deci?.orDefault(default: Deci): Deci = this ?: default

/**
 * Converts this [Deci] to an [Int] by truncating the fractional part.
 *
 * @throws [org.kimplify.deci.exception.DeciOverflowException] if the integer part is outside the [Int] range.
 */
fun Deci.toInt(): Int {
    val long = toLong()
    if (long < Int.MIN_VALUE || long > Int.MAX_VALUE) {
        throw DeciOverflowException(value = this.toString())
    }
    return long.toInt()
}

/**
 * Converts this [Deci] to an [Int], or returns `null` if the integer part is outside the [Int] range.
 */
fun Deci.toIntOrNull(): Int? {
    val long = toLongOrNull() ?: return null
    return if (long in Int.MIN_VALUE..Int.MAX_VALUE) long.toInt() else null
}

/**
 * Converts this [Deci] to a [Float].
 *
 * **Note:** Precision may be lost for values that cannot be exactly represented
 * as a 32-bit IEEE 754 floating-point number.
 */
fun Deci.toFloat(): Float = toDouble().toFloat()
