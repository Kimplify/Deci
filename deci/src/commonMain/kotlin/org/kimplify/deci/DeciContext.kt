package org.kimplify.deci

/**
 * Immutable context that carries scale and rounding policy for Deci operations.
 *
 * Analogous to `java.math.MathContext`, [DeciContext] allows callers to pass an
 * explicit context to each operation instead of relying on the mutable global
 * [DeciConfiguration][org.kimplify.deci.config.DeciConfiguration] singleton.
 *
 * @property precision The number of fractional digits to retain. Must be non-negative.
 * @property roundingMode The [RoundingMode] applied when a result must be
 *                        rounded to fit [precision].
 */
data class DeciContext(
    val precision: Int,
    val roundingMode: RoundingMode,
) {
    init {
        require(precision >= 0) {
            "precision must be non-negative (was $precision)"
        }
    }

    companion object {
        /**
         * Default context: 20 fractional digits with [RoundingMode.HALF_UP].
         * Matches the default [org.kimplify.deci.config.DeciDivisionPolicy].
         */
        val DEFAULT =
            DeciContext(
                precision = 20,
                roundingMode = RoundingMode.HALF_UP,
            )

        /** Context suitable for USD currency: 2 fractional digits, HALF_UP. */
        val CURRENCY_USD =
            DeciContext(
                precision = 2,
                roundingMode = RoundingMode.HALF_UP,
            )

        /** Banker's rounding with 2 fractional digits. */
        val BANKING =
            DeciContext(
                precision = 2,
                roundingMode = RoundingMode.HALF_EVEN,
            )
    }
}
