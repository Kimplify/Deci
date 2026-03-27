# Remove `toPlainString()` — Unify String Representation

**Date:** 2026-03-27
**Status:** Approved

## Problem

Deci has two string conversion methods: `toString()` (may use scientific notation on JVM/Android) and `toPlainString()` (never scientific notation). This creates confusion and cross-platform inconsistency. For a financial library, scientific notation is never appropriate, and having two methods that *should* behave identically is unnecessary API surface.

Additionally, the JVM/Android `stripTrailingZeros()` call in the constructor has already been removed (on the `fix/remove-strip-trailing-zeros` branch), so trailing zeros are now preserved.

## Decision

Remove `toPlainString()` entirely. Make `toString()` the single string representation method, and guarantee it never uses scientific notation on any platform.

## Changes

### 1. API Surface (commonMain)

- Remove `fun toPlainString(): String` from the `expect` declaration in `commonMain/Deci.kt`
- Update `toString()` KDoc: remove the scientific notation caveat, state it always returns plain decimal form preserving scale

### 2. Platform Implementations

**JVM (`jvmMain/Deci.kt`) and Android (`androidMain/Deci.kt`):**
- Remove `actual fun toPlainString()`
- Change `toString()` from `internal.toString()` to `internal.toPlainString()` (BigDecimal's method)

**JS (`jsMain/Deci.kt`) and wasmJs (`wasmJsMain/Deci.kt`):**
- Remove `actual fun toPlainString()`
- `toString()` unchanged (already never uses scientific notation)

**Apple (`appleMain/Deci.kt`):**
- Remove `actual fun toPlainString()`
- `toString()` unchanged (already never uses scientific notation)

### 3. Internal Call Site Migrations (`toPlainString()` -> `toString()`)

All these files call `Deci.toPlainString()` and must switch to `toString()`:

- `DeciSerializer.kt:30`
- `DeciFormatting.kt:37, 69, 91, 141, 143, 159, 257`
- `DeciValidation.kt:66, 118, 237, 246`
- `DeciMath.kt:155`
- `DeciExtensions.kt:34`

**Note:** JVM/Android files that call `BigDecimal.toPlainString()` (e.g., in `operate()`, `divide()`, `abs()`, `negate()`) are calling Java's method, not Deci's. These do not change.

### 4. Test Updates

- `DeciTest.kt:261` — `d.toPlainString()` -> `d.toString()`
- `DeciTest.kt:285-286` — flip assertion: `Deci("1.2300").toString()` should equal `"1.2300"` (trailing zeros preserved)
- `DeciTest.kt:289-291` — rename test to "toString never uses scientific notation", change `toPlainString()` calls to `toString()`
- `DeciTest.kt:294-297` — delete "toString may use scientific notation" test
- `DeciPropertyTest.kt:207` — `a.toPlainString()` -> `a.toString()`
- `DeciSerializationTest.kt:83` — `restored.toPlainString()` -> `restored.toString()`
- `DeciMathExtendedTest.kt:268` — `result.toPlainString()` -> `result.toString()`
- `DeciBulkOperationsTest.kt:267` — `result[0].toPlainString()` -> `result[0].toString()`

### 5. Sample App

- `ValidationScreen.kt:284` — two `.toPlainString()` calls -> `.toString()`

### 6. Documentation

- `commonMain/Deci.kt` KDoc on `toString()` — remove scientific notation language, document plain decimal form
- `DeciSerializer.kt` KDoc — remove `toPlainString` reference, say `toString()` is used
- README.MD and CLAUDE.md have no direct references to `toPlainString`, no changes needed

### 7. API Compatibility

- Run `./gradlew apiDump` after all changes to update the `.api` file (removing `toPlainString` from the public API)

## Out of Scope

- Changing how arithmetic propagates scale (e.g., what scale `Deci("1.50") + Deci("2.5")` produces) — that's a separate concern
- Adding locale-aware formatting — `toString()` always uses `.` as separator; locale formatting belongs in `DeciFormatting`
