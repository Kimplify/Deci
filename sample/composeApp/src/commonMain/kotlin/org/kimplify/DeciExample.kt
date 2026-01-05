package org.kimplify

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciConstants
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.bulk.averageDeci
import org.kimplify.deci.bulk.multiplyAllBy
import org.kimplify.deci.extension.sumDeci
import org.kimplify.deci.formatting.formatAsPercentage
import org.kimplify.deci.formatting.formatCurrency
import org.kimplify.deci.formatting.formatWithThousandsSeparator
import org.kimplify.deci.formatting.toScientificNotation
import org.kimplify.deci.math.roundToNearest
import org.kimplify.deci.math.sqrt
import org.kimplify.deci.statistics.mean
import org.kimplify.deci.statistics.standardDeviation
import org.kimplify.deci.validation.isValidDeci
import org.kimplify.deci.validation.validateForForm

/**
 * Comprehensive examples demonstrating the enhanced Deci library features.
 */
object DeciExample {

    fun demonstrateBasicMath(): String {
        val number = Deci("16")
        val squareRoot = number.sqrt()
        val rounded = Deci("12.7").roundToNearest(Deci("5"))

        return buildString {
            appendLine("=== Basic Math Functions ===")
            appendLine("Square root of 16: $squareRoot")
            appendLine("12.7 rounded to nearest 5: $rounded")
            appendLine("Pi constant: ${DeciConstants.PI}")
            appendLine()
        }
    }


    fun demonstrateStatistics(): String {
        val salesData = listOf(
            Deci("1200"), Deci("1450"), Deci("980"), Deci("1650"), Deci("1320"),
            Deci("1180"), Deci("1520"), Deci("1380"), Deci("1290"), Deci("1410")
        )

        val average = salesData.mean() ?: Deci.Companion.ZERO
        val stdDev = salesData.standardDeviation() ?: Deci.Companion.ZERO
        val total = salesData.sumDeci()

        return buildString {
            appendLine("=== Statistical Analysis ===")
            appendLine("Monthly sales data (10 months)")
            appendLine("Average: ${average.formatCurrency()}")
            appendLine(
                "Standard deviation: ${
                    stdDev.setScale(2, RoundingMode.HALF_UP).formatCurrency()
                }"
            )
            appendLine("Total: ${total.formatCurrency()}")
            appendLine()
        }
    }

    fun demonstrateFormatting(): String {
        val amount = Deci("1234567.89")
        val percentage = Deci("0.15")
        val currency = amount.formatCurrency("€", 2, ".")
        val scientific = amount.toScientificNotation(3)

        return buildString {
            appendLine("=== Formatting Examples ===")
            appendLine("Amount as USD: ${amount.formatCurrency()}")
            appendLine("Amount as EUR: $currency")
            appendLine("Scientific notation: $scientific")
            appendLine("Percentage: ${percentage.formatAsPercentage()}")
            appendLine("With thousands separator: ${amount.formatWithThousandsSeparator()}")
            appendLine()
        }
    }

    fun demonstrateValidation(): String {
        val inputs = listOf("123.45", "abc", "", "1,234.56", "-45.67")

        return buildString {
            appendLine("=== Input Validation ===")
            inputs.forEach { input ->
                val isValid = input.isValidDeci()
                val status = if (isValid) "✓ Valid" else "✗ Invalid"
                appendLine("'$input': $status")
            }

            // Form validation example
            val userInput = Deci("75.50")
            val validation = userInput.validateForForm(
                minValue = Deci("0"),
                maxValue = Deci("100"),
                maxDecimalPlaces = 2,
                mustBePositive = true
            )
            appendLine("Validating $75.50 for form: ${if (validation.isValid) "✓ Valid" else "✗ ${validation.errorMessage}"}")
            appendLine()
        }
    }

    fun demonstrateBulkOperations(): String {
        val prices = listOf(Deci("10.99"), Deci("25.50"), Deci("15.75"), Deci("8.25"))

        val withTax = prices.multiplyAllBy(Deci("1.08")) // Add 8% tax
        val average = prices.averageDeci() ?: Deci.ZERO
        val total = prices.sumDeci()

        return buildString {
            appendLine("=== Bulk Operations ===")
            appendLine("Original prices: ${prices.joinToString(", ") { it.formatCurrency() }}")
            appendLine("With 8% tax: ${withTax.joinToString(", ") { it.formatCurrency() }}")
            appendLine("Average price: ${average.formatCurrency()}")
            appendLine("Total: ${total.formatCurrency()}")
            appendLine()
        }
    }

    fun runAllExamples(): String {
        return buildString {
            appendLine("🧮 DECI LIBRARY FEATURE DEMONSTRATION")
            appendLine("=====================================")
            appendLine()
            append(demonstrateBasicMath())
            append(demonstrateStatistics())
            append(demonstrateFormatting())
            append(demonstrateValidation())
            append(demonstrateBulkOperations())
            appendLine("=====================================")
            appendLine("✨ All features working correctly! ✨")
        }
    }
}