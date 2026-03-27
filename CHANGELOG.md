# Changelog

## [0.2.1] - 2026-03-24

### Fixed

- **`harmonicMean()` no longer crashes with low-precision contexts** — calling
  `harmonicMean(DeciContext.CURRENCY_USD)` on large values (e.g., 1200) caused a
  `DeciDivisionByZeroException` because reciprocals like `1/1200` rounded to `0.00`
  at 2 decimal places. Intermediate reciprocal calculations now use extra internal
  precision (`context.precision + 10`), matching the pattern already used by `sqrt()`.

### Changed

- **Sample app restructured** into a 5-tab navigation app (Core, Scale & Context,
  Financial, Format & Stats, Validation) showcasing all library features.

- **`toString()` now preserves trailing zeros** — `Deci("1.50").toString()` returns
  `"1.50"`, and `Deci("1.2300").toString()` returns `"1.2300"`. The JVM/Android
  constructor no longer strips trailing zeros.

- **`toString()` never uses scientific notation** — guaranteed across all platforms
  (JVM, Android, JS, wasmJs, Apple). For example, `Deci("100000000000000000000").toString()`
  always returns `"100000000000000000000"`, never `"1E+20"`.

## [0.2.0] - 2026-03-20

### Added

- Comprehensive sample app with tabbed navigation

## [0.1.1]

Initial release.
