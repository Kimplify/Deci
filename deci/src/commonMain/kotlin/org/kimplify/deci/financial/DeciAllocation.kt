package org.kimplify.deci.financial

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext
import org.kimplify.deci.RoundingMode
import org.kimplify.deci.extension.sumDeci

/**
 * Splits this amount into [parts] equal portions, distributing any
 * remainder from rounding across the first portions.
 *
 * The sum of the returned list is always exactly equal to `this`.
 *
 * @param parts the number of portions (must be positive).
 * @param context the [DeciContext] controlling scale and rounding.
 * @return a list of [parts] [Deci] values that sum to `this`.
 */
fun Deci.allocate(
    parts: Int,
    context: DeciContext = DeciContext.CURRENCY_USD,
): List<Deci> {
    require(parts > 0) { "Parts must be positive: $parts" }

    val base = this.divide(Deci(parts), context)
    val allocated = base * Deci(parts)
    val remainder = this - allocated

    if (remainder.isZero()) return List(parts) { base }

    val scale = context.precision
    val unit =
        if (scale > 0) {
            Deci.ONE.divide(Deci("1" + "0".repeat(scale)), context)
        } else {
            Deci.ONE
        }

    val remainderUnits =
        if (unit.isZero()) {
            0
        } else {
            val count = remainder.abs().divide(unit, DeciContext(0, RoundingMode.DOWN))
            count.toString().toLong().toInt()
        }

    val adjustment = if (remainder.isPositive()) unit else unit.negate()

    return List(parts) { index ->
        if (index < remainderUnits) base + adjustment else base
    }
}

/**
 * Splits this amount according to the given [ratios], distributing any
 * remainder from rounding to the last element.
 *
 * The sum of the returned list is always exactly equal to `this`.
 *
 * @param ratios the proportional weights for each portion (must not be empty, must not sum to zero).
 * @param context the [DeciContext] controlling scale and rounding.
 * @return a list of [Deci] values proportional to [ratios] that sum to `this`.
 */
fun Deci.allocateByRatios(
    ratios: List<Deci>,
    context: DeciContext = DeciContext.CURRENCY_USD,
): List<Deci> {
    require(ratios.isNotEmpty()) { "Ratios must not be empty" }
    val totalRatio = ratios.sumDeci()
    require(!totalRatio.isZero()) { "Total ratio must not be zero" }

    val results =
        ratios.map { ratio ->
            (this * ratio).divide(totalRatio, context)
        }

    val allocated = results.sumDeci()
    val remainder = this - allocated
    return if (remainder.isZero()) {
        results
    } else {
        results.toMutableList().apply {
            this[lastIndex] = this[lastIndex] + remainder
        }
    }
}
