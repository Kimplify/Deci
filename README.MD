<img src="deci-logo.svg" alt="Deci" width="430" />

> Precise decimal arithmetic for every Kotlin platform: Android, iOS, JVM, JS, WASM, and Native.

[![Maven Central](https://img.shields.io/maven-central/v/org.kimplify/deci)](https://central.sonatype.com/artifact/org.kimplify/deci)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-7F52FF)](https://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Deci is a Kotlin Multiplatform library that brings high-precision decimal arithmetic, predictable rounding, and serialization support to every Kotlin target. Built for money, taxes, invoicing, and any workload where `Float`/`Double` simply cannot be trusted.

## Table of Contents

- [Highlights](#highlights)
- [Supported Targets](#supported-targets)
- [Installation](#installation)
- [Usage](#usage)
  - [Constructing Decimals](#constructing-decimals)
  - [Arithmetic](#arithmetic)
  - [Scale & Rounding](#scale--rounding)
  - [Comparisons & Sign Helpers](#comparisons--sign-helpers)
  - [Constants & Safe Parsing](#constants--safe-parsing)
  - [DeciContext](#decicontext)
  - [Extension Helpers](#extension-helpers)
  - [Math](#math)
  - [Formatting](#formatting)
  - [Serialization](#serialization)
- [Platform Internals](#platform-internals)
- [Sample Applications](#sample-applications)
- [Testing & Quality](#testing--quality)
- [Configuration & Logging](#configuration--logging)
- [Contributing](#contributing)
- [License](#license)

## Highlights

- **Multiplatform-native** -- one dependency that runs on Android, JVM, iOS, JS, WASM, macOS, Linux, and Windows via `expect`/`actual` implementations.
- **Exact decimal math** -- all operations rely on arbitrary-precision engines (`BigDecimal`, `NSDecimalNumber`, `decimal.js`).
- **Comprehensive API** -- arithmetic operators, rounding control, comparison helpers, math extensions, formatting, validation, bulk operations, statistics, and serialization.
- **Normalization & validation** -- both `"1.234,56"` and `"1,234.56"` are accepted and normalized before evaluation.
- **Serialization ready** -- ships with a `kotlinx.serialization` `KSerializer` for seamless DTOs and network payloads.
- **Production tooling** -- comprehensive `commonTest` suite plus a Compose Multiplatform sample app covering every platform.

## Supported Targets

| Target          | Toolchain / Runtime               | Notes                                     |
|-----------------|-----------------------------------|-------------------------------------------|
| Android         | API 21+, Kotlin/JVM               | Published as an Android library variant.  |
| JVM             | Java 21 toolchain, Kotlin/JVM     | Uses `java.math.BigDecimal` internally.   |
| JS (IR)         | Kotlin/JS + `decimal.js` 10.6.0   | Browser-ready via Kotlin/JS IR compiler.  |
| WASM-JS         | Kotlin/Wasm + `decimal.js` 10.6.0 | Same API surface, shared JS runtime.      |
| iOS             | Kotlin/Native (x64, arm64, sim)   | Backed by `NSDecimalNumber`.              |
| macOS           | Kotlin/Native (x64, arm64)        | Backed by `NSDecimalNumber`.              |
| Linux x64       | Kotlin/Native                     | Pure Kotlin implementation.               |
| Windows (mingw) | Kotlin/Native                     | Pure Kotlin implementation.               |

## Installation

Add Maven Central and the dependency to your Gradle build:

```kotlin
// settings.gradle.kts or build.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}
```

### Kotlin Multiplatform module

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("org.kimplify:deci:0.1.1")
        }
    }
}
```

### Single-platform Gradle module

```kotlin
dependencies {
    implementation("org.kimplify:deci:0.1.1")
}
```

> Use a version catalog or Gradle property so the same version is applied everywhere.

## Usage

### Constructing Decimals

```kotlin
val fromString = Deci("123.45")
val fromComma  = Deci("1.234,56")   // normalized to 1234.56
val fromInt    = Deci(100)
val fromLong   = Deci(1000L)
val fromDouble = Deci(99.99)
```

Inputs are trimmed, validated by a locale-aware regular expression, and normalized so `"1 234,56"`, `"1,234.56"`, and `"1234.56"` all behave consistently.

### Arithmetic

```kotlin
val a = Deci("10.5")
val b = Deci("2.3")

val sum        = a + b    // 12.8
val difference = a - b    // 8.2
val product    = a * b    // 24.15
val quotient   = a / b    // 4.565217391304347826086956521739130435
val remainder  = a % b    // 1.3
val negated    = -a       // -10.5
```

### Scale & Rounding

```kotlin
val pi = Deci("3.14159265359")

val rounded   = pi.setScale(2, RoundingMode.HALF_UP)                       // 3.14
val repeating = Deci("1").divide(Deci("3"), 6, RoundingMode.HALF_UP)       // 0.333333
```

Supported `RoundingMode` values:

| Mode        | Behavior                                |
|-------------|-----------------------------------------|
| `UP`        | Round away from zero.                   |
| `DOWN`      | Truncate toward zero.                   |
| `CEILING`   | Toward positive infinity.               |
| `FLOOR`     | Toward negative infinity.               |
| `HALF_UP`   | Round to nearest; ties go up.           |
| `HALF_DOWN` | Round to nearest; ties go down.         |
| `HALF_EVEN` | Banker's rounding; ties go to even digit. |

### Comparisons & Sign Helpers

```kotlin
val x = Deci("5.5")
val y = Deci("3.2")

x > y              // true
x.max(y)           // 5.5
x.min(y)           // 3.2

val negative = Deci("-7.5")
negative.abs()        // 7.5
negative.negate()     // 7.5
negative.isNegative() // true
negative.isPositive() // false
negative.isZero()     // false
```

### Constants & Safe Parsing

```kotlin
// Companion constants
Deci.ZERO   // 0
Deci.ONE    // 1
Deci.TEN    // 10

// Extended constants
DeciConstants.PI          // 3.1415926535897932384626433832795
DeciConstants.E           // 2.7182818284590452353602874713527
DeciConstants.HALF        // 0.5
DeciConstants.HUNDRED     // 100
DeciConstants.THOUSAND    // 1000
DeciConstants.NEGATIVE_ONE // -1

// Safe parsing
val maybeNumber = Deci.fromStringOrNull("42.5")   // Deci(42.5)
val safeNumber  = Deci.fromStringOrZero("invalid") // Deci.ZERO
```

### DeciContext

`DeciContext` bundles a precision (number of fractional digits) and a `RoundingMode` into a single reusable object. Pass it to `divide` or math operations instead of repeating scale and rounding mode everywhere.

```kotlin
val ctx = DeciContext(precision = 4, roundingMode = RoundingMode.HALF_UP)
val result = Deci("10").divide(Deci("3"), ctx) // 3.3333

// Built-in presets
DeciContext.DEFAULT       // 20 digits, HALF_UP
DeciContext.CURRENCY_USD  // 2 digits,  HALF_UP
DeciContext.BANKING       // 2 digits,  HALF_EVEN (banker's rounding)
```

### Extension Helpers

```kotlin
// Type conversions
val fromInt    = 42.toDeci()
val fromLong   = 1000L.toDeci()
val fromString = "123.45".toDeci()
val safe       = "maybe".toDeciOrNull()  // null

// Collection helpers
val numbers = listOf(Deci("1.5"), Deci("2.3"), Deci("3.7"))
val total   = numbers.sumDeci()          // 7.5

// Long conversion
val asLong = Deci("42.7").toLong()       // 42

// Introspection
val s = Deci("1.50").scale()             // 2
val p = Deci("1.50").precision()         // 3
```

### Math

```kotlin
val base = Deci("2")
val cube = base.pow(Deci("3"))           // 8

Deci("16").sqrt()                        // 4.0000000000  (10 decimal places by default)
Deci("16").sqrt(precision = 2)           // 4.00

Deci("10").mod(Deci("3"))               // 1
Deci("10").remainder(Deci("3"))         // 1

Deci("1234").roundToNearest(Deci("50")) // 1250
Deci("1234.567").roundToSignificantDigits(4) // 1235
```

### Formatting

```kotlin
val price = Deci("1234567.891")

price.formatCurrency()                             // "$1,234,567.89"
price.formatCurrency(currencySymbol = "EUR ", scale = 2) // "EUR 1,234,567.89"
price.formatWithThousandsSeparator()               // "1,234,567.891"
price.formatAsPercentage()                         // "123456789.1%"
price.toScientificNotation()                       // "1.234567E+6"
price.format("#,##0.00")                           // "1,234,567.89"

Deci("42").toWords()                               // "forty two"
Deci("7.5").pad(10)                                // "       7.5"
```

### Serialization

Deci ships with a `kotlinx.serialization` serializer, so you can embed it directly in DTOs:

```kotlin
@Serializable
data class Price(
    val amount: Deci,
    val currency: String
)

val json = Json.encodeToString(Price.serializer(), Price(Deci("99.99"), "USD"))
// {"amount":"99.99","currency":"USD"}

val parsed = Json.decodeFromString(Price.serializer(), json)
```

Values serialize as JSON **strings** (not numbers) to preserve trailing zeros and avoid lossy float coercion by JSON parsers.

## Platform Internals

| Platform       | Backend                   | Notes                                                                 |
|----------------|---------------------------|-----------------------------------------------------------------------|
| JVM / Android  | `java.math.BigDecimal`    | `equals`/`hashCode` delegates to stripped `BigDecimal` so `1.0 == 1`. |
| iOS / macOS    | `NSDecimalNumber`         | Foundation-backed; identical API surface.                             |
| JS / WASM      | `decimal.js` v10.6.0      | Arbitrary-precision math in browsers.                                |
| Linux / Windows| Pure Kotlin               | String-based decimal arithmetic; no native dependencies.             |

All implementations are coordinated through Kotlin's `expect`/`actual` mechanism, guaranteeing identical semantics on every platform.

A locale-aware regex and the `normalizeDecimalString` helper sanitize input so grouping separators and decimal commas are accepted safely.

## Sample Applications

The repository ships with a Compose Multiplatform demo that exercises the entire API surface:

| Platform      | Command                                                      |
|---------------|--------------------------------------------------------------|
| Desktop (JVM) | `./gradlew :sample:composeApp:run`                           |
| Android       | `./gradlew :sample:composeApp:installDebug`                  |
| Web (JS)      | `./gradlew :sample:composeApp:jsBrowserDevelopmentRun`       |
| Web (WASM)    | `./gradlew :sample:composeApp:wasmJsBrowserDevelopmentRun`   |
| iOS           | Open `sample/iosApp/iosApp.xcodeproj` in Xcode and run.      |

## Testing & Quality

Run the multiplatform test suite before submitting changes:

```bash
./gradlew :deci:allTests       # every target
./gradlew :deci:jvmTest        # JVM only
./gradlew :deci:jsTest         # JS only
```

Add `--stacktrace` or `--info` when debugging rounding edge cases.

## Configuration & Logging

Deci exposes a lightweight configuration object you can tweak at runtime:

```kotlin
DeciConfiguration.divisionPolicy = DeciDivisionPolicy(
    fractionalDigits = 6,
    roundingMode = RoundingMode.HALF_UP
)

// Plug in any logging backend via DeciLogSink (disabled by default)
DeciConfiguration.logSink = DeciLogSink { tag, message ->
    println("[$tag] $message")
}

// Or route to your existing logger (Timber, SLF4J, etc.)
DeciConfiguration.logSink = DeciLogSink { tag, message ->
    Timber.tag(tag).d(message)
}

// Restore defaults when you are done
DeciConfiguration.resetDivisionPolicy()
DeciConfiguration.disableLogging()
```

Logs are emitted with the `Deci` tag. Set `logSink` to `null` (default) for zero-overhead production builds.

## Contributing

1. Fork the repository and create a feature branch.
2. Make your changes plus the relevant tests (see `deci/src/commonTest`).
3. Run `./gradlew :deci:allTests` and, if you touch the sample, the relevant run tasks.
4. Open a Pull Request describing the motivation, implementation details, and verification steps.

Bug reports and feature requests are welcome -- please include reproducible steps or a failing test case when possible.

## License

Deci is released under the [MIT License](LICENSE).
