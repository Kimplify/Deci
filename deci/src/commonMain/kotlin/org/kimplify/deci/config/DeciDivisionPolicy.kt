package org.kimplify.deci.config

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import org.kimplify.deci.RoundingMode
import kotlin.concurrent.Volatile

/**
 * Describes the default scale and rounding strategy applied by the `Deci` division operator.
 *
 * @property fractionalDigits Number of digits to keep to the right of the decimal separator.
 * @property roundingMode Rounding mode used when trimming to [fractionalDigits].
 */
@Immutable
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
 *
 * **Important for Compose:** This object is marked @Stable for compatibility, but contains
 * mutable state. Changes to [divisionPolicy] or [loggingEnabled] will NOT trigger
 * Compose recomposition automatically. If you need reactive configuration in Compose,
 * wrap values in mutableStateOf():
 * ```kotlin
 * val config by remember { mutableStateOf(DeciConfiguration.divisionPolicy) }
 * ```
 */
@Stable
object DeciConfiguration {
    private val defaultDivisionPolicy = DeciDivisionPolicy(
        fractionalDigits = 20,
        roundingMode = RoundingMode.HALF_UP
    )

    @Volatile
    var divisionPolicy: DeciDivisionPolicy = defaultDivisionPolicy

    /**
     * Toggle Cedar logging for literal normalization and validation events.
     * Leave `false` (default) to skip log emission entirely.
     */
    @Volatile
    var loggingEnabled: Boolean = false

    /** Restores [divisionPolicy] to its library default values. */
    fun resetDivisionPolicy() {
        divisionPolicy = defaultDivisionPolicy
    }

    /** Disables Cedar logging. */
    fun disableLogging() {
        loggingEnabled = false
    }
}
