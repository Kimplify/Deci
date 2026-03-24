package org.kimplify.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.kimplify.components.DemoItem
import org.kimplify.components.DemoSection
import org.kimplify.components.InteractiveCard
import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext
import org.kimplify.deci.RoundingMode

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScaleContextScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Section 1: Scale Explorer
        ScaleExplorerSection()

        // Section 2: DeciContext Presets
        DeciContextPresetsSection()

        // Section 3: Division with Context
        DivisionWithContextSection()
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ScaleExplorerSection() {
    var inputValue by remember { mutableStateOf("3.14159265358979") }
    var scaleValue by remember { mutableStateOf("2") }
    var selectedMode by remember { mutableStateOf(RoundingMode.HALF_UP) }

    val roundingModes = listOf(
        RoundingMode.UP,
        RoundingMode.DOWN,
        RoundingMode.CEILING,
        RoundingMode.FLOOR,
        RoundingMode.HALF_UP,
        RoundingMode.HALF_DOWN,
        RoundingMode.HALF_EVEN,
    )

    InteractiveCard(
        title = "Scale Explorer",
        description = "See how setScale() rounds a value with different rounding modes",
    ) {
        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = { Text("Decimal value") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        OutlinedTextField(
            value = scaleValue,
            onValueChange = { scaleValue = it },
            label = { Text("Scale (decimal places)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            roundingModes.forEach { mode ->
                FilterChip(
                    selected = mode == selectedMode,
                    onClick = { selectedMode = mode },
                    label = { Text(mode.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }

        val parsedValue = Deci.fromStringOrNull(inputValue)
        val parsedScale = scaleValue.toIntOrNull()

        if (parsedValue != null && parsedScale != null) {
            val result = try {
                parsedValue.setScale(parsedScale, selectedMode)
            } catch (e: Exception) {
                null
            }
            if (result != null) {
                Text(
                    text = "Result: $result",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    text = "Error: could not apply setScale with the given parameters",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            Text(
                text = if (parsedValue == null) {
                    "Invalid decimal value"
                } else {
                    "Invalid scale value"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DeciContextPresetsSection() {
    var contextInput by remember { mutableStateOf("1234.5678") }
    var selectedPreset by remember { mutableStateOf("CURRENCY_USD") }

    val presets = mapOf(
        "DEFAULT" to DeciContext.DEFAULT,
        "CURRENCY_USD" to DeciContext.CURRENCY_USD,
        "CURRENCY_EUR" to DeciContext.CURRENCY_EUR,
        "CURRENCY_JPY" to DeciContext.CURRENCY_JPY,
        "CURRENCY_BTC" to DeciContext.CURRENCY_BTC,
        "BANKING" to DeciContext.BANKING,
    )

    InteractiveCard(
        title = "DeciContext Presets",
        description = "Apply predefined contexts for different currencies and use cases",
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            presets.keys.forEach { presetName ->
                FilterChip(
                    selected = presetName == selectedPreset,
                    onClick = { selectedPreset = presetName },
                    label = { Text(presetName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }

        val currentContext = presets[selectedPreset]
        if (currentContext != null) {
            Text(
                text = "Precision: ${currentContext.precision} | Rounding: ${currentContext.roundingMode}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value = contextInput,
            onValueChange = { contextInput = it },
            label = { Text("Decimal value") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        val parsedValue = Deci.fromStringOrNull(contextInput)
        if (parsedValue != null && currentContext != null) {
            val result = try {
                parsedValue.setScale(currentContext.precision, currentContext.roundingMode)
            } catch (e: Exception) {
                null
            }
            if (result != null) {
                Text(
                    text = "Result: $result",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    text = "Error: could not apply context to the given value",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else if (parsedValue == null) {
            Text(
                text = "Invalid decimal value",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }

        val gbpContext = DeciContext.forCurrency("GBP")
        DemoItem(
            text = "DeciContext.forCurrency(\"GBP\") -> precision=${gbpContext.precision}, rounding=${gbpContext.roundingMode}",
        )
    }
}

@Composable
private fun DivisionWithContextSection() {
    DemoSection(title = "Division with Context") {
        val one = Deci("1")
        val three = Deci("3")
        DemoItem("1 \u00F7 3 (USD) = ${one.divide(three, DeciContext.CURRENCY_USD)}")
        DemoItem("1 \u00F7 3 (BTC) = ${one.divide(three, DeciContext.CURRENCY_BTC)}")
        DemoItem("1 \u00F7 3 (JPY) = ${one.divide(three, DeciContext.CURRENCY_JPY)}")
        DemoItem("1 \u00F7 3 (BANKING) = ${one.divide(three, DeciContext.BANKING)}")
        DemoItem("1 \u00F7 3 (DEFAULT) = ${one.divide(three, DeciContext.DEFAULT)}")
    }
}
