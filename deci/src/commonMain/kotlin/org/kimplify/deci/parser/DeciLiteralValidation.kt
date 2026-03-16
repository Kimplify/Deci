package org.kimplify.deci.parser

import org.kimplify.deci.exception.DeciParseException
import org.kimplify.deci.logging.DeciLogger

internal fun validateAndNormalizeDecimalLiteral(rawValue: String): String {
    val trimmed = rawValue.trim()

    if (trimmed.isEmpty()) {
        DeciLogger.logLiteralRejected(rawValue, "Value is blank or whitespace only")
        throw DeciParseException(rawValue = rawValue, message = "Deci literal must not be blank")
    }

    if (!DECIMAL_REGEX.matches(trimmed)) {
        DeciLogger.logLiteralRejected(rawValue, "Value does not match decimal format")
        throw DeciParseException(rawValue = rawValue)
    }

    val normalized = rawValue.normalizeDecimalString()
    if (normalized != trimmed) {
        DeciLogger.logLiteralNormalized(rawValue, normalized)
    }

    return normalized
}
