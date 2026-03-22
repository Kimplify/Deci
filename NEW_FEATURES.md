# Deci Library - New Feature Proposals

> Comprehensive feature proposals to enhance the Deci library for financial, scientific, and general-purpose decimal computing.
> Date: 2026-03-20 | Version: 0.1.1

---

## Table of Contents

- [Core API Extensions](#core-api-extensions)
- [Financial Computing](#financial-computing)
- [Advanced Math](#advanced-math)
- [Serialization & Interop](#serialization--interop)
- [Reactive & Coroutines Integration](#reactive--coroutines-integration)
- [Developer Experience](#developer-experience)
- [Platform-Specific Enhancements](#platform-specific-enhancements)
- [New Modules](#new-modules)

---

## Core API Extensions

### 1. `Deci.Companion.of()` Factory Methods

**Motivation:** The constructors all go through string parsing, even for common values. Factory methods can provide semantic clarity and optimization.

**Proposal:**
```kotlin
expect class Deci {
    companion object {
        // Existing
        val ZERO: Deci
        val ONE: Deci
        val TEN: Deci

        // New factory methods
        fun of(value: String): Deci           // Alias for constructor
        fun of(value: Int): Deci              // Alias for constructor
        fun of(value: Long): Deci             // Alias for constructor
        fun ofOrNull(value: String): Deci?    // Alias for fromStringOrNull

        /**
         * Creates a Deci representing the given amount of cents/minor units.
         * Example: Deci.ofMinorUnits(1599, 2) -> Deci("15.99")
         */
        fun ofMinorUnits(units: Long, scale: Int): Deci

        /**
         * Creates a Deci from a fraction: numerator / denominator.
         * Example: Deci.ofFraction(1, 3, DeciContext.DEFAULT) -> Deci("0.33333...")
         */
        fun ofFraction(numerator: Long, denominator: Long, context: DeciContext = DeciContext.DEFAULT): Deci
    }
}
```

**Use Case:** Cleaner API surface, especially `ofMinorUnits` for payment systems that work in cents/pence.

---

### 2. Ranges and Progressions

**Motivation:** Kotlin ranges (`1..10`, `1..10 step 2`) don't work with `Deci`.

**Proposal:**
```kotlin
class DeciRange(
    override val start: Deci,
    override val endInclusive: Deci,
) : ClosedRange<Deci>, Iterable<Deci> {

    override fun iterator(): Iterator<Deci> = DeciRangeIterator(start, endInclusive, Deci.ONE)

    infix fun step(step: Deci): DeciProgression = DeciProgression(start, endInclusive, step)
}

class DeciProgression(
    val start: Deci,
    val endInclusive: Deci,
    val step: Deci,
) : Iterable<Deci> {
    override fun iterator(): Iterator<Deci> = DeciRangeIterator(start, endInclusive, step)
}

operator fun Deci.rangeTo(other: Deci): DeciRange = DeciRange(this, other)

// Usage:
for (value in Deci("0.0")..Deci("1.0") step Deci("0.1")) {
    println(value) // 0.0, 0.1, 0.2, ..., 1.0
}
```

---

### 3. Destructuring Support

**Motivation:** Allow destructuring a `Deci` into its integer and fractional parts.

**Proposal:**
```kotlin
// Extension functions for destructuring
operator fun Deci.component1(): Deci = this.setScale(0, RoundingMode.DOWN) // integer part
operator fun Deci.component2(): Deci = this - component1()                  // fractional part

// Usage:
val price = Deci("15.99")
val (dollars, cents) = price
println("$dollars dollars and ${cents * Deci("100")} cents")
// Output: 15 dollars and 99 cents
```

---

### 4. Operator Overloads for Primitive Types

**Motivation:** Currently `Deci("10") + Deci("5")` works, but `Deci("10") + 5` doesn't.

**Proposal:**
```kotlin
expect class Deci {
    // Add to existing operators
    operator fun plus(other: Int): Deci
    operator fun plus(other: Long): Deci
    operator fun minus(other: Int): Deci
    operator fun minus(other: Long): Deci
    operator fun times(other: Int): Deci
    operator fun times(other: Long): Deci
    operator fun div(other: Int): Deci
    operator fun div(other: Long): Deci
}

// Plus reverse operators as extensions:
operator fun Int.plus(other: Deci): Deci = other + this
operator fun Int.minus(other: Deci): Deci = Deci(this) - other
operator fun Int.times(other: Deci): Deci = other * this
```

**Use Case:** Dramatically reduces boilerplate in math-heavy code:
```kotlin
// Before:
val tax = price * Deci("0.08")
val total = price + Deci(shipping)

// After:
val tax = price * Deci("0.08")
val total = price + shipping  // shipping is Int
```

---

### 5. `Deci.POSITIVE_INFINITY` and `Deci.NEGATIVE_INFINITY` Sentinels

**Motivation:** Some financial systems need "no limit" sentinels.

**Proposal:**
```kotlin
expect class Deci {
    companion object {
        val MAX_VALUE: Deci   // Platform-dependent maximum representable value
        val MIN_VALUE: Deci   // Platform-dependent minimum (closest to zero)
    }

    fun isFinite(): Boolean
}
```

**Note:** This is intentionally NOT IEEE infinity. These are just large sentinel values for range validation:
```kotlin
fun Deci.isInRange(min: Deci = Deci.MIN_VALUE, max: Deci = Deci.MAX_VALUE): Boolean
```

---

## Financial Computing

### 6. Money Type with Currency

**Motivation:** A `Deci` has no currency context. Financial applications need currency-aware operations that prevent adding USD + EUR.

**Proposal:** New file `deci/src/commonMain/kotlin/org/kimplify/deci/money/Money.kt`:

```kotlin
@Immutable
data class Money(
    val amount: Deci,
    val currency: Currency,
) : Comparable<Money> {

    operator fun plus(other: Money): Money {
        require(currency == other.currency) {
            "Cannot add ${currency.code} and ${other.currency.code}"
        }
        return Money(amount + other.amount, currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) {
            "Cannot subtract ${currency.code} from ${other.currency.code}"
        }
        return Money(amount - other.amount, currency)
    }

    operator fun times(multiplier: Deci): Money = Money(amount * multiplier, currency)

    fun format(): String = amount.formatCurrency(
        currencySymbol = currency.symbol,
        scale = currency.decimalPlaces,
    )

    fun roundToMinorUnit(): Money = Money(
        amount.setScale(currency.decimalPlaces, currency.roundingMode),
        currency,
    )

    override fun compareTo(other: Money): Int {
        require(currency == other.currency) { "Cannot compare different currencies" }
        return amount.compareTo(other.amount)
    }
}

@Immutable
data class Currency(
    val code: String,        // ISO 4217
    val symbol: String,
    val decimalPlaces: Int,
    val roundingMode: RoundingMode = RoundingMode.HALF_UP,
) {
    companion object {
        val USD = Currency("USD", "$", 2)
        val EUR = Currency("EUR", "\u20AC", 2)
        val GBP = Currency("GBP", "\u00A3", 2)
        val JPY = Currency("JPY", "\u00A5", 0)
        val BTC = Currency("BTC", "\u20BF", 8)
        val ETH = Currency("ETH", "\u039E", 18)
    }
}

// Extension
fun Deci.toMoney(currency: Currency): Money = Money(this, currency)
```

---

### 7. Compound Interest Calculator

**Motivation:** Common financial operation not currently supported.

**Proposal:** New file `deci/src/commonMain/kotlin/org/kimplify/deci/finance/DeciFinance.kt`:

```kotlin
/**
 * Calculates compound interest.
 *
 * Formula: A = P * (1 + r/n)^(n*t)
 *
 * @param principal Initial investment amount
 * @param annualRate Annual interest rate (e.g., 0.05 for 5%)
 * @param compoundingsPerYear Number of times interest is compounded per year
 * @param years Number of years
 * @param context Precision context for intermediate calculations
 * @return Final amount including principal and interest
 */
fun compoundInterest(
    principal: Deci,
    annualRate: Deci,
    compoundingsPerYear: Int,
    years: Int,
    context: DeciContext = DeciContext.DEFAULT,
): Deci {
    val n = Deci(compoundingsPerYear)
    val ratePerPeriod = annualRate.divide(n, context)
    val base = Deci.ONE + ratePerPeriod
    val totalPeriods = compoundingsPerYear * years
    val multiplier = base.pow(Deci(totalPeriods))
    return principal * multiplier
}

/**
 * Calculates simple interest.
 *
 * Formula: I = P * r * t
 */
fun simpleInterest(
    principal: Deci,
    annualRate: Deci,
    years: Deci,
): Deci = principal * annualRate * years

/**
 * Calculates the present value of a future amount.
 *
 * Formula: PV = FV / (1 + r)^n
 */
fun presentValue(
    futureValue: Deci,
    discountRate: Deci,
    periods: Int,
    context: DeciContext = DeciContext.DEFAULT,
): Deci {
    val divisor = (Deci.ONE + discountRate).pow(Deci(periods))
    return futureValue.divide(divisor, context)
}

/**
 * Calculates the payment amount for an amortized loan.
 *
 * Formula: PMT = P * [r(1+r)^n] / [(1+r)^n - 1]
 */
fun loanPayment(
    principal: Deci,
    monthlyRate: Deci,
    totalPayments: Int,
    context: DeciContext = DeciContext.DEFAULT,
): Deci {
    if (monthlyRate.isZero()) {
        return principal.divide(Deci(totalPayments), context)
    }
    val factor = (Deci.ONE + monthlyRate).pow(Deci(totalPayments))
    val numerator = principal * monthlyRate * factor
    val denominator = factor - Deci.ONE
    return numerator.divide(denominator, context)
}
```

---

### 8. Tax Calculation Utilities

**Proposal:**
```kotlin
/**
 * Calculates tax amount from a gross value.
 * Example: Deci("100").taxAmount(Deci("0.08")) -> Deci("8.00")
 */
fun Deci.taxAmount(
    taxRate: Deci,
    context: DeciContext = DeciContext.CURRENCY_USD,
): Deci = (this * taxRate).setScale(context.precision, context.roundingMode)

/**
 * Adds tax to this amount.
 * Example: Deci("100").withTax(Deci("0.08")) -> Deci("108.00")
 */
fun Deci.withTax(
    taxRate: Deci,
    context: DeciContext = DeciContext.CURRENCY_USD,
): Deci = this + taxAmount(taxRate, context)

/**
 * Extracts the pre-tax amount from a tax-inclusive value.
 * Example: Deci("108").preTax(Deci("0.08")) -> Deci("100.00")
 */
fun Deci.preTax(
    taxRate: Deci,
    context: DeciContext = DeciContext.CURRENCY_USD,
): Deci = this.divide(Deci.ONE + taxRate, context)

/**
 * Calculates discount amount.
 * Example: Deci("100").discountAmount(Deci("0.20")) -> Deci("20.00")
 */
fun Deci.discountAmount(
    discountRate: Deci,
    context: DeciContext = DeciContext.CURRENCY_USD,
): Deci = (this * discountRate).setScale(context.precision, context.roundingMode)

/**
 * Applies discount to this amount.
 * Example: Deci("100").withDiscount(Deci("0.20")) -> Deci("80.00")
 */
fun Deci.withDiscount(
    discountRate: Deci,
    context: DeciContext = DeciContext.CURRENCY_USD,
): Deci = this - discountAmount(discountRate, context)
```

---

### 9. Allocation / Split with Remainder Distribution

**Motivation:** Splitting $100 three ways gives $33.33 * 3 = $99.99, losing a cent. Financial systems need "fair allocation" that distributes remainders.

**Proposal:**
```kotlin
/**
 * Splits this amount into [parts] equal portions, distributing any
 * remainder from rounding across the first portions.
 *
 * Example: Deci("100").allocate(3, DeciContext.CURRENCY_USD)
 *   -> [Deci("33.34"), Deci("33.33"), Deci("33.33")]
 *   // Sum is exactly 100.00
 */
fun Deci.allocate(
    parts: Int,
    context: DeciContext = DeciContext.CURRENCY_USD,
): List<Deci> {
    require(parts > 0) { "Parts must be positive: $parts" }

    val base = this.divide(Deci(parts), context)
    val allocated = base * Deci(parts)
    val remainder = this - allocated

    val unit = if (remainder.isPositive()) {
        Deci.ONE.setScale(context.precision, context.roundingMode)
            .divide(Deci(10).pow(Deci(context.precision)), context)
    } else if (remainder.isNegative()) {
        Deci("-1").setScale(context.precision, context.roundingMode)
            .divide(Deci(10).pow(Deci(context.precision)), context)
    } else {
        Deci.ZERO
    }

    val remainderUnits = if (unit.isZero()) 0
        else (remainder.divide(unit, DeciContext(0, RoundingMode.DOWN))).toLong().toInt().let { kotlin.math.abs(it) }

    return List(parts) { index ->
        if (index < remainderUnits) base + unit else base
    }
}

/**
 * Splits this amount according to the given ratios.
 *
 * Example: Deci("100").allocateByRatios(listOf(Deci("2"), Deci("3"), Deci("5")))
 *   -> [Deci("20.00"), Deci("30.00"), Deci("50.00")]
 */
fun Deci.allocateByRatios(
    ratios: List<Deci>,
    context: DeciContext = DeciContext.CURRENCY_USD,
): List<Deci> {
    require(ratios.isNotEmpty()) { "Ratios must not be empty" }
    val totalRatio = ratios.sumDeci()
    require(!totalRatio.isZero()) { "Total ratio must not be zero" }

    val results = ratios.map { ratio ->
        (this * ratio).divide(totalRatio, context)
    }

    // Distribute remainder to last element
    val allocated = results.sumDeci()
    val remainder = this - allocated
    return results.toMutableList().apply {
        this[lastIndex] = this[lastIndex] + remainder
    }
}
```

---

### 10. Percentage Change & Markup/Margin Calculations

**Proposal:**
```kotlin
/**
 * Calculates the percentage change from this value to [other].
 * Example: Deci("100").percentageChangeTo(Deci("125")) -> Deci("25")  (25%)
 */
fun Deci.percentageChangeTo(
    other: Deci,
    context: DeciContext = DeciContext.DEFAULT,
): Deci {
    if (this.isZero()) throw DeciDivisionByZeroException("Cannot calculate percentage change from zero")
    return ((other - this).divide(this, context)) * DeciConstants.HUNDRED
}

/**
 * Calculates the gross profit margin.
 * Formula: (revenue - cost) / revenue * 100
 */
fun grossMargin(
    revenue: Deci,
    cost: Deci,
    context: DeciContext = DeciContext.DEFAULT,
): Deci {
    if (revenue.isZero()) throw DeciDivisionByZeroException("Revenue cannot be zero")
    return (revenue - cost).divide(revenue, context) * DeciConstants.HUNDRED
}

/**
 * Calculates the markup percentage.
 * Formula: (price - cost) / cost * 100
 */
fun markup(
    price: Deci,
    cost: Deci,
    context: DeciContext = DeciContext.DEFAULT,
): Deci {
    if (cost.isZero()) throw DeciDivisionByZeroException("Cost cannot be zero")
    return (price - cost).divide(cost, context) * DeciConstants.HUNDRED
}
```

---

## Advanced Math

### 11. Logarithm Functions

**Motivation:** Scientific computing needs log operations.

**Proposal:**
```kotlin
/**
 * Calculates the natural logarithm (ln) of this value using the Taylor series.
 *
 * Uses the identity: ln(x) = 2 * sum(((x-1)/(x+1))^(2k+1) / (2k+1))
 *
 * @param precision Number of decimal places (default: 15)
 * @throws DeciArithmeticException if this value is not positive
 */
fun Deci.ln(precision: Int = 15): Deci {
    if (!this.isPositive()) throw DeciArithmeticException("ln requires a positive value: $this")
    if (this == Deci.ONE) return Deci.ZERO

    val context = DeciContext(precision + 10, RoundingMode.HALF_UP)

    // Reduce to range [0.5, 2) for faster convergence
    var value = this
    var adjustment = Deci.ZERO
    val ln2 = Deci("0.6931471805599453094172321214581765680755") // Pre-computed ln(2)

    while (value > DeciConstants.TWO) {
        value = value.divide(DeciConstants.TWO, context)
        adjustment += ln2
    }
    while (value < DeciConstants.HALF) {
        value *= DeciConstants.TWO
        adjustment -= ln2
    }

    // Taylor series for ln using (x-1)/(x+1) transform
    val y = (value - Deci.ONE).divide(value + Deci.ONE, context)
    val ySq = y * y
    var term = y
    var sum = y
    for (k in 1..100) {
        term *= ySq
        val next = term.divide(Deci(2 * k + 1), context)
        sum += next
        if (next.abs().setScale(precision + 5, RoundingMode.HALF_UP).isZero()) break
    }

    return (sum * DeciConstants.TWO + adjustment).setScale(precision, RoundingMode.HALF_UP)
}

/**
 * Calculates log base 10 of this value.
 */
fun Deci.log10(precision: Int = 15): Deci {
    val ln10 = Deci.TEN.ln(precision + 5)
    return this.ln(precision + 5).divide(ln10, DeciContext(precision, RoundingMode.HALF_UP))
}

/**
 * Calculates log base [base] of this value.
 */
fun Deci.log(base: Deci, precision: Int = 15): Deci {
    val lnBase = base.ln(precision + 5)
    return this.ln(precision + 5).divide(lnBase, DeciContext(precision, RoundingMode.HALF_UP))
}
```

---

### 12. Exponential Function

**Proposal:**
```kotlin
/**
 * Calculates e^x (exponential function) using Taylor series.
 *
 * @param precision Number of decimal places (default: 15)
 */
fun Deci.exp(precision: Int = 15): Deci {
    if (this.isZero()) return Deci.ONE

    val context = DeciContext(precision + 10, RoundingMode.HALF_UP)

    var term = Deci.ONE
    var sum = Deci.ONE
    for (k in 1..200) {
        term = term * this
        term = term.divide(Deci(k), context)
        sum += term
        if (term.abs().setScale(precision + 5, RoundingMode.HALF_UP).isZero()) break
    }

    return sum.setScale(precision, RoundingMode.HALF_UP)
}
```

---

### 13. Trigonometric Functions

**Proposal:**
```kotlin
/**
 * Calculates sin(x) where x is in radians, using Taylor series.
 */
fun Deci.sin(precision: Int = 15): Deci {
    // Reduce to [0, 2*PI) range
    val twoPi = DeciConstants.PI * DeciConstants.TWO
    val context = DeciContext(precision + 10, RoundingMode.HALF_UP)
    var x = this.mod(twoPi)

    var term = x
    var sum = x
    for (k in 1..100) {
        term = term * (-Deci.ONE) * x * x
        term = term.divide(Deci((2 * k) * (2 * k + 1)), context)
        sum += term
        if (term.abs().setScale(precision + 5, RoundingMode.HALF_UP).isZero()) break
    }
    return sum.setScale(precision, RoundingMode.HALF_UP)
}

/**
 * Calculates cos(x) where x is in radians.
 */
fun Deci.cos(precision: Int = 15): Deci {
    val halfPi = DeciConstants.PI.divide(DeciConstants.TWO, DeciContext(precision + 10, RoundingMode.HALF_UP))
    return (halfPi - this).sin(precision)
}

/**
 * Calculates tan(x) where x is in radians.
 */
fun Deci.tan(precision: Int = 15): Deci {
    val context = DeciContext(precision, RoundingMode.HALF_UP)
    val cosVal = this.cos(precision + 5)
    if (cosVal.isZero()) throw DeciArithmeticException("tan is undefined at this value")
    return this.sin(precision + 5).divide(cosVal, context)
}
```

---

### 14. Nth Root and Fractional Powers

**Proposal:**
```kotlin
/**
 * Calculates the nth root of this value using Newton's method.
 *
 * @param n The root to calculate (must be positive integer)
 * @param precision Number of decimal places (default: 10)
 */
fun Deci.nthRoot(n: Int, precision: Int = 10): Deci {
    require(n > 0) { "Root must be positive: $n" }
    if (n == 1) return this
    if (n == 2) return this.sqrt(precision)
    if (this.isZero()) return Deci.ZERO
    if (this.isNegative() && n % 2 == 0) {
        throw DeciArithmeticException("Cannot calculate even root of negative number: $this")
    }

    val context = DeciContext(precision + 5, RoundingMode.HALF_UP)
    val nDeci = Deci(n)
    val nMinus1 = Deci(n - 1)

    var x = this.divide(nDeci, context) // Initial guess
    repeat(100) {
        val prevX = x
        // Newton's formula: x = ((n-1)*x + value/x^(n-1)) / n
        val xPowNMinus1 = x.pow(nMinus1)
        x = (nMinus1 * x + this.divide(xPowNMinus1, context)).divide(nDeci, context)

        if ((x - prevX).abs().setScale(precision + 2, RoundingMode.HALF_UP).isZero()) {
            return@repeat
        }
    }

    return x.setScale(precision, RoundingMode.HALF_UP)
}

/**
 * Cube root.
 */
fun Deci.cbrt(precision: Int = 10): Deci = nthRoot(3, precision)
```

---

### 15. GCD and LCM

**Proposal:**
```kotlin
/**
 * Calculates the Greatest Common Divisor of two Deci values.
 * Both values must be whole numbers.
 */
fun gcd(a: Deci, b: Deci): Deci {
    require(a.isWhole() && b.isWhole()) { "GCD requires whole numbers" }
    var x = a.abs()
    var y = b.abs()
    while (!y.isZero()) {
        val temp = y
        y = x.mod(y)
        x = temp
    }
    return x
}

/**
 * Calculates the Least Common Multiple of two Deci values.
 */
fun lcm(a: Deci, b: Deci): Deci {
    if (a.isZero() || b.isZero()) return Deci.ZERO
    return (a * b).abs().divide(gcd(a, b), DeciContext(0, RoundingMode.DOWN))
}
```

---

## Serialization & Interop

### 16. Protobuf Serialization Support

**Motivation:** Many backend services use Protocol Buffers.

**Proposal:** New artifact `deci-protobuf`:

```kotlin
// String-based protobuf serialization (safest)
message DeciProto {
    string value = 1;
}

fun Deci.toProto(): DeciProto = DeciProto.newBuilder().setValue(this.toString()).build()
fun DeciProto.toDeci(): Deci = Deci(this.value)
```

---

### 17. Room/SQLDelight Type Adapters

**Motivation:** Android apps commonly use Room or SQLDelight for persistence.

**Proposal:** New artifact `deci-persistence`:

```kotlin
// SQLDelight column adapter
class DeciColumnAdapter : ColumnAdapter<Deci, String> {
    override fun decode(databaseValue: String): Deci = Deci(databaseValue)
    override fun encode(value: Deci): String = value.toString()
}

// Room TypeConverter
class DeciTypeConverters {
    @TypeConverter
    fun fromDeci(value: Deci?): String? = value?.toString()

    @TypeConverter
    fun toDeci(value: String?): Deci? = value?.let { Deci(it) }
}
```

---

### 18. Ktor Content Negotiation Support

**Proposal:**
```kotlin
// Custom Ktor serializer plugin
fun ContentNegotiation.Configuration.deciSupport() {
    // Registers custom serializer that handles Deci fields as JSON strings
    register(ContentType.Application.Json, DeciKtorSerializer())
}
```

---

### 19. Parcelable Support for Android

**Proposal:**
```kotlin
// In androidMain:
@Parcelize
class DeciParcel(private val value: String) : Parcelable {
    constructor(deci: Deci) : this(deci.toString())
    fun toDeci(): Deci = Deci(value)
}

// Or using @TypeParceler:
object DeciParceler : Parceler<Deci> {
    override fun create(parcel: Parcel): Deci = Deci(parcel.readString()!!)
    override fun Deci.write(parcel: Parcel, flags: Int) = parcel.writeString(this.toString())
}
```

---

## Reactive & Coroutines Integration

### 20. Flow-Based Accumulator

**Motivation:** Real-time data streams (stock prices, transaction feeds) need running calculations.

**Proposal:**
```kotlin
/**
 * Emits the running sum of all Deci values in the flow.
 */
fun Flow<Deci>.runningSum(): Flow<Deci> = flow {
    var sum = Deci.ZERO
    collect { value ->
        sum += value
        emit(sum)
    }
}

/**
 * Emits the running average of all Deci values.
 */
fun Flow<Deci>.runningAverage(context: DeciContext = DeciContext.DEFAULT): Flow<Deci> = flow {
    var sum = Deci.ZERO
    var count = 0
    collect { value ->
        sum += value
        count++
        emit(sum.divide(Deci(count), context))
    }
}

/**
 * Emits the exponential moving average.
 */
fun Flow<Deci>.ema(
    alpha: Deci,
    context: DeciContext = DeciContext.DEFAULT,
): Flow<Deci> = flow {
    var ema: Deci? = null
    val oneMinusAlpha = Deci.ONE - alpha
    collect { value ->
        ema = ema?.let { prev ->
            alpha * value + oneMinusAlpha * prev
        } ?: value
        emit(ema!!)
    }
}

/**
 * Emits min/max/sum/count statistics as a data class.
 */
data class RunningStats(
    val count: Int,
    val sum: Deci,
    val min: Deci,
    val max: Deci,
    val mean: Deci,
)

fun Flow<Deci>.runningStats(context: DeciContext = DeciContext.DEFAULT): Flow<RunningStats> = flow {
    var count = 0
    var sum = Deci.ZERO
    var min = Deci.ZERO
    var max = Deci.ZERO

    collect { value ->
        count++
        sum += value
        if (count == 1) { min = value; max = value }
        else {
            if (value < min) min = value
            if (value > max) max = value
        }
        emit(RunningStats(count, sum, min, max, sum.divide(Deci(count), context)))
    }
}
```

---

## Developer Experience

### 21. Compose UI Components

**Motivation:** Compose Multiplatform apps need Deci-aware input fields.

**Proposal:** New artifact `deci-compose`:

```kotlin
/**
 * A TextField that validates and formats Deci input.
 */
@Composable
fun DeciTextField(
    value: Deci?,
    onValueChange: (Deci?) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    currency: Currency? = null,
    maxDecimalPlaces: Int = 2,
    allowNegative: Boolean = false,
    minValue: Deci? = null,
    maxValue: Deci? = null,
) {
    var text by remember { mutableStateOf(value?.toString() ?: "") }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            if (isValidDeciInput(newText, maxDecimalPlaces, allowNegative)) {
                text = newText
                onValueChange(Deci.fromStringOrNull(newText))
            }
        },
        label = label,
        prefix = currency?.let { { Text(it.symbol) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = value?.let { v ->
            (minValue != null && v < minValue) || (maxValue != null && v > maxValue)
        } ?: false,
        modifier = modifier,
    )
}

/**
 * Displays a formatted Deci value.
 */
@Composable
fun DeciText(
    value: Deci,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    currency: Currency? = null,
    colorPositive: Color = Color.Unspecified,
    colorNegative: Color = Color.Red,
    colorZero: Color = Color.Unspecified,
) {
    val color = when {
        value.isNegative() -> colorNegative
        value.isPositive() -> colorPositive
        else -> colorZero
    }
    val text = currency?.let { value.formatCurrency(it.symbol, it.decimalPlaces) }
        ?: value.toString()

    Text(text = text, modifier = modifier, style = style, color = color)
}
```

---

### 22. Testing Utilities

**Motivation:** Testing Deci values needs assertion helpers.

**Proposal:** New artifact `deci-test`:

```kotlin
/**
 * Asserts that two Deci values are equal.
 */
fun assertDeciEquals(expected: Deci, actual: Deci, message: String? = null) {
    assertEquals(0, expected.compareTo(actual),
        message ?: "Expected <$expected> but was <$actual>")
}

/**
 * Asserts that a Deci value is approximately equal to expected within tolerance.
 */
fun assertDeciApproximately(
    expected: Deci,
    actual: Deci,
    tolerance: Deci = Deci("0.0001"),
    message: String? = null,
) {
    val diff = (expected - actual).abs()
    assertTrue(diff <= tolerance,
        message ?: "Expected <$expected> +/- $tolerance but was <$actual> (diff: $diff)")
}

/**
 * Asserts that a Deci is in the given range.
 */
fun assertDeciInRange(value: Deci, min: Deci, max: Deci, message: String? = null) {
    assertTrue(value >= min && value <= max,
        message ?: "Expected $value to be in range [$min, $max]")
}

/**
 * Asserts that a collection sums to the expected value (useful for allocation tests).
 */
fun assertSumEquals(expected: Deci, values: List<Deci>, message: String? = null) {
    assertDeciEquals(expected, values.sumDeci(),
        message ?: "Expected sum <$expected> but got <${values.sumDeci()}>")
}

// Kotest matchers:
fun Deci.shouldBeZero() = this.isZero() shouldBe true
fun Deci.shouldBePositive() = this.isPositive() shouldBe true
fun Deci.shouldBeNegative() = this.isNegative() shouldBe true
fun Deci.shouldBeApproximately(expected: Deci, tolerance: Deci = Deci("0.0001")) {
    (this - expected).abs() shouldBeLessThanOrEqual tolerance
}
infix fun Deci.shouldEqualDeci(expected: Deci) = assertDeciEquals(expected, this)
```

---

### 23. Deci Literal DSL with String Templates

**Motivation:** Make Deci construction more concise in DSL contexts.

**Proposal:**
```kotlin
/**
 * Prefix operator for quick Deci creation from string literals.
 * Usage: val price = d("19.99")
 */
fun d(value: String): Deci = Deci(value)
fun d(value: Int): Deci = Deci(value)
fun d(value: Long): Deci = Deci(value)

// For builder patterns:
class DeciListBuilder {
    private val values = mutableListOf<Deci>()
    operator fun String.unaryPlus() { values.add(Deci(this)) }
    operator fun Int.unaryPlus() { values.add(Deci(this)) }
    fun build(): List<Deci> = values.toList()
}

fun deciListOf(block: DeciListBuilder.() -> Unit): List<Deci> =
    DeciListBuilder().apply(block).build()

// Usage:
val prices = deciListOf {
    +"19.99"
    +"29.99"
    +"49.99"
}
```

---

## Platform-Specific Enhancements

### 24. Swift-Friendly API (iOS)

**Motivation:** Swift consumers need idiomatic API access.

**Proposal:** Add `@ObjCName` annotations and Swift-friendly extensions:

```kotlin
@ObjCName("DeciValue")
actual class Deci { ... }

// Add NSNumber bridge for iOS:
fun Deci.toNSDecimalNumber(): NSDecimalNumber = NSDecimalNumber(this.toString())
fun NSDecimalNumber.toDeci(): Deci = Deci(this.stringValue)
```

---

### 25. Java Interop Improvements

**Motivation:** Java consumers need `BigDecimal` bridges.

**Proposal:**
```kotlin
// In jvmMain:
fun Deci.toBigDecimal(): BigDecimal = BigDecimal(this.toString())
fun BigDecimal.toDeci(): Deci = Deci(this.toPlainString())

// Static factory for Java callers:
@JvmStatic
fun Deci.Companion.fromBigDecimal(value: BigDecimal): Deci = value.toDeci()
```

---

## New Modules

### 26. Module: `deci-finance`

**Scope:** All financial computing functions (interest, amortization, tax, allocation, currency, margin).

**Structure:**
```
deci-finance/
  src/commonMain/kotlin/org/kimplify/deci/finance/
    Money.kt
    Currency.kt
    Interest.kt
    Allocation.kt
    Tax.kt
    Margin.kt
```

---

### 27. Module: `deci-compose`

**Scope:** Compose Multiplatform UI components for Deci.

**Structure:**
```
deci-compose/
  src/commonMain/kotlin/org/kimplify/deci/compose/
    DeciTextField.kt
    DeciText.kt
    DeciFormatter.kt
    CurrencyInput.kt
```

---

### 28. Module: `deci-test`

**Scope:** Testing assertions, matchers, and generators for Deci.

**Structure:**
```
deci-test/
  src/commonMain/kotlin/org/kimplify/deci/test/
    DeciAssertions.kt
    DeciMatchers.kt       // Kotest matchers
    DeciGenerators.kt     // Kotest Arb generators
```

---

### 29. Module: `deci-persistence`

**Scope:** Database adapters for Room, SQLDelight, Exposed, etc.

**Structure:**
```
deci-persistence/
  src/androidMain/kotlin/org/kimplify/deci/persistence/
    RoomTypeConverters.kt
    DeciParcelable.kt
  src/commonMain/kotlin/org/kimplify/deci/persistence/
    DeciColumnAdapter.kt
```

---

### 30. Module: `deci-ktor`

**Scope:** Ktor server/client serialization support.

**Structure:**
```
deci-ktor/
  src/commonMain/kotlin/org/kimplify/deci/ktor/
    DeciContentNegotiation.kt
    DeciQueryParameter.kt
```

---

## Roadmap Priority

### Version 0.2.0 (Near-term)
| # | Feature | Impact | Effort |
|---|---------|--------|--------|
| 1 | `of()` factory methods | HIGH | Small |
| 4 | Primitive operator overloads | HIGH | Medium |
| 5 | `orZero()` extension | HIGH | Small |
| 6 | `toInt()`/`toFloat()` | MEDIUM | Small |
| 9 | Allocation with remainder | HIGH | Medium |
| 10 | Percentage change / margin | MEDIUM | Small |
| 22 | Testing utilities | HIGH | Medium |
| 23 | `d()` literal helper | MEDIUM | Small |

### Version 0.3.0 (Mid-term)
| # | Feature | Impact | Effort |
|---|---------|--------|--------|
| 2 | Ranges and progressions | MEDIUM | Medium |
| 6 | Money type with currency | HIGH | Large |
| 7 | Compound interest calculator | MEDIUM | Medium |
| 8 | Tax utilities | MEDIUM | Small |
| 11 | Logarithm functions | MEDIUM | Large |
| 12 | Exponential function | MEDIUM | Medium |
| 14 | Nth root / cbrt | MEDIUM | Medium |
| 17 | Room/SQLDelight adapters | HIGH | Medium |

### Version 0.4.0+ (Long-term)
| # | Feature | Impact | Effort |
|---|---------|--------|--------|
| 3 | Destructuring support | LOW | Small |
| 13 | Trigonometric functions | LOW | Large |
| 15 | GCD / LCM | LOW | Small |
| 16 | Protobuf support | MEDIUM | Medium |
| 19 | Parcelable support | MEDIUM | Small |
| 20 | Flow-based accumulators | MEDIUM | Medium |
| 21 | Compose UI components | HIGH | Large |
| 24 | Swift-friendly API | MEDIUM | Medium |
| 25 | Java interop | MEDIUM | Small |
