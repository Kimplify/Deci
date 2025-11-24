# Deci
> Precise decimal arithmetic for every Kotlin platform: Android, iOS, JVM, JS, and WASM.

[![Maven Central](https://img.shields.io/maven-central/v/org.kimplify.deci/deci)](https://central.sonatype.com/artifact/org.kimplify.deci/deci)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-7F52FF)](https://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Deci is a Kotlin Multiplatform library that brings high‑precision decimal arithmetic, predictable rounding, and serialization support to every Kotlin target. It is built for money, taxes, invoicing, and any other workloads where `Float`/`Double` simply cannot be trusted.

## Table of Contents

1. [Highlights](#highlights)
2. [Supported Targets](#supported-targets)
3. [Installation](#installation)
4. [Usage](#usage)
    - [Constructing Decimals](#constructing-decimals)
    - [Arithmetic](#arithmetic)
    - [Scale & Rounding](#scale--rounding)
    - [Comparisons & Sign Helpers](#comparisons--sign-helpers)
    - [Constants & Safe Parsing](#constants--safe-parsing)
    - [Extension Helpers](#extension-helpers)
    - [Serialization](#serialization)
5. [Platform Internals](#platform-internals)
6. [Sample Applications](#sample-applications)
7. [Testing & Quality](#testing--quality)
8. [Configuration & Logging](#configuration--logging)
9. [Contributing](#contributing)
10. [License](#license)

## Highlights

- **Multiplatform-native** – one dependency that runs on Android, JVM, iOS, JS, and WASM with expect/actual implementations.
- **Exact decimal math** – all operations rely on arbitrary precision engines (`BigDecimal`, `NSDecimalNumber`, `decimal.js`).
- **Comprehensive API** – arithmetic operators, rounding control, comparison helpers, power/sum extensions, and serialization.
- **Normalization & validation** – both `"1.234,56"` and `"1,234.56"` are accepted and normalized before evaluation.
- **Serialization ready** – ships with a `kotlinx.serialization` `KSerializer` for seamless DTOs and network payloads.
- **Production tooling** – battle-tested `commonTest` suite plus a Compose Multiplatform sample app covering every platform.

## Supported Targets

| Target        | Toolchain / Runtime                | Notes |
|---------------|------------------------------------|-------|
| Android       | API 21+, Kotlin 2.2.21             | Published as an Android library variant. |
| JVM           | Java 17 toolchain, Kotlin/JVM      | Uses `java.math.BigDecimal` internally. |
| JS (IR)       | Kotlin/JS + `decimal.js` 10.6.0    | Works for browser bundlers. |
| WASM-JS       | Kotlin/Wasm + `decimal.js` 10.6.0  | Same surface API, shared JS runtime. |
| iOS           | Xcode 15+, Kotlin/Native           | Backed by `NSDecimalNumber`. |

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
        val commonMain by getting {
            dependencies {
                implementation("org.kimplify.deci:deci:1.0.0")
            }
        }
    }
}
```

### Single-platform Gradle module

```kotlin
dependencies {
    implementation("org.kimplify.deci:deci:1.0.0")
}
```

> ℹ️ Use a version catalog or Gradle property (`val deciVersion = "1.0.0"`) so the same version is applied everywhere.

## Usage

### Constructing Decimals

```kotlin
val fromString = Deci("123.45")
val fromComma = Deci("1.234,56") // normalized to 1234.56
val fromInt = Deci(100)
val fromLong = Deci(1000L)
val fromDouble = Deci(99.99)
```

Inputs are trimmed, validated by a locale-aware regular expression, and normalized so `"1 234,56"`, `"1,234.56"`, and `"1234.56"` all behave consistently.

### Arithmetic

```kotlin
val a = Deci("10.5")
val b = Deci("2.3")

val sum = a + b          // 12.8
val difference = a - b   // 8.2
val product = a * b      // 24.15
val quotient = a / b     // 4.565217391304347826086956521739130435
```

### Scale & Rounding

```kotlin
val pi = Deci("3.14159265359")

val rounded = pi.setScale(2, RoundingMode.HALF_UP)           // 3.14
val repeating = Deci("1").divide(Deci("3"), 6, RoundingMode.HALF_UP) // 0.333333
```

Supported `RoundingMode` values:

| Mode | Behavior |
|------|----------|
| `UP` | Round away from zero. |
| `DOWN` | Truncate toward zero. |
| `CEILING` | Toward +∞. |
| `FLOOR` | Toward −∞. |
| `HALF_UP` | Round to nearest, ties go up. |
| `HALF_DOWN` | Round to nearest, ties go down. |
| `HALF_EVEN` | Banker’s rounding; ties go to even digit. |

### Comparisons & Sign Helpers

```kotlin
val x = Deci("5.5")
val y = Deci("3.2")

x > y        // true
x.max(y)     // 5.5
x.min(y)     // 3.2

val negative = Deci("-7.5")
negative.abs()        // 7.5
negative.negate()     // 7.5
negative.isNegative() // true
negative.isPositive() // false
negative.isZero()     // false
```

### Constants & Safe Parsing

```kotlin
Deci.ZERO   // 0
Deci.ONE    // 1
Deci.TEN    // 10

val maybeNumber = Deci.fromStringOrNull("42.5")   // Deci(42.5)
val safeNumber = Deci.fromStringOrZero("invalid") // Deci.ZERO
```

### Extension Helpers

```kotlin
val base = Deci("2")
val cube = base.pow(3)                    // 8

val numbers = listOf(Deci("1.5"), Deci("2.3"), Deci("3.7"))
val total = numbers.sumDeci()             // 7.5

val asLong = Deci("42.7").toLong()        // 42
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

## Platform Internals

- **JVM / Android** – wraps `java.math.BigDecimal` (`Deci` equals/hashCode delegates to stripped `BigDecimal` to guarantee `1.0 == 1`).  
- **iOS** – uses Foundation’s `NSDecimalNumber` while keeping the exact same API surface.  
- **JS & WASM** – relies on [`decimal.js`](https://mikemcl.github.io/decimal.js/) v10.6.0 to provide arbitrary precision math in browsers.  
- **Validation / normalization** – a locale-aware regex and the `normalizeDecimalString` helper sanitize input so grouping separators and decimal commas are accepted safely.

These implementations are coordinated through Kotlin’s `expect`/`actual` mechanism, guaranteeing identical semantics on every platform.

## Sample Applications

The repository ships with a Compose Multiplatform demo that exercises the entire API surface:

| Platform | Command |
|----------|---------|
| Desktop (JVM) | `./gradlew :sample:composeApp:run` |
| Android | `./gradlew :sample:composeApp:installDebug` (then launch on device/emulator) |
| Web (JS) | `./gradlew :sample:composeApp:jsBrowserDevelopmentRun` |
| Web (WASM) | `./gradlew :sample:composeApp:wasmJsBrowserDevelopmentRun` |
| iOS | Open `sample/iosApp/iosApp.xcodeproj` in Xcode and run on a simulator or device. |

Each sample screen renders live calculations, rounding scenarios, and serialization snippets to help you validate the behavior visually.

## Testing & Quality

Run the multiplatform test suite before submitting changes:

```bash
./gradlew :deci:allTests          # run every target test task
./gradlew :deci:commonTest        # run only common tests
```

CI-friendly flags such as `--scan`, `--stacktrace`, or `--info` are encouraged when debugging rounding edge cases.

## Configuration & Logging

Deci exposes a lightweight configuration object that you can tweak at runtime:

```kotlin
DeciConfiguration.divisionPolicy = DeciDivisionPolicy(
    fractionalDigits = 6,
    roundingMode = RoundingMode.HALF_UP
)

// Enable Cedar-backed logging (disabled by default)
DeciConfiguration.loggingEnabled = true

// Plant any Cedar tree you prefer; PlatformLogTree routes to native consoles
Cedar.plant(PlatformLogTree())

// Restore defaults when you are done
DeciConfiguration.resetDivisionPolicy()
DeciConfiguration.disableLogging()
```

Logs are emitted at debug level with the `Deci` tag. Follow [Cedar's README](https://github.com/Kimplify/Cedar-Logger/blob/main/README.MD)
for tree options if you want to route them somewhere specific. Leave `loggingEnabled` as `false`
(default) for the fastest, zero-overhead path.

## Contributing

1. Fork the repository and create a feature branch.  
2. Make your changes plus the relevant tests (see `deci/src/commonTest`).  
3. Run `./gradlew :deci:allTests` and, if you touch the sample, the relevant run tasks.  
4. Open a Pull Request describing the motivation, implementation details, and verification steps.

Bug reports and feature requests are also welcome—please include reproducible steps or a failing test case when possible.

## License

Deci is released under the [MIT License](LICENSE).
