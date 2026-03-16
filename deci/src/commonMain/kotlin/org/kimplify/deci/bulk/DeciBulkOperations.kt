package org.kimplify.deci.bulk

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext
import org.kimplify.deci.ExperimentalDeciApi
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.exception.DeciDivisionByZeroException
import org.kimplify.deci.extension.sumDeci

/**
 * Bulk operations and collection utilities for Deci.
 */

/**
 * Multiplies all values in the collection together.
 *
 * @return Product of all values, or Deci.ONE for empty collections
 */
@ExperimentalDeciApi
fun Iterable<Deci>.multiplyAll(): Deci {
    return this.fold(Deci.ONE) { acc, value -> acc * value }
}

/**
 * Calculates the average of the collection.
 *
 * Division is performed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP]) is used.
 *
 * @param context The [DeciContext] controlling precision and rounding of the division.
 * @return The average value, or null if collection is empty.
 */
@ExperimentalDeciApi
fun Iterable<Deci>.averageDeci(context: DeciContext = DeciContext.DEFAULT): Deci? {
    val values = this.toList()
    if (values.isEmpty()) return null
    return values.sumDeci().divide(Deci(values.size), context)
}

/**
 * Applies the same operation to all values in the collection.
 *
 * @param operation The operation to apply to each value
 * @return List of transformed values
 */
@ExperimentalDeciApi
fun Iterable<Deci>.applyToAll(operation: (Deci) -> Deci): List<Deci> {
    return this.map(operation)
}

/**
 * Adds the same value to all elements in the collection.
 *
 * @param value Value to add to each element
 * @return List with value added to each element
 */
@ExperimentalDeciApi
fun Iterable<Deci>.addToAll(value: Deci): List<Deci> {
    return this.map { it + value }
}

/**
 * Subtracts the same value from all elements in the collection.
 *
 * @param value Value to subtract from each element
 * @return List with value subtracted from each element
 */
@ExperimentalDeciApi
fun Iterable<Deci>.subtractFromAll(value: Deci): List<Deci> {
    return this.map { it - value }
}

/**
 * Multiplies all elements in the collection by the same value.
 *
 * @param multiplier Value to multiply each element by
 * @return List with each element multiplied by the value
 */
@ExperimentalDeciApi
fun Iterable<Deci>.multiplyAllBy(multiplier: Deci): List<Deci> {
    return this.map { it * multiplier }
}

/**
 * Divides all elements in the collection by the same value.
 *
 * Division is performed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP]) is used.
 *
 * @param divisor Value to divide each element by.
 * @param context The [DeciContext] controlling precision and rounding of the division.
 * @return List with each element divided by the value.
 * @throws DeciDivisionByZeroException if divisor is zero.
 */
@ExperimentalDeciApi
fun Iterable<Deci>.divideAllBy(
    divisor: Deci,
    context: DeciContext = DeciContext.DEFAULT,
): List<Deci> {
    if (divisor.isZero()) throw DeciDivisionByZeroException("Cannot divide: divisor is zero")
    return this.map { it.divide(divisor, context) }
}

/**
 * Applies a percentage change to all values in the collection.
 *
 * Division is performed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP]) is used.
 *
 * @param percentageChange Percentage change to apply (e.g., 10 for +10%, -5 for -5%).
 * @param context The [DeciContext] controlling precision and rounding of the division.
 * @return List with percentage change applied to each element.
 */
@ExperimentalDeciApi
fun Iterable<Deci>.applyPercentageChange(
    percentageChange: Deci,
    context: DeciContext = DeciContext.DEFAULT,
): List<Deci> {
    val multiplier = Deci.ONE + percentageChange.divide(Deci("100"), context)
    return this.multiplyAllBy(multiplier)
}

/**
 * Normalizes all values in the collection to a 0-1 range.
 *
 * Division is performed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP]) is used.
 *
 * @param context The [DeciContext] controlling precision and rounding of the division.
 * @return List of normalized values, or original list if all values are equal.
 */
@ExperimentalDeciApi
fun Iterable<Deci>.normalize(context: DeciContext = DeciContext.DEFAULT): List<Deci> {
    val values = this.toList()
    if (values.isEmpty()) return values

    val min = values.minOrNull() ?: return values
    val max = values.maxOrNull() ?: return values
    val range = max - min

    return if (range.isZero()) {
        values // All values are the same
    } else {
        values.map { (it - min).divide(range, context) }
    }
}

/**
 * Scales all values in the collection to sum to a target value.
 *
 * Division is performed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP]) is used.
 *
 * @param targetSum The desired sum of all values.
 * @param context The [DeciContext] controlling precision and rounding of the division.
 * @return List of scaled values that sum to targetSum.
 * @throws DeciDivisionByZeroException if current sum is zero.
 */
@ExperimentalDeciApi
fun Iterable<Deci>.scaleToSum(
    targetSum: Deci,
    context: DeciContext = DeciContext.DEFAULT,
): List<Deci> {
    val values = this.toList()
    if (values.isEmpty()) return values

    val currentSum = values.sumDeci()
    if (currentSum.isZero()) throw DeciDivisionByZeroException("Cannot scale when current sum is zero")

    val scaleFactor = targetSum.divide(currentSum, context)
    return values.multiplyAllBy(scaleFactor)
}

/**
 * Rounds all values in the collection to the specified scale.
 *
 * @param scale Number of decimal places
 * @param roundingMode Rounding mode to use
 * @return List of rounded values
 */
@ExperimentalDeciApi
fun Iterable<Deci>.roundAll(
    scale: Int,
    roundingMode: RoundingMode,
): List<Deci> {
    return this.map { it.setScale(scale, roundingMode) }
}

/**
 * Filters values within the specified range (inclusive).
 *
 * @param min Minimum value (inclusive)
 * @param max Maximum value (inclusive)
 * @return List of values within the range
 */
@ExperimentalDeciApi
fun Iterable<Deci>.filterInRange(
    min: Deci,
    max: Deci,
): List<Deci> {
    require(min <= max) { "Min value must be less than or equal to max value" }
    return this.filter { it >= min && it <= max }
}

/**
 * Removes statistical outliers from the collection using the Interquartile Range (IQR) method.
 *
 * Values outside the range `[Q1 - multiplier * IQR, Q3 + multiplier * IQR]` are excluded.
 * Collections with fewer than 4 elements are returned unchanged.
 *
 * @param multiplier the tolerance factor applied to the IQR (default: `1.5`).
 * @return a sorted list with outliers removed.
 */
@ExperimentalDeciApi
fun Iterable<Deci>.filterOutliers(multiplier: Deci = Deci("1.5")): List<Deci> {
    val sorted = this.toList().sorted()
    if (sorted.size < 4) return sorted

    val q1 = calculatePercentile(sorted, 25)
    val q3 = calculatePercentile(sorted, 75)
    val iqr = q3 - q1

    val lowerBound = q1 - (iqr * multiplier)
    val upperBound = q3 + (iqr * multiplier)

    return sorted.filter { it in lowerBound..upperBound }
}

private fun calculatePercentile(
    sorted: List<Deci>,
    percentile: Int,
): Deci {
    val n = sorted.size
    val index = (percentile / 100.0) * (n - 1)
    val lower = index.toInt()
    val upper = (lower + 1).coerceAtMost(n - 1)
    val fraction = Deci((index - lower).toString())
    return sorted[lower] + (sorted[upper] - sorted[lower]) * fraction
}

/**
 * Groups consecutive values that are within the specified tolerance of each other.
 *
 * @param tolerance Maximum difference between consecutive values in the same group
 * @return List of groups, where each group is a list of consecutive similar values
 */
@ExperimentalDeciApi
fun Iterable<Deci>.groupConsecutiveSimilar(tolerance: Deci): List<List<Deci>> {
    val values = this.toList()
    if (values.isEmpty()) return emptyList()

    val groups = mutableListOf<MutableList<Deci>>()
    var currentGroup = mutableListOf<Deci>()
    var previousValue: Deci? = null

    for (value in values) {
        if (previousValue == null || (value - previousValue).abs() <= tolerance) {
            currentGroup.add(value)
        } else {
            if (currentGroup.isNotEmpty()) {
                groups.add(currentGroup)
            }
            currentGroup = mutableListOf(value)
        }
        previousValue = value
    }

    if (currentGroup.isNotEmpty()) {
        groups.add(currentGroup)
    }

    return groups
}

/**
 * Partitions the collection into two lists based on a predicate.
 *
 * @param predicate Function to test each element
 * @return Pair where first list contains elements that match predicate, second contains those that don't
 */
@ExperimentalDeciApi
fun Iterable<Deci>.partitionDeci(predicate: (Deci) -> Boolean): Pair<List<Deci>, List<Deci>> {
    return this.partition(predicate)
}

/**
 * Creates a cumulative sum list where each element is the sum of all previous elements.
 *
 * @return List of cumulative sums
 */
@ExperimentalDeciApi
fun Iterable<Deci>.cumulativeSum(): List<Deci> {
    val result = mutableListOf<Deci>()
    var sum = Deci.ZERO

    for (value in this) {
        sum += value
        result.add(sum)
    }

    return result
}

/**
 * Creates a moving average with the specified window size.
 *
 * Division is performed using the supplied [context] for scale and rounding.
 * By default, [DeciContext.DEFAULT] (20 fractional digits, [RoundingMode.HALF_UP]) is used.
 *
 * @param windowSize Size of the moving window.
 * @param context The [DeciContext] controlling precision and rounding of the division.
 * @return List of moving averages (shorter than original list by windowSize - 1).
 */
@ExperimentalDeciApi
fun Iterable<Deci>.movingAverage(
    windowSize: Int,
    context: DeciContext = DeciContext.DEFAULT,
): List<Deci> {
    require(windowSize > 0) { "Window size must be positive: $windowSize" }

    val values = this.toList()
    if (values.size < windowSize) return emptyList()

    return values.windowed(windowSize) { window ->
        window.sumDeci().divide(Deci(windowSize), context)
    }
}

/**
 * Calculates the differences between consecutive elements.
 *
 * @return List of differences (one element shorter than original)
 */
@ExperimentalDeciApi
fun Iterable<Deci>.differences(): List<Deci> {
    val values = this.toList()
    if (values.size < 2) return emptyList()

    return values.zipWithNext { a, b -> b - a }
}

/**
 * Finds the top N largest values in the collection.
 *
 * @param n Number of top values to return
 * @return List of top N values in descending order
 */
@ExperimentalDeciApi
fun Iterable<Deci>.topN(n: Int): List<Deci> {
    require(n >= 0) { "N must be non-negative: $n" }
    return this.sortedDescending().take(n)
}

/**
 * Finds the bottom N smallest values in the collection.
 *
 * @param n Number of bottom values to return
 * @return List of bottom N values in ascending order
 */
@ExperimentalDeciApi
fun Iterable<Deci>.bottomN(n: Int): List<Deci> {
    require(n >= 0) { "N must be non-negative: $n" }
    return this.sorted().take(n)
}
