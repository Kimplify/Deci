package org.kimplify.deci.config

import org.kimplify.deci.logging.DeciLogSink
import kotlin.concurrent.Volatile

/**
 * Global configuration entry point for Deci.
 *
 * **Note:** This object contains mutable state. Changes to [logSink] are
 * thread-safe via @Volatile but not reactive.
 */
object DeciConfiguration {
    /**
     * Consumer-provided log sink for literal normalization and validation events.
     * Leave `null` (default) to skip log emission entirely.
     *
     * ```kotlin
     * DeciConfiguration.logSink = DeciLogSink { tag, message ->
     *     println("[$tag] $message")
     * }
     * ```
     */
    @Volatile
    var logSink: DeciLogSink? = null

    /** Removes the installed log sink, disabling all Deci logging. */
    fun disableLogging() {
        logSink = null
    }
}
