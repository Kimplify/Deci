# Changelog

## [Unreleased]

## [0.2.2] - 2026-06-05

### Removed

- **Intel Apple targets dropped** — `iosX64` and `macosX64` klibs are no longer
  published. Consumers targeting Intel macOS or the Intel iOS simulator must stay
  on `0.2.1` or earlier. The Apple Silicon targets (`iosArm64`,
  `iosSimulatorArm64`, `macosArm64`) are unaffected.

### Changed

- **Android `compileSdk` raised to 37** — projects depending on `deci` must now
  compile against Android API level 37 or later.

- **Build scripts modernized — no public API or runtime changes.** Migrated
  deprecated Gradle/AGP/Kotlin/Compose DSL across all modules: the AGP KMP
  `androidLibrary { }` block → `android { }`, `js(IR)` → `js`, the deprecated
  `compose.*` dependency shortcuts → version-catalog coordinates, and the webpack
  `devServer.static` property → the `static()` function. Removed unused build
  dependencies and version-catalog entries (coroutines core/android/swing, plus
  the `android-library`, `ktlint`, and `binary-compatibility-validator` plugin
  aliases), and enabled Android host tests so the common test suite also runs on
  the Android variant.

- **Dependency and toolchain updates** — no public API changes. Kotlin
  `2.3.20` → `2.4.0`, Android Gradle Plugin `9.0.1` → `9.2.1`, Compose
  `1.10.3` → `1.11.1`, kotlinx-coroutines `1.10.2` → `1.11.0`,
  kotlinx-serialization `1.10.0` → `1.11.0`, Kotest `6.1.9` → `6.1.11`,
  and the Gradle wrapper `9.1.0` → `9.5.1`.

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
