package org.kimplify.deci.bulk

import org.kimplify.deci.Deci
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.sumDeci

/**
 * Bulk operations and collection utilities for Deci.
 */

/**
 * Multiplies all values in the collection together.
 * 
 * @return Product of all values, or Deci.ONE for empty collections
 */
fun Iterable<Deci>.multiplyAll(): Deci {
    return this.fold(Deci.ONE) { acc, value -> acc * value }
}

/**
 * Calculates the average (arithmetic mean) of the collection.
 * 
 * @return Average value, or Deci.ZERO for empty collections
 */
fun Iterable<Deci>.averageDeci(): Deci {
    val values = this.toList()
    return if (values.isEmpty()) {
        Deci.ZERO
    } else {
        values.sumDeci() / Deci(values.size)
    }
}

/**
 * Applies the same operation to all values in the collection.
 * 
 * @param operation The operation to apply to each value
 * @return List of transformed values
 */
fun Iterable<Deci>.applyToAll(operation: (Deci) -> Deci): List<Deci> {
    return this.map(operation)
}

/**
 * Adds the same value to all elements in the collection.
 * 
 * @param value Value to add to each element
 * @return List with value added to each element
 */
fun Iterable<Deci>.addToAll(value: Deci): List<Deci> {
    return this.map { it + value }
}

/**
 * Subtracts the same value from all elements in the collection.
 * 
 * @param value Value to subtract from each element
 * @return List with value subtracted from each element
 */
fun Iterable<Deci>.subtractFromAll(value: Deci): List<Deci> {
    return this.map { it - value }
}

/**
 * Multiplies all elements in the collection by the same value.
 * 
 * @param multiplier Value to multiply each element by
 * @return List with each element multiplied by the value
 */
fun Iterable<Deci>.multiplyAllBy(multiplier: Deci): List<Deci> {
    return this.map { it * multiplier }
}

/**
 * Divides all elements in the collection by the same value.
 * 
 * @param divisor Value to divide each element by
 * @return List with each element divided by the value
 * @throws ArithmeticException if divisor is zero
 */
fun Iterable<Deci>.divideAllBy(divisor: Deci): List<Deci> {
    require(!divisor.isZero()) { "Cannot divide by zero" }
    return this.map { it / divisor }
}

/**
 * Applies a percentage change to all values in the collection.
 * 
 * @param percentageChange Percentage change to apply (e.g., 10 for +10%, -5 for -5%)
 * @return List with percentage change applied to each element
 */
fun Iterable<Deci>.applyPercentageChange(percentageChange: Deci): List<Deci> {
    val multiplier = Deci.ONE + (percentageChange / Deci("100"))
    return this.multiplyAllBy(multiplier)
}

/**
 * Normalizes all values in the collection to a 0-1 range.
 * 
 * @return List of normalized values, or original list if all values are equal
 */
fun Iterable<Deci>.normalize(): List<Deci> {
    val values = this.toList()
    if (values.isEmpty()) return values
    
    val min = values.minOrNull() ?: return values
    val max = values.maxOrNull() ?: return values
    val range = max - min
    
    return if (range.isZero()) {
        values // All values are the same
    } else {
        values.map { (it - min) / range }
    }
}

/**
 * Scales all values in the collection to sum to a target value.
 * 
 * @param targetSum The desired sum of all values
 * @return List of scaled values that sum to targetSum
 * @throws IllegalArgumentException if current sum is zero
 */
fun Iterable<Deci>.scaleToSum(targetSum: Deci): List<Deci> {
    val values = this.toList()
    if (values.isEmpty()) return values
    
    val currentSum = values.sumDeci()
    require(!currentSum.isZero()) { "Cannot scale when current sum is zero" }
    
    val scaleFactor = targetSum / currentSum
    return values.multiplyAllBy(scaleFactor)
}

/**
 * Rounds all values in the collection to the specified scale.
 * 
 * @param scale Number of decimal places
 * @param roundingMode Rounding mode to use
 * @return List of rounded values
 */
fun Iterable<Deci>.roundAll(scale: Int, roundingMode: RoundingMode): List<Deci> {
    return this.map { it.setScale(scale, roundingMode) }
}

/**
 * Filters values within the specified range (inclusive).
 * 
 * @param min Minimum value (inclusive)
 * @param max Maximum value (inclusive)
 * @return List of values within the range
 */
fun Iterable<Deci>.filterInRange(min: Deci, max: Deci): List<Deci> {
    require(min <= max) { "Min value must be less than or equal to max value" }
    return this.filter { it >= min && it <= max }
}

/**
 * Filters out outliers using the interquartile range (IQR) method.
 * 
 * @param multiplier IQR multiplier for outlier detection (default: 1.5)
 * @return List with outliers removed
 */
fun Iterable<Deci>.filterOutliers(multiplier: Deci = Deci("1.5")): List<Deci> {
    val sorted = this.toList().sorted()
    if (sorted.size < 4) return sorted
    
    val q1Index = sorted.size / 4
    val q3Index = (sorted.size * 3) / 4
    val q1 = sorted[q1Index]
    val q3 = sorted[q3Index]
    val iqr = q3 - q1
    
    val lowerBound = q1 - (iqr * multiplier)
    val upperBound = q3 + (iqr * multiplier)
    
    return sorted.filter { it in lowerBound..upperBound }
}

/**
 * Groups consecutive values that are within the specified tolerance of each other.
 * 
 * @param tolerance Maximum difference between consecutive values in the same group
 * @return List of groups, where each group is a list of consecutive similar values
 */
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
fun Iterable<Deci>.partitionDeci(predicate: (Deci) -> Boolean): Pair<List<Deci>, List<Deci>> {
    return this.partition(predicate)
}

/**
 * Creates a cumulative sum list where each element is the sum of all previous elements.
 * 
 * @return List of cumulative sums
 */
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
 * @param windowSize Size of the moving window
 * @return List of moving averages (shorter than original list by windowSize - 1)
 */
fun Iterable<Deci>.movingAverage(windowSize: Int): List<Deci> {
    require(windowSize > 0) { "Window size must be positive: $windowSize" }
    
    val values = this.toList()
    if (values.size < windowSize) return emptyList()
    
    return values.windowed(windowSize) { window ->
        window.sumDeci() / Deci(windowSize)
    }
}

/**
 * Calculates the differences between consecutive elements.
 * 
 * @return List of differences (one element shorter than original)
 */
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
fun Iterable<Deci>.bottomN(n: Int): List<Deci> {
    require(n >= 0) { "N must be non-negative: $n" }
    return this.sorted().take(n)
}
