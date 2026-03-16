package org.kimplify.deci.logging

import org.kimplify.cedar.logging.Cedar

internal actual fun logDebug(tag: String, message: String) {
    Cedar.tag(tag).d(message)
}
