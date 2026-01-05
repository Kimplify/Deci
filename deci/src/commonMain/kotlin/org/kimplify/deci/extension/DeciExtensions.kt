package org.kimplify.deci.extension

import org.kimplify.deci.Deci
import org.kimplify.deci.RoundingMode

fun Iterable<Deci>.sumDeci(): Deci = this.fold(Deci.ZERO) { accumulated, value ->
    accumulated + value
}

fun Deci.toLong(): Long = toLongExact()

fun Deci.toLongOrNull(): Long? {
    val truncated = this.setScale(0, RoundingMode.DOWN)
    val str = truncated.toString()
    return str.toLongOrNull()
}

fun Deci.toLongExact(): Long {
    return toLongOrNull()
        ?: throw ArithmeticException("Deci value $this is outside Long range")
}

/** Returns the number of digits to the right of the decimal separator in the canonical string form. */
fun Deci.scale(): Int {
    val text = toString()
    val separatorIndex = text.indexOf('.')
    if (separatorIndex < 0) return 0
    return text.length - separatorIndex - 1
}

/** Returns the count of significant digits (excluding the sign and decimal separator). */
fun Deci.precision(): Int {
    val text = toString()
    return text.count { it.isDigit() }
}
