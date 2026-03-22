package org.kimplify.deci.extension

import org.kimplify.deci.Deci

/** Adds an [Int] to this [Deci]. */
operator fun Deci.plus(other: Int): Deci = this + Deci(other)

/** Subtracts an [Int] from this [Deci]. */
operator fun Deci.minus(other: Int): Deci = this - Deci(other)

/** Multiplies this [Deci] by an [Int]. */
operator fun Deci.times(other: Int): Deci = this * Deci(other)

/** Adds a [Long] to this [Deci]. */
operator fun Deci.plus(other: Long): Deci = this + Deci(other)

/** Subtracts a [Long] from this [Deci]. */
operator fun Deci.minus(other: Long): Deci = this - Deci(other)

/** Multiplies this [Deci] by a [Long]. */
operator fun Deci.times(other: Long): Deci = this * Deci(other)

/** Multiplies an [Int] by a [Deci]. */
operator fun Int.times(other: Deci): Deci = Deci(this) * other

/** Multiplies a [Long] by a [Deci]. */
operator fun Long.times(other: Deci): Deci = Deci(this) * other

/** Short factory function for creating a [Deci] from a [String] literal. */
fun d(value: String): Deci = Deci(value)

/** Short factory function for creating a [Deci] from an [Int]. */
fun d(value: Int): Deci = Deci(value)

/** Short factory function for creating a [Deci] from a [Long]. */
fun d(value: Long): Deci = Deci(value)
