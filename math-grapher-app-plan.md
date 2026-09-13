# Math Grapher — Real-Time Equation Visualizer for Android
### Built Entirely in Termux
**Author:** Prasad Dhodamani

---

## 1. Project Overview

**Math Grapher** is a native Android app that parses a mathematical equation typed by the user and renders its graph **live**, updating the curve as they type — no "submit" button, no lag-heavy re-render, no ad-cluttered UI. The target feel is closer to Desmos than to a typical student calculator app: clean, fast, and confident in its own design decisions.

Two things make this project non-trivial:
1. **A correct, fast expression parser/evaluator** (you're writing a mini compiler front-end — this plays to your existing compiler work on Prashc).
2. **A real-time rendering pipeline** that redraws a curve at 60fps as the user pans, zooms, or edits the equation, without janking the input field.

---

## 2. Goals & Non-Goals

**Goals**
- Type an equation like `y = sin(x) * e^(-x/4)` and see it graphed instantly.
- Support multiple simultaneous curves, each with its own color.
- Pinch-to-zoom, drag-to-pan, tap-to-inspect (x, y) coordinates.
- Professional, minimal UI — Material 3 based but with a distinct identity, not a stock template.
- Fully buildable from Termux with no desktop machine or Android Studio.

**Non-Goals (v1)**
- 3D graphing / implicit multivariable surfaces.
- Symbolic calculus (derivatives/integrals as exact symbolic output) — numeric approximation only.
- Cloud sync / accounts.

---

## 3. Tech Stack & Why

| Layer | Choice | Reasoning |
|---|---|---|
| Language | **Kotlin** | First-class Android support, safer than Java, you're comfortable with statically-typed systems languages already |
| UI Toolkit | **Jetpack Compose** | Declarative UI, built-in `Canvas` composable is ideal for a live-redrawing graph surface; avoids hand-rolled XML layout files which are painful to author from a terminal |
| Math Engine | **Hand-written recursive-descent parser + AST evaluator (pure Kotlin, no dependency)** | You already do this in Prashc (AArch64 compiler) — same tokenizer → parser → tree-walk pattern, just evaluating instead of codegen. No external math libraries needed; keeps the APK small and gives you full control over supported syntax |
| Build System | **Gradle (command-line, no Android Studio)** | Fully scriptable from Termux; Android Studio is not required to produce a signed APK |
| Rendering | **Compose `Canvas` + `Path`** | Hardware-accelerated, integrates cleanly with Compose state/recomposition for real-time updates |
| Min SDK | **API 26 (Android 8.0)** | Covers effectively all active devices while keeping Compose feature set fully available |

---

## 4. Termux Development Environment Setup

```bash
# Core toolchain
pkg update && pkg upgrade -y
pkg install -y openjdk-17 git wget unzip aapt2

# Kotlin + Gradle (via SDKMAN inside Termux, or pkg if available)
pkg install -y kotlin gradle

# Android command-line SDK (no Studio)
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-XXXX_latest.zip
unzip commandlinetools-linux-XXXX_latest.zip
mv cmdline-tools latest

# Set env vars (add to ~/.bashrc)
export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Accept licenses & install platform + build-tools
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

**Note:** check the current commandline-tools version number at the time you set this up — Google rotates the download URL periodically.

Project skeleton is a standard Gradle Android project (`app/`, `build.gradle.kts`, `settings.gradle.kts`) — no Studio-specific files needed. You write `.kt` files directly in Termux with `vim`/`nano`/`nvim`, then run:

```bash
./gradlew assembleDebug        # build debug APK
./gradlew installDebug         # push straight to a connected/adb-linked device
```

For on-device install without a PC, use `termux-adb` or simply `adb install` if you have wireless debugging enabled on the target phone (your Galaxy A17 works fine for this).

---

## 5. Project Architecture (MVVM)

```
app/src/main/java/com/prasad/mathgrapher/
├── MainActivity.kt
├── math/
│   ├── Tokenizer.kt         # string -> List<Token>
│   ├── Parser.kt            # tokens -> AST (recursive descent)
│   ├── AstNode.kt           # sealed class: Num, Var, BinOp, UnaryOp, FuncCall
│   ├── Evaluator.kt         # AST + x-value -> Double
│   └── MathException.kt     # structured parse/eval errors for UI feedback
├── graph/
│   ├── GraphViewModel.kt    # holds equations, viewport (pan/zoom), sample points
│   ├── GraphCanvas.kt       # Compose Canvas drawing logic
│   ├── Viewport.kt          # world<->screen coordinate transforms
│   └── CurveSampler.kt      # adaptive sampling (denser near curvature/asymptotes)
├── ui/
│   ├── theme/               # Color.kt, Type.kt, Shape.kt — design tokens (Section 8)
│   ├── EquationInputBar.kt
│   ├── EquationList.kt      # chips for each active equation, color-coded
│   └── GraphScreen.kt       # top-level screen composition
└── util/
    └── DebounceState.kt     # debounces re-parse while typing (avoid parsing every keystroke)
```

This separation matters because the math engine has **zero Android dependencies** — you can unit test `Tokenizer`/`Parser`/`Evaluator` as plain Kotlin/JUnit, entirely from Termux, with no emulator needed.

---

## 6. Math Engine — Parser & Evaluator

### 6.1 Tokenizer
Breaks input into: numbers, identifiers (`x`, `pi`, `e`), operators (`+ - * / ^ %`), parentheses, commas, function names (`sin`, `cos`, `tan`, `log`, `ln`, `sqrt`, `abs`, `exp`).

### 6.2 Grammar (recursive descent, standard precedence climbing)
```
expression := term (('+' | '-') term)*
term       := factor (('*' | '/' ) factor)*
factor     := unary ('^' unary)*        // right-associative
unary      := ('-' | '+')? primary
primary    := NUMBER | VARIABLE | FUNC '(' expression ')' | '(' expression ')'
```
Implicit multiplication (`2x`, `2(x+1)`, `x sin(x)`) should be handled by inserting a synthetic `*` token during tokenization when a number/`)` is immediately followed by a variable/`(`/function — this is what makes the input feel natural instead of pedantic.

### 6.3 Evaluator
Simple tree-walking evaluator: `evaluate(node: AstNode, x: Double): Double`. For a real-time graph you call this per sample point (typically 200–800 x-values across the visible viewport per frame), so it must be allocation-light — reuse the AST across frames, only re-parse when the equation text actually changes (debounce ~150ms after last keystroke).

### 6.4 Error Handling
Parse/eval errors should never crash the render loop. Wrap in `Result<Double>`; on failure, show a small inline error state under the input field ("Unexpected token '%' at position 4") rather than a generic crash or silent blank graph.

---

## 7. Real-Time Rendering Pipeline

1. **Viewport model**: maintain `xMin, xMax, yMin, yMax` in world space; map to screen pixels via `Viewport.worldToScreen()`.
2. **Adaptive sampling**: sample ~1.5x the pixel width of the canvas across the visible x-range, so the curve stays smooth on pan/zoom without over-computing off-screen regions.
3. **Path building**: build an Android `Path` by moving to the first valid point, then `lineTo` for each subsequent point; break the path (start a new subpath) wherever the function is undefined (e.g. `tan(x)` asymptotes, division by zero) so you don't draw connecting artifacts across discontinuities.
4. **Gesture handling**: Compose's `detectTransformGestures` for combined pan+zoom in one modifier — update the viewport's bounds live, triggering recomposition of just the `Canvas`, not the whole screen.
5. **Frame budget**: keep evaluation + path-building under ~8ms so you have headroom within a 16ms (60fps) frame; this is very achievable for a single-variable evaluator with a few hundred sample points.

---

## 8. UI/UX Design System (this is what separates it from "AI slop")

The single biggest tell of an unpolished app is inconsistent spacing and a default/unconsidered color palette. Define these as fixed tokens up front and use nothing else.

### 8.1 Color Palette
A restrained, purposeful palette — not the default Material purple.

| Token | Light | Dark | Use |
|---|---|---|---|
| `background` | `#FAFAF8` | `#12141A` | Screen background |
| `surface` | `#FFFFFF` | `#1B1E27` | Cards, input bar |
| `primary` | `#2F6FED` | `#5B8DEF` | Primary curve, active states |
| `onSurfaceMuted` | `#6B7280` | `#9CA3AF` | Secondary text, axis labels |
| `gridLine` | `#E5E7EB` | `#2A2E3A` | Graph gridlines (low contrast, never competes with curves) |
| Curve palette | `#2F6FED, #ED6A5A, #4CAF8C, #F2B705, #9B59B6` | same | Cycled per added equation — chosen for distinguishability, not garishness |

### 8.2 Typography
- **Display/UI text**: Inter or the system default (Roboto Flex on modern Android) — do not use a "techy" or "playful" font; equations demand a neutral, legible face.
- **Equation text specifically**: a monospace or math-optimized font (e.g. JetBrains Mono, or Android's `monospace` family) so `^`, `(`, `)` alignment is visually unambiguous while typing.
- Scale: 12 / 14 / 16 / 20 / 28sp — five sizes only. Body text 14–16sp, equation input 20sp, screen title 28sp.

### 8.3 Spacing & Shape
- 8dp base spacing grid (4dp for tight groupings only).
- Corner radius: 12dp for cards/input bar, 20dp for pill-shaped equation chips — consistent radius family, not mixed rounding.
- Elevation: prefer subtle 1dp borders/tonal surfaces over heavy drop shadows (matches modern Material 3 "tonal" aesthetic rather than dated skeuomorphic shadow stacks).

### 8.4 Motion
- Curve redraws: no animation on the path itself (must feel instant/live).
- Chip add/remove: 150ms fade+scale.
- Viewport reset ("home" button after a pan/zoom): 250ms ease-out camera tween, not an instant snap — this one deliberate animation reads as "considered," not flashy.

---

## 9. Screen-by-Screen UI Spec

**Single-screen app (`GraphScreen`)** — avoid unnecessary navigation for a v1 utility app:

```
┌─────────────────────────────────────┐
│  Math Grapher              [☰] [☀/🌙]│  <- top bar: title, menu, theme toggle
├─────────────────────────────────────┤
│                                       │
│                                       │
│         [ live graph canvas ]        │  <- full-bleed Canvas, gridlines,
│                                       │     axis labels, pinch/pan active
│                                       │
│                          [⌖ reset]    │  <- floating "recenter" button, bottom-right
├─────────────────────────────────────┤
│ ● y = sin(x)        [✕]              │  <- equation chip list, horizontally
│ ● y = x^2/4         [✕]              │     scrollable if >1 line, color dot = curve color
├─────────────────────────────────────┤
│ [ y = ▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁ ]  [ + ]    │  <- persistent input bar, monospace field
└─────────────────────────────────────┘
```

- Input bar is **always visible** (not modal) — real-time graphing loses its point if the field disappears while the keyboard is open, so the canvas resizes/compresses rather than the input being hidden.
- Tapping a point on the curve shows a small tooltip-style card with `(x, y)` at that position, dismissed on next tap elsewhere.
- Empty state (no equations yet): a light gridded canvas with faint placeholder text `Try: y = x^2 - 3` — never a blank white void.

---

## 10. Feature Roadmap

**Phase 1 — Core (MVP)**
- Tokenizer/Parser/Evaluator with unit tests
- Single-equation live graph, pan/zoom, light+dark theme

**Phase 2 — Multi-curve & polish**
- Multiple equations with color-coded chips
- Tap-to-inspect coordinates, recenter button
- Adaptive sampling near asymptotes/discontinuities

**Phase 3 — Quality of life**
- Save/load equation sets (local storage, Room DB)
- Slider-based parameters (e.g. `y = a*sin(x)` with a draggable `a` slider — Desmos-style)
- Export graph as PNG/share sheet

**Phase 4 — Stretch**
- Polar (`r = f(θ)`) and parametric equation support
- Numeric derivative/tangent-line overlay at a point
- Widget: home-screen live mini-graph

---

## 11. Testing Strategy

- **Math engine**: pure JUnit tests in Termux (`./gradlew test`) — no emulator required. Test tokenizer edge cases (implicit multiplication, negative exponents, nested functions), and evaluator correctness against known values.
- **Rendering**: instrumented tests are heavier from Termux (need an emulator or physical device via `adb`); prioritize manual testing on your Galaxy A17 over investing in full Compose UI test infra for v1.
- **Performance**: log frame evaluation time in debug builds; flag anything over ~10ms per frame for optimization (usually means sampling too many points or re-parsing unnecessarily).

---

## 12. Build & Distribution

```bash
./gradlew assembleRelease
```
- Sign with a keystore generated via `keytool` (available in the `openjdk-17` package already installed) — fully doable from Termux, no desktop needed.
- Distribute the signed APK directly (GitHub Releases, F-Droid submission if open-sourced, or side-load) — no Play Store dependency required for a personal/portfolio project.

---

## 13. Why This Plan Avoids "AI Slop"

The usual tells of a low-effort generated app — default Material purple, inconsistent spacing, generic sans-serif everywhere, no empty states, jarring instant-snap interactions — are addressed explicitly above with fixed design tokens (Section 8) rather than left to whatever a UI framework defaults to. The other tell, a shaky or over-engineered math core, is avoided by leaning on a pattern you already know well from compiler work: tokenize → parse → walk the tree — applied here to evaluation instead of code generation.
