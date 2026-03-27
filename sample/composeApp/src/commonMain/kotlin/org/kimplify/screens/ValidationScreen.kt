package org.kimplify.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json
import org.kimplify.components.DemoItem
import org.kimplify.components.DemoSection
import org.kimplify.components.InteractiveCard
import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext
import org.kimplify.deci.DeciSerializer
import org.kimplify.deci.validation.ValidationResult
import org.kimplify.deci.validation.clamp
import org.kimplify.deci.validation.hasValidDecimalPlaces
import org.kimplify.deci.validation.isApproximatelyEqual
import org.kimplify.deci.validation.isEven
import org.kimplify.deci.validation.isInRange
import org.kimplify.deci.validation.isNonNegative
import org.kimplify.deci.validation.isOdd
import org.kimplify.deci.validation.isPositiveStrict
import org.kimplify.deci.validation.isValidCurrencyAmount
import org.kimplify.deci.validation.isValidDeci
import org.kimplify.deci.validation.isValidInterestRate
import org.kimplify.deci.validation.isValidPercentage
import org.kimplify.deci.validation.isValidTaxRate
import org.kimplify.deci.validation.isWhole
import org.kimplify.deci.validation.safeDivide
import org.kimplify.deci.validation.toDeciOrError
import org.kimplify.deci.validation.validateForForm

@Composable
fun ValidationScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        FormValidatorSection()

        StringValidationSection()

        ValueValidationSection()

        FinancialValidationSection()

        ApproximateEqualitySection()

        SerializationSection()
    }
}

@Composable
private fun FormValidatorSection() {
    InteractiveCard(
        title = "Form Validator",
        description = "Test Deci's form validation with custom constraints",
    ) {
        var valueInput by remember { mutableStateOf("75.50") }
        var mustBePositive by remember { mutableStateOf(true) }
        var maxDecPlaces by remember { mutableStateOf("2") }
        var minValueInput by remember { mutableStateOf("0") }
        var maxValueInput by remember { mutableStateOf("100") }

        OutlinedTextField(
            value = valueInput,
            onValueChange = { valueInput = it },
            label = { Text("Value to validate") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Switch(
                checked = mustBePositive,
                onCheckedChange = { mustBePositive = it },
            )
            Text(
                text = "Must be positive",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        OutlinedTextField(
            value = maxDecPlaces,
            onValueChange = { maxDecPlaces = it },
            label = { Text("Max decimal places") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        OutlinedTextField(
            value = minValueInput,
            onValueChange = { minValueInput = it },
            label = { Text("Min value") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        OutlinedTextField(
            value = maxValueInput,
            onValueChange = { maxValueInput = it },
            label = { Text("Max value") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        val value = Deci.fromStringOrNull(valueInput)
        val minVal = Deci.fromStringOrNull(minValueInput)
        val maxVal = Deci.fromStringOrNull(maxValueInput)
        val maxDec = maxDecPlaces.toIntOrNull()

        if (value != null) {
            val result = value.validateForForm(
                minValue = minVal,
                maxValue = maxVal,
                maxDecimalPlaces = maxDec,
                mustBePositive = mustBePositive,
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (result.isValid) {
                        Text(
                            text = "Valid",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Text(
                            text = result.errorMessage ?: "Invalid",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        } else {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Enter a valid decimal value to validate.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun StringValidationSection() {
    DemoSection(title = "String Validation") {
        val inputs = listOf("123.45", "abc", "", "1,234.56", "-45.67", "0.001")
        inputs.forEach { input ->
            val valid = input.isValidDeci()
            DemoItem("\"$input\".isValidDeci() = $valid")
        }

        val result = "42.5".toDeciOrError()
        DemoItem("\"42.5\".toDeciOrError() = $result")
        val errorResult = "abc".toDeciOrError()
        DemoItem("\"abc\".toDeciOrError() = $errorResult")
    }
}

@Composable
private fun ValueValidationSection() {
    DemoSection(title = "Value Validation") {
        val value = Deci("42.50")
        DemoItem("$value.isWhole() = ${value.isWhole()}")
        DemoItem("Deci(\"42\").isWhole() = ${Deci("42").isWhole()}")
        DemoItem("Deci(\"42\").isEven() = ${Deci("42").isEven()}")
        DemoItem("Deci(\"43\").isOdd() = ${Deci("43").isOdd()}")
        DemoItem("$value.isInRange(0, 100) = ${value.isInRange(Deci("0"), Deci("100"))}")
        DemoItem("Deci(\"150\").clamp(0, 100) = ${Deci("150").clamp(Deci("0"), Deci("100"))}")
        DemoItem("$value.hasValidDecimalPlaces(2) = ${value.hasValidDecimalPlaces(2)}")
        DemoItem("$value.hasValidDecimalPlaces(1) = ${value.hasValidDecimalPlaces(1)}")
        DemoItem("$value.isPositiveStrict() = ${value.isPositiveStrict()}")
        DemoItem("$value.isNonNegative() = ${value.isNonNegative()}")
    }
}

@Composable
private fun FinancialValidationSection() {
    DemoSection(title = "Financial Validation") {
        val usd = Deci("19.99")
        val btc = Deci("0.00234567")
        val jpy = Deci("1500")

        DemoItem("$usd.isValidCurrencyAmount(\"USD\") = ${usd.isValidCurrencyAmount("USD")}")
        DemoItem("$btc.isValidCurrencyAmount(\"BTC\") = ${btc.isValidCurrencyAmount("BTC")}")
        DemoItem("$jpy.isValidCurrencyAmount(\"JPY\") = ${jpy.isValidCurrencyAmount("JPY")}")
        DemoItem(
            "Deci(\"19.999\").isValidCurrencyAmount(\"USD\") = ${
                Deci("19.999").isValidCurrencyAmount("USD")
            }",
        )

        DemoItem("Deci(\"50\").isValidPercentage() = ${Deci("50").isValidPercentage()}")
        DemoItem("Deci(\"150\").isValidPercentage() = ${Deci("150").isValidPercentage()}")
        DemoItem("Deci(\"0.08\").isValidTaxRate() = ${Deci("0.08").isValidTaxRate()}")
        DemoItem("Deci(\"1.5\").isValidTaxRate() = ${Deci("1.5").isValidTaxRate()}")
        DemoItem("Deci(\"0.05\").isValidInterestRate() = ${Deci("0.05").isValidInterestRate()}")
    }
}

@Composable
private fun ApproximateEqualitySection() {
    DemoSection(title = "Approximate Equality & Safe Division") {
        val a = Deci("1.0000001")
        val b = Deci("1.0000002")
        DemoItem("$a ≈ $b (tolerance 0.000001) = ${a.isApproximatelyEqual(b)}")
        DemoItem(
            "$a ≈ $b (tolerance 0.0000001) = ${
                a.isApproximatelyEqual(b, Deci("0.0000001"))
            }",
        )

        DemoItem("safeDivide(10, 0) = ${Deci("10").safeDivide(Deci.ZERO)}")
        DemoItem("safeDivide(10, 0, default=-1) = ${Deci("10").safeDivide(Deci.ZERO, Deci("-1"))}")
        DemoItem("safeDivide(10, 3) = ${Deci("10").safeDivide(Deci("3"))}")
    }
}

@Composable
private fun SerializationSection() {
    DemoSection(title = "Serialization") {
        val original = Deci("1.50")
        val json = Json.encodeToString(DeciSerializer, original)
        val deserialized = Json.decodeFromString(DeciSerializer, json)
        DemoItem("Original: $original")
        DemoItem("Serialized (JSON string): $json")
        DemoItem("Deserialized: $deserialized")
        DemoItem(
            "Trailing zeros preserved: ${
                original.toString() == deserialized.toString()
            }",
        )

        val another = Deci("100.00")
        val json2 = Json.encodeToString(DeciSerializer, another)
        DemoItem("$another → $json2 (string, not number)")
    }
}
