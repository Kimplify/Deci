package org.kimplify.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import org.kimplify.deci.extension.sumDeci
import org.kimplify.deci.financial.allocate
import org.kimplify.deci.financial.allocateByRatios
import org.kimplify.deci.financial.grossMargin
import org.kimplify.deci.financial.markup
import org.kimplify.deci.financial.percentageChangeTo
import org.kimplify.deci.financial.preTax
import org.kimplify.deci.financial.taxAmount
import org.kimplify.deci.financial.withDiscount
import org.kimplify.deci.financial.withTax

@Composable
fun FinancialScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        TaxCalculatorSection()

        DiscountCalculatorSection()

        MoneyAllocationSection()

        AllocationByRatiosSection()

        PercentageAndMarginsSection()
    }
}

@Composable
private fun TaxCalculatorSection() {
    InteractiveCard(
        title = "Tax Calculator",
        description = "Calculate tax amounts with precision",
    ) {
        var amountInput by remember { mutableStateOf("100.00") }
        var taxRateInput by remember { mutableStateOf("0.21") }

        OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Amount") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        OutlinedTextField(
            value = taxRateInput,
            onValueChange = { taxRateInput = it },
            label = { Text("Tax rate (e.g., 0.21 for 21%)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        val amount = Deci.fromStringOrNull(amountInput)
        val taxRate = Deci.fromStringOrNull(taxRateInput)

        if (amount != null && taxRate != null) {
            val taxAmountValue = amount.taxAmount(taxRate)
            val withTaxValue = amount.withTax(taxRate)
            val preTaxValue = amount.withTax(taxRate).preTax(taxRate)

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
                        text = "Results",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    DemoItem("taxAmount(): $taxAmountValue")
                    DemoItem("withTax(): $withTaxValue")
                    DemoItem("preTax() (round-trip): $preTaxValue")
                }
            }
        } else {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Text(
                    text = "Please enter valid numeric values for both fields.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(20.dp),
                )
            }
        }
    }
}

@Composable
private fun DiscountCalculatorSection() {
    InteractiveCard(
        title = "Discount Calculator",
        description = "Apply discounts to prices",
    ) {
        var priceInput by remember { mutableStateOf("250.00") }
        var discountInput by remember { mutableStateOf("0.15") }

        OutlinedTextField(
            value = priceInput,
            onValueChange = { priceInput = it },
            label = { Text("Price") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        OutlinedTextField(
            value = discountInput,
            onValueChange = { discountInput = it },
            label = { Text("Discount rate") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        val price = Deci.fromStringOrNull(priceInput)
        val discountRate = Deci.fromStringOrNull(discountInput)

        if (price != null && discountRate != null) {
            val discountedPrice = price.withDiscount(discountRate)

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
                        text = "Result",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    DemoItem("withDiscount(): $discountedPrice")
                }
            }
        } else {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Text(
                    text = "Please enter valid numeric values for both fields.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(20.dp),
                )
            }
        }
    }
}

@Composable
private fun MoneyAllocationSection() {
    InteractiveCard(
        title = "Money Allocation",
        description = "Split amounts fairly with remainder distribution",
    ) {
        var totalInput by remember { mutableStateOf("100.00") }
        var partsInput by remember { mutableStateOf("3") }

        OutlinedTextField(
            value = totalInput,
            onValueChange = { totalInput = it },
            label = { Text("Total amount") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        OutlinedTextField(
            value = partsInput,
            onValueChange = { partsInput = it },
            label = { Text("Number of parts") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        val total = Deci.fromStringOrNull(totalInput)
        val partsCount = partsInput.toIntOrNull()

        if (total != null && partsCount != null && partsCount > 0) {
            val parts = total.allocate(partsCount)

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
                        text = "Allocation",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    parts.forEachIndexed { i, part ->
                        DemoItem("Part ${i + 1}: $part")
                    }
                    DemoItem("Sum: ${parts.sumDeci()}")
                }
            }
        } else {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Text(
                    text = "Please enter a valid amount and a positive number of parts.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(20.dp),
                )
            }
        }
    }
}

@Composable
private fun AllocationByRatiosSection() {
    DemoSection(title = "Allocation by Ratios") {
        val total = Deci("1000")
        val ratios = listOf(Deci("50"), Deci("30"), Deci("20"))
        val allocated = total.allocateByRatios(ratios)

        DemoItem("\$1000 split by ratios [50, 30, 20]:")
        allocated.forEachIndexed { i, part ->
            DemoItem("  Part ${i + 1}: $part")
        }
        DemoItem("Sum: ${allocated.sumDeci()}")
    }
}

@Composable
private fun PercentageAndMarginsSection() {
    DemoSection(title = "Percentage & Margins") {
        val from = Deci("80")
        val to = Deci("100")
        val change = from.percentageChangeTo(to)
        DemoItem("\$80 -> \$100: ${change.setScale(2, RoundingMode.HALF_UP)}% change")

        val revenue = Deci("500")
        val cost = Deci("300")
        val margin = grossMargin(revenue, cost)
        DemoItem("Gross margin (revenue=$revenue, cost=$cost): ${margin.setScale(2, RoundingMode.HALF_UP)}%")

        val markupVal = markup(revenue, cost)
        DemoItem("Markup (price=$revenue, cost=$cost): ${markupVal.setScale(2, RoundingMode.HALF_UP)}%")
    }
}
