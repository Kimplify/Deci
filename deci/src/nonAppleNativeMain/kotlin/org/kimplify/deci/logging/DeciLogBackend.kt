package org.kimplify.deci.logging

internal actual fun logDebug(tag: String, message: String) {
    println("[$tag] $message")
}
