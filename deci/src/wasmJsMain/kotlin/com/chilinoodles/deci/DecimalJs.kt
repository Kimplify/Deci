@file:OptIn(ExperimentalWasmJsInterop::class)
@file:JsModule("decimal.js")

package com.chilinoodles.deci

@JsName("default")
external class DecimalJs(value: String) {
    fun add(other: DecimalJs): DecimalJs
    fun sub(other: DecimalJs): DecimalJs
    fun mul(other: DecimalJs): DecimalJs
    fun div(other: DecimalJs): DecimalJs
    fun comparedTo(other: DecimalJs): Int
    fun toDecimalPlaces(scale: Int, roundingMode: Int): DecimalJs
    override fun toString(): String
    fun toNumber(): Double
    fun isZero(): Boolean
    fun isNegative(): Boolean
    fun isPositive(): Boolean
    fun abs(): DecimalJs
    fun neg(): DecimalJs
}

