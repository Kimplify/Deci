package org.kimplify.deci

import kotlinx.serialization.Serializable
import org.kimplify.deci.config.DeciConfiguration
import org.kimplify.deci.exception.DeciArithmeticException
import org.kimplify.deci.exception.DeciDivisionByZeroException
import org.kimplify.deci.exception.DeciScaleException
import org.kimplify.deci.parser.validateAndNormalizeDecimalLiteral
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode as JavaRoundingMode

@Serializable(with = DeciSerializer::class)
actual class Deci(
    private val internal: BigDecimal,
) : Comparable<Deci> {
    actual constructor(value: String) : this(
        BigDecimal(validateAndNormalizeDecimalLiteral(value)),
    )

    actual constructor(value: Long) : this(value.toString())
    actual constructor(value: Int) : this(value.toString())
    actual constructor(value: Double) : this(value.toString())

    actual companion object {
        actual val ZERO = Deci("0")
        actual val ONE = Deci("1")
        actual val TEN = Deci("10")

        actual fun fromStringOrNull(value: String): Deci? =
            runCatching { Deci(value) }
                .getOrNull()

        actual fun fromStringOrZero(value: String): Deci = fromStringOrNull(value) ?: ZERO
    }

    private inline fun operate(
        other: Deci,
        mc: MathContext? = null,
        op: (BigDecimal, BigDecimal, MathContext?) -> BigDecimal,
    ): Deci {
        val result = op(internal, other.internal, mc)
        return Deci(result.toPlainString())
    }

    actual operator fun plus(other: Deci): Deci = operate(other) { a, b, _ -> a.add(b) }

    actual operator fun minus(other: Deci): Deci = operate(other) { a, b, _ -> a.subtract(b) }

    actual operator fun times(other: Deci): Deci = operate(other) { a, b, _ -> a.multiply(b) }

    @Throws(DeciDivisionByZeroException::class, DeciArithmeticException::class)
    actual operator fun div(other: Deci): Deci {
        if (other.isZero()) throw DeciDivisionByZeroException()
        return try {
            operate(other) { a, b, _ ->
                val policy = DeciConfiguration.divisionPolicy
                a.divide(b, policy.fractionalDigits, convert(policy.roundingMode))
            }
        } catch (_: ArithmeticException) {
            throw DeciArithmeticException("Division produced a non-terminating decimal expansion")
        }
    }

    actual operator fun rem(other: Deci): Deci {
        if (other.isZero()) throw DeciDivisionByZeroException()
        val quotient = (this / other).setScale(0, RoundingMode.DOWN)
        return this - (quotient * other)
    }

    @Throws(DeciDivisionByZeroException::class, DeciScaleException::class, DeciArithmeticException::class)
    actual fun divide(
        divisor: Deci,
        scale: Int,
        roundingMode: RoundingMode,
    ): Deci {
        if (scale < 0) throw DeciScaleException(scale)
        if (divisor.isZero()) throw DeciDivisionByZeroException()
        return try {
            Deci(
                internal
                    .divide(
                        divisor.internal,
                        scale,
                        convert(roundingMode),
                    ).toPlainString(),
            )
        } catch (_: ArithmeticException) {
            throw DeciArithmeticException("Division produced a non-terminating decimal expansion")
        }
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
        return Deci(internal.setScale(scale, convert(roundingMode)))
    }

    actual override fun toString(): String = internal.toPlainString()

    actual fun toDouble(): Double = internal.toDouble()

    actual fun isZero(): Boolean = internal.signum() == 0

    actual fun isNegative(): Boolean = internal.signum() < 0

    actual fun isPositive(): Boolean = internal.signum() > 0

    actual fun abs(): Deci = Deci(internal.abs().toPlainString())

    actual fun negate(): Deci = Deci(internal.negate().toPlainString())

    actual operator fun unaryMinus(): Deci = negate()

    actual fun max(other: Deci): Deci = if (this >= other) this else other

    actual fun min(other: Deci): Deci = if (this <= other) this else other

    actual override fun compareTo(other: Deci): Int = internal.compareTo(other.internal)

    override fun equals(other: Any?): Boolean = this === other || (other is Deci && compareTo(other) == 0)

    override fun hashCode(): Int = internal.stripTrailingZeros().hashCode()

    private fun convert(mode: RoundingMode): JavaRoundingMode =
        when (mode) {
            RoundingMode.UP -> JavaRoundingMode.UP
            RoundingMode.DOWN -> JavaRoundingMode.DOWN
            RoundingMode.CEILING -> JavaRoundingMode.CEILING
            RoundingMode.FLOOR -> JavaRoundingMode.FLOOR
            RoundingMode.HALF_UP -> JavaRoundingMode.HALF_UP
            RoundingMode.HALF_DOWN -> JavaRoundingMode.HALF_DOWN
            RoundingMode.HALF_EVEN -> JavaRoundingMode.HALF_EVEN
        }
}
