package org.kimplify.deci

import androidx.compose.runtime.Immutable

/**
 * Enumerates every rounding strategy supported by Deci across platforms.
 *
 * The entries mirror the semantics of `java.math.RoundingMode` and
 * `NSRoundingMode`, ensuring identical behavior on JVM, Android, iOS, JS, and WASM.
 */
@Immutable
enum class RoundingMode {
    UP,
    DOWN,
    CEILING,
    FLOOR,
    HALF_UP,
    HALF_DOWN,
    HALF_EVEN
}
