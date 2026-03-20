package org.kimplify.deci.exception

/**
 * Base class for all Deci domain exceptions.
 *
 * Catch this type to handle any error originating from the Deci library.
 * Prefer catching a more specific subclass when only a subset of errors
 * is expected.
 */
sealed class DeciException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * Thrown when a string cannot be parsed into a valid Deci value.
 *
 * @property rawValue The original string that failed parsing.
 */
class DeciParseException(
    val rawValue: String,
    message: String = "Invalid decimal literal: '$rawValue'",
    cause: Throwable? = null,
) : DeciException(message, cause)

/**
 * Thrown when a Deci arithmetic operation is mathematically undefined
 * or produces an unrepresentable result.
 */
open class DeciArithmeticException(
    message: String,
    cause: Throwable? = null,
) : DeciException(message, cause)

/**
 * Thrown specifically when a division operation has a zero divisor.
 */
class DeciDivisionByZeroException(
    message: String = "Division by zero",
    cause: Throwable? = null,
) : DeciArithmeticException(message, cause)

/**
 * Thrown when a Deci value cannot be converted to a narrower type
 * (e.g. Long) because it exceeds the target type's range.
 *
 * @property value String representation of the Deci that overflowed.
 */
class DeciOverflowException(
    val value: String,
    message: String = "Deci value $value is outside representable range",
    cause: Throwable? = null,
) : DeciArithmeticException(message, cause)

/**
 * Thrown when an invalid scale parameter is provided.
 *
 * @property scale The invalid scale value that was supplied.
 */
class DeciScaleException(
    val scale: Int,
    message: String = "Scale must be non-negative: $scale",
) : DeciException(message)

/**
 * Thrown when [format][org.kimplify.deci.formatting.format] receives an unsupported format pattern.
 *
 * @property pattern The pattern string that was not recognized.
 */
class DeciFormatException(
    val pattern: String,
    message: String = "Unknown format pattern: $pattern",
) : DeciException(message)

/**
 * Thrown when deserialization of a Deci value fails.
 *
 * @property rawValue The string that could not be deserialized.
 */
class DeciSerializationException(
    val rawValue: String,
    message: String = "Invalid Deci value: '$rawValue'",
    cause: Throwable? = null,
) : DeciException(message, cause)
