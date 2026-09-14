# Math Grapher — Bugs & Fixes + Help/About/Menu Upgrade Guide

Nothing in your project files was touched. Everything below is a snippet you paste in
yourself, file by file.

---

## PART 1 — Bugs found and how to fix them

### Bug 1 (worst one): `xy` and `sinx` silently give a WRONG graph instead of an error

**File:** `math/Tokenizer.kt`
**Where:** `readIdentifier()`

**Problem:** the tokenizer reads *every* consecutive letter as one identifier. So `xy`
(meant as `x * y`) becomes a single unknown variable called `"xy"`. `Evaluator` then
defaults any unknown variable to `1.0` — so instead of an error, you silently get a
wrong graph that looks plausible. Same thing happens with `sinx` (missing parens).

**Fix — replace `readIdentifier()` and the letter branch in `tokenize()`:**

```kotlin
// in tokenize()'s when-block, replace:
//   char.isLetter() -> tokens.add(readIdentifier())
// with:
char.isLetter() -> tokens.addAll(readIdentifierTokens())
```

```kotlin
private val KNOWN_WORDS = KNOWN_FUNCTIONS + setOf("pi", "e")

// Only keep a run of letters as ONE token if it's an exact function/constant name.
// Otherwise split it into single-letter variables (xy -> x, y) or split off a known
// function prefix so "sinx" surfaces a clear "expected (" error instead of guessing.
private fun readIdentifierTokens(): List<Token> {
    val startPos = pos
    while (pos < input.length && input[pos].isLetter()) {
        pos++
    }
    val word = input.substring(startPos, pos)

    if (KNOWN_WORDS.contains(word)) {
        return listOf(Token(TokenType.IDENTIFIER, word, startPos))
    }

    for (fn in KNOWN_FUNCTIONS.sortedByDescending { it.length }) {
        if (word.length > fn.length && word.startsWith(fn)) {
            val result = mutableListOf(Token(TokenType.IDENTIFIER, fn, startPos))
            val rest = word.substring(fn.length)
            for ((idx, ch) in rest.withIndex()) {
                result.add(Token(TokenType.IDENTIFIER, ch.toString(), startPos + fn.length + idx))
            }
            return result
        }
    }

    return word.mapIndexed { idx, ch -> Token(TokenType.IDENTIFIER, ch.toString(), startPos + idx) }
}

// delete the old readIdentifier() function entirely
```

**Also fix `insertImplicitMultiplication()`** so split letters like `x`,`y` actually
multiply — add this new `else if` branch right after the existing
`IDENTIFIER && LPAREN` one:

```kotlin
} else if (prevToken.type == TokenType.IDENTIFIER && currentToken.type == TokenType.IDENTIFIER) {
    // needed so split "xy" -> [x][y] becomes x * y.
    // Skipped when prevToken is a real function name, so "sin","x" (from "sinx")
    // is left alone and surfaces a clear parse error instead of multiplying.
    if (!KNOWN_FUNCTIONS.contains(prevToken.value)) {
        insertStar = true
    }
}
```

**Result after fix:** `xy` now correctly means `x * y`. `sinx` now gives a clear
"expected (" parse error instead of a silently wrong curve.

---

### Bug 2: parametric detection misfires on `sqrt`, `cot`, `atan`

**File:** `math/EquationClassifier.kt`

**Problem:**

```kotlin
if (str.contains(",") && (str.contains("t") || str.contains(" t ") || str.contains("t="))) {
```

The extra conditions are redundant — this line really just means *"has a comma AND
contains the letter t anywhere."* Any comma'd input with `sqrt`, `cot`, or `atan` in it
gets wrongly routed into parametric parsing.

**Fix:** check for a *standalone* `t`, not a substring match.

```kotlin
// add near the top of the object, above classify():
private val STANDALONE_T = Regex("(?<![a-zA-Z])t(?![a-zA-Z])")
```

```kotlin
// replace the condition with:
if (str.contains(",") && STANDALONE_T.containsMatchIn(str)) {
```

---

### Bug 3: the tapped-point label is invisible in dark mode

**File:** `graph/GraphCanvas.kt`, in the `inspectedPoint?.let { ... }` block

**Problem:** the label's `Paint()` is hardcoded to `android.graphics.Color.BLACK`,
while everything else in this file (grid labels) correctly pulls from the theme. Black
text on a dark background is unreadable.

**Fix — replace the whole `drawIntoCanvas` block inside `inspectedPoint?.let`:**

```kotlin
drawIntoCanvas { canvas ->
    val text = String.format(Locale.US, "(%.2f, %.2f)", worldX, worldY)
    val pointHaloPaint = Paint().apply {
        val bg = mathColors.surfaceElevated
        color = android.graphics.Color.argb(
            220,
            (bg.red * 255).toInt(),
            (bg.green * 255).toInt(),
            (bg.blue * 255).toInt()
        )
        textSize = 36f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    val pointTextPaint = Paint().apply {
        color = android.graphics.Color.argb(
            255,
            (textColor.red * 255).toInt(),
            (textColor.green * 255).toInt(),
            (textColor.blue * 255).toInt()
        )
        textSize = 36f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    canvas.nativeCanvas.drawText(text, sx, sy - 20f, pointHaloPaint)
    canvas.nativeCanvas.drawText(text, sx, sy - 20f, pointTextPaint)
}
```

This reuses the same halo-behind-text trick the grid labels already use, so it stays
readable over both light and dark curves.

---

### Bug 4: toggling degrees/radians is a hack, not real state

**File:** `graph/GraphViewModel.kt` + `ui/GraphScreen.kt`

**Problem:** `Evaluator.isDegreesMode` is a plain mutable `var` on a singleton object —
Compose has no way to know it changed. `GraphScreen.kt` works around this with:

```kotlin
// Trigger a recomposition and re-evaluation. A clean way without architecture
// changes is to just nudge the viewport.
viewModel.panViewport(0f, 0f, 100f, 100f)
```

That's a real comment already in your code — it's an admitted workaround.

**Fix — add real Compose state in `GraphViewModel.kt`:**

```kotlin
// add these imports at the top:
import com.prasad.mathgrapher.math.Evaluator
```

```kotlin
// add inside the GraphViewModel class, near nextId/nextColorIndex:
private val _isDegreesMode = mutableStateOf(Evaluator.isDegreesMode)
val isDegreesMode: State<Boolean> = _isDegreesMode

fun toggleAngleMode() {
    Evaluator.isDegreesMode = !Evaluator.isDegreesMode
    _isDegreesMode.value = Evaluator.isDegreesMode
}
```

Then in `GraphScreen.kt`, replace the angle `DropdownMenuItem`'s `onClick` (the one
with the viewport-nudge hack) with:

```kotlin
onClick = {
    viewModel.toggleAngleMode()
    showMenu = false
}
```

and read `val isDegreesMode by viewModel.isDegreesMode` near your other
`by viewModel....` lines, instead of calling `Evaluator.isDegreesMode` directly in the
UI.

---

### Bug 5 (lower priority — flagging, not a quick patch): redraws are uncached

`ImplicitSampler` evaluates a 150×150 grid (22,500 evaluations) per implicit equation,
and `CurveSampler` samples up to 2,000 points — all recomputed **every single frame**
while you pan/zoom, with nothing cached. With one or two implicit equations on screen,
dragging will visibly stutter. Fixing this properly means hoisting the sampling out of
the `Canvas` draw block into a `remember(equations, viewport, canvasWidth, canvasHeight) { ... }`
above it, so it only recomputes when those actually change — worth doing once the app
is otherwise stable, not urgent right now.

### Bug 6 (test gap, not a runtime bug): no tests on the actual graphing math

You have unit tests for `Tokenizer`/`Parser`/`Evaluator` (the easy layer) but zero for
`ImplicitSampler`, `CurveSampler`, `ParametricSampler`, `PolarSampler`, or
`GraphViewModel` — the numerically trickiest code, with no safety net. Worth adding
once features settle down.

### Bug 7 (known limitation): equations aren't saved

Equations live only in `GraphViewModel`'s memory. They survive a screen rotation but
not the app being killed in the background (which Android does often) — everything
you typed is gone on relaunch. Not a bug to "fix" quickly, more a feature to plan for
later (e.g. saving the equation list to `DataStore`).

---

## PART 2 — Add a "Help" item to the ⋮ menu

**File:** `ui/GraphScreen.kt`

Add a new dialog-visibility flag next to your other `rememberSaveable` ones:

```kotlin
var showHelpDialog by rememberSaveable { mutableStateOf(false) }
```

Add the Help dialog composable (put it right after your existing
`if (showCreditsDialog) { ... }` block):

```kotlin
if (showHelpDialog) {
    AlertDialog(
        onDismissRequest = { showHelpDialog = false },
        title = { Text("How to write equations") },
        text = {
            LazyColumn(modifier = Modifier.height(420.dp)) {
                item {
                    Text(
                        "This app understands 5 kinds of equations. Pick the form that " +
                        "matches what you're trying to graph:",
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                item { HelpSection(
                    title = "1. Explicit — y in terms of x",
                    body = "Write it starting with \"y =\". Covers lines, parabolas, " +
                           "sine/cosine waves, anything you can solve for y.",
                    examples = listOf("y = 2*x + 1", "y = x^2 - 4", "y = sin(x)")
                ) }
                item { HelpSection(
                    title = "2. Implicit — x and y mixed together",
                    body = "No clean \"y =\" form. Used for circles, ellipses, and " +
                           "curves where y can't be isolated.",
                    examples = listOf("x^2 + y^2 = 25", "x^2/4 + y^2/9 = 1")
                ) }
                item { HelpSection(
                    title = "3. Parametric — both x and y depend on t",
                    body = "Separate the x-part and y-part with a comma. Great for " +
                           "circles, spirals, and anything that looks like motion.",
                    examples = listOf("x = cos(t), y = sin(t)", "x = t, y = t^2")
                ) }
                item { HelpSection(
                    title = "4. Polar — distance from center depends on angle",
                    body = "Start with \"r =\". Used for spirals, roses, and cardioids.",
                    examples = listOf("r = 2 + 2*cos(theta)", "r = theta")
                ) }
                item { HelpSection(
                    title = "5. Inequality — shade a region instead of a line",
                    body = "Use <, >, <=, or >= instead of =.",
                    examples = listOf("y < x^2", "x^2 + y^2 <= 9")
                ) }
                item {
                    Text(
                        "Common mistakes",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Text("• Function names need parentheses: write sin(x), not sinx.")
                    Text("• Use * for multiplication: 2*x, not 2x written as \"2x\" alone works, but a*b needs the star.")
                    Text("• Use ^ for powers: x^2, not x².")
                    Text("• Parametric equations need a comma between the x-part and y-part.")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { showHelpDialog = false }) { Text("Got it") }
        }
    )
}
```

Add this small helper composable anywhere in the file (outside `GraphScreen`):

```kotlin
@Composable
private fun HelpSection(title: String, body: String, examples: List<String>) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(body, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))
        examples.forEach { ex ->
            Text("→ $ex", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
```

You'll need these extra imports at the top of the file:

```kotlin
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
```

(`height` is likely already imported; `LazyColumn` probably isn't.)

---

## PART 3 — Update the About dialog to list every supported equation type

Replace your existing `if (showAboutDialog) { ... }` block's `text = { ... }` content
with:

```kotlin
text = {
    Column {
        Text("Math Grapher graphs 5 kinds of equations:")
        Spacer(modifier = Modifier.height(8.dp))
        Text("• Explicit (y = ...) — lines, parabolas, trig curves")
        Text("• Implicit (x and y mixed) — circles, ellipses")
        Text("• Parametric (x = ..., y = ...) — motion-style curves")
        Text("• Polar (r = ...) — spirals, roses, cardioids")
        Text("• Inequalities (<, >, <=, >=) — shaded regions")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Open Help from the ⋮ menu for the full writing guide with examples.")
    }
}
```

---

## PART 4 — Make the ⋮ menu 100x better

Your current menu has 4 flat items with no icons, and one ("Add") that does nothing
when tapped. Replace the whole `DropdownMenu { ... }` block with a grouped version —
settings on top, info items below, separated by a divider, each with a leading glyph
so it's scannable at a glance instead of a wall of plain text:

```kotlin
DropdownMenu(
    expanded = showMenu,
    onDismissRequest = { showMenu = false },
    modifier = Modifier.width(230.dp)
) {
    // --- Settings ---
    DropdownMenuItem(
        text = {
            Column {
                Text("Angle unit", style = MaterialTheme.typography.labelSmall, color = mathColors.onSurfaceMuted)
                Text(if (isDegreesMode) "Degrees" else "Radians", fontWeight = FontWeight.Medium)
            }
        },
        leadingIcon = { Text("∠", fontSize = 18.sp) },
        onClick = {
            viewModel.toggleAngleMode()
            showMenu = false
        }
    )

    Divider(modifier = Modifier.padding(vertical = 4.dp))

    // --- Info ---
    DropdownMenuItem(
        text = { Text("Help — how to write equations") },
        leadingIcon = { Text("❓", fontSize = 16.sp) },
        onClick = {
            showMenu = false
            showHelpDialog = true
        }
    )
    DropdownMenuItem(
        text = { Text("About") },
        leadingIcon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) },
        onClick = {
            showMenu = false
            showAboutDialog = true
        }
    )
    DropdownMenuItem(
        text = { Text("Credits") },
        leadingIcon = { Text("★", fontSize = 16.sp) },
        onClick = {
            showMenu = false
            showCreditsDialog = true
        }
    )
}
```

Notes on this version:
- Dropped the dead "Add" item entirely — it had no `onClick` behavior.
- Used plain `Text()` glyphs (∠, ❓, ★) instead of Material icon names like
  `HelpOutline`/`Speed`, because those specific icons live in the
  **material-icons-extended** library, which your `build.gradle.kts` doesn't currently
  pull in — using them as-is would fail to compile. `Icons.Default.Info` is fine, it's
  in the core icon set your project already has (same one `MoreVert` comes from). If
  you'd rather have proper icon glyphs everywhere instead of emoji, add
  `implementation("androidx.compose.material:material-icons-extended")` to
  `app/build.gradle.kts` first, then swap the `Text(...)` leading icons for
  `Icon(imageVector = Icons.Default.HelpOutline, ...)` etc.
- Needs `viewModel.isDegreesMode`/`viewModel.toggleAngleMode()` from Bug 4's fix above
  — apply that one first, or swap that item's logic back to reading
  `Evaluator.isDegreesMode` directly if you're skipping Bug 4 for now.

---

## PART 5 — Say *why* an equation didn't draw (instead of blank or crash)

Right now there are two silent failure modes, not one:

1. **Parse errors already get caught** in `GraphViewModel.addEquation()` and stored in
   `equation.error` — but the UI only uses that to turn the equation chip's text red.
   There's no way to actually read the message; you just see red and have to guess.
2. **Runtime/sampling errors are not caught at all.** `CurveSampler`, `ImplicitSampler`,
   `ParametricSampler`, and `PolarSampler` call `Evaluator.evaluate()` directly inside
   `GraphCanvas`'s draw loop with no try/catch. If evaluating a *valid-looking* equation
   throws for some edge case, it throws out of the `Canvas` draw block — a crash, not a
   blank graph. And if the equation is valid but produces no visible points (e.g. always
   `NaN`, or a real curve that's just outside the current view), nothing is shown and
   nothing explains why.

Fixing this needs three pieces: (A) catch runtime errors per-equation so one bad
equation can't crash the whole canvas, (B) tell "actually broken" apart from "valid but
nothing to see here right now", and (C) make the message actually readable, not just a
red hint.

### A) Catch runtime errors per-equation in `GraphCanvas.kt`

Wrap each equation's sampling call in its own try/catch inside the `for (equation in
equations)` loop, and report failures upward instead of letting them propagate. Add a
callback parameter to `GraphCanvas`:

```kotlin
@Composable
fun GraphCanvas(
    equations: List<Equation>,
    viewport: Viewport,
    curveColors: List<Color>,
    inspectedPoint: Pair<Double, Double>?,
    onPan: (dx: Float, dy: Float, screenWidth: Float, screenHeight: Float) -> Unit,
    onZoom: (factor: Float, focusX: Float, focusY: Float, screenWidth: Float, screenHeight: Float) -> Unit,
    onTap: (x: Float, y: Float, screenWidth: Float, screenHeight: Float) -> Unit,
    onEquationRuntimeStatus: (id: Int, message: String?) -> Unit, // NEW
    mathColors: com.prasad.mathgrapher.ui.theme.MathGrapherColors,
    modifier: Modifier = Modifier
) {
```

Then replace the `for (equation in equations) { ... }` body with a version that
catches per-equation and classifies the "nothing drawn" case:

```kotlin
for (equation in equations) {
    if (equation.error != null) continue
    val color = if (curveColors.isNotEmpty()) {
        curveColors[equation.colorIndex % curveColors.size]
    } else {
        Color.Red
    }

    try {
        val eqType = equation.equationType
        when (eqType) {
            is com.prasad.mathgrapher.math.EquationType.Explicit -> {
                val points = CurveSampler.sample(eqType.expr, viewport, screenWidth, screenHeight)
                drawCurveOrReportEmpty(equation.id, points, color, onEquationRuntimeStatus)
            }
            is com.prasad.mathgrapher.math.EquationType.Implicit -> {
                val segments = ImplicitSampler.sample(eqType.lhs, eqType.rhs, viewport, screenWidth, screenHeight)
                if (segments.isEmpty()) {
                    onEquationRuntimeStatus(equation.id, "No solutions found in the current view — try zooming out.")
                } else {
                    onEquationRuntimeStatus(equation.id, null)
                    for (seg in segments) {
                        drawLine(color = color, start = Offset(seg.x1, seg.y1), end = Offset(seg.x2, seg.y2), strokeWidth = 3.dp.toPx())
                    }
                }
            }
            is com.prasad.mathgrapher.math.EquationType.Parametric -> {
                val points = ParametricSampler.sample(eqType.xExpr, eqType.yExpr, eqType.tMin, eqType.tMax, viewport, screenWidth, screenHeight)
                drawCurveOrReportEmpty(equation.id, points, color, onEquationRuntimeStatus)
            }
            is com.prasad.mathgrapher.math.EquationType.Polar -> {
                val points = PolarSampler.sample(eqType.rExpr, eqType.thetaMin, eqType.thetaMax, viewport, screenWidth, screenHeight)
                drawCurveOrReportEmpty(equation.id, points, color, onEquationRuntimeStatus)
            }
            is com.prasad.mathgrapher.math.EquationType.Inequality -> {
                val segments = ImplicitSampler.sample(eqType.lhs, eqType.rhs, viewport, screenWidth, screenHeight)
                onEquationRuntimeStatus(equation.id, if (segments.isEmpty()) "No boundary found in the current view — try zooming out." else null)
                for (seg in segments) {
                    drawLine(color = color, start = Offset(seg.x1, seg.y1), end = Offset(seg.x2, seg.y2), strokeWidth = 2.dp.toPx())
                }
            }
            null -> {
                val ast = equation.ast ?: continue
                val points = CurveSampler.sample(ast, viewport, screenWidth, screenHeight)
                drawCurveOrReportEmpty(equation.id, points, color, onEquationRuntimeStatus)
            }
        }
    } catch (t: Throwable) {
        // A single bad equation can no longer take down the whole canvas —
        // it just doesn't draw, and the reason is reported instead of thrown.
        onEquationRuntimeStatus(
            equation.id,
            "Couldn't graph this: ${t.message ?: t.javaClass.simpleName}"
        )
    }
}
```

Add this small helper (outside `GraphCanvas`, near `drawCurve`) that tells apart
*"never had a real value"* from *"real, just off-screen right now"*:

```kotlin
private fun DrawScope.drawCurveOrReportEmpty(
    id: Int,
    points: List<SamplePoint>,
    color: Color,
    onStatus: (Int, String?) -> Unit
) {
    if (points.isEmpty()) {
        onStatus(id, "No points to draw.")
        return
    }
    val anyValid = points.any { it.isValid }
    if (!anyValid) {
        onStatus(id, "This equation has no visible points here — check for things like square roots of negative numbers, or try zooming out.")
    } else {
        onStatus(id, null)
        drawCurve(points, color)
    }
}
```

> Note: `SamplePoint.isValid` currently means "finite AND on-screen" combined — see
> `CurveSampler.kt`. If you want a sharper message ("this is mathematically undefined"
> vs "it's just off-screen, pan to find it"), split `isValid` into two separate flags
> (`isFinite`, `isOnScreen`) in `SamplePoint` and check `worldY.isFinite()` separately
> from the screen-range check. Not required, but it makes the message more precise.

### B) Store the runtime status on the equation, in `GraphViewModel.kt`

Add a second error-like field so "broken equation" (red, from parsing) and "valid but
nothing to see" (can be a softer color) aren't visually identical:

```kotlin
data class Equation(
    val id: Int,
    val text: String,
    val ast: AstNode? = null,
    val equationType: EquationType? = null,
    val error: String? = null,        // parse-time — equation is invalid
    val runtimeNote: String? = null,  // NEW — equation is valid but drew nothing / partly failed
    val colorIndex: Int = 0
)
```

```kotlin
// add this function inside GraphViewModel:
fun setRuntimeNote(id: Int, message: String?) {
    val index = _equations.indexOfFirst { it.id == id }
    if (index != -1 && _equations[index].runtimeNote != message) {
        _equations[index] = _equations[index].copy(runtimeNote = message)
    }
}
```

Wire it up in `GraphScreen.kt` where `GraphCanvas` is called:

```kotlin
GraphCanvas(
    equations = equations,
    viewport = viewport,
    curveColors = mathColors.curveColors,
    inspectedPoint = inspectedPoint,
    onPan = { dx, dy, width, height -> viewModel.panViewport(dx, dy, width, height) },
    onZoom = { factor, focusX, focusY, width, height -> viewModel.zoomViewport(factor, focusX, focusY, width, height) },
    onTap = { x, y, width, height -> viewModel.inspectPoint(x, y, width, height) },
    onEquationRuntimeStatus = { id, message -> viewModel.setRuntimeNote(id, message) }, // NEW
    mathColors = mathColors,
    modifier = Modifier.fillMaxSize()
)
```

### C) Make the message actually readable, in `EquationList.kt`

Right now the chip just turns red — tapping it only removes the equation, there's no
way to read *why*. Make the chip tappable to show the message, and use a different
color for "broken" vs "valid but empty":

```kotlin
// add state for which equation's error is being shown, above the LazyRow:
var errorDialogFor by remember { mutableStateOf<Equation?>(null) }
```

```kotlin
// inside the items{} block, change the equation-text Text() to also react to taps
// and pick its color based on which kind of issue (if any) it has:
val statusMessage = equation.error ?: equation.runtimeNote
val textColor = when {
    equation.error != null -> MaterialTheme.colorScheme.error
    equation.runtimeNote != null -> mathColors.onSurfaceMuted // softer — valid, just nothing to show
    else -> MaterialTheme.colorScheme.onSurface
}

Text(
    text = equation.text.take(15) + if (equation.text.length > 15) "..." else "",
    style = TextStyle(fontFamily = EquationFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium),
    color = textColor,
    modifier = Modifier.clickable(enabled = statusMessage != null) {
        errorDialogFor = equation
    }
)
```

```kotlin
// add this dialog once, at the bottom of EquationList's Composable body,
// alongside the LazyRow (needs `import androidx.compose.material3.AlertDialog`,
// `androidx.compose.material3.TextButton`, and `androidx.compose.runtime.remember`
// / `mutableStateOf` / `getValue` / `setValue`):
errorDialogFor?.let { eq ->
    AlertDialog(
        onDismissRequest = { errorDialogFor = null },
        title = { Text(if (eq.error != null) "Couldn't graph this" else "Nothing to show") },
        text = { Text(eq.error ?: eq.runtimeNote ?: "") },
        confirmButton = {
            TextButton(onClick = { errorDialogFor = null }) { Text("OK") }
        }
    )
}
```

### Result

- A genuinely broken equation (bad syntax, unknown function) still turns the chip red
  — but now tapping it shows the *actual* parser message (e.g. `Unexpected token 'x' at
  position 4`) instead of leaving you to guess.
- An equation that's valid but happens to draw nothing right now (always undefined, or
  just outside the current pan/zoom) shows a muted color and, on tap, a plain-language
  reason ("try zooming out", "check for square roots of negative numbers") instead of
  just sitting there blank.
- A genuine runtime exception during sampling no longer crashes the app — it's caught
  per-equation, that one curve just doesn't draw, and the reason shows up the same way.
