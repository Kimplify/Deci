package org.kimplify.deci

import kotlinx.serialization.Serializable
import org.kimplify.deci.config.DeciConfiguration
import org.kimplify.deci.exception.DeciDivisionByZeroException
import org.kimplify.deci.exception.DeciScaleException
import org.kimplify.deci.parser.validateAndNormalizeDecimalLiteral

@Serializable(with = DeciSerializer::class)
actual class Deci private constructor(
    private val internal: PureDecimal,
) : Comparable<Deci> {
    actual constructor(value: String) : this(
        PureDecimal.fromString(validateAndNormalizeDecimalLiteral(value)),
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

    actual operator fun plus(other: Deci): Deci = Deci(internal + other.internal)

    actual operator fun minus(other: Deci): Deci = Deci(internal - other.internal)

    actual operator fun times(other: Deci): Deci = Deci(internal * other.internal)

    actual operator fun div(other: Deci): Deci {
        if (other.isZero()) throw DeciDivisionByZeroException()
        val policy = DeciConfiguration.divisionPolicy
        return Deci(internal.divide(other.internal, policy.fractionalDigits, policy.roundingMode))
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
        if (scale < 0) throw DeciScaleException(scale)
        if (divisor.isZero()) throw DeciDivisionByZeroException()
        return Deci(internal.divide(divisor.internal, scale, roundingMode))
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
        return Deci(internal.setScale(scale, roundingMode))
    }

    actual override fun toString(): String = internal.toString()

    actual fun toDouble(): Double = internal.toDouble()

    actual fun isZero(): Boolean = internal.isZero

    actual fun isNegative(): Boolean = internal.isNegative

    actual fun isPositive(): Boolean = internal.isPositive

    actual fun abs(): Deci = if (isNegative()) negate() else this

    actual fun negate(): Deci = Deci(internal.negate())

    actual operator fun unaryMinus(): Deci = negate()

    actual fun max(other: Deci): Deci = if (this >= other) this else other

    actual fun min(other: Deci): Deci = if (this <= other) this else other

    actual override fun compareTo(other: Deci): Int = internal.compareTo(other.internal)

    override fun equals(other: Any?): Boolean = this === other || (other is Deci && compareTo(other) == 0)

    override fun hashCode(): Int = internal.toString().hashCode()
}
