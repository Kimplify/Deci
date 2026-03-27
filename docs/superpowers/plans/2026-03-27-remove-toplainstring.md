# Remove `toPlainString()` — Unify String Representation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove `toPlainString()` from the Deci API and make `toString()` the single string representation method that never uses scientific notation.

**Architecture:** Remove `toPlainString()` from the expect declaration and all actuals. On JVM/Android, switch `toString()` to delegate to `BigDecimal.toPlainString()`. Migrate all internal and test call sites from `toPlainString()` to `toString()`.

**Tech Stack:** Kotlin Multiplatform, BigDecimal (JVM), DecimalJS (JS/wasmJs), NSDecimalNumber (Apple)

---

### Task 1: Remove `toPlainString()` from expect declaration and update `toString()` KDoc

**Files:**
- Modify: `deci/src/commonMain/kotlin/org/kimplify/deci/Deci.kt:131-148`

- [ ] **Step 1: Remove `toPlainString()` and update `toString()` KDoc**

Replace lines 131-148 with:

```kotlin

    /**
     * Returns the string representation of this [Deci] without scientific notation,
     * preserving the scale (e.g. `"1.50"` stays `"1.50"`).
     *
     * This method never uses exponential notation regardless of the value's magnitude.
     */
    override fun toString(): String
```

- [ ] **Step 2: Verify commonMain compiles**

Run: `./gradlew :deci:compileCommonMainKotlinMetadata`
Expected: Compilation errors in actuals and call sites (expected at this stage)

- [ ] **Step 3: Commit**

```bash
git add deci/src/commonMain/kotlin/org/kimplify/deci/Deci.kt
git commit -m "Remove toPlainString from expect declaration, update toString KDoc"
```

---

### Task 2: Update JVM actual — remove `toPlainString()`, fix `toString()`

**Files:**
- Modify: `deci/src/jvmMain/kotlin/org/kimplify/deci/Deci.kt:106-108`

- [ ] **Step 1: Remove `toPlainString()` and change `toString()` to use `BigDecimal.toPlainString()`**

Replace:

```kotlin
    actual override fun toString(): String = internal.toString()

    actual fun toPlainString(): String = internal.toPlainString()
```

With:

```kotlin
    actual override fun toString(): String = internal.toPlainString()
```

- [ ] **Step 2: Commit**

```bash
git add deci/src/jvmMain/kotlin/org/kimplify/deci/Deci.kt
git commit -m "JVM: remove toPlainString, make toString use BigDecimal.toPlainString"
```

---

### Task 3: Update Android actual — remove `toPlainString()`, fix `toString()`

**Files:**
- Modify: `deci/src/androidMain/kotlin/org/kimplify/deci/Deci.kt:106-108`

- [ ] **Step 1: Remove `toPlainString()` and change `toString()` to use `BigDecimal.toPlainString()`**

Replace:

```kotlin
    actual override fun toString(): String = internal.toString()

    actual fun toPlainString(): String = internal.toPlainString()
```

With:

```kotlin
    actual override fun toString(): String = internal.toPlainString()
```

- [ ] **Step 2: Commit**

```bash
git add deci/src/androidMain/kotlin/org/kimplify/deci/Deci.kt
git commit -m "Android: remove toPlainString, make toString use BigDecimal.toPlainString"
```

---

### Task 4: Update JS actual — remove `toPlainString()`, fix `toString()`

**Files:**
- Modify: `deci/src/jsMain/kotlin/org/kimplify/deci/Deci.kt:79-88`

- [ ] **Step 1: Remove `toPlainString()` and fix `toString()` to never use scientific notation**

`DecimalJs.toString()` can use scientific notation for extreme values. Replace both methods with a single `toString()` that uses `toFixed()` (which never uses scientific notation):

Replace:

```kotlin
    actual override fun toString(): String {
        val scale = _scale ?: return internal.toString()
        if (scale <= 0) return internal.toString()
        return internal.toFixed(scale)
    }

    actual fun toPlainString(): String {
        val scale = _scale
        return if (scale != null) internal.toFixed(scale) else internal.toFixed()
    }
```

With:

```kotlin
    actual override fun toString(): String {
        val scale = _scale
        return if (scale != null && scale > 0) internal.toFixed(scale) else internal.toFixed()
    }
```

- [ ] **Step 2: Commit**

```bash
git add deci/src/jsMain/kotlin/org/kimplify/deci/Deci.kt
git commit -m "JS: remove toPlainString, fix toString to never use scientific notation"
```

---

### Task 5: Update wasmJs actual — remove `toPlainString()`, fix `toString()`

**Files:**
- Modify: `deci/src/wasmJsMain/kotlin/org/kimplify/deci/Deci.kt:79-88`

- [ ] **Step 1: Remove `toPlainString()` and fix `toString()` to never use scientific notation**

Same fix as JS — `DecimalJs.toString()` can use scientific notation. Replace both methods:

Replace:

```kotlin
    actual override fun toString(): String {
        val scale = _scale ?: return internal.toString()
        if (scale <= 0) return internal.toString()
        return internal.toFixed(scale)
    }

    actual fun toPlainString(): String {
        val scale = _scale
        return if (scale != null) internal.toFixed(scale) else internal.toFixed()
    }
```

With:

```kotlin
    actual override fun toString(): String {
        val scale = _scale
        return if (scale != null && scale > 0) internal.toFixed(scale) else internal.toFixed()
    }
```

- [ ] **Step 2: Commit**

```bash
git add deci/src/wasmJsMain/kotlin/org/kimplify/deci/Deci.kt
git commit -m "wasmJs: remove toPlainString, fix toString to never use scientific notation"
```

---

### Task 6: Update Apple actual — remove `toPlainString()`

**Files:**
- Modify: `deci/src/appleMain/kotlin/org/kimplify/deci/Deci.kt:174-182`

- [ ] **Step 1: Remove `toPlainString()`**

Remove these lines:

```kotlin
    actual fun toPlainString(): String {
        val str = internal.stringValue
        val scale = _scale ?: return str
        if (scale == 0) return str.split(".")[0]
        val parts = str.split(".")
        val intPart = parts[0]
        val fracPart = if (parts.size > 1) parts[1] else ""
        return "$intPart.${fracPart.padEnd(scale, '0')}"
    }
```

- [ ] **Step 2: Commit**

```bash
git add deci/src/appleMain/kotlin/org/kimplify/deci/Deci.kt
git commit -m "Apple: remove toPlainString"
```

---

### Task 7: Migrate internal call sites from `toPlainString()` to `toString()`

**Files:**
- Modify: `deci/src/commonMain/kotlin/org/kimplify/deci/DeciSerializer.kt:30`
- Modify: `deci/src/commonMain/kotlin/org/kimplify/deci/formatting/DeciFormatting.kt:37,69,91,141,143,159,257`
- Modify: `deci/src/commonMain/kotlin/org/kimplify/deci/validation/DeciValidation.kt:66,118,237,246`
- Modify: `deci/src/commonMain/kotlin/org/kimplify/deci/math/DeciMath.kt:155`
- Modify: `deci/src/commonMain/kotlin/org/kimplify/deci/extension/DeciExtensions.kt:34`

- [ ] **Step 1: Update DeciSerializer**

In `DeciSerializer.kt` line 30, replace:

```kotlin
        encoder.encodeString(value.toPlainString())
```

With:

```kotlin
        encoder.encodeString(value.toString())
```

Also update the class KDoc — replace:

```kotlin
 * For example, `Deci("1.50")` is serialized as the JSON string `"1.50"`, not the
 * JSON number `1.5`.
 *
 * Deserialization parses the decoded string via the [Deci] string constructor.
```

With:

```kotlin
 * For example, `Deci("1.50")` is serialized as the JSON string `"1.50"`, not the
 * JSON number `1.5`.
 *
 * Serialization uses [Deci.toString], which never produces scientific notation.
 * Deserialization parses the decoded string via the [Deci] string constructor.
```

- [ ] **Step 2: Update DeciFormatting**

In `DeciFormatting.kt`, replace all `.toPlainString()` with `.toString()`. There are 7 occurrences:

Line 37: `val str = this.toPlainString()` → `val str = this.toString()`
Line 69: `return "${rounded.toPlainString()}$symbol"` → `return "${rounded.toString()}$symbol"`
Line 91: `val str = this.abs().toPlainString()` → `val str = this.abs().toString()`
Line 141: `"0.00" -> this.setScale(2, RoundingMode.HALF_UP).toPlainString()` → `"0.00" -> this.setScale(2, RoundingMode.HALF_UP).toString()`
Line 143: `"0.0000" -> this.setScale(4, RoundingMode.HALF_UP).toPlainString()` → `"0.0000" -> this.setScale(4, RoundingMode.HALF_UP).toString()`
Line 159: `val parts = abs.toPlainString().split(".")` → `val parts = abs.toString().split(".")`
Line 257: `val str = this.toPlainString()` → `val str = this.toString()`

- [ ] **Step 3: Update DeciValidation**

In `DeciValidation.kt`, replace all `.toPlainString()` with `.toString()`. There are 4 occurrences:

Line 66: `val str = this.toPlainString()` → `val str = this.toString()`
Line 118: `val str = this.toPlainString()` → `val str = this.toString()`
Line 237: `"Value must be at least ${min.toPlainString()}"` → `"Value must be at least ${min.toString()}"`
Line 246: `"Value must be at most ${max.toPlainString()}"` → `"Value must be at most ${max.toString()}"`

- [ ] **Step 4: Update DeciMath**

In `DeciMath.kt` line 155, replace:

```kotlin
    val str = absValue.toPlainString()
```

With:

```kotlin
    val str = absValue.toString()
```

- [ ] **Step 5: Update DeciExtensions**

In `DeciExtensions.kt` line 34, replace:

```kotlin
    val str = truncated.toPlainString()
```

With:

```kotlin
    val str = truncated.toString()
```

- [ ] **Step 6: Commit**

```bash
git add deci/src/commonMain/kotlin/org/kimplify/deci/DeciSerializer.kt \
       deci/src/commonMain/kotlin/org/kimplify/deci/formatting/DeciFormatting.kt \
       deci/src/commonMain/kotlin/org/kimplify/deci/validation/DeciValidation.kt \
       deci/src/commonMain/kotlin/org/kimplify/deci/math/DeciMath.kt \
       deci/src/commonMain/kotlin/org/kimplify/deci/extension/DeciExtensions.kt
git commit -m "Migrate all internal toPlainString calls to toString"
```

---

### Task 8: Update tests

**Files:**
- Modify: `deci/src/commonTest/kotlin/org/kimplify/deci/DeciTest.kt:258-297`
- Modify: `deci/src/commonTest/kotlin/org/kimplify/deci/DeciPropertyTest.kt:207`
- Modify: `deci/src/commonTest/kotlin/org/kimplify/deci/DeciSerializationTest.kt:83`
- Modify: `deci/src/commonTest/kotlin/org/kimplify/deci/math/DeciMathExtendedTest.kt:268`
- Modify: `deci/src/commonTest/kotlin/org/kimplify/deci/bulk/DeciBulkOperationsTest.kt:267`

- [ ] **Step 1: Update DeciTest.kt — fix roundtrip test**

Replace:

```kotlin
    @Test fun `toDouble and toPlainString roundtrip for simple values`() {
        listOf("0", "1.5", "-2.75").forEach { s ->
            val d = Deci(s)
            assertEquals(s, d.toPlainString())
            assertEquals(s.toDouble(), d.toDouble())
        }
    }
```

With:

```kotlin
    @Test fun `toDouble and toString roundtrip for simple values`() {
        listOf("0", "1.5", "-2.75").forEach { s ->
            val d = Deci(s)
            assertEquals(s, d.toString())
            assertEquals(s.toDouble(), d.toDouble())
        }
    }
```

- [ ] **Step 2: Update DeciTest.kt — flip trailing zeros test**

Replace:

```kotlin
    @Test fun `trailing zeros are stripped by constructor`() {
        assertEquals("1.23", Deci("1.2300").toPlainString())
    }
```

With:

```kotlin
    @Test fun `trailing zeros are preserved by constructor`() {
        assertEquals("1.2300", Deci("1.2300").toString())
    }
```

- [ ] **Step 3: Update DeciTest.kt — rename scientific notation test**

Replace:

```kotlin
    @Test fun `toPlainString never uses scientific notation`() {
        assertEquals("100000000000000000000", Deci("100000000000000000000").toPlainString())
        assertEquals("0.000000001", Deci("0.000000001").toPlainString())
    }
```

With:

```kotlin
    @Test fun `toString never uses scientific notation`() {
        assertEquals("100000000000000000000", Deci("100000000000000000000").toString())
        assertEquals("0.000000001", Deci("0.000000001").toString())
    }
```

- [ ] **Step 4: Update DeciTest.kt — delete the old scientific notation test**

Delete:

```kotlin
    @Test fun `toString may use scientific notation for extreme values`() {
        val large = Deci("100000000000000000000")
        assertEquals(large, Deci(large.toPlainString()))
    }
```

- [ ] **Step 5: Update DeciPropertyTest.kt**

Line 207, replace:

```kotlin
                val copy = Deci(a.toPlainString())
```

With:

```kotlin
                val copy = Deci(a.toString())
```

- [ ] **Step 6: Update DeciSerializationTest.kt**

Line 83, replace:

```kotlin
            assertEquals(s, restored.toPlainString(), "String representation changed for $s")
```

With:

```kotlin
            assertEquals(s, restored.toString(), "String representation changed for $s")
```

- [ ] **Step 7: Update DeciMathExtendedTest.kt**

Line 268, replace:

```kotlin
        assertTrue(result.toPlainString().startsWith("12"))
```

With:

```kotlin
        assertTrue(result.toString().startsWith("12"))
```

- [ ] **Step 8: Update DeciBulkOperationsTest.kt**

Line 267, replace:

```kotlin
        assertTrue(result[0].toPlainString().length > 5)
```

With:

```kotlin
        assertTrue(result[0].toString().length > 5)
```

- [ ] **Step 9: Commit**

```bash
git add deci/src/commonTest/kotlin/org/kimplify/deci/DeciTest.kt \
       deci/src/commonTest/kotlin/org/kimplify/deci/DeciPropertyTest.kt \
       deci/src/commonTest/kotlin/org/kimplify/deci/DeciSerializationTest.kt \
       deci/src/commonTest/kotlin/org/kimplify/deci/math/DeciMathExtendedTest.kt \
       deci/src/commonTest/kotlin/org/kimplify/deci/bulk/DeciBulkOperationsTest.kt
git commit -m "Update all tests: toPlainString -> toString, fix trailing zeros assertion"
```

---

### Task 9: Update sample app

**Files:**
- Modify: `sample/composeApp/src/commonMain/kotlin/org/kimplify/screens/ValidationScreen.kt:284`

- [ ] **Step 1: Replace `toPlainString()` call**

Line 284, replace:

```kotlin
                original.toPlainString() == deserialized.toPlainString()
```

With:

```kotlin
                original.toString() == deserialized.toString()
```

- [ ] **Step 2: Commit**

```bash
git add sample/composeApp/src/commonMain/kotlin/org/kimplify/screens/ValidationScreen.kt
git commit -m "Sample app: toPlainString -> toString"
```

---

### Task 10: Build, test, and update API dump

- [ ] **Step 1: Build all targets**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run all tests**

Run: `./gradlew allTests`
Expected: All tests pass

- [ ] **Step 3: Update API dump**

Run: `./gradlew apiDump`
Expected: `.api` file updated, `toPlainString` removed from public API

- [ ] **Step 4: Verify API dump is clean**

Run: `./gradlew apiCheck`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit API dump**

```bash
git add deci/api/
git commit -m "Update API dump: remove toPlainString from public API"
```
