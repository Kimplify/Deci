package org.kimplify.deci

import kotlinx.serialization.Serializable

/**
 * Multiplatform arbitrary-precision decimal that normalizes user input and delegates
 * to the most capable numeric engine available on each platform.
 */
@Serializable(with = DeciSerializer::class)
expect class Deci : Comparable<Deci> {
    /**
     * Creates a [Deci] from the given decimal [String].
     *
     * The string is normalized (leading/trailing whitespace stripped, comma converted
     * to period) and validated against the decimal grammar before construction.
     *
     * @param value a string representation of a decimal number (e.g. `"123.45"`, `"-0.001"`).
     * @throws [org.kimplify.deci.exception.DeciParseException] if [value] is not a valid decimal literal.
     */
    constructor(value: String)

    /**
     * Creates a [Deci] from the given [Long] value.
     *
     * @param value the integer value.
     */
    constructor(value: Long)

    /**
     * Creates a [Deci] from the given [Int] value.
     *
     * @param value the integer value.
     */
    constructor(value: Int)

    /**
     * Creates a [Deci] from the given [Double] value.
     *
     * **Warning:** The [Double] is first converted to its [String] representation via
     * [Double.toString], which may introduce floating-point artifacts. For example,
     * `0.1 + 0.2` yields `0.30000000000000004` as a [Double]. Prefer the [String]
     * constructor whenever exact representation is required.
     *
     * @param value the double value.
     */
    constructor(value: Double)

    /**
     * Adds [other] to this value and returns the result.
     *
     * No rounding is applied; the result retains full precision.
     */
    operator fun plus(other: Deci): Deci

    /**
     * Subtracts [other] from this value and returns the result.
     *
     * No rounding is applied; the result retains full precision.
     */
    operator fun minus(other: Deci): Deci

    /**
     * Multiplies this value by [other] and returns the result.
     *
     * No rounding is applied; the result retains full precision.
     */
    operator fun times(other: Deci): Deci

    /**
     * Divides this value by [other] using the global [DeciConfiguration.divisionPolicy][org.kimplify.deci.config.DeciConfiguration.divisionPolicy].
     *
     * By default the result is rounded to 20 fractional digits with [RoundingMode.HALF_UP].
     * For explicit control over scale and rounding, use [divide] with a [DeciContext] or
     * explicit `scale`/`roundingMode` parameters instead.
     *
     * @throws [org.kimplify.deci.exception.DeciDivisionByZeroException] if [other] is zero.
     */
    operator fun div(other: Deci): Deci

    /**
     * Returns the remainder of dividing this value by [other].
     *
     * The remainder is computed as `this - (this / other).setScale(0, DOWN) * other`,
     * i.e., the quotient is truncated toward zero before multiplication.
     *
     * @throws [org.kimplify.deci.exception.DeciDivisionByZeroException] if [other] is zero.
     */
    operator fun rem(other: Deci): Deci

    /**
     * Divides this [Deci] by [divisor] with the specified [scale] and [roundingMode].
     *
     * The result is rounded to [scale] decimal places using the given [roundingMode].
     *
     * @param divisor the value to divide by.
     * @param scale the number of decimal places in the result.
     * @param roundingMode the rounding strategy to apply.
     * @return the quotient rounded to the specified scale.
     * @throws [org.kimplify.deci.exception.DeciDivisionByZeroException] if [divisor] is zero.
     */
    fun divide(
        divisor: Deci,
        scale: Int,
        roundingMode: RoundingMode,
    ): Deci

    /**
     * Divides this value by [other] using the supplied [context] for scale and rounding.
     *
     * @param other The divisor.
     * @param context The [DeciContext] providing precision and rounding mode.
     * @return The quotient, rounded according to [context].
     * @throws [org.kimplify.deci.exception.DeciDivisionByZeroException] if [other] is zero.
     */
    fun divide(
        other: Deci,
        context: DeciContext,
    ): Deci

    /**
     * Returns a [Deci] whose scale is the specified value, rounding if necessary.
     *
     * @param scale the number of decimal places in the result.
     * @param roundingMode the rounding strategy to apply when digits must be discarded.
     * @return a new [Deci] with the requested scale.
     */
    fun setScale(
        scale: Int,
        roundingMode: RoundingMode,
    ): Deci

    /**
     * Returns the canonical string representation of this [Deci], preserving
     * trailing zeros (e.g. `"1.50"` stays `"1.50"`).
     */
    override fun toString(): String

    /**
     * Converts this [Deci] to a [Double].
     *
     * **Note:** Precision may be lost for values that cannot be exactly represented
     * as a 64-bit IEEE 754 floating-point number.
     */
    fun toDouble(): Double

    /** Returns `true` if this value is numerically equal to zero. */
    fun isZero(): Boolean

    /** Returns `true` if this value is strictly less than zero. */
    fun isNegative(): Boolean

    /** Returns `true` if this value is strictly greater than zero. */
    fun isPositive(): Boolean

    /**
     * Returns the absolute value of this [Deci].
     *
     * @return this value if non-negative, otherwise its negation.
     */
    fun abs(): Deci

    /**
     * Returns the negation of this value (i.e. `this * -1`).
     *
     * @return the additive inverse of this [Deci].
     */
    fun negate(): Deci

    /** Returns the negation of this value. Equivalent to [negate]. */
    operator fun unaryMinus(): Deci

    /**
     * Returns the greater of this value and [other].
     *
     * @param other the value to compare with.
     * @return whichever of this and [other] is larger (or this if equal).
     */
    fun max(other: Deci): Deci

    /**
     * Returns the lesser of this value and [other].
     *
     * @param other the value to compare with.
     * @return whichever of this and [other] is smaller (or this if equal).
     */
    fun min(other: Deci): Deci

    /**
     * Compares this value with [other] for order.
     *
     * @return a negative integer, zero, or a positive integer as this value is
     *         less than, equal to, or greater than [other].
     */
    override fun compareTo(other: Deci): Int

    companion object {
        /** A [Deci] constant representing the value `0`. */
        val ZERO: Deci

        /** A [Deci] constant representing the value `1`. */
        val ONE: Deci

        /** A [Deci] constant representing the value `10`. */
        val TEN: Deci

        /**
         * Parses [value] as a [Deci], returning [ZERO] if the string is not a valid decimal literal.
         *
         * @param value the string to parse.
         * @return the parsed [Deci] or [ZERO].
         */
        fun fromStringOrZero(value: String): Deci

        /**
         * Parses [value] as a [Deci], returning `null` if the string is not a valid decimal literal.
         *
         * @param value the string to parse.
         * @return the parsed [Deci] or `null`.
         */
        fun fromStringOrNull(value: String): Deci?
    }
}
