package org.kimplify.deci

import org.kimplify.deci.parser.normalizeDecimalString
import kotlin.test.Test
import kotlin.test.assertEquals

class NormalizeDecimalStringTest {
    @Test
    fun `normalize decimal string various cases`() {
        val cases =
            listOf(
                "" to "",
                "   " to "0",
                "-" to "-0",
                "-   " to "-0",
                "+" to "0",
                "   +   " to "0",
                "0" to "0",
                "000" to "000",
                "12" to "12",
                "-12" to "-12",
                "+12" to "12",
                "1,234" to "1.234",
                "1.234" to "1.234",
                "1,234.56" to "1234.56",
                "1.234,56" to "1234.56",
                "-1.234,56" to "-1234.56",
                "+1.234,56" to "1234.56",
                ".5" to "0.5",
                ",5" to "0.5",
                "-.5" to "-0.5",
                "-,5" to "-0.5",
                "+.5" to "0.5",
                "1,2,3" to "12.3",
                "1.2.3" to "12.3",
                "1,2.3" to "12.3",
                "1.2,3" to "12.3",
                "  1,234.56  " to "1234.56",
            )

        for ((input, expected) in cases) {
            val actual = input.normalizeDecimalString()
            assertEquals(
                expected,
                actual,
                "Failed for input: '$input'",
            )
        }
    }
}
