package org.kimplify.deci.statistics

import org.kimplify.deci.Deci
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DeciStatisticsTest {

    // ========== Mean Tests ==========

    @Test
    fun `mean returns null for empty collection`() {
        val result = emptyList<Deci>().mean()
        assertNull(result)
    }

    @Test
    fun `mean returns value for single element`() {
        val result = listOf(Deci("5")).mean()
        assertEquals(Deci("5"), result)
    }

    @Test
    fun `mean calculates average correctly`() {
        val result = listOf(Deci("1"), Deci("2"), Deci("3"), Deci("4"), Deci("5")).mean()
        assertEquals(Deci("3"), result)
    }

    @Test
    fun `mean handles decimal values`() {
        val result = listOf(Deci("1.5"), Deci("2.5"), Deci("3.5")).mean()
        assertEquals(Deci("2.5"), result)
    }

    @Test
    fun `mean handles negative values`() {
        val result = listOf(Deci("-10"), Deci("10"), Deci("0")).mean()
        assertEquals(Deci("0"), result)
    }

    // ========== Median Tests ==========

    @Test
    fun `median returns null for empty collection`() {
        val result = emptyList<Deci>().median()
        assertNull(result)
    }

    @Test
    fun `median returns value for single element`() {
        val result = listOf(Deci("7")).median()
        assertEquals(Deci("7"), result)
    }

    @Test
    fun `median returns middle value for odd-sized collection`() {
        val result = listOf(Deci("1"), Deci("3"), Deci("5")).median()
        assertEquals(Deci("3"), result)
    }

    @Test
    fun `median returns average of two middle values for even-sized collection`() {
        val result = listOf(Deci("1"), Deci("2"), Deci("3"), Deci("4")).median()
        assertEquals(Deci("2.5"), result)
    }

    @Test
    fun `median sorts unsorted collection`() {
        val result = listOf(Deci("5"), Deci("1"), Deci("3"), Deci("2"), Deci("4")).median()
        assertEquals(Deci("3"), result)
    }

    @Test
    fun `median handles negative values`() {
        val result = listOf(Deci("-3"), Deci("-1"), Deci("2"), Deci("5")).median()
        assertEquals(Deci("0.5"), result)
    }

    // ========== MinDeci Tests ==========

    @Test
    fun `minDeci returns null for empty collection`() {
        val result = emptyList<Deci>().minDeci()
        assertNull(result)
    }

    @Test
    fun `minDeci returns value for single element`() {
        val result = listOf(Deci("42")).minDeci()
        assertEquals(Deci("42"), result)
    }

    @Test
    fun `minDeci finds minimum value`() {
        val result = listOf(Deci("5"), Deci("1"), Deci("9"), Deci("3")).minDeci()
        assertEquals(Deci("1"), result)
    }

    @Test
    fun `minDeci handles negative values`() {
        val result = listOf(Deci("5"), Deci("-10"), Deci("0")).minDeci()
        assertEquals(Deci("-10"), result)
    }

    // ========== MaxDeci Tests ==========

    @Test
    fun `maxDeci returns null for empty collection`() {
        val result = emptyList<Deci>().maxDeci()
        assertNull(result)
    }

    @Test
    fun `maxDeci returns value for single element`() {
        val result = listOf(Deci("42")).maxDeci()
        assertEquals(Deci("42"), result)
    }

    @Test
    fun `maxDeci finds maximum value`() {
        val result = listOf(Deci("5"), Deci("1"), Deci("9"), Deci("3")).maxDeci()
        assertEquals(Deci("9"), result)
    }

    @Test
    fun `maxDeci handles negative values`() {
        val result = listOf(Deci("-5"), Deci("-10"), Deci("-1")).maxDeci()
        assertEquals(Deci("-1"), result)
    }

    // ========== Range Tests ==========

    @Test
    fun `range returns null for empty collection`() {
        val result = emptyList<Deci>().range()
        assertNull(result)
    }

    @Test
    fun `range returns zero for single element`() {
        val result = listOf(Deci("5")).range()
        assertEquals(Deci("0"), result)
    }

    @Test
    fun `range calculates difference between max and min`() {
        val result = listOf(Deci("1"), Deci("5"), Deci("3")).range()
        assertEquals(Deci("4"), result)
    }

    @Test
    fun `range handles negative values`() {
        val result = listOf(Deci("-10"), Deci("10")).range()
        assertEquals(Deci("20"), result)
    }

    // ========== Variance Tests ==========

    @Test
    fun `variance returns null for empty collection`() {
        val result = emptyList<Deci>().variance()
        assertNull(result)
    }

    @Test
    fun `sample variance returns null for single element`() {
        val result = listOf(Deci("5")).variance(isPopulation = false)
        assertNull(result)
    }

    @Test
    fun `population variance returns zero for single element`() {
        val result = listOf(Deci("5")).variance(isPopulation = true)
        assertEquals(Deci("0"), result)
    }

    @Test
    fun `sample variance calculates correctly`() {
        val result = listOf(Deci("2"), Deci("4"), Deci("4"), Deci("4"), Deci("5"), Deci("5"), Deci("7"), Deci("9")).variance(isPopulation = false)
        assertNotNull(result)
        assertEquals(Deci("4.57142857142857142857"), result)
    }

    @Test
    fun `population variance calculates correctly`() {
        val result = listOf(Deci("2"), Deci("4"), Deci("4"), Deci("4"), Deci("5"), Deci("5"), Deci("7"), Deci("9")).variance(isPopulation = true)
        assertNotNull(result)
        assertEquals(Deci("4"), result)
    }

    @Test
    fun `variance handles identical values`() {
        val result = listOf(Deci("5"), Deci("5"), Deci("5")).variance()
        assertEquals(Deci("0"), result)
    }

    // ========== Standard Deviation Tests ==========

    @Test
    fun `standardDeviation returns null for empty collection`() {
        val result = emptyList<Deci>().standardDeviation()
        assertNull(result)
    }

    @Test
    fun `sample standardDeviation returns null for single element`() {
        val result = listOf(Deci("5")).standardDeviation(isPopulation = false)
        assertNull(result)
    }

    @Test
    fun `population standardDeviation returns zero for single element`() {
        val result = listOf(Deci("5")).standardDeviation(isPopulation = true)
        assertEquals(Deci("0"), result)
    }

    @Test
    fun `standardDeviation calculates correctly`() {
        val values = listOf(Deci("2"), Deci("4"), Deci("4"), Deci("4"), Deci("5"), Deci("5"), Deci("7"), Deci("9"))
        val result = values.standardDeviation(isPopulation = false)
        assertNotNull(result)
        assertEquals(Deci("2.1380899353"), result)
    }

    @Test
    fun `standardDeviation handles identical values`() {
        val result = listOf(Deci("3"), Deci("3"), Deci("3")).standardDeviation()
        assertEquals(Deci("0"), result)
    }

    // ========== Weighted Average Tests ==========

    @Test
    fun `weightedAverage returns null for empty values`() {
        val result = emptyList<Deci>().weightedAverage(emptyList())
        assertNull(result)
    }

    @Test
    fun `weightedAverage returns null when sizes differ`() {
        val values = listOf(Deci("1"), Deci("2"))
        val weights = listOf(Deci("1"))
        val result = values.weightedAverage(weights)
        assertNull(result)
    }

    @Test
    fun `weightedAverage returns null for zero total weight`() {
        val values = listOf(Deci("1"), Deci("2"))
        val weights = listOf(Deci("0"), Deci("0"))
        val result = values.weightedAverage(weights)
        assertNull(result)
    }

    @Test
    fun `weightedAverage calculates correctly with equal weights`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        val weights = listOf(Deci("1"), Deci("1"), Deci("1"))
        val result = values.weightedAverage(weights)
        assertEquals(Deci("2"), result)
    }

    @Test
    fun `weightedAverage calculates correctly with different weights`() {
        val values = listOf(Deci("70"), Deci("80"), Deci("90"))
        val weights = listOf(Deci("0.2"), Deci("0.3"), Deci("0.5"))
        val result = values.weightedAverage(weights)
        assertEquals(Deci("83"), result)
    }

    @Test
    fun `weightedAverage handles single value`() {
        val values = listOf(Deci("42"))
        val weights = listOf(Deci("5"))
        val result = values.weightedAverage(weights)
        assertEquals(Deci("42"), result)
    }

    // ========== Harmonic Mean Tests ==========

    @Test
    fun `harmonicMean returns null for empty collection`() {
        val result = emptyList<Deci>().harmonicMean()
        assertNull(result)
    }

    @Test
    fun `harmonicMean returns null for collection with zero`() {
        val result = listOf(Deci("1"), Deci("0"), Deci("3")).harmonicMean()
        assertNull(result)
    }

    @Test
    fun `harmonicMean returns null for collection with negative values`() {
        val result = listOf(Deci("1"), Deci("-2"), Deci("3")).harmonicMean()
        assertNull(result)
    }

    @Test
    fun `harmonicMean calculates correctly for single value`() {
        val result = listOf(Deci("5")).harmonicMean()
        assertEquals(Deci("5"), result)
    }

    @Test
    fun `harmonicMean calculates correctly`() {
        val result = listOf(Deci("1"), Deci("2"), Deci("4")).harmonicMean()
        assertNotNull(result)
        // Harmonic mean of 1, 2, 4 = 3 / (1/1 + 1/2 + 1/4) = 3 / 1.75 = 1.714285...
        assertEquals(Deci("1.71428571428571428571"), result)
    }

    @Test
    fun `harmonicMean of equal values equals the value`() {
        val result = listOf(Deci("5"), Deci("5"), Deci("5")).harmonicMean()
        assertEquals(Deci("5"), result)
    }

    // ========== CountWhere Tests ==========

    @Test
    fun `countWhere returns zero for empty collection`() {
        val result = emptyList<Deci>().countWhere { it > Deci("0") }
        assertEquals(0, result)
    }

    @Test
    fun `countWhere counts matching elements`() {
        val values = listOf(Deci("1"), Deci("-2"), Deci("3"), Deci("-4"), Deci("5"))
        val result = values.countWhere { it > Deci("0") }
        assertEquals(3, result)
    }

    @Test
    fun `countWhere returns zero when no matches`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        val result = values.countWhere { it > Deci("10") }
        assertEquals(0, result)
    }

    @Test
    fun `countWhere returns all when all match`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        val result = values.countWhere { it > Deci("0") }
        assertEquals(3, result)
    }

    // ========== SumOfSquares Tests ==========

    @Test
    fun `sumOfSquares returns null for empty collection`() {
        val result = emptyList<Deci>().sumOfSquares()
        assertNull(result)
    }

    @Test
    fun `sumOfSquares returns zero for single element`() {
        val result = listOf(Deci("5")).sumOfSquares()
        assertEquals(Deci("0"), result)
    }

    @Test
    fun `sumOfSquares calculates correctly`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        // Mean = 2, deviations: -1, 0, 1, squares: 1, 0, 1, sum = 2
        val result = values.sumOfSquares()
        assertEquals(Deci("2"), result)
    }

    @Test
    fun `sumOfSquares returns zero for identical values`() {
        val result = listOf(Deci("4"), Deci("4"), Deci("4")).sumOfSquares()
        assertEquals(Deci("0"), result)
    }

    @Test
    fun `sumOfSquares handles decimal values`() {
        val values = listOf(Deci("1.5"), Deci("2.5"), Deci("3.5"))
        // Mean = 2.5, deviations: -1, 0, 1, squares: 1, 0, 1, sum = 2
        val result = values.sumOfSquares()
        assertEquals(Deci("2"), result)
    }

    // ========== Edge Cases and Integration Tests ==========

    @Test
    fun `statistics work with large numbers`() {
        val values = listOf(Deci("1000000"), Deci("2000000"), Deci("3000000"))
        assertEquals(Deci("2000000"), values.mean())
        assertEquals(Deci("2000000"), values.median())
        assertEquals(Deci("2000000"), values.range())
    }

    @Test
    fun `statistics work with very small numbers`() {
        val values = listOf(Deci("0.0001"), Deci("0.0002"), Deci("0.0003"))
        assertEquals(Deci("0.0002"), values.mean())
        assertEquals(Deci("0.0002"), values.median())
    }

    @Test
    fun `mean and median differ for skewed distribution`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"), Deci("100"))
        val mean = values.mean()
        val median = values.median()
        assertNotNull(mean)
        assertNotNull(median)
        assertEquals(Deci("26.5"), mean)
        assertEquals(Deci("2.5"), median)
    }
}