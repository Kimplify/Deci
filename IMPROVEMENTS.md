# Deci Library - Improvements & Refactoring

> Code quality improvements, architectural enhancements, performance optimizations, and developer experience upgrades.
> Date: 2026-03-20 | Version: 0.1.1

---

## Table of Contents

- [Architecture Improvements](#architecture-improvements)
- [API Design Improvements](#api-design-improvements)
- [Performance Optimizations](#performance-optimizations)
- [Code Quality & Maintainability](#code-quality--maintainability)
- [Testing Improvements](#testing-improvements)
- [Build & Tooling](#build--tooling)
- [Documentation Improvements](#documentation-improvements)

---

## Architecture Improvements

### 1. Eliminate Android/JVM Source Duplication

**Current State:** `androidMain` and `jvmMain` contain identical `Deci.kt` implementations (~140 lines each), both wrapping `java.math.BigDecimal`.

**Problem:** Every bug fix or feature change must be applied twice. The implementations can silently diverge.

**Improvement:** Create a shared intermediate source set using Kotlin's hierarchical project structure:

```kotlin
// In convention plugin (deci.kmp.library.gradle.kts):
kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("jvmCommon") {
                withJvm()
                withAndroidTarget()
            }
        }
    }
}
```

Then move the shared `Deci.kt` to `jvmCommonMain/kotlin/org/kimplify/deci/Deci.kt`.

**Impact:** Eliminates ~140 lines of duplicated code, prevents future divergence.

---

### 2. Migrate Away from Global Mutable Configuration

**Current State:** `DeciConfiguration` is a singleton with `@Volatile` mutable state:

```kotlin
object DeciConfiguration {
    @Volatile var divisionPolicy: DeciDivisionPolicy = defaultDivisionPolicy
    @Volatile var logSink: DeciLogSink? = null
}
```

**Problem:**
- Global mutable state makes testing difficult (requires `resetDivisionPolicy()` calls)
- Not reactive — changes don't affect already-constructed `Deci` instances
- Thread-safe via `@Volatile` but not atomically consistent for compound reads
- The `div` operator on every platform reads `DeciConfiguration.divisionPolicy` on each call

**Improvement:** Complete the migration to `DeciContext`:
1. Make the `div` operator use `DeciContext.DEFAULT` directly instead of reading from `DeciConfiguration`
2. Add a deprecation notice with a migration timeline
3. Eventually make `divisionPolicy` internal

```kotlin
// Phase 1: Change div to use DeciContext.DEFAULT
actual operator fun div(other: Deci): Deci {
    if (other.isZero()) throw DeciDivisionByZeroException()
    return divide(other, DeciContext.DEFAULT)
}

// Phase 2 (next major version): Remove DeciConfiguration.divisionPolicy
```

---

### 3. Introduce `DeciScope` for Scoped Configuration

**Current State:** Users must either use global configuration or pass `DeciContext` to every operation.

**Improvement:** Add a DSL-style scope for operations that share the same context:

```kotlin
data class DeciScope(val context: DeciContext) {
    fun Deci.div(other: Deci): Deci = this.divide(other, context)
    fun Iterable<Deci>.mean(): Deci? = this.mean(context)
    // ... other context-dependent operations
}

fun withDeciContext(context: DeciContext, block: DeciScope.() -> Unit) {
    DeciScope(context).block()
}

// Usage:
withDeciContext(DeciContext.CURRENCY_USD) {
    val total = price.div(quantity)
    val average = prices.mean()
}
```

---

### 4. Sealed Interface for Rounding Results

**Current State:** Rounding operations return raw `Deci` values with no way to know if rounding actually occurred.

**Improvement:** For financial applications, add an optional "auditable rounding" API:

```kotlin
data class RoundingResult(
    val value: Deci,
    val originalValue: Deci,
    val wasRounded: Boolean,
    val roundingDelta: Deci,
)

fun Deci.setScaleAudited(scale: Int, roundingMode: RoundingMode): RoundingResult {
    val rounded = setScale(scale, roundingMode)
    return RoundingResult(
        value = rounded,
        originalValue = this,
        wasRounded = rounded != this,
        roundingDelta = rounded - this,
    )
}
```

---

## API Design Improvements

### 5. Add `orZero()` Extension for Null-Coalescing

**Current State:** Many statistical functions return `Deci?`, forcing users into repetitive patterns:

```kotlin
val avg = prices.mean() ?: Deci.ZERO
val med = prices.median() ?: Deci.ZERO
val dev = prices.standardDeviation() ?: Deci.ZERO
```

**Improvement:**
```kotlin
fun Deci?.orZero(): Deci = this ?: Deci.ZERO
fun Deci?.orOne(): Deci = this ?: Deci.ONE
fun Deci?.orDefault(default: Deci): Deci = this ?: default

// Usage:
val avg = prices.mean().orZero()
```

---

### 6. Add `toInt()` and `toFloat()` Conversions

**Current State:** The library provides `toDouble()` and `toLong()` but not `toInt()` or `toFloat()`.

**Improvement:**
```kotlin
fun Deci.toInt(): Int {
    val long = toLong()
    if (long < Int.MIN_VALUE || long > Int.MAX_VALUE) {
        throw DeciOverflowException(value = this.toString())
    }
    return long.toInt()
}

fun Deci.toIntOrNull(): Int? {
    val long = toLongOrNull() ?: return null
    return if (long in Int.MIN_VALUE..Int.MAX_VALUE) long.toInt() else null
}

fun Deci.toFloat(): Float = toDouble().toFloat()
```

---

### 7. Improve `format()` to Support Custom Patterns

**Current State:** `format()` only supports 4 hardcoded patterns and throws for anything else:

```kotlin
fun Deci.format(pattern: String): String = when (pattern) {
    "0.00" -> ...
    "#,##0.00" -> ...
    "0.0000" -> ...
    "#,##0" -> ...
    else -> throw DeciFormatException(pattern)
}
```

**Improvement:** Parse the pattern dynamically:

```kotlin
fun Deci.format(pattern: String): String {
    val hasGrouping = pattern.contains('#') || pattern.contains(",")
    val decimalPlaces = pattern.substringAfterLast('.', "").length
    val hasDecimal = pattern.contains('.')

    val scaled = if (hasDecimal) {
        this.setScale(decimalPlaces, RoundingMode.HALF_UP)
    } else {
        this.setScale(0, RoundingMode.HALF_UP)
    }

    return if (hasGrouping) {
        scaled.formatWithThousandsSeparator()
    } else {
        scaled.toString()
    }
}
```

---

### 8. Add Infix Comparison Functions

**Current State:** Comparisons require verbose `compareTo` calls or operator overloads.

**Improvement:** Add readable infix functions:

```kotlin
infix fun Deci.isGreaterThan(other: Deci): Boolean = this > other
infix fun Deci.isLessThan(other: Deci): Boolean = this < other
infix fun Deci.isEqualTo(other: Deci): Boolean = this.compareTo(other) == 0

// Usage:
if (price isGreaterThan threshold) { ... }
```

---

### 9. Add `coerceIn()`, `coerceAtLeast()`, `coerceAtMost()`

**Current State:** `clamp()` exists in validation but follows a different naming convention than Kotlin stdlib.

**Improvement:** Add stdlib-aligned extension functions:

```kotlin
fun Deci.coerceIn(min: Deci, max: Deci): Deci = clamp(min, max)
fun Deci.coerceAtLeast(min: Deci): Deci = if (this < min) min else this
fun Deci.coerceAtMost(max: Deci): Deci = if (this > max) max else this
```

---

### 10. Builder Pattern for `DeciContext`

**Current State:** `DeciContext` only has 3 presets (DEFAULT, CURRENCY_USD, BANKING).

**Improvement:** Add a builder with common financial presets:

```kotlin
data class DeciContext(...) {
    companion object {
        // ... existing presets ...

        val CURRENCY_EUR = DeciContext(precision = 2, roundingMode = RoundingMode.HALF_UP)
        val CURRENCY_JPY = DeciContext(precision = 0, roundingMode = RoundingMode.HALF_UP)
        val CURRENCY_BTC = DeciContext(precision = 8, roundingMode = RoundingMode.HALF_UP)
        val HIGH_PRECISION = DeciContext(precision = 50, roundingMode = RoundingMode.HALF_UP)
        val SCIENTIFIC = DeciContext(precision = 15, roundingMode = RoundingMode.HALF_EVEN)

        fun forCurrency(currencyCode: String): DeciContext = when (currencyCode.uppercase()) {
            "USD", "EUR", "GBP", "CAD", "AUD" -> CURRENCY_USD
            "JPY", "KRW" -> CURRENCY_JPY
            "BTC" -> CURRENCY_BTC
            else -> CURRENCY_USD
        }
    }
}
```

---

## Performance Optimizations

### 11. Cache Common String Operations

**Current State:** `isZero()`, `isNegative()`, `isPositive()` on JVM call `BigDecimal.signum()` every time (efficient). But `scale()`, `precision()`, `isWhole()` all call `toString()` and parse the string.

**Improvement:** Consider caching the string representation since `Deci` is immutable:

```kotlin
actual class Deci private constructor(private val internal: BigDecimal) : Comparable<Deci> {
    // Lazy-cached string representation
    private val cachedString: String by lazy { internal.toPlainString() }

    actual override fun toString(): String = cachedString
}
```

---

### 12. Optimize `sumDeci()` for Large Collections

**Current State:** Uses `fold()` which creates intermediate `Deci` objects on every addition:

```kotlin
fun Iterable<Deci>.sumDeci(): Deci =
    this.fold(Deci.ZERO) { accumulated, value -> accumulated + value }
```

**Improvement:** On JVM, accumulate using `BigDecimal` directly to avoid string parsing overhead:

```kotlin
// JVM-specific optimized version
actual fun Iterable<Deci>.sumDeciOptimized(): Deci {
    var acc = BigDecimal.ZERO
    for (value in this) {
        acc = acc.add(value.internal)
    }
    return Deci(acc)
}
```

---

### 13. Avoid Repeated `toList()` in Statistical Functions

**Current State:** `variance()` calls `this.toList()` and then calls `values.mean()` which calls `this.toList()` again. For an `Iterable` backed by a non-list, this creates two copies.

```kotlin
fun Iterable<Deci>.variance(...): Deci? {
    val values = this.toList()         // First copy
    // ...
    val mean = values.mean(context)     // values.mean() calls toList() again!
    // ...
}
```

**Fix:** Pass the list directly or extract the mean calculation inline:

```kotlin
fun Iterable<Deci>.variance(...): Deci? {
    val values = this.toList()
    if (values.isEmpty()) return null
    if (!isPopulation && values.size <= 1) return null

    val sum = values.sumDeci()
    val mean = sum.divide(Deci(values.size), context)
    // ... rest uses `mean` directly
}
```

---

## Code Quality & Maintainability

### 14. Standardize Null vs Empty Semantics

**Current State:** Inconsistent return behavior for empty collections:

| Function | Empty Collection Return |
|----------|----------------------|
| `sumDeci()` | `Deci.ZERO` |
| `multiplyAll()` | `Deci.ONE` |
| `mean()` | `null` |
| `median()` | `null` |
| `cumulativeSum()` | `emptyList()` |
| `topN(n)` | `emptyList()` |

**Improvement:** Document and standardize the conventions:
1. Functions returning a single `Deci` from aggregation → return `null` for empty
2. Functions returning a collection → return `emptyList()` for empty
3. Functions with mathematical identity → return the identity (`ZERO` for sum, `ONE` for product)

Add KDoc explaining the convention in a central place.

---

### 15. Replace Regex Comments with Named Patterns

**Current State:** The `DECIMAL_REGEX` is a complex single-line regex with no explanation:

```kotlin
internal val DECIMAL_REGEX = Regex(
    """^[-+]?(?:\d{1,3}(?:[.,]\d{3})*(?:[.,]\d*)?|\d+[.,]\d*|\d+|[.,]\d+)$"""
)
```

**Improvement:** Break into named components:

```kotlin
internal val DECIMAL_REGEX: Regex by lazy {
    val sign = """[-+]?"""
    val groupedNumber = """\d{1,3}(?:[.,]\d{3})*(?:[.,]\d*)?""" // 1,234,567.89 or 1.234.567,89
    val decimalWithLeading = """\d+[.,]\d*"""                     // 123.45 or 123,45
    val integerOnly = """\d+"""                                    // 123
    val decimalWithoutLeading = """[.,]\d+"""                      // .45 or ,45

    Regex("^$sign(?:$groupedNumber|$decimalWithLeading|$integerOnly|$decimalWithoutLeading)$")
}
```

---

### 16. Add `@Immutable` Annotations for Compose Stability

**Current State:** The recent commit `9d23f6b` added `@Immutable` annotations, but the coverage may be incomplete.

**Improvement:** Ensure all public data classes and value-bearing classes are annotated:

```kotlin
@Immutable
data class DeciContext(...)

@Immutable
data class DeciDivisionPolicy(...)

@Immutable
data class ValidationResult(...)

@Immutable
data class RoundingResult(...)  // If added
```

---

### 17. Use `require()` Consistently for Preconditions

**Current State:** Some functions throw custom exceptions for invalid input while others use `require()`:

```kotlin
// Uses require:
require(digits > 0) { "Number of significant digits must be positive: $digits" }

// Throws custom exception:
if (scale < 0) throw DeciScaleException(scale)
```

**Improvement:** Standardize the convention:
- **Public API boundary:** Throw typed `DeciException` subclasses (for consumer error handling)
- **Internal/extension boundary:** Use `require()` (for programming errors)

Document this convention in CLAUDE.md.

---

### 18. Extract Rounding Mode Conversion to a Shared Utility

**Current State:** Each platform has its own rounding mode conversion:
- JVM: `convert(mode: RoundingMode): JavaRoundingMode`
- Apple: `toNativeMode(mode: RoundingMode): NSRoundingMode`
- JS: Inline number mapping

**Improvement:** Create a platform-specific extension in each source set:

```kotlin
// In each platform source set:
internal expect fun RoundingMode.toPlatformMode(): PlatformRoundingMode
```

This centralizes the conversion logic and makes it easier to test independently.

---

## Testing Improvements

### 19. Add Cross-Platform Consistency Tests

**Current State:** All tests are in `commonTest`, which runs on each platform independently. But there's no test that explicitly verifies cross-platform consistency.

**Improvement:** Add a dedicated test suite that captures expected values and verifies them across all platforms:

```kotlin
class CrossPlatformConsistencyTest {
    @Test
    fun `setScale produces identical results across platforms`() {
        val testCases = listOf(
            Triple("2.5", 0, RoundingMode.HALF_UP) to "3",
            Triple("2.5", 0, RoundingMode.HALF_DOWN) to "2",
            Triple("2.5", 0, RoundingMode.HALF_EVEN) to "2",
            Triple("-2.5", 0, RoundingMode.UP) to "-3",
            Triple("-2.5", 0, RoundingMode.DOWN) to "-2",
        )

        for ((input, expected) in testCases) {
            val (value, scale, mode) = input
            assertEquals(expected, Deci(value).setScale(scale, mode).toString(),
                "Failed for $value with scale=$scale, mode=$mode")
        }
    }
}
```

---

### 20. Add Benchmark Tests

**Current State:** No performance benchmarks exist.

**Improvement:** Add a benchmarking module using `kotlinx-benchmark`:

```kotlin
@State(Scope.Benchmark)
class DeciBenchmark {
    private val values = (1..10_000).map { Deci(it.toString()) }

    @Benchmark
    fun sumLargeCollection() = values.sumDeci()

    @Benchmark
    fun multiplyChain() {
        var result = Deci.ONE
        repeat(1000) { result *= Deci("1.001") }
    }

    @Benchmark
    fun divisionWithContext() {
        Deci("1").divide(Deci("3"), DeciContext.DEFAULT)
    }

    @Benchmark
    fun stringParsing() = Deci("123456789.123456789")
}
```

---

### 21. Add Fuzz Testing for Parser

**Current State:** The parser has regex and normalization tests, but no fuzz/random testing.

**Improvement:** Use kotest property testing to generate random strings:

```kotlin
class DeciParserFuzzTest : FunSpec({
    test("parser should not throw unexpected exceptions") {
        checkAll(Arb.string(0..50)) { input ->
            // Should either parse successfully or throw DeciParseException
            val result = runCatching { Deci(input) }
            if (result.isFailure) {
                result.exceptionOrNull() shouldBeInstanceOf DeciParseException::class
            }
        }
    }
})
```

---

### 22. Add Platform-Specific Integration Tests

**Current State:** No tests verify platform-specific implementation details (e.g., NSDecimalNumber behavior on Apple).

**Improvement:** Add tests in `appleTest`, `jvmTest`, etc. that verify platform-specific edge cases:

```kotlin
// In appleTest:
class AppleDeciTest {
    @Test
    fun `NSDecimalNumber handles subnormal values`() { ... }

    @Test
    fun `rounding mode mapping is correct for all modes`() { ... }
}
```

---

## Build & Tooling

### 23. Add Detekt for Static Analysis

**Current State:** Only ktlint (formatting) is configured. No static analysis for code smells.

**Improvement:** Add Detekt:

```kotlin
// In build-logic convention plugin:
plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config.setFrom("$rootDir/config/detekt.yml")
    buildUponDefaultConfig = true
}
```

Key rules to enable:
- `complexity/LongMethod`
- `style/MagicNumber`
- `potential-bugs/EqualsWithHashCodeExist`
- `performance/SpreadOperator`

---

### 24. Add Code Coverage Reporting

**Current State:** No code coverage tool is configured.

**Improvement:** Add Kover (official Kotlin coverage tool):

```kotlin
plugins {
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

kover {
    reports {
        total {
            html { onCheck = true }
            verify {
                rule { minBound(80) } // Enforce 80% coverage
            }
        }
    }
}
```

---

### 25. Add Dokka for API Documentation

**Current State:** KDoc comments exist but there's no HTML documentation generation.

**Improvement:** Add Dokka to generate API docs:

```kotlin
plugins {
    id("org.jetbrains.dokka") version "2.0.0"
}
```

Generate docs as part of CI and publish to GitHub Pages.

---

### 26. Upgrade Kotest to Stable Release

**Current State:** Using `kotest = "6.0.0.M1"` (milestone/pre-release).

**Improvement:** Monitor for stable 6.0.0 release and upgrade. Milestone releases may have breaking changes.

---

### 27. Add GitHub Actions for All Platforms

**Current State:** CI runs on `macos-latest` and tests JVM, iOS, JS, WASM. No Linux or Windows testing.

**Improvement:** Add matrix builds:

```yaml
strategy:
  matrix:
    os: [macos-latest, ubuntu-latest, windows-latest]
    include:
      - os: macos-latest
        tasks: ":deci:allTests"
      - os: ubuntu-latest
        tasks: ":deci:jvmTest :deci:jsTest :deci:wasmJsTest"
      - os: windows-latest
        tasks: ":deci:jvmTest"
```

---

## Documentation Improvements

### 28. Add Migration Guide for DeciContext

**Current State:** `DeciConfiguration.divisionPolicy` is deprecated but no migration guide exists.

**Improvement:** Create a `MIGRATION.md`:

```markdown
## Migrating from DeciConfiguration to DeciContext

### Before (deprecated):
DeciConfiguration.divisionPolicy = DeciDivisionPolicy(2, RoundingMode.HALF_UP)
val result = a / b

### After (recommended):
val result = a.divide(b, DeciContext.CURRENCY_USD)
```

---

### 29. Add CHANGELOG.md

**Current State:** No changelog exists.

**Improvement:** Create `CHANGELOG.md` following [Keep a Changelog](https://keepachangelog.com/) format:

```markdown
# Changelog

## [0.1.1] - 2026-XX-XX
### Added
- @Immutable annotations for Compose stability
### Changed
- Deprecated DeciConfiguration.divisionPolicy in favor of DeciContext
```

---

### 30. Add KDoc to All Public Functions

**Current State:** Most functions have KDoc, but some lack `@throws` annotations or have incomplete parameter documentation.

**Improvement:** Audit all public functions for complete KDoc coverage:
- Every `@param` documented
- Every `@throws` listed
- Every `@return` described
- Cross-references via `[links]` where helpful

---

## Summary

| # | Improvement | Category | Impact | Effort |
|---|-------------|----------|--------|--------|
| 1 | Eliminate Android/JVM duplication | Architecture | HIGH | Medium |
| 2 | Migrate from global configuration | Architecture | HIGH | Large |
| 3 | DeciScope for scoped config | Architecture | MEDIUM | Medium |
| 4 | Auditable rounding results | Architecture | LOW | Small |
| 5 | `orZero()` extension | API Design | MEDIUM | Small |
| 6 | `toInt()`/`toFloat()` conversions | API Design | MEDIUM | Small |
| 7 | Dynamic format patterns | API Design | MEDIUM | Medium |
| 8 | Infix comparison functions | API Design | LOW | Small |
| 9 | `coerceIn()` stdlib alignment | API Design | LOW | Small |
| 10 | DeciContext builder + presets | API Design | MEDIUM | Small |
| 11 | Cache string representation | Performance | MEDIUM | Small |
| 12 | Optimize sumDeci for JVM | Performance | LOW | Medium |
| 13 | Avoid repeated toList() | Performance | LOW | Small |
| 14 | Standardize null semantics | Code Quality | MEDIUM | Small |
| 15 | Named regex patterns | Code Quality | LOW | Small |
| 16 | Complete @Immutable coverage | Code Quality | LOW | Small |
| 17 | Consistent precondition checks | Code Quality | LOW | Small |
| 18 | Shared rounding mode utility | Code Quality | LOW | Medium |
| 19 | Cross-platform consistency tests | Testing | HIGH | Medium |
| 20 | Benchmark tests | Testing | MEDIUM | Large |
| 21 | Fuzz testing for parser | Testing | MEDIUM | Small |
| 22 | Platform-specific tests | Testing | HIGH | Medium |
| 23 | Add Detekt | Build & Tooling | MEDIUM | Small |
| 24 | Add Kover coverage | Build & Tooling | HIGH | Small |
| 25 | Add Dokka | Build & Tooling | MEDIUM | Small |
| 26 | Upgrade Kotest stable | Build & Tooling | LOW | Small |
| 27 | Multi-OS CI matrix | Build & Tooling | MEDIUM | Small |
| 28 | Migration guide | Documentation | MEDIUM | Small |
| 29 | CHANGELOG.md | Documentation | MEDIUM | Small |
| 30 | Complete KDoc audit | Documentation | LOW | Medium |
