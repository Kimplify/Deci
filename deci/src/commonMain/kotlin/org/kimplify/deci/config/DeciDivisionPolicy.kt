package org.kimplify.deci.config

import org.kimplify.deci.RoundingMode
import org.kimplify.deci.logging.DeciLogSink
import kotlin.concurrent.Volatile

/**
 * Describes the default scale and rounding strategy applied by the `Deci` division operator.
 *
 * @property fractionalDigits Number of digits to keep to the right of the decimal separator.
 * @property roundingMode Rounding mode used when trimming to [fractionalDigits].
 */
data class DeciDivisionPolicy(
    val fractionalDigits: Int,
    val roundingMode: RoundingMode,
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
 *
 * **Note:** This object contains mutable state. Changes to [divisionPolicy] or
 * [logSink] are thread-safe via @Volatile but not reactive.
 */
object DeciConfiguration {
    private val defaultDivisionPolicy =
        DeciDivisionPolicy(
            fractionalDigits = 20,
            roundingMode = RoundingMode.HALF_UP,
        )

    @Deprecated(
        message = "Use DeciContext and the divide(other, context) overload instead.",
        replaceWith = ReplaceWith("DeciContext", "org.kimplify.deci.DeciContext"),
    )
    @Volatile
    var divisionPolicy: DeciDivisionPolicy = defaultDivisionPolicy

    /**
     * Consumer-provided log sink for literal normalization and validation events.
     * Leave `null` (default) to skip log emission entirely.
     *
     * ```kotlin
     * DeciConfiguration.logSink = DeciLogSink { tag, message ->
     *     println("[$tag] $message")
     * }
     * ```
     */
    @Volatile
    var logSink: DeciLogSink? = null

    /** Restores [divisionPolicy] to its library default values. */
    fun resetDivisionPolicy() {
        divisionPolicy = defaultDivisionPolicy
    }

    /** Removes the installed log sink, disabling all Deci logging. */
    fun disableLogging() {
        logSink = null
    }
}
