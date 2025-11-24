package org.kimplify.deci

import kotlinx.serialization.json.Json
import org.kimplify.deci.config.DeciConfiguration
import org.kimplify.deci.config.DeciDivisionPolicy
import org.kimplify.deci.extension.precision
import org.kimplify.deci.extension.scale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeciTest {

    @Test
    fun `scale helper counts fraction digits`() {
        assertEquals(0, Deci("10").scale())
        assertEquals(2, Deci("10.25").scale())
        assertEquals(3, Deci("-0.125").scale())
    }

    @Test
    fun `precision helper counts digits ignoring separators`() {
        assertEquals(2, Deci("10").precision())
        assertEquals(4, Deci("10.25").precision())
        assertEquals(4, Deci("-0.125").precision())
    }

    @Test
    fun `division policy can be customized and reset`() {
        DeciConfiguration.divisionPolicy = DeciDivisionPolicy(
            fractionalDigits = 2,
            roundingMode = RoundingMode.DOWN
        )

        val result = Deci("1") / Deci("3")
        assertEquals("0.33", result.toString())

        DeciConfiguration.resetDivisionPolicy()
        val defaultResult = Deci("1") / Deci("3")
        assertEquals("0.33333333333333333333", defaultResult.toString())
    }

    @Test
    fun `plus should add two values`() {
        assertEquals(Deci("3"), Deci("1") + Deci("2"))
    }

    @Test
    fun `minus should subtract correctly`() {
        assertEquals(Deci("-1"), Deci("1") - Deci("2"))
    }

    @Test
    fun `times should multiply values`() {
        assertEquals(Deci("6"), Deci("2") * Deci("3"))
    }

    @Test
    fun `div should divide with default scale`() {
        val result = Deci("5") / Deci("2")
        assertEquals("2.5", result.toString())
    }

    @Test
    fun `div should throw on division by zero`() {
        assertFailsWith<ArithmeticException> {
            Deci("1") / Deci("0")
        }
    }

    @Test
    fun `divide with scale and rounding`() {
        val result = Deci("1").divide(Deci("3"), scale = 2, roundingMode = RoundingMode.HALF_UP)
        assertEquals(Deci("0.33"), result)
    }

    @Test
    fun `setScale adjusts decimal places`() {
        assertEquals(Deci("1.20"), Deci("1.2").setScale(2, RoundingMode.DOWN))
    }

    @Test
    fun `abs and negate behave as expected`() {
        assertEquals(Deci("5"), Deci("-5").abs())
        assertEquals(Deci("-5"), Deci("5").negate())
    }

    @Test
    fun `max and min choose correctly`() {
        assertEquals(Deci("10"), Deci("5").max(Deci("10")))
        assertEquals(Deci("5"), Deci("5").min(Deci("10")))
    }

    @Test
    fun `equals and hashCode are consistent`() {
        val a = Deci("2.00")
        val b = Deci("2.0")
        assertTrue(a == b && a.hashCode() == b.hashCode())
    }

    @Test
    fun `invalid string literal throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("")
        }
    }

    @Test
    fun `fromStringOrNull and fromStringOrZero behave correctly`() {
        assertNull(Deci.fromStringOrNull("foo"))
        assertEquals(Deci.ZERO, Deci.fromStringOrZero("foo"))
    }

    @Test
    fun `setScale UP rounds away from zero`() {
        assertEquals(
            Deci("1.24"),
            Deci("1.231").setScale(2, RoundingMode.UP)
        )
        assertEquals(
            Deci("-1.24"),
            Deci("-1.231").setScale(2, RoundingMode.UP)
        )
    }

    @Test
    fun `setScale DOWN truncates toward zero`() {
        assertEquals(
            Deci("1.23"),
            Deci("1.239").setScale(2, RoundingMode.DOWN)
        )
        assertEquals(
            Deci("-1.23"),
            Deci("-1.239").setScale(2, RoundingMode.DOWN)
        )
    }

    @Test
    fun `setScale CEILING and FLOOR behave per infinities`() {
        assertEquals(
            Deci("1.24"),
            Deci("1.231").setScale(2, RoundingMode.CEILING)
        )
        assertEquals(
            Deci("-1.23"),
            Deci("-1.239").setScale(2, RoundingMode.CEILING)
        )
        assertEquals(
            Deci("1.23"),
            Deci("1.239").setScale(2, RoundingMode.FLOOR)
        )
        assertEquals(
            Deci("-1.24"),
            Deci("-1.231").setScale(2, RoundingMode.FLOOR)
        )
    }

    @Test
    fun `setScale HALF_UP rounds ties up`() {
        assertEquals(
            Deci("1.24"),
            Deci("1.235").setScale(2, RoundingMode.HALF_UP)
        )
    }

    @Test
    fun `setScale HALF_DOWN rounds ties down`() {
        assertEquals(
            Deci("1.23"),
            Deci("1.235").setScale(2, RoundingMode.HALF_DOWN)
        )
    }

    @Test
    fun `setScale HALF_EVEN does banker's rounding`() {
        assertEquals(
            Deci("1.22"),
            Deci("1.225").setScale(2, RoundingMode.HALF_EVEN)
        )
        assertEquals(
            Deci("1.24"),
            Deci("1.235").setScale(2, RoundingMode.HALF_EVEN)
        )
    }

    @Test
    fun `divide scale 0 HALF_UP at 5 boundary`() {
        assertEquals(
            Deci("1"),
            Deci("1").divide(Deci("2"), scale = 0, roundingMode = RoundingMode.HALF_UP)
        )
    }

    @Test
    fun `divide scale 0 HALF_DOWN at 5 boundary`() {
        assertEquals(
            Deci("0"),
            Deci("1").divide(Deci("2"), scale = 0, roundingMode = RoundingMode.HALF_DOWN)
        )
    }

    @Test
    fun `operator div throws ArithmeticException on zero divisor`() {
        assertFailsWith<ArithmeticException> {
            Deci("5") / Deci("0")
        }
    }

    @Test
    fun `divide throws ArithmeticException on zero divisor`() {
        assertFailsWith<ArithmeticException> {
            Deci("5").divide(Deci("0"), scale = 2, roundingMode = RoundingMode.HALF_UP)
        }
    }

    @Test
    fun `divide negative scale throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("1").divide(Deci("2"), scale = -1, roundingMode = RoundingMode.UP)
        }
    }

    @Test
    fun `setScale negative scale throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            Deci("1.23").setScale(-1, RoundingMode.DOWN)
        }
    }

    @Test
    fun `constructor replaces comma with dot`() {
        assertEquals(
            Deci("1.23"),
            Deci("1,23")
        )
    }

    @Test
    fun `kotlinx json serialization roundtrip`() {
        val json = Json { encodeDefaults = true }
        val original = Deci("123.45")
        val text = json.encodeToString(Deci.serializer(), original)
        val parsed = json.decodeFromString(Deci.serializer(), text)
        assertEquals(original, parsed)
    }

    @Test fun `constants ZERO ONE TEN behave`() {
        assertEquals(Deci("0"), Deci.ZERO)
        assertEquals(Deci("1"), Deci.ONE)
        assertEquals(Deci("10"), Deci.TEN)
    }

    @Test fun `toDouble and toString roundtrip for simple values`() {
        listOf("0", "1.5", "-2.75").forEach { s ->
            val d = Deci(s)
            assertEquals(s, d.toString())
            assertEquals(s.toDouble(), d.toDouble())
        }
    }

    @Test fun `compareTo must satisfy comparator contract`() {
        val list = listOf(Deci("3"), Deci("1"), Deci("2"))
        val sorted = list.sorted()
        assertEquals(listOf("1", "2", "3"), sorted.map { it.toString() })
    }

    @Test fun `equals is reflexive symmetric transitive and hashCode consistent`() {
        val a = Deci("2.000")
        val b = Deci("2.0")
        val c = Deci("2")
        assertTrue(a == b && b == c && a == c)
        assertTrue(a.hashCode() == b.hashCode() && b.hashCode() == c.hashCode())
    }

    @Test fun `negative zero is equal to zero`() {
        assertEquals(Deci("0"), Deci("-0.000"))
        assertEquals(Deci.ZERO, Deci("-0"))
    }

    @Test fun `trailing zeros are preserved only when setScale`() {
        assertEquals("1.23",   Deci("1.2300").toString())
    }

    @Test fun `invalid string formats throw`() {
        listOf("", "foo", "1.2.3", ".", "-").forEach { bad ->
            assertFailsWith<IllegalArgumentException> { Deci(bad) }
        }
    }

    @Test fun `divide by zero always throws`() {
        assertFailsWith<ArithmeticException> { Deci("1").divide(Deci("0"), 2, RoundingMode.UP) }
        assertFailsWith<ArithmeticException> { Deci("5") / Deci("0") }
    }

    @Test fun `negative scale in divide and setScale throws`() {
        assertFailsWith<IllegalArgumentException> { Deci("1").divide(Deci("2"), -1, RoundingMode.UP) }
        assertFailsWith<IllegalArgumentException> { Deci("1.23").setScale(-5, RoundingMode.DOWN) }
    }
}

