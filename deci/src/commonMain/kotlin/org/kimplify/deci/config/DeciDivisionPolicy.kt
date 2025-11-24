package org.kimplify.deci.config

import org.kimplify.deci.RoundingMode
import org.kimplify.deci.logging.DeciLogEvent
import org.kimplify.deci.logging.DeciLogger
import kotlin.concurrent.Volatile

/**
 * Describes the default scale and rounding strategy applied by the `Deci` division operator.
 *
 * @property fractionalDigits Number of digits to keep to the right of the decimal separator.
 * @property roundingMode Rounding mode used when trimming to [fractionalDigits].
 */
data class DeciDivisionPolicy(
    val fractionalDigits: Int,
    val roundingMode: RoundingMode
) {
    init {
        require(fractionalDigits >= 0) {
            "fractionalDigits must be zero or positive (was $fractionalDigits)"
        }
    }
}

/**
 * Global configuration entry point for Deci. Consumers can override the default
 * division scale to align with domain-specific requirements.
 */
object DeciConfiguration {
    private val defaultDivisionPolicy = DeciDivisionPolicy(
        fractionalDigits = 20,
        roundingMode = RoundingMode.HALF_UP
    )

    @Volatile
    var divisionPolicy: DeciDivisionPolicy = defaultDivisionPolicy

    /**
     * Optional logger that records literal normalization and validation events.
     * Assign `null` (default) to disable logging entirely.
     */
    @Volatile
    var logger: DeciLogger? = null

    /** Restores [divisionPolicy] to its library default values. */
    fun resetDivisionPolicy() {
        divisionPolicy = defaultDivisionPolicy
    }

    /** Clears the installed [DeciLogger] (if any). */
    fun clearLogger() {
        logger = null
    }

    internal fun logEvent(builder: () -> DeciLogEvent) {
        logger?.log(builder())
    }
}
