package org.kimplify.deci.logging

import org.kimplify.deci.config.DeciConfiguration

/**
 * Internal logger that routes Deci events through Cedar on platforms that
 * support it, and falls back to [println] elsewhere.
 */
internal object DeciLogger {
    private const val TAG = "Deci"

    fun logLiteralNormalized(
        rawValue: String,
        normalizedValue: String,
    ) = logIfEnabled {
        DeciLiteralNormalizedEvent(rawValue, normalizedValue)
    }

    fun logLiteralRejected(
        rawValue: String,
        reason: String,
    ) = logIfEnabled {
        DeciLiteralRejectedEvent(rawValue, reason)
    }

    private inline fun logIfEnabled(builder: () -> DeciLogEvent) {
        if (!DeciConfiguration.loggingEnabled) return
        logDebug(TAG, builder().message)
    }
}

/**
 * Platform-specific debug log call.
 * Uses Cedar on platforms where it is available; falls back to [println] elsewhere.
 */
internal expect fun logDebug(tag: String, message: String)

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
    val normalizedValue: String,
) : DeciLogEvent {
    override val message: String = "Normalized literal '$rawValue' to '$normalizedValue'"
}

/**
 * Emitted when Deci rejects a literal because it fails validation.
 */
internal data class DeciLiteralRejectedEvent(
    val rawValue: String,
    val reason: String,
) : DeciLogEvent {
    override val message: String = "Rejected literal '$rawValue': $reason"
}
