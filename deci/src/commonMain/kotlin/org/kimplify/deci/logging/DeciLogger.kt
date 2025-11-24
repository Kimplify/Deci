package org.kimplify.deci.logging

/**
 * Pluggable logger that Deci uses to report normalization and validation events.
 * Set [org.kimplify.deci.config.DeciConfiguration.logger] to an instance of this interface to observe the emitted events.
 */
fun interface DeciLogger {
    fun log(event: DeciLogEvent)

    companion object {
        val NoOp: DeciLogger = DeciLogger { _ -> }
    }
}

/**
 * Describes a logging event emitted by Deci during literal processing.
 */
sealed interface DeciLogEvent {
    val message: String
}

/**
 * Emitted when Deci normalizes a user-provided literal (for example, swapping commas for dots).
 */
data class DeciLiteralNormalizedEvent(
    val rawValue: String,
    val normalizedValue: String
) : DeciLogEvent {
    override val message: String = "Normalized literal '$rawValue' to '$normalizedValue'"
}

/**
 * Emitted when Deci rejects a literal because it fails validation.
 */
data class DeciLiteralRejectedEvent(
    val rawValue: String,
    val reason: String
) : DeciLogEvent {
    override val message: String = "Rejected literal '$rawValue': $reason"
}
