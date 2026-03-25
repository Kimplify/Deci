package org.kimplify.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.kimplify.components.DemoItem
import org.kimplify.components.DemoSection
import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.bulk.addToAll
import org.kimplify.deci.bulk.bottomN
import org.kimplify.deci.bulk.cumulativeSum
import org.kimplify.deci.bulk.differences
import org.kimplify.deci.bulk.filterInRange
import org.kimplify.deci.bulk.movingAverage
import org.kimplify.deci.bulk.multiplyAllBy
import org.kimplify.deci.bulk.roundAll
import org.kimplify.deci.bulk.scaleToSum
import org.kimplify.deci.bulk.topN
import org.kimplify.deci.extension.sumDeci
import org.kimplify.deci.formatting.format
import org.kimplify.deci.formatting.formatAsPercentage
import org.kimplify.deci.formatting.formatCurrency
import org.kimplify.deci.formatting.formatWithThousandsSeparator
import org.kimplify.deci.formatting.pad
import org.kimplify.deci.formatting.toScientificNotation
import org.kimplify.deci.formatting.toWords
import org.kimplify.deci.statistics.harmonicMean
import org.kimplify.deci.statistics.maxDeci
import org.kimplify.deci.statistics.mean
import org.kimplify.deci.statistics.median
import org.kimplify.deci.statistics.minDeci
import org.kimplify.deci.statistics.range
import org.kimplify.deci.statistics.standardDeviation
import org.kimplify.deci.statistics.sumOfSquares
import org.kimplify.deci.statistics.variance
import org.kimplify.deci.statistics.weightedAverage

@Composable
fun FormatStatsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        CurrencyFormattingSection()
        NumberFormattingSection()
        StatisticsSection()
        BulkOperationsSection()
    }
}

@Composable
private fun CurrencyFormattingSection() {
    DemoSection("Currency Formatting") {
        val amount = Deci("1234567.89")
        DemoItem("formatCurrency() = ${amount.formatCurrency()}")
        DemoItem("formatCurrency(\"€\", 2, \".\") = ${amount.formatCurrency("€", 2, ".")}")
        DemoItem("formatCurrency(\"¥\", 0) = ${amount.formatCurrency("¥", 0)}")
    }
}

@Composable
private fun NumberFormattingSection() {
    DemoSection("Number Formatting") {
        val amount = Deci("1234567.89")
        val rate = Deci("0.15")
        DemoItem("formatAsPercentage(): ${rate.formatAsPercentage()}")
        DemoItem("formatWithThousandsSeparator(): ${amount.formatWithThousandsSeparator()}")
        DemoItem("toScientificNotation(3): ${amount.toScientificNotation(3)}")
        DemoItem("toWords(): ${Deci("42").toWords()}")
        DemoItem("toWords(): ${Deci("1500").toWords()}")
        DemoItem("format(\"0.00\"): ${amount.format("0.00")}")
        DemoItem("format(\"#,##0.00\"): ${amount.format("#,##0.00")}")
        DemoItem("format(\"0.0000\"): ${amount.format("0.0000")}")
        DemoItem("format(\"#,##0\"): ${amount.format("#,##0")}")
        DemoItem("pad(20, '0'): ${amount.pad(20, '0')}")
    }
}

@Composable
private fun StatisticsSection() {
    val sales = listOf(
        Deci("1200"), Deci("1450"), Deci("980"), Deci("1650"), Deci("1320"),
        Deci("1180"), Deci("1520"), Deci("1380"), Deci("1290"), Deci("1410"),
    )
    val ctx = DeciContext.CURRENCY_USD

    DemoSection("Statistics") {
        DemoItem("Dataset: ${sales.joinToString(", ")}")
        DemoItem("mean() = ${sales.mean(ctx)}")
        DemoItem("median() = ${sales.median(ctx)}")
        DemoItem("standardDeviation() = ${sales.standardDeviation(context = ctx)}")
        DemoItem("variance() = ${sales.variance(context = ctx)}")
        DemoItem("range() = ${sales.range()}")
        DemoItem("minDeci() = ${sales.minDeci()}")
        DemoItem("maxDeci() = ${sales.maxDeci()}")
        DemoItem("sumDeci() = ${sales.sumDeci()}")
        DemoItem("sumOfSquares() = ${sales.sumOfSquares(ctx)}")
        DemoItem("harmonicMean() = ${sales.harmonicMean(ctx)}")

        val weights = listOf(
            Deci("1"), Deci("2"), Deci("1"), Deci("3"), Deci("2"),
            Deci("1"), Deci("2"), Deci("1"), Deci("1"), Deci("2"),
        )
        DemoItem("weightedAverage() = ${sales.weightedAverage(weights, ctx)}")
    }
}

@Composable
private fun BulkOperationsSection() {
    val sales = listOf(
        Deci("1200"), Deci("1450"), Deci("980"), Deci("1650"), Deci("1320"),
        Deci("1180"), Deci("1520"), Deci("1380"), Deci("1290"), Deci("1410"),
    )
    val data = listOf(Deci("10"), Deci("20"), Deci("30"), Deci("40"))

    DemoSection("Bulk Operations") {
        DemoItem("Data: ${data.joinToString(", ")}")
        DemoItem("cumulativeSum() = ${data.cumulativeSum().joinToString(", ")}")
        DemoItem("differences() = ${data.differences().joinToString(", ")}")
        DemoItem("movingAverage(3) = ${data.movingAverage(3).joinToString(", ")}")
        DemoItem("addToAll(5) = ${data.addToAll(Deci("5")).joinToString(", ")}")
        DemoItem("multiplyAllBy(2) = ${data.multiplyAllBy(Deci("2")).joinToString(", ")}")
        DemoItem("roundAll(0, HALF_UP) = ${data.roundAll(0, RoundingMode.HALF_UP).joinToString(", ")}")
        DemoItem("topN(2) = ${sales.topN(2).joinToString(", ")}")
        DemoItem("bottomN(2) = ${sales.bottomN(2).joinToString(", ")}")
        DemoItem("filterInRange(1200, 1400) = ${sales.filterInRange(Deci("1200"), Deci("1400")).joinToString(", ")}")

        val portions = listOf(Deci("25"), Deci("35"), Deci("40"))
        val scaled = portions.scaleToSum(Deci("1000"))
        DemoItem("scaleToSum(1000) on [25, 35, 40] = ${scaled.joinToString(", ")}")
    }
}
