package org.kimplify.deci.parser

internal val DECIMAL_REGEX = Regex(
    """^[-+]?(?:\d{1,3}(?:[.,]\d{3})*(?:[.,]\d*)?|\d+[.,]\d*|\d+|[.,]\d+)$"""
)
