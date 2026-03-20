package org.kimplify.deci

import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Property-based tests verifying algebraic laws of Deci arithmetic.
 *
 * Uses kotest-property generators that construct Deci from String only,
 * never from Double, to avoid floating-point imprecision per CLAUDE.md.
 */
class DeciPropertyTest {
    private val config = PropTestConfig(seed = 12345L, iterations = 500)

    /**
     * Generates arbitrary [Deci] values from string representations.
     * Integer part in [-999_999, 999_999], fractional digits in [0, 6].
     */
    private fun arbDeci(): Arb<Deci> =
        arbitrary { rs ->
            val intPart = Arb.int(-999_999..999_999).bind()
            val scale = Arb.int(0..6).bind()
            if (scale == 0) {
                Deci(intPart.toString())
            } else {
                val maxFrac = pow10(scale) - 1
                val fracInt = Arb.int(0..maxFrac).bind()
                val fracStr = fracInt.toString().padStart(scale, '0')
                Deci("$intPart.$fracStr")
            }
        }

    private fun arbNonZeroDeci(): Arb<Deci> =
        arbitrary { rs ->
            var d: Deci
            do {
                d = arbDeci().bind()
            } while (d.isZero())
            d
        }

    /** Simple integer power of 10 for small exponents. */
    private fun pow10(n: Int): Int {
        var result = 1
        repeat(n) { result *= 10 }
        return result
    }

    @Test
    fun `addition is commutative`() =
        runTest {
            checkAll(config, arbDeci(), arbDeci()) { a, b ->
                assertEquals(a + b, b + a, "a=$a, b=$b")
            }
        }

    @Test
    fun `addition is associative`() =
        runTest {
            checkAll(config, arbDeci(), arbDeci(), arbDeci()) { a, b, c ->
                assertEquals((a + b) + c, a + (b + c), "a=$a, b=$b, c=$c")
            }
        }

    @Test
    fun `additive identity`() =
        runTest {
            checkAll(config, arbDeci()) { a ->
                assertEquals(a, a + Deci.ZERO, "a=$a")
            }
        }

    @Test
    fun `additive inverse`() =
        runTest {
            checkAll(config, arbDeci()) { a ->
                assertEquals(Deci.ZERO, a + (-a), "a=$a")
            }
        }

    @Test
    fun `subtraction equals addition of negation`() =
        runTest {
            checkAll(config, arbDeci(), arbDeci()) { a, b ->
                assertEquals(a - b, a + (-b), "a=$a, b=$b")
            }
        }

    @Test
    fun `multiplication is commutative`() =
        runTest {
            checkAll(config, arbDeci(), arbDeci()) { a, b ->
                assertEquals(a * b, b * a, "a=$a, b=$b")
            }
        }

    @Test
    fun `multiplication is associative`() =
        runTest {
            val smallArb =
                arbitrary { rs ->
                    val intPart = Arb.int(-999..999).bind()
                    val scale = Arb.int(0..3).bind()
                    if (scale == 0) {
                        Deci(intPart.toString())
                    } else {
                        val maxFrac = pow10(scale) - 1
                        val fracInt = Arb.int(0..maxFrac).bind()
                        val fracStr = fracInt.toString().padStart(scale, '0')
                        Deci("$intPart.$fracStr")
                    }
                }
            checkAll(config, smallArb, smallArb, smallArb) { a, b, c ->
                assertEquals((a * b) * c, a * (b * c), "a=$a, b=$b, c=$c")
            }
        }

    @Test
    fun `multiplicative identity`() =
        runTest {
            checkAll(config, arbDeci()) { a ->
                assertEquals(a, a * Deci.ONE, "a=$a")
            }
        }

    @Test
    fun `multiplication distributes over addition`() =
        runTest {
            val smallArb =
                arbitrary { rs ->
                    val intPart = Arb.int(-999..999).bind()
                    val scale = Arb.int(0..3).bind()
                    if (scale == 0) {
                        Deci(intPart.toString())
                    } else {
                        val maxFrac = pow10(scale) - 1
                        val fracInt = Arb.int(0..maxFrac).bind()
                        val fracStr = fracInt.toString().padStart(scale, '0')
                        Deci("$intPart.$fracStr")
                    }
                }
            checkAll(config, smallArb, smallArb, smallArb) { a, b, c ->
                assertEquals(a * (b + c), a * b + a * c, "a=$a, b=$b, c=$c")
            }
        }

    @Test
    fun `double negation returns original`() =
        runTest {
            checkAll(config, arbDeci()) { a ->
                assertEquals(a, -(-a), "a=$a")
            }
        }

    @Test
    fun `absolute value is non-negative`() =
        runTest {
            checkAll(config, arbDeci()) { a ->
                assertTrue(a.abs() >= Deci.ZERO, "a=$a, abs=${a.abs()}")
            }
        }

    @Test
    fun `absolute value is symmetric`() =
        runTest {
            checkAll(config, arbDeci()) { a ->
                assertEquals(a.abs(), (-a).abs(), "a=$a")
            }
        }

    @Test
    fun `comparison is antisymmetric`() =
        runTest {
            checkAll(config, arbDeci(), arbDeci()) { a, b ->
                val cmp = a.compareTo(b)
                when {
                    cmp > 0 -> assertTrue(b < a, "a=$a > b=$b but b !< a")
                    cmp < 0 -> assertTrue(b > a, "a=$a < b=$b but b !> a")
                    else -> assertEquals(0, b.compareTo(a), "a=$a == b=$b but b.compareTo(a) != 0")
                }
            }
        }

    @Test
    fun `comparison is transitive`() =
        runTest {
            checkAll(config, arbDeci(), arbDeci(), arbDeci()) { a, b, c ->
                val sorted = listOf(a, b, c).sorted()
                assertTrue(
                    sorted[0] <= sorted[1] && sorted[1] <= sorted[2],
                    "sorted=$sorted not in order",
                )
            }
        }

    @Test
    fun `compareTo zero implies equality`() =
        runTest {
            checkAll(config, arbDeci()) { a ->
                val copy = Deci(a.toPlainString())
                assertTrue(a.compareTo(copy) == 0, "a=$a not equal to copy")
                assertEquals(a, copy)
            }
        }
}
