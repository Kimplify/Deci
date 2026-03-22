# Deci Library - Bug Fixes & Issues

> Comprehensive audit of bugs, correctness issues, and edge cases found in the Deci library.
> Date: 2026-03-20 | Version: 0.1.1

---

## Table of Contents

- [Critical Issues](#critical-issues)
- [Correctness Issues](#correctness-issues)
- [Edge Case Failures](#edge-case-failures)
- [Platform-Specific Issues](#platform-specific-issues)
- [API Contract Violations](#api-contract-violations)

---

## Critical Issues

### 1. `calculatePercentile()` Uses Double Arithmetic (Precision Loss)

**File:** `deci/src/commonMain/kotlin/org/kimplify/deci/bulk/DeciBulkOperations.kt:220`

**Problem:** The private `calculatePercentile()` function used by `filterOutliers()` performs index calculation using `Double` arithmetic, which contradicts the library's core guarantee of arbitrary-precision decimal math.

```kotlin
// Current (broken):
val index = (percentile / 100.0) * (n - 1)  // Double arithmetic!
val fraction = Deci((index - lower).toString())
```

**Impact:** For large collections, the floating-point index calculation can produce incorrect percentile boundaries, leading to valid data being incorrectly classified as outliers or outliers being retained.

**Fix:**
```kotlin
private fun calculatePercentile(sorted: List<Deci>, percentile: Int): Deci {
    val n = sorted.size
    val index = Deci(percentile).divide(Deci("100"), DeciContext.DEFAULT) * Deci(n - 1)
    val lower = index.setScale(0, RoundingMode.DOWN)
    val lowerIdx = lower.toLong().toInt()
    val upper = (lowerIdx + 1).coerceAtMost(n - 1)
    val fraction = index - lower
    return sorted[lowerIdx] + (sorted[upper] - sorted[lowerIdx]) * fraction
}
```

**Priority:** HIGH - This directly violates the library's precision guarantees.

---

### 2. Apple `hashCode()` Inconsistency

**File:** `deci/src/appleMain/kotlin/org/kimplify/deci/Deci.kt:143-147`

**Problem:** The Apple implementation's `hashCode()` uses `NSDecimalNumberHandler.defaultDecimalNumberHandler()` for rounding before hashing, which may not strip trailing zeros the same way as the JVM implementation (`BigDecimal.stripTrailingZeros().hashCode()`).

```kotlin
// Apple:
override fun hashCode(): Int =
    internal
        .decimalNumberByRoundingAccordingToBehavior(NSDecimalNumberHandler.defaultDecimalNumberHandler())
        .stringValue
        .hashCode()

// JVM:
override fun hashCode(): Int = internal.stripTrailingZeros().hashCode()
```

**Impact:** `Deci("1.0")` and `Deci("1.00")` may produce different hash codes on Apple platforms but the same on JVM. This breaks the `equals`/`hashCode` contract when these values are used in `HashMap` or `HashSet` across platforms.

**Fix:** Normalize to a canonical string representation before hashing on all platforms:
```kotlin
override fun hashCode(): Int {
    // Normalize: strip trailing zeros from string representation
    val canonical = internal.stringValue
        .trimEnd('0')
        .trimEnd('.')
        .ifEmpty { "0" }
    return canonical.hashCode()
}
```

**Priority:** HIGH - Breaks HashMap/HashSet behavior cross-platform.

---

### 3. Apple `toNativeMode()` Sign-Dependent Rounding

**File:** `deci/src/appleMain/kotlin/org/kimplify/deci/Deci.kt:149-171`

**Problem:** The `toNativeMode()` function makes `RoundingMode.UP` and `RoundingMode.DOWN` sign-dependent, checking `isPositive()` to decide between `NSRoundUp`/`NSRoundDown`. However, `isPositive()` returns `false` for zero, meaning rounding zero with `UP` or `DOWN` produces `NSRoundDown` (the `else` branch), which may cause inconsistency.

```kotlin
RoundingMode.UP ->
    if (isPositive()) NSRoundingMode.NSRoundUp
    else NSRoundingMode.NSRoundDown  // Zero falls here!

RoundingMode.DOWN ->
    if (isPositive()) NSRoundingMode.NSRoundDown
    else NSRoundingMode.NSRoundUp    // Zero falls here!
```

**Impact:** Rounding `Deci("0")` or `Deci("0.00")` with `UP`/`DOWN` may behave differently on Apple vs JVM platforms.

**Fix:** Add explicit zero check:
```kotlin
RoundingMode.UP ->
    when {
        isZero() -> NSRoundingMode.NSRoundPlain // Zero rounds to zero regardless
        isPositive() -> NSRoundingMode.NSRoundUp
        else -> NSRoundingMode.NSRoundDown
    }
```

**Priority:** HIGH - Cross-platform behavior divergence.

---

### 4. Apple `HALF_DOWN` Maps to `NSRoundDown` (Incorrect)

**File:** `deci/src/appleMain/kotlin/org/kimplify/deci/Deci.kt:169`

**Problem:** `RoundingMode.HALF_DOWN` is mapped to `NSRoundingMode.NSRoundDown`, but `NSRoundDown` always truncates toward zero regardless of the midpoint. The correct behavior for `HALF_DOWN` is to round toward the nearest neighbor, choosing the "down" direction only when the value is exactly at the midpoint (e.g., `2.5` rounds to `2`, but `2.6` rounds to `3`).

```kotlin
RoundingMode.HALF_DOWN -> NSRoundingMode.NSRoundDown  // Wrong!
```

**Impact:** `Deci("2.6").setScale(0, RoundingMode.HALF_DOWN)` returns `2` on Apple (truncated) but `3` on JVM (correctly rounded). This is a silent precision error.

**Fix:** There is no direct `NSRoundingMode` equivalent for `HALF_DOWN`. A workaround is needed:
```kotlin
RoundingMode.HALF_DOWN -> {
    // NSDecimalNumber has no HALF_DOWN equivalent.
    // Manual implementation: check if the digit at (scale+1) is exactly 5
    // with no further digits, and if so, round down; otherwise use NSRoundPlain.
    // This requires a helper function.
    NSRoundingMode.NSRoundPlain // Fallback, then fix in setScale logic
}
```

**Priority:** CRITICAL - Produces silently wrong results for a documented rounding mode.

---

## Correctness Issues

### 5. `remainder()` Uses HALF_UP Instead of DOWN for Quotient Truncation

**File:** `deci/src/commonMain/kotlin/org/kimplify/deci/math/DeciMath.kt:125`

**Problem:** The `remainder()` function rounds the quotient with `HALF_UP`, but the mathematical definition of remainder requires truncation toward zero (`DOWN`). This makes `remainder()` identical to a custom operation that doesn't match IEEE 754 or Java's `BigDecimal.remainder()` semantics.

```kotlin
// Current:
val quotient = (this / divisor).setScale(0, RoundingMode.HALF_UP)  // Wrong!

// Expected (standard remainder):
val quotient = (this / divisor).setScale(0, RoundingMode.DOWN)
```

**Impact:** `Deci("10").remainder(Deci("3"))` returns `Deci("-2")` instead of `Deci("1")`.

**Fix:**
```kotlin
fun Deci.remainder(divisor: Deci): Deci {
    if (divisor.isZero()) throw DeciDivisionByZeroException("Cannot compute remainder: divisor is zero")
    val quotient = (this / divisor).setScale(0, RoundingMode.DOWN)
    return this - (quotient * divisor)
}
```

**Priority:** HIGH - Mathematical incorrectness.

---

### 6. `precision()` Counts Leading Zeros as Significant

**File:** `deci/src/commonMain/kotlin/org/kimplify/deci/extension/DeciExtensions.kt:68-71`

**Problem:** The `precision()` function counts ALL digit characters, including leading zeros after the decimal point. For `Deci("0.00123")`, it returns `6` (counting `0`, `0`, `0`, `1`, `2`, `3`) instead of the mathematically correct `3` significant digits.

```kotlin
fun Deci.precision(): Int {
    val text = toString()
    return text.count { it.isDigit() }  // Counts ALL digits, including leading zeros
}
```

**Impact:** Any code relying on `precision()` for significant-digit calculations will get incorrect results.

**Fix:**
```kotlin
fun Deci.precision(): Int {
    val text = this.abs().toString().replace(".", "")
    val trimmed = text.trimStart('0')
    return if (trimmed.isEmpty()) 1 else trimmed.length
}
```

**Priority:** MEDIUM - Misleading API behavior. Consider whether this should be renamed to `digitCount()` if current behavior is intentional, and add a separate `significantDigits()`.

---

### 7. `toWords()` Ignores Fractional Part

**File:** `deci/src/commonMain/kotlin/org/kimplify/deci/formatting/DeciFormatting.kt:158-170`

**Problem:** `toWords()` silently discards the fractional part. `Deci("1.5").toWords()` returns `"one"` with no indication that data was lost.

```kotlin
val parts = abs.toString().split(".")
val integerPart = parts[0].toLongOrNull() ?: return "number too large"
// parts[1] (fractional part) is never used!
```

**Impact:** Users may expect `"one point five"` but get `"one"`.

**Fix:** Either handle the fractional part or document the limitation:
```kotlin
fun Deci.toWords(): String {
    // ... existing integer handling ...

    val fractionalPart = if (parts.size > 1 && parts[1] != "0") {
        val digits = parts[1].map { digitToWord(it) }
        " point ${digits.joinToString(" ")}"
    } else ""

    val result = convertIntegerToWords(integerPart) + fractionalPart
    return if (isNegative) "negative $result" else result
}
```

**Priority:** MEDIUM - Silent data loss.

---

### 8. `toWords()` Fails for Numbers >= 1000

**File:** `deci/src/commonMain/kotlin/org/kimplify/deci/formatting/DeciFormatting.kt:202`

**Problem:** `convertIntegerToWords()` returns `"number too large"` for any value >= 1000.

```kotlin
else -> "number too large"
```

**Impact:** This makes the function impractical for most real-world use. `Deci("1500").toWords()` returns `"number too large"`.

**Fix:** Extend the function to handle thousands, millions, etc.:
```kotlin
private fun convertIntegerToWords(number: Long): String {
    if (number == 0L) return "zero"
    // ... existing ones, teens, tens arrays ...

    return when {
        // ... existing cases ...
        number < 1_000_000 -> {
            val thousands = (number / 1000).toInt()
            val remainder = number % 1000
            convertIntegerToWords(thousands.toLong()) + " thousand" +
                if (remainder > 0) " ${convertIntegerToWords(remainder)}" else ""
        }
        number < 1_000_000_000 -> {
            val millions = (number / 1_000_000).toInt()
            val remainder = number % 1_000_000
            convertIntegerToWords(millions.toLong()) + " million" +
                if (remainder > 0) " ${convertIntegerToWords(remainder)}" else ""
        }
        // ... billions, trillions ...
        else -> "number too large"
    }
}
```

**Priority:** MEDIUM - Severely limited utility.

---

### 9. `isValidPercentage()` Has Asymmetric Bounds

**File:** `deci/src/commonMain/kotlin/org/kimplify/deci/validation/DeciValidation.kt:159-166`

**Problem:** When `allowNegative = true`, the min is `-100`, but when `allowOver100 = true`, the max is `1000`. These bounds are arbitrary and asymmetric.

```kotlin
val min = if (allowNegative) Deci("-100") else Deci.ZERO
val max = if (allowOver100) Deci("1000") else Deci("100")
```

**Impact:** A user might expect `allowNegative` to allow any negative percentage, but it caps at -100. Similarly, `allowOver100` caps at 1000 for no documented reason.

**Fix:** Remove the arbitrary upper/lower bounds when flags are set:
```kotlin
fun Deci.isValidPercentage(
    allowNegative: Boolean = false,
    allowOver100: Boolean = false,
): Boolean {
    if (!allowNegative && this < Deci.ZERO) return false
    if (!allowOver100 && this > Deci("100")) return false
    return true
}
```

**Priority:** LOW - Unintuitive but not a bug per se.

---

## Edge Case Failures

### 10. `formatWithThousandsSeparator()` Reverses Multi-Char Separators

**File:** `deci/src/commonMain/kotlin/org/kimplify/deci/formatting/DeciFormatting.kt:49-56`

**Problem:** The algorithm reverses the digit string, chunks it, joins with the separator, then reverses back. If the separator is a multi-character string (e.g., `" "` for thin space), the reversal of the joined string reverses the separator too.

```kotlin
val formattedInteger = digits.reversed()
    .chunked(3)
    .joinToString(separator)
    .reversed()  // This reverses the separator characters too!
```

**Example:** With separator `" ."`:
- Input: `"1234567"`
- After reverse + chunk + join: `"765. 432. 1"`
- After final reverse: `"1 .234 .567"` — separator is reversed!

**Fix:**
```kotlin
val formattedInteger = digits.reversed()
    .chunked(3)
    .reversed()
    .joinToString(separator)
```

**Priority:** MEDIUM - Breaks with non-palindromic multi-char separators.

---

### 11. `scale()` Returns Incorrect Value for Platform-Normalized Strings

**File:** `deci/src/commonMain/kotlin/org/kimplify/deci/extension/DeciExtensions.kt:56-61`

**Problem:** On JVM, `BigDecimal.stripTrailingZeros()` is called in the constructor, meaning `Deci("1.50").toString()` may return `"1.5"` on JVM. But `scale()` operates on the string representation, so the result depends on the platform's normalization behavior.

**Impact:** `Deci("1.50").scale()` returns `1` on JVM but `2` on Apple (NSDecimalNumber preserves trailing zeros differently). This is a cross-platform inconsistency.

**Fix:** Either document this as platform-dependent or standardize:
```kotlin
fun Deci.scale(): Int {
    val text = this.setScale(/* determine max meaningful scale */).toString()
    // ...
}
```

**Priority:** LOW - Expected behavior varies by platform; needs documentation.

---

### 12. `sqrt()` May Not Converge for Very Small Numbers

**File:** `deci/src/commonMain/kotlin/org/kimplify/deci/math/DeciMath.kt:26-48`

**Problem:** The Newton's method implementation uses a fixed 50 iterations and starts with `this / 2` as the initial guess. For very small numbers (e.g., `Deci("0.0000000001")`), the initial guess `0.00000000005` is poor and convergence may require more iterations at the requested precision.

**Fix:** Use a better initial guess based on the order of magnitude:
```kotlin
// Better initial guess
var x = if (this < Deci.ONE) {
    Deci.ONE  // Start from 1 for values < 1
} else {
    this.divide(DeciConstants.TWO, internalContext)
}
```

**Priority:** LOW - Unlikely to affect most users.

---

## Platform-Specific Issues

### 13. Android Implementation Is Identical to JVM

**File:** `deci/src/androidMain/kotlin/org/kimplify/deci/Deci.kt`

**Problem:** The Android `actual class Deci` is a copy-paste of the JVM implementation. Any bug fix in JVM must be manually replicated in Android.

**Fix:** Use a shared source set (e.g., `jvmAndAndroidMain`) to eliminate duplication. The convention plugin already uses `applyDefaultHierarchyTemplate()`, which supports this.

**Priority:** MEDIUM - Maintenance burden, high risk of divergence.

---

### 14. JS/WASM `decimal.js` Version Pinned Without Lock

**File:** `deci/build.gradle.kts` (npm dependency)

**Problem:** The `decimal.js` dependency is specified as `"10.6.0"` but npm resolution may allow patch updates depending on the lock file state. The `kotlin-js-store/yarn.lock` is in `.gitignore` (or not tracked).

**Fix:** Ensure `yarn.lock` is committed or pin with `=10.6.0`.

**Priority:** LOW - Could cause non-reproducible builds.

---

## API Contract Violations

### 15. `averageDeci()` Duplicates `mean()`

**File:** `deci/src/commonMain/kotlin/org/kimplify/deci/bulk/DeciBulkOperations.kt:31-35`
**File:** `deci/src/commonMain/kotlin/org/kimplify/deci/statistics/DeciStatistics.kt:22-26`

**Problem:** `averageDeci()` and `mean()` are functionally identical but live in different packages. This is confusing for library consumers.

**Fix:** Deprecate `averageDeci()` in favor of `mean()`, or make `averageDeci()` delegate to `mean()`:
```kotlin
@Deprecated("Use mean() instead", ReplaceWith("mean(context)", "org.kimplify.deci.statistics.mean"))
fun Iterable<Deci>.averageDeci(context: DeciContext = DeciContext.DEFAULT): Deci? = mean(context)
```

**Priority:** LOW - API confusion, not a bug.

---

### 16. `partitionDeci()` and `applyToAll()` Add No Value

**File:** `deci/src/commonMain/kotlin/org/kimplify/deci/bulk/DeciBulkOperations.kt:43-45, 266-268`

**Problem:** These functions are trivial wrappers around `map()` and `partition()` that add no Deci-specific logic:

```kotlin
fun Iterable<Deci>.applyToAll(operation: (Deci) -> Deci): List<Deci> = this.map(operation)
fun Iterable<Deci>.partitionDeci(predicate: (Deci) -> Boolean) = this.partition(predicate)
```

**Impact:** Clutters the API surface without providing value. Users already know `map()` and `partition()`.

**Fix:** Remove or deprecate these functions in the next minor version.

**Priority:** LOW - API hygiene.

---

## Summary

| # | Issue | Severity | File | Est. Effort |
|---|-------|----------|------|-------------|
| 1 | Double arithmetic in percentile calc | HIGH | DeciBulkOperations.kt | 30 min |
| 2 | hashCode inconsistency across platforms | HIGH | Deci.kt (apple/jvm) | 1 hour |
| 3 | Sign-dependent rounding for zero | HIGH | Deci.kt (apple) | 30 min |
| 4 | HALF_DOWN incorrect on Apple | CRITICAL | Deci.kt (apple) | 2 hours |
| 5 | remainder() uses wrong rounding | HIGH | DeciMath.kt | 15 min |
| 6 | precision() counts leading zeros | MEDIUM | DeciExtensions.kt | 30 min |
| 7 | toWords() ignores fractions | MEDIUM | DeciFormatting.kt | 1 hour |
| 8 | toWords() fails >= 1000 | MEDIUM | DeciFormatting.kt | 1 hour |
| 9 | Asymmetric percentage bounds | LOW | DeciValidation.kt | 15 min |
| 10 | Separator reversal bug | MEDIUM | DeciFormatting.kt | 15 min |
| 11 | scale() platform inconsistency | LOW | DeciExtensions.kt | 30 min |
| 12 | sqrt() convergence for small nums | LOW | DeciMath.kt | 30 min |
| 13 | Android/JVM code duplication | MEDIUM | Deci.kt (android) | 2 hours |
| 14 | npm version not locked | LOW | build.gradle.kts | 15 min |
| 15 | averageDeci/mean duplication | LOW | DeciBulkOperations.kt | 15 min |
| 16 | Trivial wrapper functions | LOW | DeciBulkOperations.kt | 15 min |
