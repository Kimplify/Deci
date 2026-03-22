package org.kimplify.deci.financial

import org.kimplify.deci.Deci
import org.kimplify.deci.DeciConstants
import org.kimplify.deci.DeciContext
import org.kimplify.deci.exception.DeciDivisionByZeroException

/**
 * Calculates the percentage change from this value to [other].
 *
 * Formula: `((other - this) / this) * 100`
 *
 * @param other the target value.
 * @param context precision and rounding for intermediate division.
 * @return the percentage change (e.g. `25` for a 25% increase).
 * @throws DeciDivisionByZeroException if this value is zero.
 */
fun Deci.percentageChangeTo(
    other: Deci,
    context: DeciContext = DeciContext.DEFAULT,
): Deci {
    if (this.isZero()) throw DeciDivisionByZeroException("Cannot calculate percentage change from zero")
    return (other - this).divide(this, context) * DeciConstants.HUNDRED
}

/**
 * Calculates the gross profit margin.
 *
 * Formula: `(revenue - cost) / revenue * 100`
 *
 * @throws DeciDivisionByZeroException if [revenue] is zero.
 */
fun grossMargin(
    revenue: Deci,
    cost: Deci,
    context: DeciContext = DeciContext.DEFAULT,
): Deci {
    if (revenue.isZero()) throw DeciDivisionByZeroException("Revenue cannot be zero")
    return (revenue - cost).divide(revenue, context) * DeciConstants.HUNDRED
}

/**
 * Calculates the markup percentage.
 *
 * Formula: `(price - cost) / cost * 100`
 *
 * @throws DeciDivisionByZeroException if [cost] is zero.
 */
fun markup(
    price: Deci,
    cost: Deci,
    context: DeciContext = DeciContext.DEFAULT,
): Deci {
    if (cost.isZero()) throw DeciDivisionByZeroException("Cost cannot be zero")
    return (price - cost).divide(cost, context) * DeciConstants.HUNDRED
}
