package org.kimplify.deci

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Exhaustive, table-driven rounding mode tests for all 7 [RoundingMode] values.
 *
 * Covers positive midpoints, negative midpoints, below-midpoint, above-midpoint,
 * even-digit midpoints, exact values, and zero-boundary cases.
 */
class DeciRoundingModeTest {
    private data class Case(
        val input: String,
        val scale: Int,
        val mode: RoundingMode,
        val expected: String,
    )

    @Test
    fun `setScale rounding table - positive odd-digit midpoint 1_235`() {
        val cases =
            listOf(
                Case("1.235", 2, RoundingMode.UP, "1.24"),
                Case("1.235", 2, RoundingMode.DOWN, "1.23"),
                Case("1.235", 2, RoundingMode.CEILING, "1.24"),
                Case("1.235", 2, RoundingMode.FLOOR, "1.23"),
                Case("1.235", 2, RoundingMode.HALF_UP, "1.24"),
                Case("1.235", 2, RoundingMode.HALF_DOWN, "1.23"),
                Case("1.235", 2, RoundingMode.HALF_EVEN, "1.24"),
            )
        assertAllCases(cases)
    }

    @Test
    fun `setScale rounding table - negative odd-digit midpoint neg1_235`() {
        val cases =
            listOf(
                Case("-1.235", 2, RoundingMode.UP, "-1.24"),
                Case("-1.235", 2, RoundingMode.DOWN, "-1.23"),
                Case("-1.235", 2, RoundingMode.CEILING, "-1.23"),
                Case("-1.235", 2, RoundingMode.FLOOR, "-1.24"),
                Case("-1.235", 2, RoundingMode.HALF_UP, "-1.24"),
                Case("-1.235", 2, RoundingMode.HALF_DOWN, "-1.23"),
                Case("-1.235", 2, RoundingMode.HALF_EVEN, "-1.24"),
            )
        assertAllCases(cases)
    }

    @Test
    fun `setScale rounding table - positive even-digit midpoint 1_225`() {
        val cases =
            listOf(
                Case("1.225", 2, RoundingMode.UP, "1.23"),
                Case("1.225", 2, RoundingMode.DOWN, "1.22"),
                Case("1.225", 2, RoundingMode.CEILING, "1.23"),
                Case("1.225", 2, RoundingMode.FLOOR, "1.22"),
                Case("1.225", 2, RoundingMode.HALF_UP, "1.23"),
                Case("1.225", 2, RoundingMode.HALF_DOWN, "1.22"),
                Case("1.225", 2, RoundingMode.HALF_EVEN, "1.22"),
            )
        assertAllCases(cases)
    }

    @Test
    fun `setScale rounding table - negative even-digit midpoint neg1_225`() {
        val cases =
            listOf(
                Case("-1.225", 2, RoundingMode.UP, "-1.23"),
                Case("-1.225", 2, RoundingMode.DOWN, "-1.22"),
                Case("-1.225", 2, RoundingMode.CEILING, "-1.22"),
                Case("-1.225", 2, RoundingMode.FLOOR, "-1.23"),
                Case("-1.225", 2, RoundingMode.HALF_UP, "-1.23"),
                Case("-1.225", 2, RoundingMode.HALF_DOWN, "-1.22"),
                Case("-1.225", 2, RoundingMode.HALF_EVEN, "-1.22"),
            )
        assertAllCases(cases)
    }

    @Test
    fun `setScale rounding table - positive below midpoint 1_231`() {
        val cases =
            listOf(
                Case("1.231", 2, RoundingMode.UP, "1.24"),
                Case("1.231", 2, RoundingMode.DOWN, "1.23"),
                Case("1.231", 2, RoundingMode.CEILING, "1.24"),
                Case("1.231", 2, RoundingMode.FLOOR, "1.23"),
                Case("1.231", 2, RoundingMode.HALF_UP, "1.23"),
                Case("1.231", 2, RoundingMode.HALF_DOWN, "1.23"),
                Case("1.231", 2, RoundingMode.HALF_EVEN, "1.23"),
            )
        assertAllCases(cases)
    }

    @Test
    fun `setScale rounding table - negative below midpoint neg1_231`() {
        val cases =
            listOf(
                Case("-1.231", 2, RoundingMode.UP, "-1.24"),
                Case("-1.231", 2, RoundingMode.DOWN, "-1.23"),
                Case("-1.231", 2, RoundingMode.CEILING, "-1.23"),
                Case("-1.231", 2, RoundingMode.FLOOR, "-1.24"),
                Case("-1.231", 2, RoundingMode.HALF_UP, "-1.23"),
                Case("-1.231", 2, RoundingMode.HALF_DOWN, "-1.23"),
                Case("-1.231", 2, RoundingMode.HALF_EVEN, "-1.23"),
            )
        assertAllCases(cases)
    }

    @Test
    fun `setScale rounding table - positive above midpoint 1_239`() {
        val cases =
            listOf(
                Case("1.239", 2, RoundingMode.UP, "1.24"),
                Case("1.239", 2, RoundingMode.DOWN, "1.23"),
                Case("1.239", 2, RoundingMode.CEILING, "1.24"),
                Case("1.239", 2, RoundingMode.FLOOR, "1.23"),
                Case("1.239", 2, RoundingMode.HALF_UP, "1.24"),
                Case("1.239", 2, RoundingMode.HALF_DOWN, "1.24"),
                Case("1.239", 2, RoundingMode.HALF_EVEN, "1.24"),
            )
        assertAllCases(cases)
    }

    @Test
    fun `setScale rounding table - negative above midpoint neg1_239`() {
        val cases =
            listOf(
                Case("-1.239", 2, RoundingMode.UP, "-1.24"),
                Case("-1.239", 2, RoundingMode.DOWN, "-1.23"),
                Case("-1.239", 2, RoundingMode.CEILING, "-1.23"),
                Case("-1.239", 2, RoundingMode.FLOOR, "-1.24"),
                Case("-1.239", 2, RoundingMode.HALF_UP, "-1.24"),
                Case("-1.239", 2, RoundingMode.HALF_DOWN, "-1.24"),
                Case("-1.239", 2, RoundingMode.HALF_EVEN, "-1.24"),
            )
        assertAllCases(cases)
    }

    @Test
    fun `setScale rounding table - exact value 1_200`() {
        val cases =
            listOf(
                Case("1.200", 2, RoundingMode.UP, "1.20"),
                Case("1.200", 2, RoundingMode.DOWN, "1.20"),
                Case("1.200", 2, RoundingMode.CEILING, "1.20"),
                Case("1.200", 2, RoundingMode.FLOOR, "1.20"),
                Case("1.200", 2, RoundingMode.HALF_UP, "1.20"),
                Case("1.200", 2, RoundingMode.HALF_DOWN, "1.20"),
                Case("1.200", 2, RoundingMode.HALF_EVEN, "1.20"),
            )
        assertAllCases(cases)
    }

    @Test
    fun `setScale rounding table - zero boundary midpoint 0_005`() {
        val cases =
            listOf(
                Case("0.005", 2, RoundingMode.UP, "0.01"),
                Case("0.005", 2, RoundingMode.DOWN, "0.00"),
                Case("0.005", 2, RoundingMode.CEILING, "0.01"),
                Case("0.005", 2, RoundingMode.FLOOR, "0.00"),
                Case("0.005", 2, RoundingMode.HALF_UP, "0.01"),
                Case("0.005", 2, RoundingMode.HALF_DOWN, "0.00"),
                Case("0.005", 2, RoundingMode.HALF_EVEN, "0.00"),
            )
        assertAllCases(cases)
    }

    @Test
    fun `setScale rounding table - negative zero boundary neg0_005`() {
        val cases =
            listOf(
                Case("-0.005", 2, RoundingMode.UP, "-0.01"),
                Case("-0.005", 2, RoundingMode.DOWN, "0.00"),
                Case("-0.005", 2, RoundingMode.CEILING, "0.00"),
                Case("-0.005", 2, RoundingMode.FLOOR, "-0.01"),
                Case("-0.005", 2, RoundingMode.HALF_UP, "-0.01"),
                Case("-0.005", 2, RoundingMode.HALF_DOWN, "0.00"),
                Case("-0.005", 2, RoundingMode.HALF_EVEN, "0.00"),
            )
        assertAllCases(cases)
    }

    @Test
    fun `divide 1 by 3 scale 2 all modes`() {
        val cases =
            listOf(
                Case("1", 2, RoundingMode.UP, "0.34"),
                Case("1", 2, RoundingMode.DOWN, "0.33"),
                Case("1", 2, RoundingMode.CEILING, "0.34"),
                Case("1", 2, RoundingMode.FLOOR, "0.33"),
                Case("1", 2, RoundingMode.HALF_UP, "0.33"),
                Case("1", 2, RoundingMode.HALF_DOWN, "0.33"),
                Case("1", 2, RoundingMode.HALF_EVEN, "0.33"),
            )
        for (case in cases) {
            val result = Deci(case.input).divide(Deci("3"), case.scale, case.mode)
            assertEquals(
                Deci(case.expected),
                result,
                "divide(${case.input}, 3, ${case.scale}, ${case.mode}): expected ${case.expected}, got $result",
            )
        }
    }

    @Test
    fun `divide 1 by 2 scale 0 all modes - exact 0_5 boundary`() {
        val cases =
            listOf(
                Case("1", 0, RoundingMode.UP, "1"),
                Case("1", 0, RoundingMode.DOWN, "0"),
                Case("1", 0, RoundingMode.CEILING, "1"),
                Case("1", 0, RoundingMode.FLOOR, "0"),
                Case("1", 0, RoundingMode.HALF_UP, "1"),
                Case("1", 0, RoundingMode.HALF_DOWN, "0"),
                Case("1", 0, RoundingMode.HALF_EVEN, "0"),
            )
        for (case in cases) {
            val result = Deci(case.input).divide(Deci("2"), case.scale, case.mode)
            assertEquals(
                Deci(case.expected),
                result,
                "divide(${case.input}, 2, ${case.scale}, ${case.mode}): expected ${case.expected}, got $result",
            )
        }
    }

    @Test
    fun `divide neg1 by 2 scale 0 all modes - negative 0_5 boundary`() {
        val cases =
            listOf(
                Case("-1", 0, RoundingMode.UP, "-1"),
                Case("-1", 0, RoundingMode.DOWN, "0"),
                Case("-1", 0, RoundingMode.CEILING, "0"),
                Case("-1", 0, RoundingMode.FLOOR, "-1"),
                Case("-1", 0, RoundingMode.HALF_UP, "-1"),
                Case("-1", 0, RoundingMode.HALF_DOWN, "0"),
                Case("-1", 0, RoundingMode.HALF_EVEN, "0"),
            )
        for (case in cases) {
            val result = Deci(case.input).divide(Deci("2"), case.scale, case.mode)
            assertEquals(
                Deci(case.expected),
                result,
                "divide(${case.input}, 2, ${case.scale}, ${case.mode}): expected ${case.expected}, got $result",
            )
        }
    }

    @Test
    fun `setScale 0 on 2_5 all modes`() {
        val cases =
            listOf(
                Case("2.5", 0, RoundingMode.UP, "3"),
                Case("2.5", 0, RoundingMode.DOWN, "2"),
                Case("2.5", 0, RoundingMode.CEILING, "3"),
                Case("2.5", 0, RoundingMode.FLOOR, "2"),
                Case("2.5", 0, RoundingMode.HALF_UP, "3"),
                Case("2.5", 0, RoundingMode.HALF_DOWN, "2"),
                Case("2.5", 0, RoundingMode.HALF_EVEN, "2"),
            )
        assertAllCases(cases)
    }

    @Test
    fun `setScale 0 on 3_5 all modes - HALF_EVEN rounds to 4`() {
        val cases =
            listOf(
                Case("3.5", 0, RoundingMode.UP, "4"),
                Case("3.5", 0, RoundingMode.DOWN, "3"),
                Case("3.5", 0, RoundingMode.CEILING, "4"),
                Case("3.5", 0, RoundingMode.FLOOR, "3"),
                Case("3.5", 0, RoundingMode.HALF_UP, "4"),
                Case("3.5", 0, RoundingMode.HALF_DOWN, "3"),
                Case("3.5", 0, RoundingMode.HALF_EVEN, "4"),
            )
        assertAllCases(cases)
    }

    private fun assertAllCases(cases: List<Case>) {
        for (case in cases) {
            val result = Deci(case.input).setScale(case.scale, case.mode)
            assertEquals(
                Deci(case.expected),
                result,
                "setScale(${case.scale}, ${case.mode}) on ${case.input}: expected ${case.expected}, got $result",
            )
        }
    }
}
