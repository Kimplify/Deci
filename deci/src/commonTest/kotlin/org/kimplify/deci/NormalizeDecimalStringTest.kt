package org.kimplify.deci

import kotlin.test.Test
import kotlin.test.assertEquals

class NormalizeDecimalStringTest {

    @Test
    fun `normalize decimal string various cases`() {
        val cases = listOf(
            // Empty & blank
            "" to "",
            "   " to "0",

            // Only sign
            "-" to "-0",
            "-   " to "-0",
            "+" to "0",
            "   +   " to "0",

            // Plain integers
            "0" to "0",
            "000" to "000",
            "12" to "12",
            "-12" to "-12",
            "+12" to "12",

            // Single separator – interpreted as decimal separator
            "1,234" to "1.234",
            "1.234" to "1.234",

            // Both comma and dot – last one is decimal, others are grouping
            "1,234.56" to "1234.56",
            "1.234,56" to "1234.56",
            "-1.234,56" to "-1234.56",
            "+1.234,56" to "1234.56",

            // No integer part
            ".5" to "0.5",
            ",5" to "0.5",
            "-.5" to "-0.5",
            "-,5" to "-0.5",
            "+.5" to "0.5",

            // Multiple separators – only last is decimal
            "1,2,3" to "12.3",
            "1.2.3" to "12.3",
            "1,2.3" to "12.3",
            "1.2,3" to "12.3",

            // Leading & trailing whitespace around valid number
            "  1,234.56  " to "1234.56",
        )

        for ((input, expected) in cases) {
            val actual = input.normalizeDecimalString()
            assertEquals(
                expected,
                actual,
                "Failed for input: '$input'"
            )
        }
    }
}