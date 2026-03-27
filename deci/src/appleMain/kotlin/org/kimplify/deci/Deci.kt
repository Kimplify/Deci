package org.kimplify.deci

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import org.kimplify.deci.config.DeciConfiguration
import org.kimplify.deci.exception.DeciDivisionByZeroException
import org.kimplify.deci.exception.DeciScaleException
import org.kimplify.deci.parser.extractScale
import org.kimplify.deci.parser.validateAndNormalizeDecimalLiteral
import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSDecimalNumberHandler
import platform.Foundation.NSRoundingMode

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
@Serializable(with = DeciSerializer::class)
actual class Deci private constructor(
    private val internal: NSDecimalNumber,
    private val _scale: Int? = null,
) : Comparable<Deci> {
    actual constructor(value: String) : this(
        NSDecimalNumber(validateAndNormalizeDecimalLiteral(value)),
        extractScale(validateAndNormalizeDecimalLiteral(value)),
    )

    actual constructor(value: Long) : this(value.toString())
    actual constructor(value: Int) : this(value.toString())
    actual constructor(value: Double) : this(value.toString())

    private fun roundWithHandler(
        value: NSDecimalNumber,
        scale: Int,
        nativeMode: NSRoundingMode,
    ): NSDecimalNumber {
        val handler =
            NSDecimalNumberHandler(
                nativeMode,
                scale.toShort(),
                raiseOnExactness = false,
                raiseOnOverflow = false,
                raiseOnUnderflow = false,
                raiseOnDivideByZero = false,
            )
        return value.decimalNumberByRoundingAccordingToBehavior(handler)
    }

    private fun roundDecimal(
        value: NSDecimalNumber,
        scale: Int,
        roundingMode: RoundingMode,
    ): NSDecimalNumber {
        if (roundingMode == RoundingMode.HALF_DOWN) {
            return roundHalfDown(value, scale)
        }
        return roundWithHandler(value, scale, toNativeMode(roundingMode))
    }

    private fun roundHalfDown(
        value: NSDecimalNumber,
        scale: Int,
    ): NSDecimalNumber {
        val halfUpResult = roundWithHandler(value, scale, NSRoundingMode.NSRoundPlain)
        val isNeg = value.compare(NSDecimalNumber.zero) < 0L
        val downMode =
            if (isNeg) NSRoundingMode.NSRoundUp else NSRoundingMode.NSRoundDown
        val downResult = roundWithHandler(value, scale, downMode)

        if (halfUpResult.compare(downResult) == 0L) return halfUpResult

        val diff = value.decimalNumberBySubtracting(downResult)
        val absDiff =
            if (diff.compare(NSDecimalNumber.zero) < 0L) {
                diff.decimalNumberByMultiplyingBy(NSDecimalNumber(-1))
            } else {
                diff
            }
        val midpoint =
            NSDecimalNumber(string = "5")
                .decimalNumberByMultiplyingByPowerOf10((-scale - 1).toShort())

        return if (absDiff.compare(midpoint) == 0L) downResult else halfUpResult
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
        return Deci(raw.decimalNumberByRoundingAccordingToBehavior(handler), policy.fractionalDigits)
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
        val rounded = roundDecimal(internal, scale, roundingMode)
        return Deci(rounded, scale)
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
        if (roundingMode == RoundingMode.HALF_DOWN) {
            // Compute raw quotient, then delegate to setScale which handles HALF_DOWN
            val raw = Deci(internal.decimalNumberByDividingBy(divisor.internal).stringValue)
            return raw.setScale(scale, RoundingMode.HALF_DOWN)
        }
        val raw = internal.decimalNumberByDividingBy(divisor.internal)
        val rounded = roundDecimal(raw, scale, roundingMode)
        return Deci(rounded, scale)
    }

    actual override fun toString(): String {
        val str = internal.stringValue
        val scale = _scale ?: return str
        if (scale <= 0) return str
        val dotIndex = str.indexOf('.')
        if (dotIndex < 0) {
            return "$str.${"0".repeat(scale)}"
        }
        val currentScale = str.length - dotIndex - 1
        return if (currentScale >= scale) str else str + "0".repeat(scale - currentScale)
    }

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

    override fun hashCode(): Int {
        val s = internal.stringValue
        val normalized = if ('.' in s) s.trimEnd('0').trimEnd('.') else s
        return normalized.hashCode()
    }

    private fun toNativeMode(mode: RoundingMode): NSRoundingMode =
        when (mode) {
            RoundingMode.UP ->
                if (!isNegative()) {
                    NSRoundingMode.NSRoundUp
                } else {
                    NSRoundingMode.NSRoundDown
                }

            RoundingMode.DOWN ->
                if (!isNegative()) {
                    NSRoundingMode.NSRoundDown
                } else {
                    NSRoundingMode.NSRoundUp
                }

            RoundingMode.CEILING -> NSRoundingMode.NSRoundUp
            RoundingMode.FLOOR -> NSRoundingMode.NSRoundDown

            RoundingMode.HALF_UP -> NSRoundingMode.NSRoundPlain
            RoundingMode.HALF_DOWN -> NSRoundingMode.NSRoundPlain
            RoundingMode.HALF_EVEN -> NSRoundingMode.NSRoundBankers
        }
}
