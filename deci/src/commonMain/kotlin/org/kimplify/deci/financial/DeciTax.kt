package org.kimplify.deci.financial

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciContext

/**
 * Calculates tax amount from this gross value.
 *
 * Example: `Deci("100").taxAmount(Deci("0.08"))` -> `Deci("8.00")`
 */
fun Deci.taxAmount(
    taxRate: Deci,
    context: DeciContext = DeciContext.CURRENCY_USD,
): Deci = (this * taxRate).setScale(context.precision, context.roundingMode)

/**
 * Adds tax to this amount.
 *
 * Example: `Deci("100").withTax(Deci("0.08"))` -> `Deci("108.00")`
 */
fun Deci.withTax(
    taxRate: Deci,
    context: DeciContext = DeciContext.CURRENCY_USD,
): Deci = (this + taxAmount(taxRate, context)).setScale(context.precision, context.roundingMode)

/**
 * Extracts the pre-tax amount from a tax-inclusive value.
 *
 * Example: `Deci("108").preTax(Deci("0.08"))` -> `Deci("100.00")`
 */
fun Deci.preTax(
    taxRate: Deci,
    context: DeciContext = DeciContext.CURRENCY_USD,
): Deci = this.divide(Deci.ONE + taxRate, context)

/**
 * Applies a discount to this amount.
 *
 * Example: `Deci("100").withDiscount(Deci("0.20"))` -> `Deci("80.00")`
 */
fun Deci.withDiscount(
    discountRate: Deci,
    context: DeciContext = DeciContext.CURRENCY_USD,
): Deci = (this - this * discountRate).setScale(context.precision, context.roundingMode)
