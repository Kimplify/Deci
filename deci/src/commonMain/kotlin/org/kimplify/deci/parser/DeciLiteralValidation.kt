package org.kimplify.deci.parser

import org.kimplify.deci.config.DeciConfiguration
import org.kimplify.deci.logging.DeciLiteralNormalizedEvent
import org.kimplify.deci.logging.DeciLiteralRejectedEvent

internal fun validateAndNormalizeDecimalLiteral(rawValue: String): String {
    val trimmed = rawValue.trim()

    if (trimmed.isEmpty()) {
        DeciConfiguration.logEvent {
            DeciLiteralRejectedEvent(rawValue, "Value is blank or whitespace only")
        }
        throw IllegalArgumentException("Deci literal must not be blank")
    }

    if (!DECIMAL_REGEX.matches(trimmed)) {
        DeciConfiguration.logEvent {
            DeciLiteralRejectedEvent(rawValue, "Value does not match decimal format")
        }
        throw IllegalArgumentException("Invalid decimal literal: '$rawValue'")
    }

    val normalized = rawValue.normalizeDecimalString()
    if (normalized != trimmed) {
        DeciConfiguration.logEvent {
            DeciLiteralNormalizedEvent(rawValue, normalized)
        }
    }

    return normalized
}
