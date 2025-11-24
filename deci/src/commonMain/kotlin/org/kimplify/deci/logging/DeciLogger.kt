package org.kimplify.deci.logging

import org.kimplify.deci.config.DeciConfiguration
import org.kimplify.cedar.logging.Cedar

/**
 * Internal logger that routes Deci events through Cedar.
 */
internal object DeciLogger {
    private const val TAG = "Deci"

    fun logLiteralNormalized(rawValue: String, normalizedValue: String) = logIfEnabled {
        DeciLiteralNormalizedEvent(rawValue, normalizedValue)
    }

    fun logLiteralRejected(rawValue: String, reason: String) = logIfEnabled {
        DeciLiteralRejectedEvent(rawValue, reason)
    }

    private inline fun logIfEnabled(builder: () -> DeciLogEvent) {
        if (!DeciConfiguration.loggingEnabled) return
        Cedar.tag(TAG).d(builder().message)
    }
}

/**
 * Describes a logging event emitted by Deci during literal processing.
 */
internal sealed interface DeciLogEvent {
    val message: String
}

/**
 * Emitted when Deci normalizes a user-provided literal (for example, swapping commas for dots).
 */
internal data class DeciLiteralNormalizedEvent(
    val rawValue: String,
    val normalizedValue: String
) : DeciLogEvent {
    override val message: String = "Normalized literal '$rawValue' to '$normalizedValue'"
}

/**
 * Emitted when Deci rejects a literal because it fails validation.
 */
internal data class DeciLiteralRejectedEvent(
    val rawValue: String,
    val reason: String
) : DeciLogEvent {
    override val message: String = "Rejected literal '$rawValue': $reason"
}
