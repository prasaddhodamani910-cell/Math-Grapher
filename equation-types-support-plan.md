# Math Grapher — Universal Equation Support Plan
### Every equation type the app should be able to draw, and how

---

## 0. The honest starting point

There is no single algorithm that "draws any equation ever written" — some equations aren't graphable at all (undefined everywhere, or not a real-valued relation). But there **is** one technique that covers the overwhelming majority of what you'll ever type — lines, circles, ellipses, hyperbolas, and any weird curve you can write as `F(x, y) = 0` — called **implicit curve rendering**. Combined with a handful of specialized renderers for the equation *forms* that aren't naturally `F(x,y)=0` (parametric, polar, sequences, etc.), this gets you as close to "any equation" as a real app can get.

The architecture below has one job: **detect what kind of equation was typed, then hand it to the right renderer.**

---

## 1. Equation Type Catalog

### 1.1 Explicit functions — `y = f(x)`
**Examples:** `y = x^2 - 3`, `y = sin(x)/x`, `y = ln(x+1)`
**Already covered** in your current engine (Section 6–7 of the original plan). This stays the fast-path renderer since it's the most common case and cheapest to compute (one evaluation per x-pixel).

### 1.2 Implicit equations — `F(x, y) = 0`
**Examples:** `x^2 + y^2 = 25` (circle), `x^2 - y^2 = 1` (hyperbola), `x^3 + y^3 = 3xy` (folium of Descartes), `sin(x*y) = cos(x+y)`
**This is the big one — it's what makes "almost anything" possible.**

**Algorithm: Marching Squares**
1. Rearrange the equation to the form `F(x,y) = LHS - RHS = 0`.
2. Overlay a grid of sample cells across the visible viewport.
3. Evaluate `F` at all four corners of each cell.
4. If the sign of `F` differs between corners, the curve passes through that cell — interpolate the crossing point(s) on the cell edges (linear interpolation for speed, or bisection for accuracy) and connect them.
5. Stitch crossing points across adjacent cells into continuous path segments.

This single algorithm draws circles, ellipses, all conics, and genuinely arbitrary implicit curves without needing any special-case code per shape. Grid resolution controls quality vs. speed — start around 150×150 cells for the visible viewport and increase near regions with sign changes (adaptive refinement) for crisp curves without paying the cost everywhere.

### 1.3 Parametric equations — `x = f(t), y = g(t)`
**Examples:** `x = cos(t), y = sin(t)` (circle), `x = t - sin(t), y = 1 - cos(t)` (cycloid)
**Algorithm:** Direct — sample `t` across a user-adjustable range (default `[0, 2π]` or `[-10, 10]`), evaluate both expressions, plot each `(x(t), y(t))` pair, connect sequentially. No implicit solving needed — this is the simplest renderer in the app.

### 1.4 Polar equations — `r = f(θ)`
**Examples:** `r = 1 + cos(θ)` (cardioid), `r = θ` (spiral), `r = sin(3θ)` (rose curve)
**Algorithm:** Sample `θ` from `0` to `2π` (or further, for spirals), evaluate `r`, convert to Cartesian via `x = r·cos(θ), y = r·sin(θ)`, plot sequentially — mechanically this is just parametric plotting with a coordinate conversion step, so it can reuse the parametric renderer internally.

### 1.5 Piecewise functions
**Example:** `f(x) = x^2 if x < 0, else 2x + 1`
**Grammar extension needed:** a piecewise syntax like `{x^2 : x<0, 2x+1 : x>=0}`. Parser produces a list of `(condition AST, expression AST)` pairs. Evaluator checks conditions in order and evaluates the first match; renderer draws each piece over its own x-range and breaks the path at domain boundaries (open/closed circles at boundary points, matching standard math notation).

### 1.6 Inequalities (shaded regions)
**Examples:** `y > x^2`, `x^2 + y^2 <= 9`
**Algorithm:** Reuse the implicit-equation grid — instead of drawing the boundary curve only, evaluate `F(x,y)` at every grid cell and shade cells where the inequality holds true (semi-transparent fill), with the boundary curve itself drawn solid for `≤/≥` and dashed for strict `</>` (standard math convention).

### 1.7 Systems of equations / intersections
**Example:** graphing `y = x + 1` and `y = x^2 - 2` together, marking where they cross
**This isn't a new renderer** — it's a feature layered on top of multi-curve support you already have. After rendering each curve, do a numeric intersection pass: walk both sample sets and flag x-ranges where `f(x) - g(x)` changes sign, refine with bisection, and drop a marker dot with an `(x, y)` label at each crossing.

### 1.8 Conic sections (as a UX shortcut, not a new algorithm)
Circles, ellipses, parabolas, hyperbolas are all just implicit equations (1.2) — no special renderer needed. What's worth adding is a **template/autocomplete UI**: typing "circle" or tapping a shape icon inserts the standard form (`(x-h)^2 + (y-k)^2 = r^2`) with editable placeholders, which is a UX win for an 11th-grade context without any new rendering code.

### 1.9 Sequences and series (discrete plots)
**Examples:** `a_n = n^2`, `a_n = a_(n-1) + 2` (recursive)
**Algorithm:** Not a continuous curve — plot as discrete points (dot mode, no connecting line) at integer `n` values. Recursive sequences need a small evaluator extension: maintain a memoized table of prior terms so `a_(n-1)` can look itself up rather than re-deriving a closed form.

### 1.10 Slope fields / differential equations — `dy/dx = f(x, y)`
**Example:** `dy/dx = x - y`
**Algorithm:** Overlay a grid of points; at each point, evaluate `f(x,y)` to get a slope, draw a short line segment at that angle. Optionally overlay a numerically-integrated solution curve (Euler's method or RK4) starting from a user-tapped point — this is a genuinely nice "wow" feature and a natural fit once you already have a grid-evaluation pass from implicit rendering.

### 1.11 3D surfaces — `z = f(x, y)` (stretch goal, separate rendering mode)
**Example:** `z = x^2 + y^2` (paraboloid)
This needs a real 3D pipeline (rotation, projection, depth-sorting or a wireframe/heightmap approach) and is architecturally a different screen, not an extension of the 2D `Canvas`. Recommend treating this as a distinct **Phase 4+** feature using OpenGL ES or a lightweight software rasterizer — don't try to force it into the 2D grid renderer.

### 1.12 Complex-valued functions (domain coloring)
**Example:** visualizing `f(z) = z^2` over the complex plane
Advanced/stretch — color each pixel by the argument (hue) and magnitude (brightness) of `f(z)` evaluated at that point. Conceptually a specialized implicit-style grid pass, but genuinely a v2+ feature; flag it in the roadmap rather than building now.

---

## 2. Architecture Changes to Support All Types

### 2.1 Equation type detection
When the user submits text, run a lightweight classifier before full parsing:

```
"y = ..."           -> Explicit
"x = ..., y = ..."  -> Parametric   (two expressions, comma-joined, variable t)
"r = ..."           -> Polar        (variable θ)
"dy/dx = ..."       -> SlopeField
"{...}"             -> Piecewise
contains one of <,>,<=,>=  -> Inequality
contains = but not solvable for y alone (e.g. both x and y on one side, or nonlinear in y) -> Implicit
"a_n = ..."         -> Sequence
```

This can be simple pattern/keyword detection — it doesn't need to be a full grammar ambiguity resolver.

### 2.2 New architecture layer: `EquationType` + renderer dispatch
```
sealed class Equation {
    data class Explicit(val expr: AstNode): Equation()
    data class Implicit(val lhs: AstNode, val rhs: AstNode): Equation()
    data class Parametric(val xExpr: AstNode, val yExpr: AstNode, val tRange: ClosedRange<Double>): Equation()
    data class Polar(val rExpr: AstNode): Equation()
    data class Piecewise(val pieces: List<Pair<AstNode, AstNode>>): Equation()
    data class Inequality(val lhs: AstNode, val rhs: AstNode, val op: CompareOp): Equation()
    data class Sequence(val expr: AstNode, val isRecursive: Boolean): Equation()
    data class SlopeField(val dydx: AstNode): Equation()
}

interface CurveRenderer { fun render(eq: Equation, viewport: Viewport): List<Path> }
```
Each `Equation` subtype maps to one `CurveRenderer` implementation. `GraphCanvas` just iterates active equations, dispatches to the right renderer, and draws the returned paths — the Canvas code itself never needs to know *how* a curve was computed.

### 2.3 Grammar/tokenizer extensions needed
- Comparison operators as first-class tokens: `< > <= >= =` (currently `=` is probably ignored/assumed; now it's meaningful)
- Second free variable `y` and `θ`, `t`, `n` depending on mode (currently your evaluator likely only binds `x`)
- Subscript-style function call syntax for sequences: `a_n`, `a_(n-1)` — parse `a_` + expression in parens/underscore as an indexed lookup into a memo table
- Piecewise braces `{ }` and colon `:` as new structural tokens

### 2.4 Evaluator extensions
- `evaluate(node, bindings: Map<String, Double>)` instead of `evaluate(node, x: Double)` — generalizes to any variable set (x, y, t, θ, n) with one signature change
- Add a `memo: MutableMap<Int, Double>` for recursive sequences so `a_(n-1)` doesn't recompute the whole chain from scratch each call

---

## 3. Updated Feature Roadmap

**Phase 1 (already planned)** — Explicit functions, pan/zoom, core UI

**Phase 2 — General equation support (this document's core addition)**
- Implicit equations via marching squares (unlocks circles, ellipses, hyperbolas, arbitrary implicit curves)
- Parametric equations
- Polar equations

**Phase 3 — Refinement forms**
- Piecewise functions
- Inequalities (shaded regions)
- Systems / intersection markers
- Sequences (discrete plotting)

**Phase 4 — Advanced/stretch**
- Slope fields + numeric ODE solution curves
- 3D surfaces (separate rendering pipeline)
- Complex domain coloring

---

## 4. Why this gets you "basically everything"

Once implicit rendering (1.2) exists, typing *any* equation you'll encounter through the end of high school and most of an early university curriculum — every conic, every polynomial relation, trig identities graphed as curves, anything expressible as one side minus the other equaling zero — just works, with no per-equation special-casing. The remaining categories (parametric, polar, piecewise, sequences, slope fields) exist because they're written in a fundamentally different *shape*, not because they need a fundamentally different *engine* — each is a thin adapter that ultimately feeds coordinate pairs into the same `Path`-drawing code you already have.
