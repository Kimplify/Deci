package org.kimplify.deci

import kotlinx.serialization.Serializable

/**
 * Multiplatform arbitrary-precision decimal that normalizes user input and delegates
 * to the most capable numeric engine available on each platform.
 */
@Serializable(with = DeciSerializer::class)
expect class Deci : Comparable<Deci> {
    constructor(value: String)
    constructor(value: Long)
    constructor(value: Int)
    constructor(value: Double)
    
    operator fun plus(other: Deci): Deci
    operator fun minus(other: Deci): Deci
    operator fun times(other: Deci): Deci
    operator fun div(other: Deci): Deci
    fun divide(divisor: Deci, scale: Int, roundingMode: RoundingMode): Deci
    fun setScale(scale: Int, roundingMode: RoundingMode): Deci
    override fun toString(): String
    fun toDouble(): Double
    fun isZero(): Boolean
    fun isNegative(): Boolean
    fun isPositive(): Boolean
    fun abs(): Deci
    fun negate(): Deci
    fun max(other: Deci): Deci
    fun min(other: Deci): Deci

    override fun compareTo(other: Deci): Int

    companion object {
        val ZERO: Deci
        val ONE: Deci
        val TEN: Deci

        @Deprecated("Use constructor instead", ReplaceWith("Deci(value)"))
        fun fromInt(value: Int): Deci
        @Deprecated("Use constructor instead", ReplaceWith("Deci(value)"))
        fun fromDouble(value: Double): Deci

        @Deprecated("Use constructor instead", ReplaceWith("Deci(value)"))
        fun fromStringOrThrow(value: String): Deci

        fun fromStringOrZero(value: String): Deci
        fun fromStringOrNull(value: String): Deci?
    }
}
