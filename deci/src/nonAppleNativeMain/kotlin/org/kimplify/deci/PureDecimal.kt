package org.kimplify.deci

/**
 * Pure-Kotlin arbitrary-precision decimal for non-Apple native targets.
 *
 * Representation: sign + unscaled value (stored as a [LongArray] of base-10^9 limbs) + scale.
 * The numeric value is: sign * unscaledValue * 10^(-scale)
 *
 * Each limb holds a value in [0, 999_999_999]. Limb 0 is the least significant.
 */
internal class PureDecimal private constructor(
    /** 1 for positive/zero, -1 for negative */
    private val sign: Int,
    /** Base-10^9 limbs, least-significant first. Never empty. */
    private val limbs: LongArray,
    /** Number of digits after the decimal point. Always >= 0. */
    private val scale: Int,
) : Comparable<PureDecimal> {

    init {
        require(scale >= 0) { "scale must be non-negative: $scale" }
        require(limbs.isNotEmpty()) { "limbs must not be empty" }
    }

    companion object {
        private const val BASE = 1_000_000_000L
        private const val LIMB_DIGITS = 9

        val ZERO = PureDecimal(sign = 1, limbs = longArrayOf(0L), scale = 0)
        val ONE = fromString("1")
        val TEN = fromString("10")

        fun fromString(s: String): PureDecimal {
            if (s.isEmpty()) return ZERO

            val negative = s.startsWith('-')
            val start = if (negative || s.startsWith('+')) 1 else 0
            val dotIndex = s.indexOf('.')

            val intPart: String
            val fracPart: String
            if (dotIndex < 0) {
                intPart = s.substring(start)
                fracPart = ""
            } else {
                intPart = s.substring(start, dotIndex)
                fracPart = s.substring(dotIndex + 1)
            }

            val digits = intPart + fracPart
            val scale = fracPart.length

            val stripped = digits.trimStart('0')
            val effectiveDigits = stripped.ifEmpty { "0" }

            val limbs = digitsToLimbs(effectiveDigits)
            val isZeroValue = limbs.size == 1 && limbs[0] == 0L
            return PureDecimal(
                sign = if (negative && !isZeroValue) -1 else 1,
                limbs = limbs,
                scale = scale,
            )
        }

        fun fromLong(value: Long): PureDecimal {
            if (value == 0L) return ZERO
            val negative = value < 0
            var v = if (negative) {
                if (value == Long.MIN_VALUE) {
                    return fromString(value.toString())
                }
                -value
            } else {
                value
            }
            val limbList = mutableListOf<Long>()
            while (v > 0) {
                limbList.add(v % BASE)
                v /= BASE
            }
            return PureDecimal(
                sign = if (negative) -1 else 1,
                limbs = limbList.toLongArray(),
                scale = 0,
            )
        }

        private fun digitsToLimbs(digits: String): LongArray {
            if (digits.isEmpty() || digits == "0") return longArrayOf(0L)

            val limbList = mutableListOf<Long>()
            var i = digits.length
            while (i > 0) {
                val start = maxOf(0, i - LIMB_DIGITS)
                val chunk = digits.substring(start, i)
                limbList.add(chunk.toLong())
                i = start
            }
            return limbList.toLongArray()
        }

        /** Create from already-normalized components. */
        internal fun fromComponents(sign: Int, limbs: LongArray, scale: Int): PureDecimal {
            val normalized = stripLeadingZeroLimbs(limbs)
            val isZeroValue = normalized.size == 1 && normalized[0] == 0L
            return PureDecimal(
                sign = if (isZeroValue) 1 else sign,
                limbs = normalized,
                scale = scale,
            )
        }

        private fun stripLeadingZeroLimbs(limbs: LongArray): LongArray {
            var lastNonZero = limbs.size - 1
            while (lastNonZero > 0 && limbs[lastNonZero] == 0L) {
                lastNonZero--
            }
            return if (lastNonZero == limbs.size - 1) limbs else limbs.copyOfRange(0, lastNonZero + 1)
        }
    }

    val isZero: Boolean get() = limbs.size == 1 && limbs[0] == 0L
    val isNegative: Boolean get() = sign < 0 && !isZero
    val isPositive: Boolean get() = sign > 0 && !isZero

    fun negate(): PureDecimal =
        if (isZero) this
        else PureDecimal(sign = -sign, limbs = limbs.copyOf(), scale = scale)

    fun abs(): PureDecimal =
        if (sign >= 0) this
        else PureDecimal(sign = 1, limbs = limbs.copyOf(), scale = scale)

    operator fun plus(other: PureDecimal): PureDecimal {
        val (a, b) = alignScales(this, other)
        return if (a.sign == b.sign) {
            fromComponents(a.sign, addMagnitudes(a.limbs, b.limbs), a.scale)
        } else {
            val cmp = compareMagnitudes(a.limbs, b.limbs)
            when {
                cmp > 0 -> fromComponents(a.sign, subtractMagnitudes(a.limbs, b.limbs), a.scale)
                cmp < 0 -> fromComponents(b.sign, subtractMagnitudes(b.limbs, a.limbs), a.scale)
                else -> PureDecimal(sign = 1, limbs = longArrayOf(0L), scale = a.scale)
            }
        }
    }

    operator fun minus(other: PureDecimal): PureDecimal = this + other.negate()

    operator fun times(other: PureDecimal): PureDecimal {
        if (this.isZero || other.isZero) {
            return PureDecimal(sign = 1, limbs = longArrayOf(0L), scale = this.scale + other.scale)
        }
        val resultSign = if (this.sign == other.sign) 1 else -1
        val product = multiplyMagnitudes(this.limbs, other.limbs)
        return fromComponents(resultSign, product, this.scale + other.scale)
    }

    /**
     * Long division producing [resultScale] fractional digits, rounded with [roundingMode].
     */
    fun divide(other: PureDecimal, resultScale: Int, roundingMode: RoundingMode): PureDecimal {
        if (other.isZero) throw ArithmeticException("Division by zero")
        if (this.isZero) return PureDecimal(sign = 1, limbs = longArrayOf(0L), scale = resultScale)

        val resultSign = if (this.sign == other.sign) 1 else -1

        val extraDigits = resultScale + 1 + other.scale - this.scale
        val scaledDividend = if (extraDigits > 0) {
            multiplyByPowerOf10(this.limbs, extraDigits)
        } else if (extraDigits < 0) {
            multiplyByPowerOf10(this.limbs, 0)
        } else {
            this.limbs.copyOf()
        }

        val scaledDivisor = if (extraDigits < 0) {
            multiplyByPowerOf10(other.limbs, -extraDigits)
        } else {
            other.limbs.copyOf()
        }

        val (quotient, remainder) = divideWithRemainder(scaledDividend, scaledDivisor)

        val rounded = roundQuotient(quotient, remainder, scaledDivisor, resultScale, resultSign, roundingMode)
        return fromComponents(resultSign, rounded, resultScale)
    }

    fun setScale(newScale: Int, roundingMode: RoundingMode): PureDecimal {
        if (newScale == this.scale) return this

        return if (newScale > this.scale) {
            val extraDigits = newScale - this.scale
            val newLimbs = multiplyByPowerOf10(this.limbs, extraDigits)
            fromComponents(sign, newLimbs, newScale)
        } else {
            val removeDigits = this.scale - newScale
            val divisorLimbs = powerOf10Limbs(removeDigits)
            val (quotient, remainder) = divideWithRemainder(this.limbs, divisorLimbs)
            val rounded = roundQuotient(quotient, remainder, divisorLimbs, 0, sign, roundingMode)
            fromComponents(sign, rounded, newScale)
        }
    }

    fun toDouble(): Double = toString().toDouble()

    override fun compareTo(other: PureDecimal): Int {
        if (this.isZero && other.isZero) return 0
        if (this.sign != other.sign) return if (this.sign > other.sign) 1 else -1

        val (a, b) = alignScales(this, other)
        val magCmp = compareMagnitudes(a.limbs, b.limbs)
        return if (a.sign > 0) magCmp else -magCmp
    }

    override fun equals(other: Any?): Boolean =
        this === other || (other is PureDecimal && compareTo(other) == 0)

    override fun hashCode(): Int = stripTrailingZeros().toCanonicalString().hashCode()

    override fun toString(): String = toFormattedString()

    private fun toFormattedString(): String {
        val digits = limbsToDigits(limbs)
        if (scale == 0) {
            return if (sign < 0) "-$digits" else digits
        }

        val paddedDigits = if (digits.length <= scale) {
            digits.padStart(scale + 1, '0')
        } else {
            digits
        }

        val intPart = paddedDigits.substring(0, paddedDigits.length - scale)
        val fracPart = paddedDigits.substring(paddedDigits.length - scale)
        val result = "$intPart.$fracPart"
        return if (sign < 0) "-$result" else result
    }

    private fun toCanonicalString(): String {
        val digits = limbsToDigits(limbs)
        return if (sign < 0 && !isZero) "-$digits@$scale" else "$digits@$scale"
    }

    private fun stripTrailingZeros(): PureDecimal {
        if (isZero) return PureDecimal(1, longArrayOf(0L), 0)
        if (scale == 0) return this

        val digits = limbsToDigits(limbs)
        val trailingZeros = digits.reversed().takeWhile { it == '0' }.length
        val removable = minOf(trailingZeros, scale)
        if (removable == 0) return this

        val newDigits = digits.substring(0, digits.length - removable)
        val effectiveDigits = newDigits.ifEmpty { "0" }
        return PureDecimal(sign, digitsToLimbs(effectiveDigits), scale - removable)
    }

    private fun addMagnitudes(a: LongArray, b: LongArray): LongArray {
        val maxLen = maxOf(a.size, b.size)
        val result = LongArray(maxLen + 1)
        var carry = 0L
        for (i in 0 until maxLen) {
            val sum = (if (i < a.size) a[i] else 0L) + (if (i < b.size) b[i] else 0L) + carry
            result[i] = sum % BASE
            carry = sum / BASE
        }
        result[maxLen] = carry
        return stripLeadingZeroLimbs(result)
    }

    /** Subtract b from a. Caller guarantees a >= b in magnitude. */
    private fun subtractMagnitudes(a: LongArray, b: LongArray): LongArray {
        val result = LongArray(a.size)
        var borrow = 0L
        for (i in a.indices) {
            val diff = a[i] - (if (i < b.size) b[i] else 0L) - borrow
            if (diff < 0) {
                result[i] = diff + BASE
                borrow = 1
            } else {
                result[i] = diff
                borrow = 0
            }
        }
        return stripLeadingZeroLimbs(result)
    }

    private fun multiplyMagnitudes(a: LongArray, b: LongArray): LongArray {
        val result = LongArray(a.size + b.size)
        for (i in a.indices) {
            var carry = 0L
            for (j in b.indices) {
                val prod = a[i] * b[j] + result[i + j] + carry
                result[i + j] = prod % BASE
                carry = prod / BASE
            }
            result[i + b.size] += carry
        }
        return stripLeadingZeroLimbs(result)
    }

    /** Compare unsigned magnitudes. Returns >0 if a > b, <0 if a < b, 0 if equal. */
    private fun compareMagnitudes(a: LongArray, b: LongArray): Int {
        val aLen = effectiveLength(a)
        val bLen = effectiveLength(b)
        if (aLen != bLen) return if (aLen > bLen) 1 else -1
        for (i in aLen - 1 downTo 0) {
            if (a[i] != b[i]) return if (a[i] > b[i]) 1 else -1
        }
        return 0
    }

    private fun effectiveLength(limbs: LongArray): Int {
        var len = limbs.size
        while (len > 1 && limbs[len - 1] == 0L) len--
        return len
    }

    private fun alignScales(a: PureDecimal, b: PureDecimal): Pair<PureDecimal, PureDecimal> {
        if (a.scale == b.scale) return a to b
        return if (a.scale > b.scale) {
            val newBLimbs = multiplyByPowerOf10(b.limbs, a.scale - b.scale)
            a to PureDecimal(b.sign, newBLimbs, a.scale)
        } else {
            val newALimbs = multiplyByPowerOf10(a.limbs, b.scale - a.scale)
            PureDecimal(a.sign, newALimbs, b.scale) to b
        }
    }

    private fun multiplyByPowerOf10(limbs: LongArray, power: Int): LongArray {
        if (power == 0) return limbs.copyOf()

        val fullLimbs = power / LIMB_DIGITS
        val remainderPow = power % LIMB_DIGITS

        var result = if (fullLimbs > 0) {
            val shifted = LongArray(limbs.size + fullLimbs)
            limbs.copyInto(shifted, destinationOffset = fullLimbs)
            shifted
        } else {
            limbs.copyOf()
        }

        if (remainderPow > 0) {
            val multiplier = pow10(remainderPow)
            result = multiplySingleLimb(result, multiplier)
        }

        return stripLeadingZeroLimbs(result)
    }

    private fun pow10(n: Int): Long {
        var result = 1L
        repeat(n) { result *= 10L }
        return result
    }

    private fun powerOf10Limbs(power: Int): LongArray {
        val digits = buildString {
            append('1')
            repeat(power) { append('0') }
        }
        return digitsToLimbs(digits)
    }

    private fun multiplySingleLimb(limbs: LongArray, factor: Long): LongArray {
        val result = LongArray(limbs.size + 1)
        var carry = 0L
        for (i in limbs.indices) {
            val prod = limbs[i] * factor + carry
            result[i] = prod % BASE
            carry = prod / BASE
        }
        result[limbs.size] = carry
        return stripLeadingZeroLimbs(result)
    }

    /**
     * Schoolbook long division. Returns (quotient, remainder) as limb arrays.
     */
    private fun divideWithRemainder(dividend: LongArray, divisor: LongArray): Pair<LongArray, LongArray> {
        val cmp = compareMagnitudes(dividend, divisor)
        if (cmp < 0) return longArrayOf(0L) to dividend.copyOf()
        if (cmp == 0) return longArrayOf(1L) to longArrayOf(0L)

        if (divisor.size == 1 || (divisor.size == 2 && divisor[1] == 0L)) {
            val d = divisor[0]
            val quotient = LongArray(dividend.size)
            var rem = 0L
            for (i in dividend.size - 1 downTo 0) {
                val cur = rem * BASE + dividend[i]
                quotient[i] = cur / d
                rem = cur % d
            }
            return stripLeadingZeroLimbs(quotient) to longArrayOf(rem)
        }

        return divideByDigitConversion(dividend, divisor)
    }

    /**
     * Division via string-based digit extraction. Not the fastest approach,
     * but provably correct and sufficient for financial precision workloads.
     */
    private fun divideByDigitConversion(dividend: LongArray, divisor: LongArray): Pair<LongArray, LongArray> {
        val dividendDigits = limbsToDigits(dividend)
        val divisorDigits = limbsToDigits(divisor)

        val quotientDigits = StringBuilder()
        var remainder = ""

        for (digit in dividendDigits) {
            remainder += digit
            remainder = remainder.trimStart('0').ifEmpty { "0" }

            var count = 0
            while (compareDigitStrings(remainder, divisorDigits) >= 0) {
                remainder = subtractDigitStrings(remainder, divisorDigits)
                count++
            }
            quotientDigits.append(count)
        }

        val qStr = quotientDigits.toString().trimStart('0').ifEmpty { "0" }
        val rStr = remainder.trimStart('0').ifEmpty { "0" }
        return digitsToLimbs(qStr) to digitsToLimbs(rStr)
    }

    /** Compare two non-negative integer digit strings. */
    private fun compareDigitStrings(a: String, b: String): Int {
        val aStripped = a.trimStart('0').ifEmpty { "0" }
        val bStripped = b.trimStart('0').ifEmpty { "0" }
        if (aStripped.length != bStripped.length) {
            return if (aStripped.length > bStripped.length) 1 else -1
        }
        return aStripped.compareTo(bStripped)
    }

    /** Subtract b from a (both non-negative digit strings, a >= b). */
    private fun subtractDigitStrings(a: String, b: String): String {
        val maxLen = maxOf(a.length, b.length)
        val aPadded = a.padStart(maxLen, '0')
        val bPadded = b.padStart(maxLen, '0')
        val result = CharArray(maxLen)
        var borrow = 0
        for (i in maxLen - 1 downTo 0) {
            var diff = (aPadded[i] - '0') - (bPadded[i] - '0') - borrow
            if (diff < 0) {
                diff += 10
                borrow = 1
            } else {
                borrow = 0
            }
            result[i] = ('0' + diff)
        }
        return result.concatToString().trimStart('0').ifEmpty { "0" }
    }

    /**
     * Round the quotient which has one guard digit.
     * [quotient] is the raw integer quotient from division with an extra guard digit.
     * [remainder] is the remainder from division.
     * [divisor] is the divisor limbs.
     * [targetScale] is always 0 here — we just need to drop the last digit (guard digit).
     */
    private fun roundQuotient(
        quotient: LongArray,
        remainder: LongArray,
        divisor: LongArray,
        targetScale: Int,
        resultSign: Int,
        roundingMode: RoundingMode,
    ): LongArray {
        val (truncated, guardRemainder) = divideWithRemainder(quotient, longArrayOf(10L))
        val guardDigit = guardRemainder[0].toInt()

        val hasMoreAfterGuard = !(remainder.size == 1 && remainder[0] == 0L)

        val roundUp = shouldRoundUp(guardDigit, hasMoreAfterGuard, truncated, resultSign, roundingMode)

        return if (roundUp) {
            addMagnitudes(truncated, longArrayOf(1L))
        } else {
            truncated
        }
    }

    private fun shouldRoundUp(
        guardDigit: Int,
        hasMoreAfterGuard: Boolean,
        truncatedQuotient: LongArray,
        resultSign: Int,
        roundingMode: RoundingMode,
    ): Boolean {
        val discardedIsZero = guardDigit == 0 && !hasMoreAfterGuard
        if (discardedIsZero) return false

        val discardedIsExactlyHalf = guardDigit == 5 && !hasMoreAfterGuard
        val discardedMoreThanHalf = guardDigit > 5 || (guardDigit == 5 && hasMoreAfterGuard)

        return when (roundingMode) {
            RoundingMode.UP -> true
            RoundingMode.DOWN -> false
            RoundingMode.CEILING -> resultSign > 0
            RoundingMode.FLOOR -> resultSign < 0
            RoundingMode.HALF_UP -> discardedMoreThanHalf || discardedIsExactlyHalf
            RoundingMode.HALF_DOWN -> discardedMoreThanHalf
            RoundingMode.HALF_EVEN -> {
                if (discardedMoreThanHalf) {
                    true
                } else if (discardedIsExactlyHalf) {
                    val lastDigitRem = divideWithRemainder(truncatedQuotient, longArrayOf(2L))
                    lastDigitRem.second[0] != 0L
                } else {
                    false
                }
            }
        }
    }

    private fun limbsToDigits(limbs: LongArray): String {
        if (limbs.isEmpty()) return "0"
        val sb = StringBuilder()
        sb.append(limbs[limbs.size - 1])
        for (i in limbs.size - 2 downTo 0) {
            sb.append(limbs[i].toString().padStart(LIMB_DIGITS, '0'))
        }
        val result = sb.toString().trimStart('0')
        return result.ifEmpty { "0" }
    }
}
