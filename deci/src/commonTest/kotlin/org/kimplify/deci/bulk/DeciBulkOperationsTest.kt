package org.kimplify.deci.bulk

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.exception.DeciDivisionByZeroException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeciBulkOperationsTest {
    // --- multiplyAll ---

    @Test
    fun `multiplyAll returns ONE for empty collection`() {
        val result = emptyList<Deci>().multiplyAll()
        assertEquals(Deci.ONE, result)
    }

    @Test
    fun `multiplyAll returns element for single-element collection`() {
        val result = listOf(Deci("5")).multiplyAll()
        assertEquals(Deci("5"), result)
    }

    @Test
    fun `multiplyAll multiplies all values together`() {
        val result = listOf(Deci("2"), Deci("3"), Deci("4")).multiplyAll()
        assertEquals(Deci("24"), result)
    }

    @Test
    fun `multiplyAll with zero returns zero`() {
        val result = listOf(Deci("10"), Deci("0"), Deci("5")).multiplyAll()
        assertEquals(Deci("0"), result)
    }

    @Test
    fun `multiplyAll handles negative values`() {
        val result = listOf(Deci("-2"), Deci("3")).multiplyAll()
        assertEquals(Deci("-6"), result)
    }

    @Test
    fun `multiplyAll handles decimal values`() {
        val result = listOf(Deci("1.5"), Deci("2")).multiplyAll()
        assertEquals(Deci("3.0"), result)
    }

    @Test
    fun `multiplyAll with two negatives returns positive`() {
        val result = listOf(Deci("-2"), Deci("-3")).multiplyAll()
        assertEquals(Deci("6"), result)
    }

    // --- averageDeci ---

    @Test
    fun `averageDeci returns null for empty collection`() {
        val result = emptyList<Deci>().averageDeci()
        assertNull(result)
    }

    @Test
    fun `averageDeci returns value for single element`() {
        val result = listOf(Deci("10")).averageDeci()
        assertEquals(Deci("10"), result)
    }

    @Test
    fun `averageDeci calculates average correctly`() {
        val result = listOf(Deci("1"), Deci("2"), Deci("3")).averageDeci()
        assertEquals(Deci("2"), result)
    }

    @Test
    fun `averageDeci handles decimal values`() {
        val result = listOf(Deci("1.5"), Deci("2.5")).averageDeci()
        assertEquals(Deci("2"), result)
    }

    @Test
    fun `averageDeci handles negative values`() {
        val result = listOf(Deci("-10"), Deci("10")).averageDeci()
        assertEquals(Deci("0"), result)
    }

    @Test
    fun `averageDeci uses provided context for division`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.DOWN)
        val result = listOf(Deci("1"), Deci("2"), Deci("3")).averageDeci(context)
        assertEquals(Deci("2"), result)
    }

    @Test
    fun `averageDeci with non-terminating result uses context rounding`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("1"), Deci("1"), Deci("2")).averageDeci(context)
        assertEquals(Deci("1.33"), result)
    }

    // --- applyToAll ---

    @Test
    fun `applyToAll returns empty list for empty collection`() {
        val result = emptyList<Deci>().applyToAll { it * Deci("2") }
        assertTrue(result.isEmpty())
    }

    @Test
    fun `applyToAll applies operation to each element`() {
        val result = listOf(Deci("1"), Deci("2"), Deci("3")).applyToAll { it * Deci("2") }
        assertEquals(listOf(Deci("2"), Deci("4"), Deci("6")), result)
    }

    @Test
    fun `applyToAll applies negate operation`() {
        val result = listOf(Deci("1"), Deci("-2")).applyToAll { it.negate() }
        assertEquals(listOf(Deci("-1"), Deci("2")), result)
    }

    @Test
    fun `applyToAll applies abs operation`() {
        val result = listOf(Deci("-1"), Deci("2"), Deci("-3")).applyToAll { it.abs() }
        assertEquals(listOf(Deci("1"), Deci("2"), Deci("3")), result)
    }

    // --- addToAll ---

    @Test
    fun `addToAll returns empty list for empty collection`() {
        val result = emptyList<Deci>().addToAll(Deci("5"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `addToAll adds value to each element`() {
        val result = listOf(Deci("1"), Deci("2"), Deci("3")).addToAll(Deci("10"))
        assertEquals(listOf(Deci("11"), Deci("12"), Deci("13")), result)
    }

    @Test
    fun `addToAll with zero does not change values`() {
        val values = listOf(Deci("1"), Deci("2"))
        val result = values.addToAll(Deci.ZERO)
        assertEquals(listOf(Deci("1"), Deci("2")), result)
    }

    @Test
    fun `addToAll with negative value subtracts`() {
        val result = listOf(Deci("10"), Deci("20")).addToAll(Deci("-5"))
        assertEquals(listOf(Deci("5"), Deci("15")), result)
    }

    @Test
    fun `addToAll handles decimal values`() {
        val result = listOf(Deci("1.5"), Deci("2.5")).addToAll(Deci("0.25"))
        assertEquals(listOf(Deci("1.75"), Deci("2.75")), result)
    }

    // --- subtractFromAll ---

    @Test
    fun `subtractFromAll returns empty list for empty collection`() {
        val result = emptyList<Deci>().subtractFromAll(Deci("5"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `subtractFromAll subtracts value from each element`() {
        val result = listOf(Deci("10"), Deci("20"), Deci("30")).subtractFromAll(Deci("5"))
        assertEquals(listOf(Deci("5"), Deci("15"), Deci("25")), result)
    }

    @Test
    fun `subtractFromAll with zero does not change values`() {
        val values = listOf(Deci("1"), Deci("2"))
        val result = values.subtractFromAll(Deci.ZERO)
        assertEquals(listOf(Deci("1"), Deci("2")), result)
    }

    @Test
    fun `subtractFromAll can produce negative results`() {
        val result = listOf(Deci("1"), Deci("2")).subtractFromAll(Deci("5"))
        assertEquals(listOf(Deci("-4"), Deci("-3")), result)
    }

    @Test
    fun `subtractFromAll handles decimal values`() {
        val result = listOf(Deci("1.75"), Deci("2.50")).subtractFromAll(Deci("0.25"))
        assertEquals(listOf(Deci("1.50"), Deci("2.25")), result)
    }

    // --- multiplyAllBy ---

    @Test
    fun `multiplyAllBy returns empty list for empty collection`() {
        val result = emptyList<Deci>().multiplyAllBy(Deci("2"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `multiplyAllBy multiplies each element by multiplier`() {
        val result = listOf(Deci("1"), Deci("2"), Deci("3")).multiplyAllBy(Deci("3"))
        assertEquals(listOf(Deci("3"), Deci("6"), Deci("9")), result)
    }

    @Test
    fun `multiplyAllBy with one does not change values`() {
        val values = listOf(Deci("5"), Deci("10"))
        val result = values.multiplyAllBy(Deci.ONE)
        assertEquals(listOf(Deci("5"), Deci("10")), result)
    }

    @Test
    fun `multiplyAllBy with zero returns all zeros`() {
        val result = listOf(Deci("5"), Deci("10")).multiplyAllBy(Deci.ZERO)
        assertEquals(listOf(Deci("0"), Deci("0")), result)
    }

    @Test
    fun `multiplyAllBy with negative multiplier negates values`() {
        val result = listOf(Deci("2"), Deci("-3")).multiplyAllBy(Deci("-1"))
        assertEquals(listOf(Deci("-2"), Deci("3")), result)
    }

    @Test
    fun `multiplyAllBy handles decimal multiplier`() {
        val result = listOf(Deci("10"), Deci("20")).multiplyAllBy(Deci("0.5"))
        assertEquals(listOf(Deci("5.0"), Deci("10.0")), result)
    }

    // --- divideAllBy ---

    @Test
    fun `divideAllBy returns empty list for empty collection`() {
        val result = emptyList<Deci>().divideAllBy(Deci("2"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `divideAllBy divides each element by divisor`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("10"), Deci("20"), Deci("30")).divideAllBy(Deci("5"), context)
        assertEquals(listOf(Deci("2"), Deci("4"), Deci("6")), result)
    }

    @Test
    fun `divideAllBy throws on zero divisor`() {
        assertFailsWith<DeciDivisionByZeroException> {
            listOf(Deci("10")).divideAllBy(Deci.ZERO)
        }
    }

    @Test
    fun `divideAllBy uses provided context for rounding`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.DOWN)
        val result = listOf(Deci("10")).divideAllBy(Deci("3"), context)
        assertEquals(listOf(Deci("3.33")), result)
    }

    @Test
    fun `divideAllBy uses default context when not specified`() {
        val result = listOf(Deci("10")).divideAllBy(Deci("3"))
        assertTrue(result[0].toPlainString().length > 5)
    }

    @Test
    fun `divideAllBy handles negative divisor`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("10"), Deci("-20")).divideAllBy(Deci("-5"), context)
        assertEquals(listOf(Deci("-2"), Deci("4")), result)
    }

    // --- applyPercentageChange ---

    @Test
    fun `applyPercentageChange returns empty list for empty collection`() {
        val result = emptyList<Deci>().applyPercentageChange(Deci("10"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `applyPercentageChange applies positive percentage`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("100"), Deci("200")).applyPercentageChange(Deci("10"), context)
        assertEquals(listOf(Deci("110.00"), Deci("220.00")), result)
    }

    @Test
    fun `applyPercentageChange applies negative percentage`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("100")).applyPercentageChange(Deci("-20"), context)
        assertEquals(listOf(Deci("80.00")), result)
    }

    @Test
    fun `applyPercentageChange with zero does not change values`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("100")).applyPercentageChange(Deci("0"), context)
        assertEquals(listOf(Deci("100")), result)
    }

    @Test
    fun `applyPercentageChange applies 100 percent increase`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("50")).applyPercentageChange(Deci("100"), context)
        assertEquals(listOf(Deci("100.00")), result)
    }

    @Test
    fun `applyPercentageChange uses provided context`() {
        val context = DeciContext(precision = 4, roundingMode = RoundingMode.DOWN)
        val result = listOf(Deci("100")).applyPercentageChange(Deci("33"), context)
        assertEquals(listOf(Deci("133.0000")), result)
    }

    // --- normalize ---

    @Test
    fun `normalize returns empty list for empty collection`() {
        val result = emptyList<Deci>().normalize()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `normalize returns original values when all values are equal`() {
        val values = listOf(Deci("5"), Deci("5"), Deci("5"))
        val result = values.normalize()
        assertEquals(values, result)
    }

    @Test
    fun `normalize maps min to zero and max to one`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("0"), Deci("50"), Deci("100")).normalize(context)
        assertEquals(Deci("0"), result[0])
        assertEquals(Deci("0.50"), result[1])
        assertEquals(Deci("1"), result[2])
    }

    @Test
    fun `normalize handles negative values`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("-10"), Deci("0"), Deci("10")).normalize(context)
        assertEquals(Deci("0"), result[0])
        assertEquals(Deci("0.50"), result[1])
        assertEquals(Deci("1"), result[2])
    }

    @Test
    fun `normalize single element returns original`() {
        val result = listOf(Deci("42")).normalize()
        assertEquals(listOf(Deci("42")), result)
    }

    @Test
    fun `normalize two values maps to zero and one`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("10"), Deci("20")).normalize(context)
        assertEquals(Deci("0"), result[0])
        assertEquals(Deci("1"), result[1])
    }

    // --- scaleToSum ---

    @Test
    fun `scaleToSum returns empty list for empty collection`() {
        val result = emptyList<Deci>().scaleToSum(Deci("100"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `scaleToSum scales values to target sum`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("1"), Deci("1"), Deci("1"), Deci("1")).scaleToSum(Deci("100"), context)
        assertEquals(listOf(Deci("25"), Deci("25"), Deci("25"), Deci("25")), result)
    }

    @Test
    fun `scaleToSum throws on zero sum collection`() {
        assertFailsWith<DeciDivisionByZeroException> {
            listOf(Deci("0"), Deci("0")).scaleToSum(Deci("100"))
        }
    }

    @Test
    fun `scaleToSum preserves proportions`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("1"), Deci("3")).scaleToSum(Deci("100"), context)
        assertEquals(Deci("25"), result[0])
        assertEquals(Deci("75"), result[1])
    }

    @Test
    fun `scaleToSum handles target sum of zero`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("10"), Deci("20")).scaleToSum(Deci("0"), context)
        assertEquals(listOf(Deci("0"), Deci("0")), result)
    }

    @Test
    fun `scaleToSum handles negative target sum`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val result = listOf(Deci("1"), Deci("1")).scaleToSum(Deci("-100"), context)
        assertEquals(listOf(Deci("-50"), Deci("-50")), result)
    }

    // --- roundAll ---

    @Test
    fun `roundAll returns empty list for empty collection`() {
        val result = emptyList<Deci>().roundAll(2, RoundingMode.HALF_UP)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `roundAll rounds to specified scale with HALF_UP`() {
        val result = listOf(Deci("1.555"), Deci("2.444")).roundAll(2, RoundingMode.HALF_UP)
        assertEquals(listOf(Deci("1.56"), Deci("2.44")), result)
    }

    @Test
    fun `roundAll rounds with DOWN mode`() {
        val result = listOf(Deci("1.559"), Deci("2.991")).roundAll(2, RoundingMode.DOWN)
        assertEquals(listOf(Deci("1.55"), Deci("2.99")), result)
    }

    @Test
    fun `roundAll rounds to zero decimal places`() {
        val result = listOf(Deci("1.5"), Deci("2.4")).roundAll(0, RoundingMode.HALF_UP)
        assertEquals(listOf(Deci("2"), Deci("2")), result)
    }

    @Test
    fun `roundAll with HALF_EVEN uses bankers rounding`() {
        val result = listOf(Deci("2.5"), Deci("3.5")).roundAll(0, RoundingMode.HALF_EVEN)
        assertEquals(listOf(Deci("2"), Deci("4")), result)
    }

    @Test
    fun `roundAll with CEILING rounds toward positive infinity`() {
        val result = listOf(Deci("1.11"), Deci("-1.11")).roundAll(1, RoundingMode.CEILING)
        assertEquals(listOf(Deci("1.2"), Deci("-1.1")), result)
    }

    @Test
    fun `roundAll with FLOOR rounds toward negative infinity`() {
        val result = listOf(Deci("1.19"), Deci("-1.11")).roundAll(1, RoundingMode.FLOOR)
        assertEquals(listOf(Deci("1.1"), Deci("-1.2")), result)
    }

    // --- filterInRange ---

    @Test
    fun `filterInRange returns empty list for empty collection`() {
        val result = emptyList<Deci>().filterInRange(Deci("0"), Deci("10"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterInRange filters values within range inclusive`() {
        val values = listOf(Deci("1"), Deci("5"), Deci("10"), Deci("15"))
        val result = values.filterInRange(Deci("5"), Deci("10"))
        assertEquals(listOf(Deci("5"), Deci("10")), result)
    }

    @Test
    fun `filterInRange includes boundary values`() {
        val values = listOf(Deci("0"), Deci("5"), Deci("10"))
        val result = values.filterInRange(Deci("0"), Deci("10"))
        assertEquals(listOf(Deci("0"), Deci("5"), Deci("10")), result)
    }

    @Test
    fun `filterInRange returns empty when no values in range`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        val result = values.filterInRange(Deci("10"), Deci("20"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterInRange throws when min greater than max`() {
        assertFailsWith<IllegalArgumentException> {
            listOf(Deci("5")).filterInRange(Deci("10"), Deci("1"))
        }
    }

    @Test
    fun `filterInRange with equal min and max filters to exact value`() {
        val values = listOf(Deci("1"), Deci("5"), Deci("10"))
        val result = values.filterInRange(Deci("5"), Deci("5"))
        assertEquals(listOf(Deci("5")), result)
    }

    @Test
    fun `filterInRange handles negative range`() {
        val values = listOf(Deci("-10"), Deci("-5"), Deci("0"), Deci("5"))
        val result = values.filterInRange(Deci("-10"), Deci("-5"))
        assertEquals(listOf(Deci("-10"), Deci("-5")), result)
    }

    // --- filterOutliers ---

    @Test
    fun `filterOutliers returns empty for empty collection`() {
        val result = emptyList<Deci>().filterOutliers()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterOutliers returns unchanged for fewer than 4 elements`() {
        val values = listOf(Deci("1"), Deci("100"), Deci("1000"))
        val result = values.filterOutliers()
        assertEquals(values.sorted(), result)
    }

    @Test
    fun `filterOutliers removes extreme outliers`() {
        val values =
            listOf(
                Deci("10"),
                Deci("11"),
                Deci("12"),
                Deci("13"),
                Deci("14"),
                Deci("15"),
                Deci("100"),
            )
        val result = values.filterOutliers()
        assertTrue(Deci("100") !in result)
    }

    @Test
    fun `filterOutliers keeps values within IQR bounds`() {
        val values =
            listOf(
                Deci("1"),
                Deci("2"),
                Deci("3"),
                Deci("4"),
                Deci("5"),
                Deci("6"),
                Deci("7"),
                Deci("8"),
            )
        val result = values.filterOutliers()
        assertEquals(values, result)
    }

    @Test
    fun `filterOutliers returns sorted results`() {
        val values = listOf(Deci("5"), Deci("3"), Deci("1"), Deci("4"), Deci("2"))
        val result = values.filterOutliers()
        assertEquals(result.sorted(), result)
    }

    @Test
    fun `filterOutliers with custom multiplier adjusts sensitivity`() {
        val values =
            listOf(
                Deci("10"),
                Deci("11"),
                Deci("12"),
                Deci("13"),
                Deci("14"),
                Deci("15"),
                Deci("30"),
            )
        val strictResult = values.filterOutliers(Deci("0.5"))
        val lenientResult = values.filterOutliers(Deci("5"))
        assertTrue(strictResult.size <= lenientResult.size)
    }

    @Test
    fun `filterOutliers removes lower outliers too`() {
        val values =
            listOf(
                Deci("-100"),
                Deci("10"),
                Deci("11"),
                Deci("12"),
                Deci("13"),
                Deci("14"),
                Deci("15"),
            )
        val result = values.filterOutliers()
        assertTrue(Deci("-100") !in result)
    }

    // --- groupConsecutiveSimilar ---

    @Test
    fun `groupConsecutiveSimilar returns empty for empty collection`() {
        val result = emptyList<Deci>().groupConsecutiveSimilar(Deci("1"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `groupConsecutiveSimilar groups consecutive similar values`() {
        val values = listOf(Deci("1.0"), Deci("1.1"), Deci("1.2"), Deci("5.0"), Deci("5.1"))
        val result = values.groupConsecutiveSimilar(Deci("0.5"))
        assertEquals(2, result.size)
        assertEquals(listOf(Deci("1.0"), Deci("1.1"), Deci("1.2")), result[0])
        assertEquals(listOf(Deci("5.0"), Deci("5.1")), result[1])
    }

    @Test
    fun `groupConsecutiveSimilar single element returns single group`() {
        val result = listOf(Deci("5")).groupConsecutiveSimilar(Deci("1"))
        assertEquals(1, result.size)
        assertEquals(listOf(Deci("5")), result[0])
    }

    @Test
    fun `groupConsecutiveSimilar with zero tolerance groups only equal values`() {
        val values = listOf(Deci("1"), Deci("1"), Deci("2"), Deci("2"))
        val result = values.groupConsecutiveSimilar(Deci("0"))
        assertEquals(2, result.size)
        assertEquals(listOf(Deci("1"), Deci("1")), result[0])
        assertEquals(listOf(Deci("2"), Deci("2")), result[1])
    }

    @Test
    fun `groupConsecutiveSimilar with large tolerance groups everything`() {
        val values = listOf(Deci("1"), Deci("100"), Deci("200"))
        val result = values.groupConsecutiveSimilar(Deci("1000"))
        assertEquals(1, result.size)
        assertEquals(values, result[0])
    }

    @Test
    fun `groupConsecutiveSimilar handles alternating pattern`() {
        val values = listOf(Deci("1"), Deci("10"), Deci("1"), Deci("10"))
        val result = values.groupConsecutiveSimilar(Deci("1"))
        assertEquals(4, result.size)
    }

    @Test
    fun `groupConsecutiveSimilar handles negative values`() {
        val values = listOf(Deci("-1"), Deci("-0.5"), Deci("5"), Deci("5.2"))
        val result = values.groupConsecutiveSimilar(Deci("1"))
        assertEquals(2, result.size)
        assertEquals(listOf(Deci("-1"), Deci("-0.5")), result[0])
        assertEquals(listOf(Deci("5"), Deci("5.2")), result[1])
    }

    // --- partitionDeci ---

    @Test
    fun `partitionDeci returns empty pair for empty collection`() {
        val (matching, nonMatching) = emptyList<Deci>().partitionDeci { it.isPositive() }
        assertTrue(matching.isEmpty())
        assertTrue(nonMatching.isEmpty())
    }

    @Test
    fun `partitionDeci splits by positive and non-positive`() {
        val values = listOf(Deci("-1"), Deci("0"), Deci("1"), Deci("2"))
        val (positive, nonPositive) = values.partitionDeci { it.isPositive() }
        assertEquals(listOf(Deci("1"), Deci("2")), positive)
        assertEquals(listOf(Deci("-1"), Deci("0")), nonPositive)
    }

    @Test
    fun `partitionDeci all match predicate`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        val (matching, nonMatching) = values.partitionDeci { it.isPositive() }
        assertEquals(values, matching)
        assertTrue(nonMatching.isEmpty())
    }

    @Test
    fun `partitionDeci none match predicate`() {
        val values = listOf(Deci("-1"), Deci("-2"))
        val (matching, nonMatching) = values.partitionDeci { it.isPositive() }
        assertTrue(matching.isEmpty())
        assertEquals(values, nonMatching)
    }

    @Test
    fun `partitionDeci splits by threshold`() {
        val values = listOf(Deci("1"), Deci("5"), Deci("10"), Deci("15"))
        val threshold = Deci("7")
        val (above, belowOrEqual) = values.partitionDeci { it > threshold }
        assertEquals(listOf(Deci("10"), Deci("15")), above)
        assertEquals(listOf(Deci("1"), Deci("5")), belowOrEqual)
    }

    // --- cumulativeSum ---

    @Test
    fun `cumulativeSum returns empty list for empty collection`() {
        val result = emptyList<Deci>().cumulativeSum()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `cumulativeSum calculates running total`() {
        val result = listOf(Deci("1"), Deci("2"), Deci("3")).cumulativeSum()
        assertEquals(listOf(Deci("1"), Deci("3"), Deci("6")), result)
    }

    @Test
    fun `cumulativeSum single element returns same element`() {
        val result = listOf(Deci("42")).cumulativeSum()
        assertEquals(listOf(Deci("42")), result)
    }

    @Test
    fun `cumulativeSum handles negative values`() {
        val result = listOf(Deci("10"), Deci("-3"), Deci("5")).cumulativeSum()
        assertEquals(listOf(Deci("10"), Deci("7"), Deci("12")), result)
    }

    @Test
    fun `cumulativeSum handles all zeros`() {
        val result = listOf(Deci("0"), Deci("0"), Deci("0")).cumulativeSum()
        assertEquals(listOf(Deci("0"), Deci("0"), Deci("0")), result)
    }

    @Test
    fun `cumulativeSum handles decimal values`() {
        val result = listOf(Deci("0.1"), Deci("0.2"), Deci("0.3")).cumulativeSum()
        assertEquals(listOf(Deci("0.1"), Deci("0.3"), Deci("0.6")), result)
    }

    // --- movingAverage ---

    @Test
    fun `movingAverage returns empty when collection smaller than window`() {
        val result = listOf(Deci("1"), Deci("2")).movingAverage(3)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `movingAverage with window size 1 returns original values`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        val result = values.movingAverage(1)
        assertEquals(listOf(Deci("1"), Deci("2"), Deci("3")), result)
    }

    @Test
    fun `movingAverage calculates windowed averages`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val values = listOf(Deci("1"), Deci("2"), Deci("3"), Deci("4"), Deci("5"))
        val result = values.movingAverage(3, context)
        assertEquals(3, result.size)
        assertEquals(Deci("2"), result[0])
        assertEquals(Deci("3"), result[1])
        assertEquals(Deci("4"), result[2])
    }

    @Test
    fun `movingAverage with full window size returns single average`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"))
        val result = values.movingAverage(3)
        assertEquals(1, result.size)
        assertEquals(Deci("2"), result[0])
    }

    @Test
    fun `movingAverage throws on non-positive window size`() {
        assertFailsWith<IllegalArgumentException> {
            listOf(Deci("1")).movingAverage(0)
        }
    }

    @Test
    fun `movingAverage throws on negative window size`() {
        assertFailsWith<IllegalArgumentException> {
            listOf(Deci("1")).movingAverage(-1)
        }
    }

    @Test
    fun `movingAverage uses provided context`() {
        val context = DeciContext(precision = 2, roundingMode = RoundingMode.DOWN)
        val values = listOf(Deci("1"), Deci("1"), Deci("2"))
        val result = values.movingAverage(3, context)
        assertEquals(1, result.size)
        assertEquals(Deci("1.33"), result[0])
    }

    @Test
    fun `movingAverage returns empty for empty collection`() {
        val result = emptyList<Deci>().movingAverage(1)
        assertTrue(result.isEmpty())
    }

    // --- differences ---

    @Test
    fun `differences returns empty for empty collection`() {
        val result = emptyList<Deci>().differences()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `differences returns empty for single element`() {
        val result = listOf(Deci("5")).differences()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `differences calculates consecutive differences`() {
        val result = listOf(Deci("1"), Deci("3"), Deci("6"), Deci("10")).differences()
        assertEquals(listOf(Deci("2"), Deci("3"), Deci("4")), result)
    }

    @Test
    fun `differences with constant values returns zeros`() {
        val result = listOf(Deci("5"), Deci("5"), Deci("5")).differences()
        assertEquals(listOf(Deci("0"), Deci("0")), result)
    }

    @Test
    fun `differences handles decreasing values`() {
        val result = listOf(Deci("10"), Deci("7"), Deci("3")).differences()
        assertEquals(listOf(Deci("-3"), Deci("-4")), result)
    }

    @Test
    fun `differences handles decimal values`() {
        val result = listOf(Deci("1.5"), Deci("2.0"), Deci("2.7")).differences()
        assertEquals(listOf(Deci("0.5"), Deci("0.7")), result)
    }

    @Test
    fun `differences result is one element shorter than input`() {
        val values = listOf(Deci("1"), Deci("2"), Deci("3"), Deci("4"), Deci("5"))
        val result = values.differences()
        assertEquals(values.size - 1, result.size)
    }

    // --- topN ---

    @Test
    fun `topN returns empty for empty collection`() {
        val result = emptyList<Deci>().topN(3)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `topN returns top values in descending order`() {
        val values = listOf(Deci("1"), Deci("5"), Deci("3"), Deci("4"), Deci("2"))
        val result = values.topN(3)
        assertEquals(listOf(Deci("5"), Deci("4"), Deci("3")), result)
    }

    @Test
    fun `topN with n greater than size returns all sorted descending`() {
        val values = listOf(Deci("1"), Deci("2"))
        val result = values.topN(5)
        assertEquals(listOf(Deci("2"), Deci("1")), result)
    }

    @Test
    fun `topN with zero returns empty list`() {
        val result = listOf(Deci("1"), Deci("2")).topN(0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `topN throws on negative n`() {
        assertFailsWith<IllegalArgumentException> {
            listOf(Deci("1")).topN(-1)
        }
    }

    @Test
    fun `topN handles negative values`() {
        val values = listOf(Deci("-10"), Deci("-1"), Deci("0"), Deci("5"))
        val result = values.topN(2)
        assertEquals(listOf(Deci("5"), Deci("0")), result)
    }

    @Test
    fun `topN handles duplicate values`() {
        val values = listOf(Deci("5"), Deci("5"), Deci("3"), Deci("3"))
        val result = values.topN(3)
        assertEquals(listOf(Deci("5"), Deci("5"), Deci("3")), result)
    }

    // --- bottomN ---

    @Test
    fun `bottomN returns empty for empty collection`() {
        val result = emptyList<Deci>().bottomN(3)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `bottomN returns bottom values in ascending order`() {
        val values = listOf(Deci("5"), Deci("1"), Deci("3"), Deci("4"), Deci("2"))
        val result = values.bottomN(3)
        assertEquals(listOf(Deci("1"), Deci("2"), Deci("3")), result)
    }

    @Test
    fun `bottomN with n greater than size returns all sorted ascending`() {
        val values = listOf(Deci("2"), Deci("1"))
        val result = values.bottomN(5)
        assertEquals(listOf(Deci("1"), Deci("2")), result)
    }

    @Test
    fun `bottomN with zero returns empty list`() {
        val result = listOf(Deci("1"), Deci("2")).bottomN(0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `bottomN throws on negative n`() {
        assertFailsWith<IllegalArgumentException> {
            listOf(Deci("1")).bottomN(-1)
        }
    }

    @Test
    fun `bottomN handles negative values`() {
        val values = listOf(Deci("-10"), Deci("-1"), Deci("0"), Deci("5"))
        val result = values.bottomN(2)
        assertEquals(listOf(Deci("-10"), Deci("-1")), result)
    }

    @Test
    fun `bottomN handles duplicate values`() {
        val values = listOf(Deci("1"), Deci("1"), Deci("3"), Deci("5"))
        val result = values.bottomN(3)
        assertEquals(listOf(Deci("1"), Deci("1"), Deci("3")), result)
    }
}
