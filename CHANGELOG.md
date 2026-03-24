# Changelog

## [Unreleased]

### Fixed

- **`harmonicMean()` no longer crashes with low-precision contexts** — calling
  `harmonicMean(DeciContext.CURRENCY_USD)` on large values (e.g., 1200) caused a
  `DeciDivisionByZeroException` because reciprocals like `1/1200` rounded to `0.00`
  at 2 decimal places. Intermediate reciprocal calculations now use extra internal
  precision (`context.precision + 10`), matching the pattern already used by `sqrt()`.

### Changed

- **Sample app restructured** into a 5-tab navigation app (Core, Scale & Context,
  Financial, Format & Stats, Validation) showcasing all library features.

## [0.2.0] - 2026-03-20

### Breaking Changes

- **`toString()` may now return scientific notation** for very large or very small values.
  On JVM/Android this delegates to `BigDecimal.toString()`, on JS/WASM to `decimal.js`'s `toString()`.
  Previously, `toString()` always returned a plain decimal string.

### Added

- **`toPlainString()`** — returns the string representation without scientific notation,
  preserving the scale (e.g. `"1.50"` stays `"1.50"`). This is the behaviour that
  `toString()` had in 0.1.x.

### Migration Guide

Replace `toString()` with `toPlainString()` wherever you need a plain decimal string:

```kotlin
// Before (0.1.x)
val text = myDeci.toString()          // always "100000000000000000000"

// After (0.2.0)
val text = myDeci.toPlainString()     // always "100000000000000000000"
val debug = myDeci.toString()         // may return "1E+20" on JVM
```

Common patterns to update:

| Old pattern | New pattern |
|---|---|
| `deci.toString()` (for display/storage) | `deci.toPlainString()` |
| `Deci(someDeci.toString())` | `Deci(someDeci.toPlainString())` |
| `"Amount: $deci"` (if plain format needed) | `"Amount: ${deci.toPlainString()}"` |

**Serialization is unaffected** — the built-in `DeciSerializer` has been updated internally
to use `toPlainString()`, so JSON output remains a plain decimal string.

## [0.1.1]

Initial release.
