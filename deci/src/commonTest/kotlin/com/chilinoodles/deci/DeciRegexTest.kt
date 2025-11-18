package com.chilinoodles.deci

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DeciRegexTest {

    private val validInputs = listOf(
        "-.5", "0", "1.", ".1", ",0", "123", "-456", "0.1", "123.456", "-0.789", "1.000", "-123.00", "0.0001", "9999999999.9999",
        "1,230.98", "1.230,98", "1,234,567.89", "1.234.567,89", "1,000", "1.000", "1,000.5", "1.000,5"
    )

    private val invalidInputs = listOf(
        "", ".", ",", "abc", "--1",
    )

    @Test
    fun validDecimalLiteralsMatchRegex() {
        for (input in validInputs) {
            assertTrue(
                DECIMAL_REGEX.matches(input),
                "Expected '$input' to match DECIMAL_REGEX"
            )
        }
    }

    @Test
    fun invalidDecimalLiteralsDoNotMatchRegex() {
        for (input in invalidInputs) {
            assertFalse(
                DECIMAL_REGEX.matches(input),
                "Expected '$input' not to match DECIMAL_REGEX"
            )
        }
    }
}

