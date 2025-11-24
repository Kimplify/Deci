package org.kimplify.deci

import kotlinx.serialization.Serializable

@Serializable(with = DeciSerializer::class)
actual class Deci private constructor(
    private val internal: DecimalJs
) : Comparable<Deci> {

    actual constructor(value: String) : this(
        value.let {
            require(it.isNotBlank()) {
                "Deci literal must not be blank"
            }
            require(DECIMAL_REGEX.matches(it.trim())) {
                "Invalid decimal literal: '$it'"
            }
            val normalized = it.normalizeDecimalString()
            try {
                DecimalJs(normalized)
            } catch (e: Throwable) {
                println("Deci constructor error for value='$it' (normalized='$normalized'): ${e.message}")
                throw IllegalArgumentException("Failed to create DecimalJs: ${e.message}", e)
            }
        }
    )

    actual constructor(value: Long) : this(value.toString())
    actual constructor(value: Int) : this(value.toString())
    actual constructor(value: Double) : this(value.toString())

    actual companion object {
        actual val ZERO = Deci("0")
        actual val ONE = Deci("1")
        actual val TEN = Deci("10")

        actual fun fromStringOrThrow(value: String): Deci =
            Deci(value)

        actual fun fromStringOrNull(value: String): Deci? =
            runCatching {
                val result = fromStringOrThrow(value)
                println("Deci.fromStringOrNull: Attempting to parse input='$value' => result='$result'")
                result
            }.onFailure { e ->
                    println("Deci.fromStringOrNull: Error parsing input='$value': ${e.message}")
                }
                .getOrNull()

        actual fun fromStringOrZero(value: String): Deci =
            fromStringOrNull(value) ?: ZERO

        actual fun fromDouble(value: Double): Deci =
            Deci(value.toString())

        actual fun fromInt(value: Int): Deci =
            Deci(value.toString())
    }

    actual operator fun plus(other: Deci): Deci =
        Deci(internal.add(other.internal))

    actual operator fun minus(other: Deci): Deci =
        Deci(internal.sub(other.internal))

    actual operator fun times(other: Deci): Deci =
        Deci(internal.mul(other.internal))

    actual operator fun div(other: Deci): Deci {
        if (other.isZero()) throw ArithmeticException("Division by zero")
        return Deci(internal.div(other.internal))
    }

    actual fun divide(divisor: Deci, scale: Int, roundingMode: RoundingMode): Deci {
        require(scale >= 0) { "Scale must be non-negative: $scale" }
        if (divisor.isZero()) throw ArithmeticException("Division by zero")
        val result = internal.div(divisor.internal)
        val rounded = result.toDecimalPlaces(scale, convert(roundingMode))
        return Deci(rounded)
    }

    actual fun setScale(scale: Int, roundingMode: RoundingMode): Deci {
        require(scale >= 0) { "Scale must be non-negative: $scale" }
        return Deci(internal.toDecimalPlaces(scale, convert(roundingMode)))
    }

    actual override fun toString(): String =
        try {
            internal.toString()
        } catch (e: Throwable) {
            println("Deci.toString() error: ${e.message}")
            "Error"
        }

    actual fun toDouble(): Double =
        internal.toNumber()

    actual fun isZero(): Boolean =
        internal.isZero()

    actual fun isNegative(): Boolean =
        internal.isNegative()

    actual fun isPositive(): Boolean =
        internal.isPositive()

    actual fun abs(): Deci =
        Deci(internal.abs())

    actual fun negate(): Deci =
        Deci(internal.neg())

    actual fun max(other: Deci): Deci =
        if (internal.comparedTo(other.internal) >= 0) this else other

    actual fun min(other: Deci): Deci =
        if (internal.comparedTo(other.internal) <= 0) this else other

    actual override fun compareTo(other: Deci): Int =
        internal.comparedTo(other.internal)

    override fun equals(other: Any?): Boolean =
        this === other || (other is Deci && compareTo(other) == 0)

    override fun hashCode(): Int =
        internal.toString().hashCode()

    private fun convert(mode: RoundingMode): Int = when (mode) {
        RoundingMode.UP -> 0
        RoundingMode.DOWN -> 1
        RoundingMode.CEILING -> 2
        RoundingMode.FLOOR -> 3
        RoundingMode.HALF_UP -> 4
        RoundingMode.HALF_DOWN -> 5
        RoundingMode.HALF_EVEN -> 6
    }
}
