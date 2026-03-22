# Trailing-Zero Preservation: Architecture Decision Record

## Problem

`Deci("1").setScale(2, HALF_UP).toString()` returned `"1.00"` on JVM but `"1"` on Apple/JS/WASM. This violated the `toString()` KDoc contract: *"preserving trailing zeros (e.g. `"1.50"` stays `"1.50"`)"*.

### Why JVM works natively

`java.math.BigDecimal` has a persistent internal `scale` field. When `setScale(2)` is called, the resulting `BigDecimal` remembers "I have 2 decimal places" and `toPlainString()` outputs them.

### Why other platforms don't

- **Apple (NSDecimalNumber):** `stringValue` normalizes away trailing zeros.
- **JS/WASM (decimal.js):** `toString()` normalizes away trailing zeros.

## Solution: `targetScale` field

Added a private `Int` field to `Deci` on non-JVM platforms:

```kotlin
actual class Deci private constructor(
    private val internal: PlatformDecimal,
    private val targetScale: Int = -1,  // -1 = natural (no padding)
)
```

### How it works

- **Write:** `setScale(scale, roundingMode)` and `divide(divisor, scale, roundingMode)` set `targetScale = scale` on the returned instance.
- **Read:** `toString()` checks `targetScale`. If > 0, pads the fractional part to exactly `targetScale` digits. If <= 0, returns the platform's natural string.
- **Reset:** Arithmetic operators (`+`, `-`, `*`, `/`) create new `Deci` instances via the default constructor (`targetScale = -1`), naturally resetting scale. This matches JVM behavior where arithmetic on `BigDecimal` produces results with computed scales.

### Platform-specific implementation

| Platform | toString() padding strategy |
|----------|----------------------------|
| JVM | Not needed — `BigDecimal.toPlainString()` handles it natively |
| Apple | Manual string padding: split on `.`, pad fractional part with `'0'` |
| JS | `decimal.js` `toFixed(targetScale)` |
| WASM | `decimal.js` `toFixed(targetScale)` |

### Behavioral contract

| Operation | targetScale |
|-----------|-------------|
| `Deci(string)` / `Deci(int)` / `Deci(long)` | -1 (natural) |
| `setScale(n, mode)` | n |
| `divide(divisor, n, mode)` | n |
| `a + b`, `a - b`, `a * b`, `a / b` | -1 (natural) |
| `abs()`, `negate()` | -1 (natural) |

### Why this approach

1. **Proven pattern** — identical to how `java.math.BigDecimal` works internally.
2. **Minimal footprint** — 4 bytes per instance, no API changes, no new types.
3. **Clean semantics** — scale is display metadata, not numeric identity. `equals()` and `compareTo()` are unaffected.
4. **Self-contained** — logic lives in `setScale()` (write) and `toString()` (read) only.
