package org.kimplify.deci.statistics

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciConstants
import org.kimplify.deci.extension.sumDeci
import org.kimplify.deci.math.sqrt

/**
 * Statistical operations for collections of Deci values.
 */

/**
 * Calculates the arithmetic mean (average) of the collection.
 *
 * @return The mean value, or null if collection is empty
 */
fun Iterable<Deci>.mean(): Deci? {
    val values = this.toList()
    if (values.isEmpty()) return null
    return values.sumDeci() / Deci(values.size)
}

/**
 * Calculates the median (middle value) of the collection.
 *
 * @return The median value, or null if collection is empty
 */
fun Iterable<Deci>.median(): Deci? {
    val sorted = this.toList().sorted()
    if (sorted.isEmpty()) return null

    val size = sorted.size
    return if (size % 2 == 0) {
        val mid1 = sorted[size / 2 - 1]
        val mid2 = sorted[size / 2]
        (mid1 + mid2) / DeciConstants.TWO
    } else {
        sorted[size / 2]
    }
}

/**
 * Finds the minimum value in the collection.
 *
 * @return The minimum value, or null if collection is empty
 */
fun Iterable<Deci>.minDeci(): Deci? {
    return this.minOrNull()
}

/**
 * Finds the maximum value in the collection.
 *
 * @return The maximum value, or null if collection is empty
 */
fun Iterable<Deci>.maxDeci(): Deci? {
    return this.maxOrNull()
}

/**
 * Calculates the range (max - min) of the collection.
 *
 * @return The range, or null if collection is empty
 */
fun Iterable<Deci>.range(): Deci? {
    val min = minDeci() ?: return null
    val max = maxDeci() ?: return null
    return max - min
}

/**
 * Calculates the variance of the collection.
 *
 * @param isPopulation True for population variance, false for sample variance
 * @return The variance, or null if collection is empty or has only one element for sample variance
 */
fun Iterable<Deci>.variance(isPopulation: Boolean = false): Deci? {
    val values = this.toList()
    if (values.isEmpty()) return null
    if (!isPopulation && values.size <= 1) return null

    val mean = values.mean() ?: return null
    val sumOfSquares = values.fold(Deci.ZERO) { acc, value ->
        val diff = value - mean
        acc + (diff * diff)
    }

    val divisor = if (isPopulation) values.size else values.size - 1
    return sumOfSquares / Deci(divisor)
}

/**
 * Calculates the standard deviation of the collection.
 *
 * @param isPopulation True for population standard deviation, false for sample standard deviation
 * @return The standard deviation, or null if variance cannot be calculated
 */
fun Iterable<Deci>.standardDeviation(isPopulation: Boolean = false): Deci? {
    val variance = variance(isPopulation) ?: return null
    return variance.sqrt()
}

/**
 * Calculates the weighted average of the collection.
 *
 * @param weights The weights for each value (must have same size as values)
 * @return The weighted average, or null if collections are empty or different sizes
 */
fun Iterable<Deci>.weightedAverage(weights: List<Deci>): Deci? {
    val values = this.toList()
    if (values.isEmpty() || weights.isEmpty() || values.size != weights.size) return null

    val weightedSum = values.zip(weights) { value, weight -> value * weight }.sumDeci()
    val totalWeight = weights.sumDeci()

    return if (totalWeight.isZero()) null else weightedSum / totalWeight
}

/**
 * Calculates the harmonic mean of the collection.
 * Note: All values must be positive.
 *
 * @return The harmonic mean, or null if collection is empty or contains non-positive values
 */
fun Iterable<Deci>.harmonicMean(): Deci? {
    val values = this.toList()
    if (values.isEmpty()) return null
    if (values.any { it <= Deci.ZERO }) return null

    val sumOfReciprocals = values.fold(Deci.ZERO) { acc, value -> acc + (Deci.ONE / value) }
    return Deci(values.size) / sumOfReciprocals
}

/**
 * Counts values that satisfy the given predicate.
 *
 * @param predicate The condition to test
 * @return Number of values satisfying the predicate
 */
fun Iterable<Deci>.countWhere(predicate: (Deci) -> Boolean): Int {
    return this.count(predicate)
}

/**
 * Calculates the sum of squares of deviations from the mean.
 *
 * @return Sum of squares, or null if collection is empty
 */
fun Iterable<Deci>.sumOfSquares(): Deci? {
    val values = this.toList()
    if (values.isEmpty()) return null

    val mean = values.mean() ?: return null
    return values.fold(Deci.ZERO) { acc, value ->
        val deviation = value - mean
        acc + (deviation * deviation)
    }
}
