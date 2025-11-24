package org.kimplify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.kimplify.deci.Deci
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.pow
import org.kimplify.deci.sumDeci

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    MaterialTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = "Deci Library",
                            style = MaterialTheme.typography.displaySmall
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    InteractiveDeciTester()

                    DemoSection(title = "Basic Operations") {
                        val a = Deci("10.5")
                        val b = Deci("2.3")
                        DemoItem("$a + $b = ${a + b}")
                        DemoItem("$a - $b = ${a - b}")
                        DemoItem("$a × $b = ${a * b}")
                        DemoItem("$a ÷ $b = ${a / b}")
                    }

                    DemoSection(title = "Rounding & Scale") {
                        val pi = Deci("3.14159265359")
                        DemoItem("π = $pi")
                        DemoItem("π (2 decimals, HALF_UP) = ${pi.setScale(2, RoundingMode.HALF_UP)}")
                        DemoItem("π (4 decimals, DOWN) = ${pi.setScale(4, RoundingMode.DOWN)}")
                    }

                    DemoSection(title = "Division with Scale") {
                        val oneThird = Deci("1").divide(Deci("3"), 6, RoundingMode.HALF_UP)
                        DemoItem("1 ÷ 3 (6 decimals) = $oneThird")
                    }

                    DemoSection(title = "Comparisons") {
                        val x = Deci("5.5")
                        val y = Deci("3.2")
                        DemoItem("max($x, $y) = ${x.max(y)}")
                        DemoItem("min($x, $y) = ${x.min(y)}")
                        DemoItem("$x > $y: ${x > y}")
                    }

                    DemoSection(title = "Sign Operations") {
                        val negative = Deci("-7.5")
                        DemoItem("abs($negative) = ${negative.abs()}")
                        DemoItem("negate($negative) = ${negative.negate()}")
                        DemoItem("isNegative: ${negative.isNegative()}")
                        DemoItem("isPositive: ${negative.isPositive()}")
                    }

                    DemoSection(title = "Constants") {
                        DemoItem("ZERO = ${Deci.ZERO}")
                        DemoItem("ONE = ${Deci.ONE}")
                        DemoItem("TEN = ${Deci.TEN}")
                    }

                    DemoSection(title = "Power Operations") {
                        val base = Deci("2")
                        DemoItem("$base³ = ${base.pow(3)}")
                        DemoItem("$base⁵ = ${base.pow(5)}")
                    }

                    DemoSection(title = "Sum of List") {
                        val numbers = listOf(Deci("1.5"), Deci("2.3"), Deci("3.7"), Deci("4.1"))
                        DemoItem("Sum of ${numbers.joinToString(" + ")} = ${numbers.sumDeci()}")
                    }

                    DemoSection(title = "Parse with Comma (European Format)") {
                        val euroFormat = Deci("1.230,98")
                        DemoItem("Parsing '1.230,98' → $euroFormat")
                        DemoItem("Comma is automatically converted to dot")
                    }

                    DemoSection(title = "Safe Parsing") {
                        DemoItem("fromStringOrNull(\"invalid\") = ${Deci.fromStringOrNull("invalid")}")
                        DemoItem("fromStringOrZero(\"invalid\") = ${Deci.fromStringOrZero("invalid")}")
                        DemoItem("fromStringOrNull(\"42.5\") = ${Deci.fromStringOrNull("42.5")}")
                    }
                }
            }
        }
    }
}


@Composable
fun DemoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
fun DemoItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 12.dp, top = 6.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveDeciTester() {
    var inputValue by remember { mutableStateOf("") }
    var parsedValue by remember { mutableStateOf<Deci?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFocused by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Try It Yourself",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Enter a decimal value to parse and test",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = inputValue,
                onValueChange = { newValue ->
                    inputValue = newValue
                    errorMessage = null
                    parsedValue = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        val wasFocused = isFocused
                        isFocused = focusState.isFocused

                        if (wasFocused && !isFocused && inputValue.isNotBlank()) {
                            parseAndValidate(inputValue) { deci, error ->
                                parsedValue = deci
                                errorMessage = error
                            }
                        }
                    },
                label = {
                    Text(
                        "Decimal value",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                placeholder = {
                    Text(
                        "e.g., 1.230,98 or 1,234.56",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                singleLine = true,
                isError = errorMessage != null,
                shape = RoundedCornerShape(20.dp),
                supportingText = {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            "Supports European (1.230,98) and US (1,234.56) formats",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        parseAndValidate(inputValue) { deci, error ->
                            parsedValue = deci
                            errorMessage = error
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = inputValue.isNotBlank(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Parse",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = {
                        inputValue = ""
                        parsedValue = null
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f),
                    enabled = inputValue.isNotBlank() || parsedValue != null,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Clear",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            parsedValue?.let {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Parsed Successfully!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    DemoItem("Value: $parsedValue")
                    DemoItem("toString(): ${parsedValue?.toString()}")
                    DemoItem("toDouble(): ${parsedValue?.toDouble()}")
                    DemoItem("isZero(): ${parsedValue?.isZero()}")
                    DemoItem("isNegative(): ${parsedValue?.isNegative()}")
                    DemoItem("isPositive(): ${parsedValue?.isPositive()}")
                }
            }
        }
    }
}

private fun parseAndValidate(
    input: String,
    onResult: (Deci?, String?) -> Unit
) {
    if (input.isBlank()) {
        onResult(null, "Input cannot be empty")
        return
    }

    val deci = Deci.fromStringOrNull(input)
    if (deci == null) {
        onResult(null, "Invalid decimal format. Try: 123.45, 1,234.56, or 1.230,98")
    } else {
        println("Parsed Deci value: $deci")
        onResult(deci, null)
    }
}