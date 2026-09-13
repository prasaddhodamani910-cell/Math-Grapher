# Math Grapher — UI Redesign Plan
### Fixing input visibility, equation-list overflow, and an overall visual overhaul

---

## 1. Root-Cause Diagnosis

Before redesigning anything, it's worth naming exactly *why* each problem is happening — otherwise the fix is guesswork.

### 1.1 "I can't see what I'm typing"
This is almost always one (or a combination) of these causes in a Compose text field:
- **Text color too close to background color** — a common default-theme bug where `TextField` inherits a light-on-light or the cursor/text color wasn't explicitly set against your custom `background` token.
- **Keyboard covering the input field** — if the input bar sits at the very bottom of the screen with no `imePadding()` modifier, the soft keyboard opens *on top of* the field instead of pushing it up.
- **No live equation preview** — even with visible text, raw typed characters like `x^2+sin(x)` are hard to visually parse in real time; there's nothing rendering it in a readable "math" form as you type.
- **Cursor blink not visible** — if `cursorBrush` isn't explicitly styled, it can default to a low-contrast color.

### 1.2 "Equations cover the grid as they increase"
This means the equation chip list is currently laid out as a **fixed-height block that grows with content** (e.g. a `Column` that wraps chips onto multiple lines) rather than a **fixed-height, independently-scrolling region**. Every new equation pushes the list taller, eating into the `Canvas` area until the graph itself is squeezed out. This is a layout-constraint problem, not a content problem — the fix is architectural, not cosmetic.

### 1.3 "Make the UI 100x better"
Taken as a whole, this is a request for a proper information-hierarchy pass — not just cosmetic tweaks, but *how the screen is organized* so the graph (the actual point of the app) always dominates the screen, and everything else is contextual and gets out of the way when not needed.

---

## 2. Fix 1 — Equation Input Visibility

### 2.1 Explicit color contract (no more relying on theme defaults)
```kotlin
OutlinedTextField(
    value = equationText,
    onValueChange = { ... },
    textStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.onSurface   // explicit, never inherited silently
    ),
    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), // bright, unmistakable
    modifier = Modifier
        .fillMaxWidth()
        .imePadding()   // <-- this one line fixes "keyboard covers the field"
)
```
The `imePadding()` modifier is the single most common fix for "I can't see what I type" bugs — it tells Compose to push the input bar up above the keyboard rather than let the keyboard overlap it.

### 2.2 Live rendered preview above the input
Add a thin preview strip directly above the typing field that re-renders the parsed expression in proper math notation as you type (using superscripts for `^`, proper fraction bars for `/`, √ symbol for `sqrt`, etc.):

```
┌─────────────────────────────────────┐
│   x² + sin(x)              ← preview │   (large, high-contrast, updates live)
├─────────────────────────────────────┤
│ [ y = x^2+sin(x)▏          ]  [ + ]  │   (raw input, monospace, cursor visible)
└─────────────────────────────────────┘
```
This does two jobs at once: confirms the text is visible, *and* confirms the parser understood it correctly before you commit to graphing it — catching typos before they become confusing blank-graph moments.

### 2.3 Syntax highlighting in the raw input itself
Color-code as you type instead of one flat text color:
- Numbers → `onSurface` (neutral)
- Operators (`+ - * / ^`) → `primary` (accent color, makes structure scannable)
- Function names (`sin`, `sqrt`, etc.) → a distinct secondary accent
- Unmatched/invalid syntax → error red, live, before you even submit

This is done with an `AnnotatedString` `VisualTransformation` in Compose — you tokenize the text with your existing `Tokenizer` and map each token type to a color span.

---

## 3. Fix 2 — Equation List Overflow

### 3.1 The core architectural fix
Give the equation list a **fixed max height** and make it internally scrollable, so it physically cannot grow into the graph area no matter how many equations are added:

```kotlin
Column(modifier = Modifier.fillMaxSize()) {
    TopBar()
    Box(modifier = Modifier.weight(1f)) {  // <-- Canvas ALWAYS gets remaining space
        GraphCanvas()
        RecenterButton(modifier = Modifier.align(Alignment.BottomEnd))
    }
    EquationChipRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 56.dp)   // <-- hard ceiling, never grows past this
    )
    EquationInputBar()
}
```
The key idea: `Box(Modifier.weight(1f))` around the Canvas means the graph **always claims whatever space is left over** — the chip row and input bar have fixed/capped heights, so the graph area shrinks *only* as much as a single row of chips needs, never more.

### 3.2 Horizontal scroll instead of wrapping
Lay equation chips out in a single horizontally-scrolling row (`LazyRow`) instead of a wrapping grid:
```
[● y=sin(x) ✕] [● y=x²/4 ✕] [● r=1+cosθ ✕] [● ... scroll →]
```
One fixed-height row, unlimited equations, zero vertical growth — this alone solves the "covers the grid" problem completely regardless of how many curves are added.

### 3.3 Collapse-on-scroll (nice-to-have, feels premium)
When the user starts dragging/panning the graph, animate the chip row height down to a thin 8dp strip (colors still visible as a compressed indicator), and expand it back when they tap it or stop interacting with the canvas for a moment. This maximizes graph real estate exactly when the user is actively exploring the curve, and restores full labels when they're managing their equation list.

### 3.4 Overflow indicator instead of infinite scroll guessing
If there are more chips than fit on screen, show a small `"+3 more"` trailing chip that expands into a bottom sheet listing every active equation with toggle-visibility switches — keeps the primary row lightweight no matter how many equations a power user adds.

---

## 4. Fix 3 — Full UI Overhaul ("100x better")

### 4.1 Information hierarchy — what should visually dominate
Ranked by how much visual weight each element should get:
1. **The graph itself** — should occupy ~70-80% of vertical space at all times
2. **The equation being actively typed** — second most prominent, always visible
3. **The list of existing equations** — present but compact, never competing with #1
4. **Chrome (top bar, buttons)** — minimal, mostly icon-only, gets out of the way

Most "AI slop" or amateur UIs get this backwards — equal visual weight everywhere, no clear focal point. Fix: everything that isn't the graph should be visually quieter (lower contrast, smaller, more compact) than the graph and its curves.

### 4.2 Top bar simplification
Replace a heavy labeled top bar with a minimal one:
```
[Math Grapher]                              [🌙]  [⋮]
```
Just the app name, a theme toggle, and an overflow menu for secondary settings (grid density, angle mode degrees/radians, axis number formatting) — nothing that needs to be visible every single time competes for space here.

### 4.3 Graph area micro-polish
- **Gridlines**: use two tiers — faint minor gridlines every 1 unit, slightly stronger major gridlines every 5 units, with the axis lines themselves (x=0, y=0) noticeably bolder than either. This mirrors how graph paper and Desmos both do it, and it's the single biggest "looks professional" signal on the canvas itself.
- **Axis labels**: small numeric labels along the axes, positioned to never overlap the curve (offset from the axis line, with a subtle background "halo" behind each number so it stays readable even when a curve passes directly behind it).
- **Curve stroke**: 2.5–3dp width with slightly rounded line caps — thin enough to stay precise when zoomed in, thick enough to read clearly when zoomed out.

### 4.4 Equation chip visual design
```
●  y = sin(x)   ✕
```
- Colored dot matches the curve's exact color (direct visual link between list and graph)
- Chip background: very subtle tonal fill (5-8% opacity of the curve color) rather than a flat gray pill — ties the chip's identity to its curve without shouting
- Tap chip (not the ✕) to toggle curve visibility on/off without deleting it — small but genuinely useful, and cheap to add given you already track per-equation state

### 4.5 Empty/loading/error states — the details that read as "considered"
- **Empty graph**: faint placeholder gridlines with light gray ghost text `Try: y = x² - 3` centered — never a stark blank white canvas
- **Parse error**: the specific equation's chip gets a thin red outline and a small ⚠ badge, rather than a disruptive dialog or toast — errors stay local to the thing that caused them
- **Recenter button**: only appears once the viewport has actually been panned/zoomed away from default — an omnipresent button for an action that's usually irrelevant is visual noise

### 4.6 Motion — the last 10%
- Adding an equation: new chip slides in from the right with a 150ms ease-out, curve fades onto the graph over 200ms (never appears instantly/jarringly)
- Deleting: chip shrinks+fades, curve fades out — matching, symmetric motion in both directions
- Theme toggle: crossfade the whole color scheme over 300ms rather than an instant hard cut

---

## 5. Priority Order for Implementation

1. `imePadding()` + explicit text/cursor colors (Fix 1) — smallest change, most user-facing pain removed
2. `Box(Modifier.weight(1f))` layout restructure so Canvas always gets remaining space (Fix 2) — architectural, do this before adding more equations to test with
3. `LazyRow` chip list with fixed height ceiling (Fix 2)
4. Live math-notation preview strip (Fix 1, polish)
5. Gridline tiering + axis label halos (Fix 3, biggest "looks professional" ROI for the effort)
6. Syntax highlighting, chip tonal fill, motion polish (Fix 3, final pass)

Items 1–3 are bug fixes — they should ship first. Items 4–6 are what actually deliver the "100x better" feeling once the underlying layout is solid.
