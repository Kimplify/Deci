package org.kimplify.deci

/**
 * Marks declarations that are still experimental in the Deci API.
 *
 * Experimental APIs may be changed or removed in future releases without notice.
 * Consumers must explicitly opt in with `@OptIn(ExperimentalDeciApi::class)` or
 * `@ExperimentalDeciApi` to signal they accept the instability risk.
 */
@RequiresOptIn(
    message = "This API is experimental and may change without notice.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
)
annotation class ExperimentalDeciApi
