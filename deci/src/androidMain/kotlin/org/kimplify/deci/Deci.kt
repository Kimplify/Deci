package org.kimplify.deci

import kotlinx.serialization.Serializable
import org.kimplify.deci.config.DeciConfiguration
import org.kimplify.deci.parser.validateAndNormalizeDecimalLiteral
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode as JavaRoundingMode

@Serializable(with = DeciSerializer::class)
actual class Deci(private val internal: BigDecimal) : Comparable<Deci> {

    actual constructor(value: String) : this(
        BigDecimal(validateAndNormalizeDecimalLiteral(value)).stripTrailingZeros()
    )

    actual constructor(value: Long) : this(value.toString())
    actual constructor(value: Int) : this(value.toString())
    actual constructor(value: Double) : this(value.toString())

    actual companion object {
        actual val ZERO = Deci("0")
        actual val ONE = Deci("1")
        actual val TEN = Deci("10")

        @Throws(IllegalArgumentException::class)
        actual fun fromStringOrThrow(value: String): Deci =
            Deci(value)

        actual fun fromStringOrNull(value: String): Deci? =
            runCatching { fromStringOrThrow(value) }
                .getOrNull()

        actual fun fromStringOrZero(value: String): Deci =
            fromStringOrNull(value) ?: ZERO

        actual fun fromDouble(value: Double): Deci =
            Deci(value.toString())

        actual fun fromInt(value: Int): Deci =
            Deci(value.toString())
    }

    private inline fun operate(
        other: Deci,
        mc: MathContext? = null,
        op: (BigDecimal, BigDecimal, MathContext?) -> BigDecimal
    ): Deci {
        val result = op(internal, other.internal, mc)
        return Deci(result.toPlainString())
    }

    actual operator fun plus(other: Deci): Deci =
        operate(other) { a, b, _ -> a.add(b) }

    actual operator fun minus(other: Deci): Deci =
        operate(other) { a, b, _ -> a.subtract(b) }

    actual operator fun times(other: Deci): Deci =
        operate(other) { a, b, _ -> a.multiply(b) }

    @Throws(ArithmeticException::class)
    actual operator fun div(other: Deci): Deci =
        operate(other) { a, b, _ ->
            val policy = DeciConfiguration.divisionPolicy
            a.divide(b, policy.fractionalDigits, convert(policy.roundingMode))
        }

    @Throws(ArithmeticException::class)
    actual fun divide(divisor: Deci, scale: Int, roundingMode: RoundingMode): Deci {
        require(scale >= 0) { "Scale must be non-negative: $scale" }
        return Deci(
            internal.divide(
                divisor.internal,
                scale,
                convert(roundingMode)
            ).toPlainString()
        )
    }

    actual fun setScale(scale: Int, roundingMode: RoundingMode): Deci {
        require(scale >= 0) { "Scale must be non-negative: $scale" }
        return Deci(internal.setScale(scale, convert(roundingMode)))
    }

    actual override fun toString(): String =
        internal.toPlainString()

    actual fun toDouble(): Double =
        internal.toDouble()

    actual fun isZero(): Boolean =
        internal.signum() == 0

    actual fun isNegative(): Boolean =
        internal.signum() < 0

    actual fun isPositive(): Boolean =
        internal.signum() > 0

    actual fun abs(): Deci =
        Deci(internal.abs().toPlainString())

    actual fun negate(): Deci =
        Deci(internal.negate().toPlainString())

    actual fun max(other: Deci): Deci =
        if (this >= other) this else other

    actual fun min(other: Deci): Deci =
        if (this <= other) this else other

    actual override fun compareTo(other: Deci): Int =
        internal.compareTo(other.internal)

    override fun equals(other: Any?): Boolean =
        this === other || (other is Deci && compareTo(other) == 0)

    override fun hashCode(): Int =
        internal.stripTrailingZeros().hashCode()

    private fun convert(mode: RoundingMode): JavaRoundingMode = when (mode) {
        RoundingMode.UP -> JavaRoundingMode.UP
        RoundingMode.DOWN -> JavaRoundingMode.DOWN
        RoundingMode.CEILING -> JavaRoundingMode.CEILING
        RoundingMode.FLOOR -> JavaRoundingMode.FLOOR
        RoundingMode.HALF_UP -> JavaRoundingMode.HALF_UP
        RoundingMode.HALF_DOWN -> JavaRoundingMode.HALF_DOWN
        RoundingMode.HALF_EVEN -> JavaRoundingMode.HALF_EVEN
    }
}

