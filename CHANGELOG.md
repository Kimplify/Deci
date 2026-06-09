# Changelog

## [0.3.0] - 2026-06-09

### Changed

- **The `/` and `%` operators are now fixed to `DeciContext.DEFAULT`.** Behaviour
  is unchanged for any code that never reassigned the (now-removed) global
  `divisionPolicy`: bare division still yields 20 fractional digits with
  `HALF_UP` rounding, and `%` (which is computed via `/`) is likewise unaffected.

- **Migrated to the Kotlin Gradle plugin's built-in ABI validation — no public
  API or runtime changes.** Dropped the deprecated standalone
  `org.jetbrains.kotlinx:binary-compatibility-validator` plugin in favour of the
  Kotlin Gradle plugin's `abiValidation { }` DSL. The `apiDump` / `apiCheck`
  tasks are replaced by `updateKotlinAbi` / `checkKotlinAbi`, and the reference
  dump now lives at `deci/api/jvm/deci.api` (JVM) plus `deci/api/deci.klib.api`
  (Kotlin/Native, JS, and Wasm). Removed the `binary-compatibility-validator`
  version and `bcv-gradle-plugin` version-catalog entries.

### Removed

- **Deprecated division-policy configuration removed (breaking).** Deleted
  `DeciConfiguration.divisionPolicy`, `DeciConfiguration.resetDivisionPolicy()`,
  and the `DeciDivisionPolicy` class. The `/` operator no longer reads a global
  mutable policy; it always divides with `DeciContext.DEFAULT` (20 fractional
  digits, `HALF_UP`) — identical to the previous out-of-the-box default. To
  customise division scale or rounding, pass an explicit context per call:
  `a.divide(b, DeciContext.CURRENCY_USD)` or
  `a.divide(b, scale = 4, roundingMode = RoundingMode.HALF_EVEN)`.
  `DeciConfiguration.logSink` / `disableLogging()` are unaffected.

- **`Iterable<Deci>.averageDeci()` removed (breaking).** Use
  `Iterable<Deci>.mean()` from `org.kimplify.deci.statistics` instead — a drop-in
  replacement with the same signature and semantics: `values.mean(context)`.

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
