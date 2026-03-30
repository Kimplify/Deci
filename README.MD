<img src="deci-logo.svg" alt="Deci" width="430" />

[![Maven Central](https://img.shields.io/maven-central/v/org.kimplify/deci)](https://central.sonatype.com/artifact/org.kimplify/deci)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF)](https://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Deci is a Kotlin Multiplatform library for high-precision decimal arithmetic. Built for money, taxes, invoicing, and any workload where `Float`/`Double` cannot be trusted.

## Features

- **Multiplatform** -- Android, JVM, iOS, macOS, JS, WASM, Linux, Windows
- **Exact decimal math** -- backed by `BigDecimal`, `NSDecimalNumber`, and `decimal.js`
- **Predictable rounding** -- explicit rounding modes, no silent precision loss
- **Serialization ready** -- `kotlinx.serialization` support out of the box

## Installation

Kotlin Multiplatform:
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("org.kimplify:deci:0.2.0")
        }
    }
}
```

Single-platform:
```kotlin
dependencies {
    implementation("org.kimplify:deci:0.2.0")
}
```

## Quick Start

```kotlin
val a = Deci("10.5")
val b = Deci("2.3")

a + b          → 12.8
a - b          → 8.2
a * b          → 24.15
a / b          → 4.565217391304347826086956521739130435
a % b          → 1.3
-a             → -10.5
```

### Rounding

```kotlin
val pi = Deci("3.14159265359")
pi.setScale(2, RoundingMode.HALF_UP)                          → 3.14
Deci("1").divide(Deci("3"), 6, RoundingMode.HALF_UP)          → 0.333333
```

| Mode        | Behavior                                  |
|-------------|-------------------------------------------|
| `UP`        | Away from zero                            |
| `DOWN`      | Toward zero (truncate)                    |
| `CEILING`   | Toward positive infinity                  |
| `FLOOR`     | Toward negative infinity                  |
| `HALF_UP`   | Nearest; ties go up                       |
| `HALF_DOWN` | Nearest; ties go down                     |
| `HALF_EVEN` | Banker's rounding; ties go to even digit  |

### DeciContext

Bundle precision and rounding mode into a reusable object:

```kotlin
val ctx = DeciContext(precision = 4, roundingMode = RoundingMode.HALF_UP)
Deci("10").divide(Deci("3"), ctx)  → 3.3333

DeciContext.DEFAULT       → 20 digits, HALF_UP
DeciContext.CURRENCY_USD  → 2 digits,  HALF_UP
DeciContext.BANKING       → 2 digits,  HALF_EVEN
```

### Comparisons & Helpers

```kotlin
val x = Deci("5.5")
x > Deci("3.2")       → true
x.max(Deci("3.2"))    → 5.5
x.min(Deci("3.2"))    → 3.2

Deci("-7.5").abs()         → 7.5
Deci("-7.5").isNegative()  → true
Deci.ZERO.isZero()         → true
```

### Constants & Safe Parsing

```kotlin
Deci.ZERO
Deci.ONE
Deci.TEN
DeciConstants.PI
DeciConstants.E
DeciConstants.HALF
DeciConstants.HUNDRED
DeciConstants.THOUSAND
DeciConstants.NEGATIVE_ONE

Deci.fromStringOrNull("42.5")     → Deci(42.5)
Deci.fromStringOrZero("invalid")  → Deci.ZERO
```

### Extensions

```kotlin
42.toDeci()
1000L.toDeci()
"123.45".toDeci()
"maybe".toDeciOrNull()   → null

listOf(Deci("1.5"), Deci("2.3"), Deci("3.7")).sumDeci()  → 7.5

Deci("1.50").scale()      → 2
Deci("1.50").precision()  → 3
```

### Math

```kotlin
Deci("2").pow(Deci("3"))                          → 8
Deci("16").sqrt()                                  → 4.0000000000
Deci("16").sqrt(precision = 2)                     → 4.00
Deci("1234").roundToNearest(Deci("50"))            → 1250
Deci("1234.567").roundToSignificantDigits(4)       → 1235
```

### Formatting

```kotlin
val price = Deci("1234567.891")

price.formatCurrency()                    → "$1,234,567.89"
price.formatWithThousandsSeparator()      → "1,234,567.891"
price.formatAsPercentage()                → "123456789.1%"
price.toScientificNotation()              → "1.234567E+6"
price.format("#,##0.00")                  → "1,234,567.89"
Deci("42").toWords()                      → "forty two"
```

### Serialization

```kotlin
@Serializable
data class Price(val amount: Deci, val currency: String)

Json.encodeToString(Price(Deci("99.99"), "USD"))
→ {"amount":"99.99","currency":"USD"}
```

Values serialize as JSON **strings** to preserve trailing zeros and avoid lossy float coercion.

## Supported Targets

| Target          | Backend                  |
|-----------------|--------------------------|
| Android / JVM   | `java.math.BigDecimal`   |
| iOS / macOS     | `NSDecimalNumber`        |
| JS / WASM       | `decimal.js` v10.6.0     |
| Linux / Windows | Pure Kotlin              |

## Configuration

```kotlin
DeciConfiguration.divisionPolicy = DeciDivisionPolicy(
    fractionalDigits = 6,
    roundingMode = RoundingMode.HALF_UP
)

DeciConfiguration.logSink = DeciLogSink { tag, message ->
    println("[$tag] $message")
}

DeciConfiguration.resetDivisionPolicy()
DeciConfiguration.disableLogging()
```

## Sample App

The repository includes a Compose Multiplatform demo:

| Platform | Command                                                    |
|----------|------------------------------------------------------------|
| Desktop  | `./gradlew :sample:composeApp:run`                         |
| Android  | `./gradlew :sample:composeApp:installDebug`                |
| Web (JS) | `./gradlew :sample:composeApp:jsBrowserDevelopmentRun`     |
| Web (WASM)| `./gradlew :sample:composeApp:wasmJsBrowserDevelopmentRun`|
| iOS      | Open `sample/iosApp/iosApp.xcodeproj` in Xcode            |

## Contributing

1. Fork the repository and create a feature branch.
2. Add tests in `deci/src/commonTest`.
3. Run `./gradlew :deci:allTests`.
4. Open a Pull Request with motivation and verification steps.

## License

[MIT](LICENSE)