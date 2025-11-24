package org.kimplify.deci

internal fun String.normalizeDecimalString(): String {
    if (isEmpty()) return this
    
    val trimmed = trim()
    val isNegative = trimmed.startsWith("-")
    val withoutSign = if (isNegative || trimmed.startsWith("+")) trimmed.drop(1) else trimmed
    
    if (withoutSign.isEmpty()) return if (isNegative) "-0" else "0"
    
    val lastCommaIndex = withoutSign.lastIndexOf(',')
    val lastDotIndex = withoutSign.lastIndexOf('.')
    
    val hasComma = lastCommaIndex != -1
    val hasDot = lastDotIndex != -1
    
    return when {
        !hasComma && !hasDot -> {
            val normalized = withoutSign.replace(".", "").replace(",", "")
            if (isNegative) "-$normalized" else normalized
        }
        hasComma && !hasDot -> {
            val integerPart = withoutSign.substring(0, lastCommaIndex).replace(".", "").replace(",", "")
            val decimalPart = withoutSign.substring(lastCommaIndex + 1)
            val normalized = if (integerPart.isEmpty()) "0.$decimalPart" else "$integerPart.$decimalPart"
            if (isNegative) "-$normalized" else normalized
        }
        !hasComma && hasDot -> {
            val integerPart = withoutSign.substring(0, lastDotIndex).replace(".", "").replace(",", "")
            val decimalPart = withoutSign.substring(lastDotIndex + 1)
            val normalized = if (integerPart.isEmpty()) "0.$decimalPart" else "$integerPart.$decimalPart"
            if (isNegative) "-$normalized" else normalized
        }
        else -> {
            val lastSeparatorIndex = maxOf(lastCommaIndex, lastDotIndex)
            val integerPart = withoutSign.substring(0, lastSeparatorIndex)
                .replace(".", "")
                .replace(",", "")
            val decimalPart = withoutSign.substring(lastSeparatorIndex + 1)
            val normalized = if (integerPart.isEmpty()) "0.$decimalPart" else "$integerPart.$decimalPart"
            if (isNegative) "-$normalized" else normalized
        }
    }
}

