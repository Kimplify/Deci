package org.kimplify.deci.exception

/**
 * Base class for all Deci domain exceptions.
 *
 * Catch this type to handle any error originating from the Deci library.
 * Prefer catching a more specific subclass when only a subset of errors
 * is expected.
 */
public sealed class DeciException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * Thrown when a string cannot be parsed into a valid Deci value.
 *
 * @property rawValue The original string that failed parsing.
 */
public class DeciParseException(
    public val rawValue: String,
    message: String = "Invalid decimal literal: '$rawValue'",
    cause: Throwable? = null,
) : DeciException(message, cause)

/**
 * Thrown when a Deci arithmetic operation is mathematically undefined
 * or produces an unrepresentable result.
 */
public open class DeciArithmeticException(
    message: String,
    cause: Throwable? = null,
) : DeciException(message, cause)

/**
 * Thrown specifically when a division operation has a zero divisor.
 */
public class DeciDivisionByZeroException(
    message: String = "Division by zero",
    cause: Throwable? = null,
) : DeciArithmeticException(message, cause)

/**
 * Thrown when a Deci value cannot be converted to a narrower type
 * (e.g. Long) because it exceeds the target type's range.
 *
 * @property value String representation of the Deci that overflowed.
 */
public class DeciOverflowException(
    public val value: String,
    message: String = "Deci value $value is outside representable range",
    cause: Throwable? = null,
) : DeciArithmeticException(message, cause)

/**
 * Thrown when an invalid scale parameter is provided.
 *
 * @property scale The invalid scale value that was supplied.
 */
public class DeciScaleException(
    public val scale: Int,
    message: String = "Scale must be non-negative: $scale",
) : DeciException(message)

/**
 * Thrown when [format][org.kimplify.deci.formatting.format] receives an unsupported format pattern.
 *
 * @property pattern The pattern string that was not recognized.
 */
public class DeciFormatException(
    public val pattern: String,
    message: String = "Unknown format pattern: $pattern",
) : DeciException(message)

/**
 * Thrown when deserialization of a Deci value fails.
 *
 * @property rawValue The string that could not be deserialized.
 */
public class DeciSerializationException(
    public val rawValue: String,
    message: String = "Invalid Deci value: '$rawValue'",
    cause: Throwable? = null,
) : DeciException(message, cause)
