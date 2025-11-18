# Deci

A Kotlin Multiplatform library for precise decimal arithmetic across all platforms (Android, iOS, JVM, and WASM-JS).

## Features

- **Multiplatform Support**: Works seamlessly on Android, iOS, JVM, and WASM-JS
- **Precise Decimal Arithmetic**: No floating-point precision issues
- **Comprehensive API**: Addition, subtraction, multiplication, division with scale control
- **Flexible Rounding**: Multiple rounding modes (UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN)
- **Kotlinx Serialization**: Built-in serialization support
- **Type-Safe**: Strong typing prevents accidental precision loss
- **Operator Overloading**: Natural syntax with `+`, `-`, `*`, `/` operators

## Installation

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.chilinoodles.deci:deci:1.0.0")
}
```

## Usage

### Basic Operations

```kotlin
val a = Deci("10.5")
val b = Deci("2.3")

val sum = a + b          // 12.8
val difference = a - b   // 8.2
val product = a * b      // 24.15
val quotient = a / b     // 4.565217391304347826086956521739130435
```

### Constructors

```kotlin
val fromString = Deci("123.45")
val fromInt = Deci(100)
val fromLong = Deci(1000L)
val fromDouble = Deci(99.99)

// Comma as decimal separator is automatically converted
val euroFormat = Deci("1,234.56")  // 1234.56
```

### Rounding and Scale

```kotlin
val pi = Deci("3.14159265359")

// Set scale with rounding mode
val rounded = pi.setScale(2, RoundingMode.HALF_UP)  // 3.14

// Division with specific scale
val result = Deci("1").divide(Deci("3"), scale = 6, roundingMode = RoundingMode.HALF_UP)
// 0.333333
```

### Rounding Modes

- `UP`: Round away from zero
- `DOWN`: Round toward zero (truncate)
- `CEILING`: Round toward positive infinity
- `FLOOR`: Round toward negative infinity
- `HALF_UP`: Round to nearest neighbor; ties round up
- `HALF_DOWN`: Round to nearest neighbor; ties round down
- `HALF_EVEN`: Banker's rounding (ties round to even)

### Comparisons

```kotlin
val x = Deci("5.5")
val y = Deci("3.2")

x > y        // true
x >= y       // true
x < y        // false
x <= y       // false
x == y       // false

x.max(y)     // 5.5
x.min(y)     // 3.2
```

### Sign Operations

```kotlin
val negative = Deci("-7.5")

negative.abs()           // 7.5
negative.negate()        // 7.5
negative.isNegative()    // true
negative.isPositive()    // false
negative.isZero()        // false
```

### Constants

```kotlin
Deci.ZERO   // 0
Deci.ONE    // 1
Deci.TEN    // 10
```

### Safe Parsing

```kotlin
// Returns null on invalid input
val maybeNumber = Deci.fromStringOrNull("invalid")  // null
val validNumber = Deci.fromStringOrNull("42.5")     // 42.5

// Returns ZERO on invalid input
val safeNumber = Deci.fromStringOrZero("invalid")   // 0
```

### Extension Functions

```kotlin
// Power operation
val base = Deci("2")
base.pow(3)  // 8

// Sum of collection
val numbers = listOf(Deci("1.5"), Deci("2.3"), Deci("3.7"))
numbers.sumDeci()  // 7.5

// Convert to Long
val asLong = Deci("42.7").toLong()  // 42
```

### Serialization

Deci includes built-in support for kotlinx.serialization:

```kotlin
@Serializable
data class Price(
    val amount: Deci,
    val currency: String
)

val json = Json.encodeToString(Price.serializer(), Price(Deci("99.99"), "USD"))
// {"amount":"99.99","currency":"USD"}

val price = Json.decodeFromString(Price.serializer(), json)
```

## Platform Implementations

- **Android/JVM**: Uses Java's `BigDecimal` for precise arithmetic
- **iOS**: Uses Foundation's `NSDecimalNumber`
- **WASM-JS**: Uses the decimal.js library

## Why Deci?

Floating-point arithmetic (`Float`, `Double`) can lead to precision errors:

```kotlin
// ❌ Don't do this for money/financial calculations
val bad = 0.1 + 0.2  // 0.30000000000000004

// ✅ Use Deci instead
val good = Deci("0.1") + Deci("0.2")  // 0.3
```

Deci is ideal for:
- Financial calculations (money, prices, taxes)
- Scientific calculations requiring exact precision
- Any domain where decimal precision matters

## Sample App

Check out the `sample/composeApp` module for a comprehensive demo of all Deci features.

## License

MIT License - see LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
