package org.kimplify.deci.logging

import org.kimplify.deci.config.DeciConfiguration

/**
 * Callback interface for receiving Deci log events.
 *
 * Consumers can plug in any logging backend (Timber, SLF4J, println, etc.)
 * by assigning an implementation to [DeciConfiguration.logSink].
 *
 * ```kotlin
 * DeciConfiguration.logSink = DeciLogSink { tag, message -> println("[$tag] $message") }
 * ```
 */
fun interface DeciLogSink {
    fun log(tag: String, message: String)
}

/**
 * Internal logger that routes Deci events through the consumer-provided
 * [DeciLogSink] set on [DeciConfiguration.logSink].
 *
 * When no sink is installed (the default), log calls are no-ops.
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
        val sink = DeciConfiguration.logSink ?: return
        sink.log(TAG, builder().message)
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
