package org.kimplify.deci.statistics

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciConstants
import org.kimplify.deci.DeciContext
import org.kimplify.deci.extension.sumDeci
import org.kimplify.deci.math.sqrt

/**
 * Calculates the arithmetic mean (average) of the collection.
 *
 * Division is performed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP][org.kimplify.deci.RoundingMode.HALF_UP]) is used.
 *
 * @param context The [DeciContext] controlling precision and rounding of the division.
 * @return The mean value, or null if collection is empty.
 */
fun Iterable<Deci>.mean(context: DeciContext = DeciContext.DEFAULT): Deci? {
    val values = this.toList()
    if (values.isEmpty()) return null
    return values.sumDeci().divide(Deci(values.size), context)
}

/**
 * Calculates the median (middle value) of the collection.
 *
 * For even-sized collections, the median is the average of the two middle values.
 * Division is performed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP][org.kimplify.deci.RoundingMode.HALF_UP]) is used.
 *
 * @param context The [DeciContext] controlling precision and rounding of the division.
 * @return The median value, or null if collection is empty.
 */
fun Iterable<Deci>.median(context: DeciContext = DeciContext.DEFAULT): Deci? {
    val sorted = this.toList().sorted()
    if (sorted.isEmpty()) return null

    val size = sorted.size
    return if (size % 2 == 0) {
        val mid1 = sorted[size / 2 - 1]
        val mid2 = sorted[size / 2]
        (mid1 + mid2).divide(DeciConstants.TWO, context)
    } else {
        sorted[size / 2]
    }
}

/**
 * Finds the minimum value in the collection.
 *
 * @return The minimum value, or null if collection is empty
 */
fun Iterable<Deci>.minDeci(): Deci? = this.minOrNull()

/**
 * Finds the maximum value in the collection.
 *
 * @return The maximum value, or null if collection is empty
 */
fun Iterable<Deci>.maxDeci(): Deci? = this.maxOrNull()

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
 * Division is performed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP][org.kimplify.deci.RoundingMode.HALF_UP]) is used.
 *
 * @param isPopulation True for population variance, false for sample variance.
 * @param context The [DeciContext] controlling precision and rounding of the division.
 * @return The variance, or null if collection is empty or has only one element for sample variance.
 */
fun Iterable<Deci>.variance(
    isPopulation: Boolean = false,
    context: DeciContext = DeciContext.DEFAULT,
): Deci? {
    val values = this.toList()
    if (values.isEmpty()) return null
    if (!isPopulation && values.size <= 1) return null

    val mean = values.sumDeci().divide(Deci(values.size), context)
    val sumOfSquares =
        values.fold(Deci.ZERO) { acc, value ->
            val diff = value - mean
            acc + (diff * diff)
        }

    val divisor = if (isPopulation) values.size else values.size - 1
    return sumOfSquares.divide(Deci(divisor), context)
}

/**
 * Calculates the standard deviation of the collection.
 *
 * Division is performed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP][org.kimplify.deci.RoundingMode.HALF_UP]) is used.
 *
 * @param isPopulation True for population standard deviation, false for sample standard deviation.
 * @param context The [DeciContext] controlling precision and rounding of the division.
 * @return The standard deviation, or null if variance cannot be calculated.
 */
fun Iterable<Deci>.standardDeviation(
    isPopulation: Boolean = false,
    context: DeciContext = DeciContext.DEFAULT,
): Deci? {
    val variance = variance(isPopulation, context) ?: return null
    return variance.sqrt()
}

/**
 * Calculates the weighted average of the collection.
 *
 * Division is performed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP][org.kimplify.deci.RoundingMode.HALF_UP]) is used.
 *
 * @param weights The weights for each value (must have same size as values).
 * @param context The [DeciContext] controlling precision and rounding of the division.
 * @return The weighted average, or null if collections are empty or different sizes.
 */
fun Iterable<Deci>.weightedAverage(
    weights: List<Deci>,
    context: DeciContext = DeciContext.DEFAULT,
): Deci? {
    val values = this.toList()
    if (values.isEmpty() || weights.isEmpty() || values.size != weights.size) return null

    val weightedSum = values.zip(weights) { value, weight -> value * weight }.sumDeci()
    val totalWeight = weights.sumDeci()

    return if (totalWeight.isZero()) null else weightedSum.divide(totalWeight, context)
}

/**
 * Calculates the harmonic mean of the collection.
 * Note: All values must be positive.
 *
 * Division is performed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP][org.kimplify.deci.RoundingMode.HALF_UP]) is used.
 *
 * @param context The [DeciContext] controlling precision and rounding of the division.
 * @return The harmonic mean, or null if collection is empty or contains non-positive values.
 */
fun Iterable<Deci>.harmonicMean(context: DeciContext = DeciContext.DEFAULT): Deci? {
    val values = this.toList()
    if (values.isEmpty()) return null
    if (values.any { it <= Deci.ZERO }) return null

    val sumOfReciprocals = values.fold(Deci.ZERO) { acc, value -> acc + Deci.ONE.divide(value, context) }
    return Deci(values.size).divide(sumOfReciprocals, context)
}

/**
 * Counts values that satisfy the given predicate.
 *
 * @param predicate The condition to test
 * @return Number of values satisfying the predicate
 */
fun Iterable<Deci>.countWhere(predicate: (Deci) -> Boolean): Int = this.count(predicate)

/**
 * Calculates the sum of squares of deviations from the mean.
 *
 * The mean is computed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP][org.kimplify.deci.RoundingMode.HALF_UP]) is used.
 *
 * @param context The [DeciContext] controlling precision and rounding of the mean division.
 * @return Sum of squares, or null if collection is empty.
 */
fun Iterable<Deci>.sumOfSquares(context: DeciContext = DeciContext.DEFAULT): Deci? {
    val values = this.toList()
    if (values.isEmpty()) return null

    val mean = values.sumDeci().divide(Deci(values.size), context)
    return values.fold(Deci.ZERO) { acc, value ->
        val deviation = value - mean
        acc + (deviation * deviation)
    }
}
