# Math Grapher UI --- Professional Mobile UI Redesign Guide

## Goal

This document is a complete redesign guide for the current **Math
Grapher** screen.

The goal is not to make the app flashy or "AI-looking." The goal is to
make it feel like a **real, polished mathematical tool** designed by
someone who understands product UI.

The design direction is:

-   Clean
-   Mathematical
-   Calm
-   Functional
-   Modern
-   Dark-first
-   Consistent
-   Easy to scan
-   Professional without unnecessary decoration

The most important principle:

> **Professional UI comes from hierarchy, spacing, typography,
> consistency, and restraint---not from adding more effects.**

------------------------------------------------------------------------

# 1. What is wrong with the current UI?

The current screen already has a useful foundation: a graph, coordinate
system, equation input, and an add button.

The problem is visual hierarchy.

### Current issues

1.  The graph grid is visually too strong.
2.  The equation/input area feels like a prototype control rather than a
    finished equation editor.
3.  The "Try: y = x² − 3" text competes with the graph.
4.  The floating button does not have a strong relationship to the
    graph.
5.  There is not enough distinction between primary, secondary, and
    tertiary information.
6.  Many elements have similar visual weight.
7.  The bottom input area is larger than it needs to be.
8.  The UI uses rounded surfaces, but the rounding does not yet feel
    like a deliberate design system.
9.  The graph should be the hero of the screen, but the grid currently
    gets almost as much attention.
10. The screen needs a stronger structure: **Header → Graph →
    Controls/Equations**.

------------------------------------------------------------------------

# 2. The design philosophy

Do NOT try to make the app look futuristic.

Avoid:

-   Excessive gradients
-   Neon glows everywhere
-   Giant rounded cards
-   Excessive glassmorphism
-   Random floating buttons
-   Too many colors
-   Heavy shadows
-   Decorative animations with no purpose
-   Huge icons
-   Every element being a pill

Instead, aim for:

> **A serious graphing tool with a modern mobile interface.**

A useful mental reference is the intersection of:

-   Professional graphing software
-   Modern Android UI
-   Scientific/calculator interfaces
-   Clean productivity applications

The graph should remain the visual star.

------------------------------------------------------------------------

# 3. Overall screen structure

Use three major zones.

``` text
┌─────────────────────────────────────┐
│ HEADER                              │
│ Math Grapher                  ⋮     │
├─────────────────────────────────────┤
│                                     │
│                                     │
│              GRAPH                  │
│                                     │
│                                     │
│                         GRAPH       │
│                         CONTROLS    │
│                                     │
├─────────────────────────────────────┤
│ EQUATIONS                           │
│                                     │
│ ●  y = x² − 3                  ⋮   │
│                                     │
│ +  Add equation                     │
└─────────────────────────────────────┘
```

### Recommended hierarchy

**Level 1 --- Primary**

-   Graph
-   Current equation/function

**Level 2 --- Secondary**

-   Equation list
-   Add equation
-   Zoom/reset controls

**Level 3 --- Tertiary**

-   Coordinate labels
-   Menu icons
-   Visibility controls
-   Minor graph information

Everything should visually communicate this hierarchy.

------------------------------------------------------------------------

# 4. Header redesign

The top bar should be simple.

Recommended:

``` text
☰   Math Grapher                         ⋮
```

Or, if navigation is not required:

``` text
Math Grapher                         ⋮
```

### Header measurements

Recommended mobile values:

-   Height: approximately 56--64dp
-   Horizontal padding: 16--20dp
-   Title: 20--24sp
-   Title weight: Medium/Semibold
-   Icon size: 24dp
-   Icon touch target: at least 44--48dp

Do not make the title enormous.

The title should identify the screen, not dominate it.

------------------------------------------------------------------------

# 5. Typography system

Typography is one of the fastest ways to make an app feel professional.

Do not randomly choose different font sizes for every component.

Use a small type scale.

## Suggested scale

  Purpose                Size Weight
  ---------------- ---------- -----------------
  Screen title       22--24sp Medium/Semibold
  Section title      18--20sp Semibold
  Equation           18--20sp Medium
  Button text        14--16sp Medium
  Secondary text     13--14sp Regular
  Graph labels       11--13sp Regular
  Tiny metadata      10--12sp Regular

Use the same type scale everywhere.

### Text hierarchy

Primary text:

-   Approximately 90--95% visual opacity

Secondary text:

-   Approximately 60--70%

Tertiary text:

-   Approximately 40--55%

Graph grid:

-   Very low contrast

This prevents every piece of information from screaming for attention.

------------------------------------------------------------------------

# 6. Spacing system

Use a consistent spacing system instead of manually choosing random
margins.

Recommended base unit:

**4dp**

Then use:

-   4dp --- tiny gaps
-   8dp --- icon/text spacing
-   12dp --- compact component spacing
-   16dp --- normal padding
-   20dp --- major padding
-   24dp --- section spacing
-   32dp --- large separation

Most of the screen should be built using these values.

### Example

Instead of:

``` text
17dp
23dp
29dp
11dp
```

prefer:

``` text
16dp
24dp
32dp
12dp
```

The result feels much more intentional.

------------------------------------------------------------------------

# 7. Graph styling

The graph is the most important part of the app.

The graph should look precise and calm.

## Grid

Current problem:

The grid lines are too visually prominent.

Recommended:

-   Major grid: subtle
-   Minor grid: even more subtle
-   Axes: stronger than grid
-   Function curve: strongest visual element

The user should immediately see:

1.  The curve
2.  The axes
3.  The coordinate system
4.  The grid

---not the other way around.

## Recommended visual hierarchy

``` text
Function curve       ██████████  strongest
Axes                 ██████      medium
Major grid           ███         subtle
Minor grid           ██          very subtle
Coordinate labels    ██          subtle
```

Do not make every grid line the same brightness as the axes.

------------------------------------------------------------------------

# 8. Axes

The X and Y axes should be clearly distinguishable from the grid.

Recommended:

-   Slightly thicker than grid lines
-   Moderate contrast
-   Small axis labels
-   Optional arrowheads
-   Coordinate origin clearly readable

Example:

``` text
              y
              ↑
              │
              │
──────────────┼──────────────→ x
              │
              │
```

The axes should guide the eye without becoming distracting.

------------------------------------------------------------------------

# 9. Function curve

The equation curve should be the strongest element in the graph.

For:

``` text
y = x² − 3
```

the parabola should have:

-   Clear line thickness
-   Strong contrast
-   Smooth rendering
-   Rounded line joins
-   No unnecessary glow
-   Optional subtle highlight on interaction

A small amount of emphasis is enough.

Avoid huge neon strokes.

------------------------------------------------------------------------

# 10. Highlighting important points

For an interactive graph, useful mathematical points can be shown.

For:

``` text
y = x² − 3
```

the vertex is:

``` text
(0, -3)
```

A polished interaction can show:

``` text
        ●
        │
     (0, -3)
```

Use this only when relevant.

Do not permanently label every possible point.

The interface should stay quiet until the user asks for information.

------------------------------------------------------------------------

# 11. Equation editor redesign

This is one of the biggest improvements.

Instead of:

``` text
┌─────────────────────────────────┐
│ y = Try: x² − 3              + │
└─────────────────────────────────┘
```

use a proper equation section.

Example:

``` text
Equations                         + Add equation

┌─────────────────────────────────────┐
│ ●   y = x² − 3       ◉        ⋮    │
└─────────────────────────────────────┘
```

Where:

-   `●` = equation color
-   Equation = editable expression
-   `◉`/eye = visibility
-   `⋮` = more actions

------------------------------------------------------------------------

# 12. Equation list

When multiple equations exist:

``` text
Equations                         + Add equation

┌─────────────────────────────────────┐
│ ●  y = x² − 3             ◉     ⋮  │
├─────────────────────────────────────┤
│ ●  y = 2x + 1             ◉     ⋮  │
├─────────────────────────────────────┤
│ ●  y = sin(x)             ◉     ⋮  │
└─────────────────────────────────────┘
```

Each equation should have its own visual identity.

The colored indicator should correspond to the graph curve.

------------------------------------------------------------------------

# 13. Add equation interaction

Instead of making a huge blue button, use a clear action:

``` text
+  Add equation
```

This can be:

-   Text + icon
-   Compact outlined button
-   Small filled button
-   Row action

It should not visually overpower the graph.

------------------------------------------------------------------------

# 14. Floating graph controls

The current floating control feels somewhat disconnected.

A better option is a compact vertical control group:

``` text
┌─────┐
│  +  │
├─────┤
│  ⌖  │
├─────┤
│  −  │
└─────┘
```

Actions:

-   `+` = zoom in
-   `⌖` = reset/recenter
-   `−` = zoom out

Alternative:

``` text
[ − ] [ ⌖ ] [ + ]
```

Choose whichever fits your graph interaction better.

### Important

Every control should have a clear reason to exist.

Do not add buttons just because empty space exists.

------------------------------------------------------------------------

# 15. Touch targets

Even if an icon is visually 24dp, its touch target should be larger.

Recommended:

-   Minimum touch target: approximately 44--48dp
-   Icon itself: usually 20--24dp

This makes the app feel better on an actual phone.

------------------------------------------------------------------------

# 16. Cards and corner radius

Avoid making everything extremely rounded.

Recommended system:

  Component               Radius
  ------------------- ----------
  Main bottom sheet     20--24dp
  Equation card         14--16dp
  Input                 12--14dp
  Button                12--14dp
  Small control         10--12dp
  FAB                   Circular

The important part is consistency.

Do not use:

``` text
28dp + 32dp + 24dp + 18dp + 40dp
```

randomly throughout the app.

------------------------------------------------------------------------

# 17. Bottom sheet / equation panel

The equation area can become a proper bottom sheet.

Recommended structure:

``` text
┌─────────────────────────────────────┐
│                ───                  │
│                                     │
│ Equations             + Add equation│
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ ●  y = x² − 3          ◉    ⋮  │ │
│ └─────────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

The small drag handle gives the panel a clear relationship to the graph.

If your app supports expanding/collapsing the panel, this becomes
especially useful.

------------------------------------------------------------------------

# 18. Dark theme

Dark mode should not mean:

``` text
#000000 everywhere
```

Pure black can make the UI feel harsh.

Use several dark surfaces.

Example conceptual palette:

``` text
Background       very dark blue/gray
Surface          slightly lighter dark blue/gray
Elevated surface another step lighter
Primary text     near-white
Secondary text   muted gray
Accent           blue
Grid             low-contrast gray
Axes             medium gray
```

The exact colors can be adjusted to your brand.

The important thing is **surface hierarchy**.

------------------------------------------------------------------------

# 19. Accent color

Use one primary accent.

For example:

``` text
Blue = interactive / selected / active
```

Use the accent for:

-   Function curve
-   Add equation
-   Active controls
-   Selected elements
-   Focus states

Do not use the accent for everything.

If everything is blue, nothing feels important.

------------------------------------------------------------------------

# 20. Shadows and elevation

Use shadows carefully.

For a dark UI, excessive shadows can look muddy.

Prefer:

-   Slight surface contrast
-   Thin borders when needed
-   Very subtle elevation

Instead of:

``` text
Huge blur
+
Huge glow
+
Gradient
```

use:

``` text
Surface difference
+
small border
+
subtle elevation
```

This looks much more mature.

------------------------------------------------------------------------

# 21. Icons

Use one icon style throughout the app.

Do not mix:

-   Filled icons
-   Outlined icons
-   Random emoji
-   Different icon families

Choose one system.

For example:

-   24dp standard icons
-   consistent stroke width
-   consistent optical weight

Icons should support the interface rather than become decoration.

------------------------------------------------------------------------

# 22. Empty states

When there is no equation:

``` text
Equations

      No equations yet

     + Add equation

Enter a function to start graphing.
```

Keep it simple.

Do not create an enormous illustration unless the product needs one.

------------------------------------------------------------------------

# 23. Error states

For invalid math:

``` text
y = x^^2

Invalid expression
Check the exponent near "^^".
```

Avoid vague errors like:

``` text
Something went wrong.
```

A mathematical tool should explain what is wrong.

------------------------------------------------------------------------

# 24. Input behavior

A professional equation editor should make entering math easy.

Consider support for:

``` text
x²
√x
sin(x)
cos(x)
tan(x)
log(x)
ln(x)
π
e
÷
×
^
(
)
```

The user should not have to fight the keyboard.

A dedicated math keypad can be useful.

------------------------------------------------------------------------

# 25. Math expression formatting

Do not display mathematical expressions as plain programmer text
whenever possible.

Prefer:

``` text
y = x² − 3
```

instead of:

``` text
y = x^2 - 3
```

When the user enters:

``` text
x^2
```

render it visually as:

``` text
x²
```

Similarly:

``` text
sqrt(x)
```

can render as:

``` text
√x
```

This immediately makes the application feel more mathematical.

------------------------------------------------------------------------

# 26. Interaction states

Every interactive element should have states.

For example, equation visibility:

### Visible

``` text
●  y = x² − 3
```

### Hidden

``` text
○  y = x² − 3
```

### Selected

Use a subtle surface highlight.

### Pressed

Slightly change surface/opacity.

### Disabled

Reduce contrast.

These states make the app feel complete.

------------------------------------------------------------------------

# 27. Animations

Animations should communicate what happened.

Good examples:

### Adding equation

The new equation row smoothly appears.

### Removing equation

The row collapses naturally.

### Zooming

Graph updates smoothly.

### Opening equation panel

Panel slides up naturally.

### Selecting curve

Curve briefly becomes more prominent.

Avoid:

-   bouncing everything
-   glowing everything
-   unnecessary particle effects
-   excessive transitions

A professional tool should feel fast.

------------------------------------------------------------------------

# 28. Loading states

If graph calculations can take noticeable time, show a subtle state.

Example:

``` text
Calculating…
```

or a small progress indicator.

Do not freeze the entire screen.

------------------------------------------------------------------------

# 29. Accessibility

Professional UI also means accessible UI.

Important:

-   High enough text contrast
-   Large touch targets
-   Do not rely only on color to identify equations
-   Support screen readers
-   Give icons meaningful accessibility labels
-   Avoid tiny text
-   Make focus states visible
-   Make interactive states distinguishable

For equations, color should not be the only identifier.

Example:

``` text
● Blue
y = x² − 3
```

The equation itself still identifies the function.

------------------------------------------------------------------------

# 30. Responsive layout

Do not design only for one phone size.

The graph area should adapt to:

-   Small phones
-   Large phones
-   Tablets
-   Landscape mode

The equation panel should resize naturally.

Avoid hardcoding positions such as:

``` text
button.x = 623
button.y = 1398
```

Use responsive constraints/layout rules.

------------------------------------------------------------------------

# 31. Graph performance

A graphing app should feel extremely responsive.

Important implementation goals:

-   Smooth panning
-   Smooth zooming
-   Efficient redraws
-   Avoid unnecessary recomputation
-   Cache what can be cached
-   Do not redraw unrelated UI when only the graph changes

The UI can look beautiful, but if the graph stutters, the product will
still feel unfinished.

------------------------------------------------------------------------

# 32. Empty space

Do not try to fill every empty area.

Empty space is useful.

Good:

``` text
┌───────────────────────────┐
│                           │
│        GRAPH              │
│                           │
│                           │
└───────────────────────────┘
```

Bad:

``` text
Text
icon
button
label
gradient
decoration
badge
button
...
```

Professional interfaces are comfortable with empty space.

------------------------------------------------------------------------

# 33. Avoiding "AI slop" aesthetics

This is especially important.

Avoid blindly following trends.

### Common AI-looking patterns

-   Every component has huge rounded corners
-   Random blue/purple gradients
-   Excessive glassmorphism
-   Neon borders
-   Huge glowing buttons
-   Floating cards everywhere
-   Too many icons
-   Excessive shadows
-   Decorative blobs
-   Unnecessary 3D effects
-   Inconsistent spacing
-   Every element trying to look premium

The solution is not to make the UI boring.

The solution is to make every visual decision purposeful.

Ask:

> "What problem does this visual element solve?"

If the answer is "it looks cool," consider removing it.

------------------------------------------------------------------------

# 34. Suggested screen hierarchy

The final screen should feel roughly like this:

``` text
TOP
──────────────────────────────────────

Math Grapher                         ⋮

──────────────────────────────────────

                GRAPH

        subtle coordinate grid

                 │
          ╲      │      ╱
           ╲     │     ╱
            ╲    │    ╱
─────────────╲───┼───╱──────────────
               ╲ │ ╱
                ╲│╱

                     [ + ]
                     [ ⌖ ]
                     [ − ]

──────────────────────────────────────

Equations                 + Add equation

┌────────────────────────────────────┐
│ ●   y = x² − 3              ◉   ⋮ │
└────────────────────────────────────┘

──────────────────────────────────────
BOTTOM
```

This is the visual direction to aim for.

------------------------------------------------------------------------

# 35. Recommended component specifications

## Header

``` text
Height: 56–64dp
Horizontal padding: 16–20dp
Title: 22–24sp
Icon: 24dp
Touch target: 44–48dp
```

## Equation row

``` text
Height: approximately 56–64dp
Padding: 12–16dp
Radius: 14–16dp
Equation text: 18–20sp
Icon: 20–24dp
```

## Add equation

``` text
Height: approximately 44–48dp
Text: 14–16sp
Icon: 20–24dp
Radius: 12–14dp
```

## Graph controls

``` text
Control size: approximately 48dp
Icon: 20–24dp
Spacing: minimal
```

## Main spacing

``` text
Page padding: 16–20dp
Section gap: 20–24dp
Small gap: 8–12dp
```

These values are a starting point, not laws. Adjust them according to
the actual screen size and platform conventions.

------------------------------------------------------------------------

# 36. Suggested design tokens

Create centralized design tokens rather than hardcoding values
throughout the application.

Example conceptual system:

``` text
spacing.xs = 4
spacing.sm = 8
spacing.md = 12
spacing.lg = 16
spacing.xl = 24
spacing.xxl = 32

radius.sm = 8
radius.md = 12
radius.lg = 16
radius.xl = 24

touch.minimum = 48

text.title = 24
text.section = 20
text.body = 16
text.equation = 19
text.secondary = 14
text.caption = 12
```

The exact implementation depends on your framework.

The important idea is that your entire application should use the same
design vocabulary.

------------------------------------------------------------------------

# 37. Component consistency

If you have another screen with a button:

``` text
[ Continue ]
```

and another screen has:

``` text
[       Continue       ]
```

with completely different radius, padding, typography, and icon style,
the app will feel inconsistent.

Create reusable components:

``` text
AppButton
SecondaryButton
IconButton
EquationRow
SectionHeader
GraphControl
BottomSheet
MathInput
```

Then reuse them.

This is one of the biggest differences between a prototype and a
professional app.

------------------------------------------------------------------------

# 38. Microcopy

Use short, natural labels.

Prefer:

``` text
Add equation
Reset view
Hide graph
Delete equation
Edit equation
```

Avoid:

``` text
Click here to add a new mathematical equation
```

The interface should communicate quickly.

------------------------------------------------------------------------

# 39. Menus

The `⋮` menu for an equation can contain:

``` text
Edit
Duplicate
Change color
Hide
Delete
```

Do not expose every possible feature as a permanent button.

Put secondary actions in the menu.

This keeps the primary interface clean.

------------------------------------------------------------------------

# 40. Interaction model

A good flow could be:

### Step 1

User taps:

``` text
+ Add equation
```

### Step 2

Equation editor appears:

``` text
y = |
```

### Step 3

User enters:

``` text
x² − 3
```

### Step 4

Graph updates immediately.

### Step 5

The equation appears in the equation list.

This should feel almost instantaneous.

------------------------------------------------------------------------

# 41. Visual feedback

When the user selects an equation:

``` text
┌────────────────────────────────────┐
│ ●  y = x² − 3              ◉    ⋮ │
└────────────────────────────────────┘
```

The corresponding graph curve should become slightly more prominent.

When deselected:

``` text
Curve returns to normal emphasis.
```

This creates a strong relationship between the list and graph.

------------------------------------------------------------------------

# 42. Don't over-design the graph

This is important.

The graph itself already contains visual information.

Do not add:

-   giant decorative backgrounds
-   glowing circles
-   unnecessary gradients
-   floating labels everywhere
-   excessive grid lines
-   decorative particles

The mathematics is the visual content.

Let it breathe.

------------------------------------------------------------------------

# 43. Recommended first redesign order

Do NOT rebuild everything simultaneously.

Use this order:

## Phase 1 --- Typography

Fix:

-   Font sizes
-   Font weights
-   Text hierarchy
-   Label opacity

## Phase 2 --- Graph

Fix:

-   Grid contrast
-   Axis contrast
-   Curve thickness
-   Labels

## Phase 3 --- Equation editor

Fix:

-   Layout
-   Equation row
-   Add equation
-   Visibility
-   Overflow menu

## Phase 4 --- Controls

Fix:

-   Zoom
-   Reset
-   Pan/recenter
-   Touch targets

## Phase 5 --- Polish

Fix:

-   Animations
-   Pressed states
-   Focus states
-   Accessibility
-   Responsive behavior

This prevents the project from becoming messy.

------------------------------------------------------------------------

# 44. Before/after summary

## Before

``` text
Dark background
+
Heavy grid
+
Large rounded input
+
Large plus button
+
Equation text crossing graph
+
Weak hierarchy
=
Prototype feeling
```

## After

``` text
Strong graph
+
Subtle grid
+
Clear header
+
Proper equation list
+
Purposeful controls
+
Consistent spacing
+
Strong typography
+
Restrained accent color
=
Professional product feeling
```

------------------------------------------------------------------------

# 45. The most important rules

If you remember nothing else, remember these:

### Rule 1

**The graph is the hero.**

### Rule 2

**Make the grid quieter than the curve.**

### Rule 3

**Use fewer visual effects, not more.**

### Rule 4

**Use one spacing system.**

### Rule 5

**Use one typography system.**

### Rule 6

**Don't make every component extremely rounded.**

### Rule 7

**Every button needs a purpose.**

### Rule 8

**Secondary actions belong in menus.**

### Rule 9

**Use color intentionally.**

### Rule 10

**Design reusable components instead of one-off screens.**

### Rule 11

**Make interactions feel immediate.**

### Rule 12

**Leave intentional empty space.**

------------------------------------------------------------------------

# 46. Final target

The finished app should make someone think:

> "This is a proper graphing application."

---not:

> "Someone added a graph to a dark-themed UI."

The difference comes from the details:

-   consistent spacing
-   restrained colors
-   strong typography
-   subtle grid
-   clear equation management
-   purposeful controls
-   good touch targets
-   smooth interactions
-   mathematical formatting
-   responsive layout
-   accessibility
-   reusable components

The redesign should feel **quietly premium**, not flashy.

------------------------------------------------------------------------

# 47. Quick implementation checklist

Use this checklist while redesigning.

## Layout

-   [ ] Header has clear hierarchy
-   [ ] Graph gets most of the visual attention
-   [ ] Equation area is clearly separated
-   [ ] Spacing follows a consistent scale
-   [ ] Layout works on different screen sizes

## Graph

-   [ ] Grid is subtle
-   [ ] Axes are stronger than grid
-   [ ] Curve is the strongest visual element
-   [ ] Labels are readable but not distracting
-   [ ] Zoom/reset controls have clear purposes

## Equations

-   [ ] Equation list exists
-   [ ] Each equation has a color indicator
-   [ ] Visibility can be controlled
-   [ ] Secondary actions use an overflow menu
-   [ ] Add equation is easy to find
-   [ ] Math expressions render naturally

## Typography

-   [ ] Screen title is consistent
-   [ ] Section headings are consistent
-   [ ] Equation text has appropriate emphasis
-   [ ] Secondary text is visibly secondary
-   [ ] No random font sizes

## Components

-   [ ] Corner radii are consistent
-   [ ] Buttons share the same visual language
-   [ ] Icons come from one icon system
-   [ ] Touch targets are large enough
-   [ ] Components are reusable

## Polish

-   [ ] Pressed states work
-   [ ] Selected states work
-   [ ] Disabled states work
-   [ ] Animations are subtle
-   [ ] No unnecessary glow/gradient effects
-   [ ] Accessibility labels exist
-   [ ] Color is not the only way information is communicated

------------------------------------------------------------------------

# 48. One final design test

Before shipping a screen, temporarily remove:

-   colors
-   gradients
-   shadows
-   icons

and look only at the layout.

If the interface still looks organized in grayscale, the underlying
design is probably strong.

If it only looks good because of colors and effects, the hierarchy needs
more work.

> **Good UI survives without decoration.**
