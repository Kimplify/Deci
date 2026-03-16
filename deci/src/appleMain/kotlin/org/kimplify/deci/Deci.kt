package org.kimplify.deci

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import org.kimplify.deci.config.DeciConfiguration
import org.kimplify.deci.exception.DeciDivisionByZeroException
import org.kimplify.deci.exception.DeciScaleException
import org.kimplify.deci.parser.validateAndNormalizeDecimalLiteral
import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSDecimalNumberHandler
import platform.Foundation.NSRoundingMode

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
@Serializable(with = DeciSerializer::class)
actual class Deci private constructor(
    private val internal: NSDecimalNumber,
) : Comparable<Deci> {
    actual constructor(value: String) : this(
        NSDecimalNumber(validateAndNormalizeDecimalLiteral(value)),
    )

    actual constructor(value: Long) : this(value.toString())
    actual constructor(value: Int) : this(value.toString())
    actual constructor(value: Double) : this(value.toString())

    private fun NSDecimalNumber.roundedTo(
        scale: Int,
        roundingMode: RoundingMode,
    ): NSDecimalNumber {
        val native = toNativeMode(roundingMode)
        val handler =
            NSDecimalNumberHandler(
                native,
                scale.toShort(),
                raiseOnExactness = false,
                raiseOnOverflow = false,
                raiseOnUnderflow = false,
                raiseOnDivideByZero = false,
            )
        return this.decimalNumberByRoundingAccordingToBehavior(handler)
    }

    actual companion object {
        actual val ZERO = Deci("0")
        actual val ONE = Deci("1")
        actual val TEN = Deci("10")

        actual fun fromStringOrNull(value: String): Deci? =
            runCatching { Deci(value) }
                .getOrNull()

        actual fun fromStringOrZero(value: String): Deci = fromStringOrNull(value) ?: ZERO
    }

    private fun operate(
        other: Deci,
        behavior: NSDecimalNumberHandler? = null,
        op: (NSDecimalNumber, NSDecimalNumber, NSDecimalNumberHandler?) -> NSDecimalNumber,
    ): Deci =
        Deci(
            op(internal, other.internal, behavior).stringValue,
        )

    actual operator fun plus(other: Deci): Deci = operate(other) { a, b, _ -> a.decimalNumberByAdding(b) }

    actual operator fun minus(other: Deci): Deci = operate(other) { a, b, _ -> a.decimalNumberBySubtracting(b) }

    actual operator fun times(other: Deci): Deci = operate(other) { a, b, _ -> a.decimalNumberByMultiplyingBy(b) }

    actual operator fun div(other: Deci): Deci {
        if (other.isZero()) throw DeciDivisionByZeroException()
        val raw = internal.decimalNumberByDividingBy(other.internal)
        val policy = DeciConfiguration.divisionPolicy
        val handler =
            NSDecimalNumberHandler(
                toNativeMode(policy.roundingMode),
                scale = policy.fractionalDigits.toShort(),
                raiseOnExactness = false,
                raiseOnOverflow = false,
                raiseOnUnderflow = false,
                raiseOnDivideByZero = false,
            )
        return Deci(raw.decimalNumberByRoundingAccordingToBehavior(handler).stringValue)
    }

    actual fun divide(
        other: Deci,
        context: DeciContext,
    ): Deci = divide(other, context.precision, context.roundingMode)

    actual fun setScale(
        scale: Int,
        roundingMode: RoundingMode,
    ): Deci {
        if (scale < 0) throw DeciScaleException(scale)
        val rounded = internal.roundedTo(scale, roundingMode)
        return Deci(rounded)
    }

    actual operator fun rem(other: Deci): Deci {
        if (other.isZero()) throw DeciDivisionByZeroException()
        val quotient = (this / other).setScale(0, RoundingMode.DOWN)
        return this - (quotient * other)
    }

    actual fun divide(
        divisor: Deci,
        scale: Int,
        roundingMode: RoundingMode,
    ): Deci {
        if (divisor.isZero()) throw DeciDivisionByZeroException()
        if (scale < 0) throw DeciScaleException(scale)
        val raw = internal.decimalNumberByDividingBy(divisor.internal)
        val rounded = raw.roundedTo(scale, roundingMode)
        return Deci(rounded.stringValue)
    }

    actual override fun toString(): String = internal.stringValue

    actual fun toDouble(): Double = internal.doubleValue

    actual fun isZero(): Boolean = internal.compare(NSDecimalNumber.zero) == 0L

    actual fun isNegative(): Boolean = internal.compare(NSDecimalNumber.zero) < 0L

    actual fun isPositive(): Boolean = internal.compare(NSDecimalNumber.zero) > 0L

    actual fun abs(): Deci = if (isNegative()) negate() else this

    actual fun negate(): Deci = Deci(internal.decimalNumberByMultiplyingBy(NSDecimalNumber(-1)).stringValue)

    actual operator fun unaryMinus(): Deci = negate()

    actual fun max(other: Deci): Deci = if (this >= other) this else other

    actual fun min(other: Deci): Deci = if (this <= other) this else other

    actual override fun compareTo(other: Deci): Int = internal.compare(other.internal).toInt()

    override fun equals(other: Any?): Boolean = this === other || (other is Deci && compareTo(other) == 0)

    override fun hashCode(): Int =
        internal
            .decimalNumberByRoundingAccordingToBehavior(NSDecimalNumberHandler.defaultDecimalNumberHandler())
            .stringValue
            .hashCode()

    private fun toNativeMode(mode: RoundingMode): NSRoundingMode =
        when (mode) {
            RoundingMode.UP ->
                if (isPositive()) {
                    NSRoundingMode.NSRoundUp
                } else {
                    NSRoundingMode.NSRoundDown
                }

            RoundingMode.DOWN ->
                if (isPositive()) {
                    NSRoundingMode.NSRoundDown
                } else {
                    NSRoundingMode.NSRoundUp
                }

            RoundingMode.CEILING -> NSRoundingMode.NSRoundUp
            RoundingMode.FLOOR -> NSRoundingMode.NSRoundDown

            RoundingMode.HALF_UP -> NSRoundingMode.NSRoundPlain
            RoundingMode.HALF_DOWN -> NSRoundingMode.NSRoundDown
            RoundingMode.HALF_EVEN -> NSRoundingMode.NSRoundBankers
        }
}
