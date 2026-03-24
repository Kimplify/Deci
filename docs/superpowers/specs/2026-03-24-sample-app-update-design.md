# Sample App Update — Comprehensive Feature Showcase

## Context

The Deci library has 100+ public API functions spanning core arithmetic, financial operations, formatting, statistics, validation, serialization, bulk operations, and more. The current sample app only demonstrates basic arithmetic, rounding, comparisons, sign operations, constants, power, sum, and parsing. Major API surfaces — financial, formatting, statistics, validation, serialization, scale/context, math, extensions, and bulk operations — are either not shown in the UI at all or exist only in an unused `DeciExample.kt` file. The sample needs to showcase all library capabilities to serve as both a demo and a living API reference.

## Design

### Navigation: Bottom Tab Bar (5 tabs)

Replace the single scrollable page with a `NavigationBar` + state-based routing. No navigation library needed — use `mutableStateOf(0)` for tab selection and `when` to render the active screen.

| Tab | Icon | Label |
|-----|------|-------|
| 1 | `Icons.Default.Home` | Core |
| 2 | `Icons.Default.Settings` | Scale & Context |
| 3 | `Icons.Default.Star` | Financial |
| 4 | `Icons.Default.List` | Format & Stats |
| 5 | `Icons.Default.Check` | Validation |

Icons are from `Icons.Default` (included in `compose.material`). No extended icon set needed.

### Error Handling Pattern (all interactive sections)

All interactive inputs use `Deci.fromStringOrNull()`. When parsing fails:
- The input field's `isError` state is set to `true`
- The result area shows a Material 3 error-styled card with "Invalid input" text
- Results clear when the input changes

This pattern is consistent across all tabs.

### File Structure

```
sample/composeApp/src/commonMain/kotlin/org/kimplify/
├── App.kt                         # Scaffold + NavigationBar + tab routing
├── components/
│   ├── DemoComponents.kt          # DemoSection, DemoItem (extracted from App.kt)
│   └── InteractiveCard.kt         # Reusable card with input field + result display
├── screens/
│   ├── CoreScreen.kt              # Existing content + math + extensions + constants
│   ├── ScaleContextScreen.kt      # setScale + DeciContext demos
│   ├── FinancialScreen.kt         # Tax, discount, allocation, margins
│   ├── FormatStatsScreen.kt       # Formatting + statistics + bulk ops
│   └── ValidationScreen.kt        # Validation + serialization
```

`DeciExample.kt` will be deleted — its functionality is absorbed into the screen files.

### Tab 1: Core (existing content + math + extensions + constants)

Moves all existing content from current `App.kt` plus adds missing core API demos:

**Interactive:** "Try It Yourself" parser, "Combine Values" playground (unchanged)

**Read-only demos (existing):** Basic Operations, Rounding & Scale, Division with Scale, Comparisons, Sign Operations, Power Operations, Sum of List, Parse with Comma, Safe Parsing

**New read-only demo — Math Functions:**
- `Deci("16").sqrt()` → "4"
- `Deci("12.7").roundToNearest(Deci("5"))` → "15"
- `Deci("3.14159").roundToSignificantDigits(4)` → "3.142"
- `Deci("10").mod(Deci("3"))` → "1"
- `Deci("10").remainder(Deci("3"))` → "1"

**New read-only demo — Extension Functions & Shorthand:**
- `d("42.5")` — shorthand factory
- `42.toDeci()`, `"99.99".toDeci()`, `"invalid".toDeciOrNull()` → null
- `null.orZero()`, `null.orOne()`, `null.orDefault(Deci("5"))`
- `Deci("123.45").scale()` → 2, `Deci("123.45").precision()` → 5
- Mixed-type operators: `Deci("10") + 5`, `3 * Deci("7")`
- `Deci("123.456").toLong()` → 123, `Deci("123.456").toInt()` → 123

**New read-only demo — DeciConstants:**
- `PI`, `E`, `HALF`, `TWO`, `HUNDRED`, `THOUSAND`, `MILLION`
- `NEGATIVE_ONE`, `ONE_TENTH`, `ONE_HUNDREDTH`, `ONE_THOUSANDTH`

**Expanded Constants section:** Merge existing ZERO/ONE/TEN with the above.

### Tab 2: Scale & Context (interactive)

**Interactive section — Scale Explorer:**
- Input field for a decimal value (default: "3.14159265358979")
- Text field for target scale (0-20)
- `FlowRow` of selectable `FilterChip`s for each `RoundingMode`: UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN
- Shows `value.setScale(scale, selectedMode)` result live

Use `FlowRow` (from `compose.foundation.layout`) for the chips to handle wrapping on narrow screens.

**Interactive section — DeciContext Presets:**
- `FlowRow` of selectable chips for each preset: DEFAULT, CURRENCY_USD, CURRENCY_EUR, CURRENCY_JPY, CURRENCY_BTC, BANKING
- Shows the context's `precision` and `roundingMode`
- Input field for a value → shows `value.setScale(context.precision, context.roundingMode)` result
- Demo: `DeciContext.forCurrency("GBP")` factory

**Read-only demo — Division with Context:**
- `Deci("1").divide(Deci("3"), DeciContext.CURRENCY_USD)` → "0.33"
- `Deci("1").divide(Deci("3"), DeciContext.CURRENCY_BTC)` → "0.33333333"
- `Deci("1").divide(Deci("3"), DeciContext.BANKING)` → "0.33"

### Tab 3: Financial (interactive)

**Interactive section — Tax Calculator:**
- Input fields: amount (default "100.00"), tax rate (default "0.21")
- Results: `taxAmount()`, `withTax()`, `preTax()`

**Interactive section — Discount Calculator:**
- Input fields: price (default "250.00"), discount rate (default "0.15")
- Result: `withDiscount()`

**Interactive section — Money Allocation:**
- Input fields: total amount (default "100.00"), number of parts (default "3")
- Result: `allocate()` → shows each part (e.g., "33.34, 33.33, 33.33")

**Read-only demos:**
- `allocateByRatios()`: Split $1000 by ratios [50, 30, 20]
- `percentageChangeTo()`: From $80 to $100 → 25%
- `grossMargin()`: Revenue $500, Cost $300 → 40%
- `markup()`: Price $500, Cost $300 → 66.67%

### Tab 4: Format & Stats (read-only)

**Formatting demos:**
- `Deci("1234567.89").formatCurrency()` → "$1,234,567.89"
- `Deci("1234567.89").formatCurrency("€", 2, ".")` → "€1.234.567.89" (note: library uses custom thousands separator but decimal point stays as `.`)
- `Deci("0.15").formatAsPercentage()` → "15.0%" (input is rate 0-1, multiplied by 100 internally)
- `Deci("1234567.89").formatWithThousandsSeparator()` → "1,234,567.89"
- `Deci("1234567.89").toScientificNotation(3)` → "1.235e+6"
- `Deci("42").toWords()` → "forty two"
- `Deci("1234.5").format("0.00")`, `format("#,##0.00")`, `format("0.0000")`
- `Deci("1234567.89").pad(20, '0', padLeft = true)`

**Statistics demos (on fixed sales dataset):**
- Dataset: [1200, 1450, 980, 1650, 1320, 1180, 1520, 1380, 1290, 1410]
- `mean()`, `median()`, `standardDeviation()`, `variance()`
- `range()`, `harmonicMean()`
- `weightedAverage()` with weights [1, 2, 1, 3, 2, 1, 2, 1, 1, 2]
- `minDeci()`, `maxDeci()`, `sumDeci()`
- `sumOfSquares()`

**Bulk Operations demos:**
- `cumulativeSum()` on [10, 20, 30, 40] → [10, 30, 60, 100]
- `movingAverage(windowSize=3)` on [10, 20, 30, 40, 50]
- `scaleToSum(targetSum)` — redistribute to hit exact target
- `differences()` — consecutive differences
- `multiplyAllBy()`, `addToAll()`, `roundAll()`
- `topN(3)`, `bottomN(3)` on the sales dataset
- `filterInRange(min, max)`

### Tab 5: Validation (interactive + read-only)

**Interactive section — Form Validator:**
- Input field for a value (OutlinedTextField)
- Toggle switches (`Switch`): must be positive
- Text field for max decimal places (OutlinedTextField, numeric)
- Text fields for min value, max value (OutlinedTextField)
- Live display of `validateForForm()` result (isValid + errorMessage)

**Read-only demos — Validation functions:**
- `isValidDeci()` on various strings: "123.45", "abc", "", "1,234.56", "-45.67"
- `isValidCurrencyAmount("USD")` / `"JPY"` / `"BTC"` on various values
- `isValidPercentage()`, `isValidTaxRate()`, `isValidInterestRate()`
- `isInRange()`, `clamp()`
- `isWhole()`, `isEven()`, `isOdd()`
- `hasValidDecimalPlaces()`
- `isPositiveStrict()`, `isNonNegative()`
- `isApproximatelyEqual()` with tolerance
- `safeDivide()` with zero divisor
- `String.toDeciOrError()` showing `Result<Deci>` usage

**Read-only demo — Serialization:**
- Show `Deci("1.50")` → `Json.encodeToString(DeciSerializer, value)` → JSON string `"1.50"` → `Json.decodeFromString(DeciSerializer, json)` → `Deci("1.50")`
- Emphasize: trailing zeros preserved, serialized as JSON string not number

### Shared Components

**`DemoSection`** — already exists, extract to `components/DemoComponents.kt`
**`DemoItem`** — already exists, extract alongside
**`InteractiveCard`** — new reusable composable:
```kotlin
@Composable
fun InteractiveCard(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
)
```
Wraps an `OutlinedCard` with consistent title/description styling, used for interactive sections across tabs.

### Dependencies

**No new compile dependencies needed for UI** — `compose.material3` provides `NavigationBar`, `NavigationBarItem`, `Icon`, `FilterChip`, `Slider`, `FlowRow`. `compose.material` provides `Icons.Default`.

**Build file changes** for serialization demo in `sample/composeApp/build.gradle.kts`:
```kotlin
// Add plugin
alias(libs.plugins.kotlinx.serialization)

// Add to commonMain.dependencies
implementation(libs.kotlinx.serialization.json)
```

## Verification

1. `./gradlew :sample:composeApp:jvmRun` — launch desktop app, verify all 5 tabs render and are navigable
2. Click through each tab: verify interactive inputs produce correct results, read-only demos display expected values
3. Test invalid inputs in interactive sections: verify error states display correctly
4. `./gradlew :sample:composeApp:build` — verify builds for all targets (Android, iOS, JS, WASM, JVM)
5. Verify no lint issues: `./gradlew ktlintCheck`
