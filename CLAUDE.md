# Deci – Claude Project Context

## Project Overview

**Deci** is a Kotlin Multiplatform (KMP) library that provides high-precision decimal arithmetic, predictable rounding, and serialization support across every Kotlin target. It is purpose-built for financial workloads — money, taxes, invoicing, and any domain where `Float`/`Double` floating-point imprecision is unacceptable.

---

## Core Goals

- **Correctness over speed** — exact decimal representation is the primary invariant; never sacrifice precision for performance.
- **Predictable rounding** — rounding modes must be explicit and deterministic; silent rounding is a bug.
- **Multiplatform first** — every public API must compile and behave identically on JVM, JS, Native, and WASM targets.
- **Serialization-ready** — values must round-trip cleanly through kotlinx.serialization (JSON, Protobuf, etc.) without precision loss.
- **Minimal dependencies** — the library must remain lightweight; avoid pulling in heavy runtime dependencies.

---

## Architecture & Module Layout

```
deci/
├── core/                  # Expect/actual declarations, public API surface
│   ├── commonMain/        # Decimal class, arithmetic operators, rounding modes
│   ├── jvmMain/           # JVM actual (backed by java.math.BigDecimal)
│   ├── jsMain/            # JS actual
│   ├── nativeMain/        # Native actual
│   └── wasmMain/          # WASM actual (if supported)
├── serialization/         # kotlinx.serialization integration
│   └── commonMain/
├── ktor/                  # Optional: Ktor content-negotiation integration
└── build-logic/           # Convention plugins, shared Gradle config
```

> When adding a new public API, always provide the `expect` declaration in `commonMain` before writing any `actual` implementations.

---

## Key Types

| Type | Purpose |
|---|---|
| `Decimal` | Core value type — immutable, arbitrary-precision decimal number |
| `RoundingMode` | Enum of supported rounding strategies (mirrors `java.math.RoundingMode` semantics) |
| `DecimalContext` | Carries precision and rounding mode; analogous to `java.math.MathContext` |
| `DecimalSerializer` | kotlinx.serialization `KSerializer<Decimal>` — serializes as JSON string |

---

## Coding Conventions

### General
- **Kotlin style** — follow [Kotlin coding conventions](https://kotlinlint.io); use `ktlint` for formatting.
- **Immutability** — `Decimal` and `DecimalContext` are value types; all operations return new instances.
- **No nullable returns** — arithmetic operations never return `null`; throw a typed exception on illegal operations (e.g. divide by zero).
- **Operator overloading** — implement `+`, `-`, `*`, `/`, `%`, `unaryMinus`, and comparison operators on `Decimal`.
- **Extension functions** — prefer extension functions over utility classes for conversions (e.g. `Int.toDecimal()`, `String.toDecimal()`).

### Precision & Rounding
- Never perform rounding implicitly — always require an explicit `RoundingMode` when a result must be rounded.
- Division must always accept a scale/rounding parameter; bare division that could produce infinite decimals must not compile without one.
- Document the rounding behaviour of every public function in its KDoc.

### Multiplatform
- Do not use `java.*` APIs in `commonMain` — they will not compile on JS/Native.
- Platform-specific optimisations belong in `actual` implementations only.
- Use `expect`/`actual` sparingly; prefer pure Kotlin algorithms in `commonMain` wherever performance allows.

### Error Handling
- Use a sealed `DecimalException` hierarchy; avoid raw `Exception` or `ArithmeticException` in public APIs.
- Validate inputs eagerly (constructor / factory functions) rather than lazily.

---

## Testing

- Tests live in `commonTest`; they run on all targets via `./gradlew allTests`.
- Use **property-based testing** (e.g. `kotest-property`) for arithmetic correctness — test with random inputs across the full value range.
- Every rounding mode must have explicit, table-driven unit tests with known inputs and expected outputs.
- Serialization tests must assert exact string representations, not just round-trip equality.
- Do not use `Float`/`Double` literals as expected values in tests — always use string-constructed `Decimal` values.

```kotlin
// ✅ correct
val expected = "0.10".toDecimal()

// ❌ wrong — introduces the float imprecision we are trying to avoid
val expected = 0.10.toDecimal()
```

---

## Common Commands

```bash
# Build all targets
./gradlew build

# Run all tests across all targets
./gradlew allTests

# Run JVM tests only
./gradlew :core:jvmTest

# Lint / format check
./gradlew ktlintCheck

# Auto-fix lint issues
./gradlew ktlintFormat

# Publish to local Maven (for local integration testing)
./gradlew publishToMavenLocal

# Generate API binary-compatibility dump
./gradlew apiDump

# Check for binary-compatibility regressions
./gradlew apiCheck
```

---

## API Stability

- This library uses [kotlin-binary-compatibility-validator](https://github.com/Kotlin/binary-compatibility-validator).
- Run `./gradlew apiDump` after any public API change and commit the updated `.api` file.
- `./gradlew apiCheck` runs in CI and will fail the build on unintentional API changes.
- Mark experimental APIs with `@ExperimentalDeciApi` and require opt-in.

---

## Serialization Contract

- `Decimal` serializes to a **JSON string** (not a JSON number) to preserve trailing zeros and avoid lossy float coercion by JSON parsers.
- Example: `Decimal("1.50")` → `"1.50"` (not `1.5` or `1.50` as a number).
- Deserialization must reject non-numeric strings and throw `DecimalSerializationException`.

---

## What Claude Should NOT Do

- Do not suggest replacing `Decimal` with `Double` or `Float` for "simplicity".
- Do not introduce rounding without an explicit `RoundingMode` parameter.
- Do not add `java.*` imports to `commonMain` source sets.
- Do not silently truncate or coerce precision in operator implementations.
- Do not use `toBigDecimal()` on a `Double` as an intermediate step — this inherits the float's imprecision.

---

## Useful References

- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
- [Kotlin Multiplatform docs](https://kotlinlang.org/docs/multiplatform.html)
- [java.math.BigDecimal Javadoc](https://docs.oracle.com/en/java/docs/api/java.base/java/math/BigDecimal.html) — reference for JVM actual semantics
- [IEEE 754 Decimal Arithmetic](https://speleotrove.com/decimal/) — background on decimal floating-point standards
