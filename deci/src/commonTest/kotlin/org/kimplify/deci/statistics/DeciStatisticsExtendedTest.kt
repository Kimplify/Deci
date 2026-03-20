package org.kimplify.deci.statistics

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext
import org.kimplify.deci.RoundingMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeciStatisticsExtendedTest {

    // --- mean extended ---

    @Test
    fun `mean returns null for empty collection`() {
        assertNull(emptyList<Deci>().mean())
    }

    @Test
    fun `mean with non-terminating result uses context`() {
        val context = DeciContext(precision = 4, roundingMode = RoundingMode.DOWN)
        val result = listOf(Deci("1"), Deci("1"), Deci("2")).mean(context)
        assertEquals(Deci("1.3333"), result)
    }

    @Test
    fun `mean with all identical values`() {
        val result = listOf(Deci("7"), Deci("7"), Deci("7")).mean()
        assertEquals(Deci("7"), result)
    }

    @Test
    fun `mean with large numbers`() {
        val result = listOf(Deci("1000000"), Deci("2000000"), Deci("3000000")).mean()
        assertEquals(Deci("2000000"), result)
    }

    @Test
    fun `mean with very small numbers`() {
        val result = listOf(Deci("0.001"), Deci("0.002"), Deci("0.003")).mean()
        assertEquals(Deci("0.002"), result)
    }

    // --- median extended ---

    @Test
    fun `median returns null for empty collection`() {
        assertNull(emptyList<Deci>().median())
    }

    @Test
    fun `median of two elements`() {
        val result = listOf(Deci("1"), Deci("3")).median()
        assertEquals(Deci("2"), result)
    }

    @Test
    fun `median with unsorted input`() {
        val result = listOf(Deci("9"), Deci("1"), Deci("5")).median()
        assertEquals(Deci("5"), result)
    }

    @Test
    fun `median with all same values`() {
        val result = listOf(Deci("3"), Deci("3"), Deci("3"), Deci("3")).median()
        assertEquals(Deci("3"), result)
    }

    @Test
    fun `median with explicit context for even-sized collection`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("1"), Deci("2"), Deci("3"), Deci("4")).median(context)
        assertEquals(Deci("2.5"), result)
    }

    // --- minDeci extended ---

    @Test
    fun `minDeci returns null for empty collection`() {
        assertNull(emptyList<Deci>().minDeci())
    }

    @Test
    fun `minDeci with all negative values`() {
        val result = listOf(Deci("-10"), Deci("-5"), Deci("-1")).minDeci()
        assertEquals(Deci("-10"), result)
    }

    @Test
    fun `minDeci with single element`() {
        assertEquals(Deci("42"), listOf(Deci("42")).minDeci())
    }

    @Test
    fun `minDeci with decimal values`() {
        val result = listOf(Deci("1.5"), Deci("0.5"), Deci("2.5")).minDeci()
        assertEquals(Deci("0.5"), result)
    }

    // --- maxDeci extended ---

    @Test
    fun `maxDeci returns null for empty collection`() {
        assertNull(emptyList<Deci>().maxDeci())
    }

    @Test
    fun `maxDeci with all negative values`() {
        val result = listOf(Deci("-10"), Deci("-5"), Deci("-1")).maxDeci()
        assertEquals(Deci("-1"), result)
    }

    @Test
    fun `maxDeci with single element`() {
        assertEquals(Deci("42"), listOf(Deci("42")).maxDeci())
    }

    @Test
    fun `maxDeci with decimal values`() {
        val result = listOf(Deci("1.5"), Deci("0.5"), Deci("2.5")).maxDeci()
        assertEquals(Deci("2.5"), result)
    }

    // --- range extended ---

    @Test
    fun `range returns null for empty collection`() {
        assertNull(emptyList<Deci>().range())
    }

    @Test
    fun `range with single element returns zero`() {
        assertEquals(Deci("0"), listOf(Deci("5")).range())
    }

    @Test
    fun `range with all same values returns zero`() {
        assertEquals(Deci("0"), listOf(Deci("3"), Deci("3"), Deci("3")).range())
    }

    @Test
    fun `range spanning negative to positive`() {
        val result = listOf(Deci("-5"), Deci("5")).range()
        assertEquals(Deci("10"), result)
    }

    @Test
    fun `range with decimal values`() {
        val result = listOf(Deci("1.5"), Deci("4.5")).range()
        assertEquals(Deci("3"), result)
    }

    // --- variance extended ---

    @Test
    fun `variance returns null for empty collection`() {
        assertNull(emptyList<Deci>().variance())
    }

    @Test
    fun `sample variance returns null for single element`() {
        assertNull(listOf(Deci("5")).variance(isPopulation = false))
    }

    @Test
    fun `population variance of single element is zero`() {
        assertEquals(Deci("0"), listOf(Deci("5")).variance(isPopulation = true))
    }

    @Test
    fun `variance of identical values is zero`() {
        assertEquals(Deci("0"), listOf(Deci("5"), Deci("5"), Deci("5")).variance())
    }

    @Test
    fun `population variance is smaller than sample variance`() {
        val values = listOf(Deci("2"), Deci("4"), Deci("6"), Deci("8"))
        val popVariance = values.variance(isPopulation = true)
        val sampleVariance = values.variance(isPopulation = false)
        assertNotNull(popVariance)
        assertNotNull(sampleVariance)
        assertTrue(popVariance < sampleVariance)
    }

    @Test
    fun `variance with explicit context`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        val context = DeciContext(precision = 4, roundingMode = RoundingMode.HALF_UP)
        val result = values.variance(isPopulation = true, context = context)
        assertNotNull(result)
        assertEquals(Deci("0.6667"), result)
    }

    // --- standardDeviation extended ---

    @Test
    fun `standardDeviation returns null for empty collection`() {
        assertNull(emptyList<Deci>().standardDeviation())
    }

    @Test
    fun `sample standardDeviation returns null for single element`() {
        assertNull(listOf(Deci("5")).standardDeviation(isPopulation = false))
    }

    @Test
    fun `population standardDeviation of single element is zero`() {
        assertEquals(Deci("0"), listOf(Deci("5")).standardDeviation(isPopulation = true))
    }

    @Test
    fun `standardDeviation of identical values is zero`() {
        assertEquals(Deci("0"), listOf(Deci("3"), Deci("3"), Deci("3")).standardDeviation())
    }

    // --- weightedAverage extended ---

    @Test
    fun `weightedAverage returns null for empty values`() {
        assertNull(emptyList<Deci>().weightedAverage(emptyList()))
    }

    @Test
    fun `weightedAverage returns null when sizes differ`() {
        assertNull(listOf(Deci("1"), Deci("2")).weightedAverage(listOf(Deci("1"))))
    }

    @Test
    fun `weightedAverage returns null for zero total weight`() {
        assertNull(listOf(Deci("1"), Deci("2")).weightedAverage(listOf(Deci.ZERO, Deci.ZERO)))
    }

    @Test
    fun `weightedAverage returns null for empty weights`() {
        assertNull(listOf(Deci("1")).weightedAverage(emptyList()))
    }

    @Test
    fun `weightedAverage with equal weights equals arithmetic mean`() {
        val values = listOf(Deci("2"), Deci("4"), Deci("6"))
        val weights = listOf(Deci("1"), Deci("1"), Deci("1"))
        assertEquals(Deci("4"), values.weightedAverage(weights))
    }

    @Test
    fun `weightedAverage with single value returns that value`() {
        assertEquals(Deci("42"), listOf(Deci("42")).weightedAverage(listOf(Deci("10"))))
    }

    @Test
    fun `weightedAverage with heavy weight on one value`() {
        val values = listOf(Deci("10"), Deci("20"))
        val weights = listOf(Deci("1"), Deci("9"))
        val result = values.weightedAverage(weights)
        assertEquals(Deci("19"), result)
    }

    // --- harmonicMean extended ---

    @Test
    fun `harmonicMean returns null for empty collection`() {
        assertNull(emptyList<Deci>().harmonicMean())
    }

    @Test
    fun `harmonicMean returns null for collection with zero`() {
        assertNull(listOf(Deci("1"), Deci("0"), Deci("3")).harmonicMean())
    }

    @Test
    fun `harmonicMean returns null for negative values`() {
        assertNull(listOf(Deci("1"), Deci("-2")).harmonicMean())
    }

    @Test
    fun `harmonicMean of single value equals that value`() {
        assertEquals(Deci("5"), listOf(Deci("5")).harmonicMean())
    }

    @Test
    fun `harmonicMean of identical values equals that value`() {
        assertEquals(Deci("4"), listOf(Deci("4"), Deci("4"), Deci("4")).harmonicMean())
    }

    @Test
    fun `harmonicMean is less than or equal to arithmetic mean`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("4"))
        val hm = values.harmonicMean()
        val am = values.mean()
        assertNotNull(hm)
        assertNotNull(am)
        assertTrue(hm <= am)
    }

    @Test
    fun `harmonicMean with explicit context`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("1"), Deci("2"), Deci("4")).harmonicMean(context)
        assertNotNull(result)
        assertEquals(Deci("1.71"), result)
    }

    // --- countWhere extended ---

    @Test
    fun `countWhere returns zero for empty collection`() {
        assertEquals(0, emptyList<Deci>().countWhere { it > Deci.ZERO })
    }

    @Test
    fun `countWhere counts positive values`() {
        val values = listOf(Deci("-2"), Deci("-1"), Deci("0"), Deci("1"), Deci("2"))
        assertEquals(2, values.countWhere { it > Deci.ZERO })
    }

    @Test
    fun `countWhere with no matches returns zero`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        assertEquals(0, values.countWhere { it > Deci("10") })
    }

    @Test
    fun `countWhere with all matches returns size`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        assertEquals(3, values.countWhere { it > Deci.ZERO })
    }

    @Test
    fun `countWhere with zero predicate`() {
        val values = listOf(Deci("0"), Deci("1"), Deci("0"), Deci("2"))
        assertEquals(2, values.countWhere { it.isZero() })
    }

    // --- sumOfSquares extended ---

    @Test
    fun `sumOfSquares returns null for empty collection`() {
        assertNull(emptyList<Deci>().sumOfSquares())
    }

    @Test
    fun `sumOfSquares returns zero for single element`() {
        assertEquals(Deci("0"), listOf(Deci("5")).sumOfSquares())
    }

    @Test
    fun `sumOfSquares returns zero for identical values`() {
        assertEquals(Deci("0"), listOf(Deci("4"), Deci("4"), Deci("4")).sumOfSquares())
    }

    @Test
    fun `sumOfSquares with simple values`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        val result = values.sumOfSquares()
        assertEquals(Deci("2"), result)
    }

    @Test
    fun `sumOfSquares with explicit context`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        val context = DeciContext(precision = 4, roundingMode = RoundingMode.HALF_UP)
        val result = values.sumOfSquares(context)
        assertEquals(Deci("2"), result)
    }

    @Test
    fun `sumOfSquares with negative values`() {
        val values = listOf(Deci("-1"), Deci("0"), Deci("1"))
        val result = values.sumOfSquares()
        assertNotNull(result)
        assertTrue(result > Deci.ZERO)
    }
}
