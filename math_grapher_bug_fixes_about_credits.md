# Math Grapher — UI Bug Fix & About/Credits Specification

**Date:** 13 September 2026  
**Developer:** Prasad Dhodamanai  
**Status:** 11th standard student

## 1. Required fixes

### A. Android status-bar overlap

The app header must respect Android system-bar insets. The phone's time, network, battery, and notification icons must never overlap the `Math Grapher` title, theme button, or three-dot menu.

Do not solve this with arbitrary fixed top padding. Use the framework's proper window/system-bar inset or safe-area API so the layout works across devices.

Desired structure:

```text
Android status bar
        ↓
┌─────────────────────────────────┐
│ Math Grapher                ⋮  │
├─────────────────────────────────┤
│                                 │
│             GRAPH               │
│                                 │
```

### B. Graph escaping the viewport

The plotted function currently can appear outside the intended graph/grid region. This must be fixed.

Create one authoritative graph viewport:

```text
graphBounds = {
    left,
    top,
    right,
    bottom
}
```

Everything belonging to the graph must use this same viewport:

- Minor grid
- Major grid
- X/Y axes
- Coordinate labels
- Function curves
- Points
- Highlights
- Graph annotations
- Touch/pan area

Before drawing graph content, clip to `graphBounds`:

```text
save canvas state

clip to graphBounds

draw grid
draw axes
draw coordinate labels
draw functions
draw points/highlights

restore canvas state
```

The exact API depends on the framework.

### C. Shared coordinate transformation

The grid and function renderer must use the same:

- origin
- scale
- zoom
- pan offset
- coordinate-to-screen transformation

Conceptually:

```text
math coordinate
      ↓
shared graph transform
      ↓
screen coordinate
      ↓
clip to graphBounds
      ↓
draw
```

This must remain correct while zooming and panning.

## 2. Graph rendering order

Use this order:

1. Graph background
2. Minor grid
3. Major grid
4. Axes
5. Coordinate labels
6. Function curves
7. Points/selections/annotations

The function curve should be visually stronger than the grid.

Recommended visual hierarchy:

```text
Function curve       ██████████  strongest
Axes                 ██████      medium
Major grid           ███         subtle
Minor grid           ██          very subtle
Coordinate labels    ██          subtle
```

## 3. Three-dot menu

Add a three-dot overflow button to the top-right header:

```text
Math Grapher                         ⋮
```

Use an approximately 44–48dp touch target with a 20–24dp icon.

The menu must contain:

```text
┌──────────────────────────┐
│  +  Add                  │
│  ⓘ  About                │
│  ★  Credits              │
└──────────────────────────┘
```

Use the same icon style and visual language as the rest of the app.

### Add

Selecting **Add** should trigger the same functionality as the existing add-equation action and focus/open the equation input.

Do not create two separate implementations of the add logic.

### About

Open a simple About page containing:

**Math Grapher**

> Math Grapher is a simple mathematical graphing tool designed to help visualize functions directly on a coordinate plane.

Keep the page clean and uncluttered.

### Credits

Open a Credits page containing:

```text
Math Grapher

Made by
Prasad Dhodamanai

11th Standard Student

Instagram
@prasad.dhodamani

Email
prasaddhodamani910@gmail.com

13 September 2026
```

Keep the information readable and professionally spaced.

## 4. Credits contact actions

If supported, make the following interactive:

- `@prasad.dhodamani` → open the Instagram profile
- `prasaddhodamani910@gmail.com` → open the default email application

Use normal Android intents/actions rather than embedding unnecessary web views.

## 5. Menu behavior

Expected behavior:

```text
Tap ⋮
   ↓
┌─────────────────┐
│ Add             │
│ About           │
│ Credits         │
└─────────────────┘
```

- Tapping an item closes the menu and performs the action.
- Tapping outside closes the menu.
- The menu must not overlap the Android status bar.
- Menu rows should have approximately 44–48dp minimum touch height.
- Avoid excessive glow, gradients, or oversized rounded surfaces.

## 6. Header layout

Recommended:

```text
┌─────────────────────────────────────┐
│                                     │
│ Math Grapher                    ⋮  │
│                                     │
├─────────────────────────────────────┤
│                                     │
│              GRAPH                  │
│                                     │
```

If the theme control is retained:

```text
Math Grapher                 ☀️   ⋮
```

Keep both controls inside the safe/inset area.

## 7. Graph and panel separation

The graph should never paint over the equation panel.

Use separate layout regions:

```text
┌─────────────────────────────────┐
│ Safe header                     │
├─────────────────────────────────┤
│                                 │
│ Graph viewport                  │
│                                 │
├─────────────────────────────────┤
│ Equation panel                  │
│                                 │
└─────────────────────────────────┘
```

The graph canvas should be clipped to its own region.

## 8. Zoom and pan requirements

When zooming or panning:

- Grid moves/scales correctly.
- Axes remain aligned with the grid.
- Labels remain aligned.
- Functions use exactly the same transform.
- Graph clipping remains unchanged.
- Header remains fixed.
- Equation panel remains fixed.

The graph should behave as one coherent canvas.

## 9. Equation area

Keep the existing equation functionality but make the relationship to the graph clear.

Suggested structure:

```text
Equations                    + Add equation

┌────────────────────────────────────┐
│ ●  y = x² − 3                  ×  │
└────────────────────────────────────┘

┌────────────────────────────────────┐
│ y = [ equation input ]         +  │
└────────────────────────────────────┘
```

The equation row should correspond visually to the plotted curve.

## 10. UI polish

Use a restrained design system:

- Page padding: approximately 16–20dp
- Common spacing: 8, 12, 16, 24, 32dp
- Equation/card radius: approximately 14–16dp
- Input/button radius: approximately 12–14dp
- Touch targets: approximately 44–48dp
- Equation text: approximately 18–20sp
- Section title: approximately 18–20sp
- Screen title: approximately 22–24sp
- Graph labels: approximately 11–13sp

Do not make every component extremely rounded.

Avoid unnecessary:

- neon effects
- gradients
- glassmorphism
- giant shadows
- decorative animations
- excessive floating cards

The goal is quietly professional rather than flashy.

## 11. Implementation priority

### Priority 1
Fix graph clipping so no curve can escape the graph viewport.

### Priority 2
Fix Android system-bar/status-bar insets.

### Priority 3
Unify the coordinate transformation used by grid, axes, labels, and functions.

### Priority 4
Add the three-dot menu with Add, About, and Credits.

### Priority 5
Polish typography, spacing, animations, accessibility, and responsive behavior.

## 12. Acceptance checklist

### Status bar
- [ ] Title does not overlap Android status-bar content.
- [ ] Theme button does not overlap it.
- [ ] Three-dot button does not overlap it.
- [ ] Works on different screen sizes and Android devices.
- [ ] No device-specific hardcoded top padding.

### Graph
- [ ] Function never renders outside graph viewport.
- [ ] Grid is clipped to the same viewport.
- [ ] Axes are clipped correctly.
- [ ] Labels stay within the intended graph area.
- [ ] Zoom does not break clipping.
- [ ] Pan does not break clipping.
- [ ] Grid and function remain aligned.
- [ ] Graph never paints over the equation panel.

### Three-dot menu
- [ ] Three-dot button is visible.
- [ ] Add works.
- [ ] About opens.
- [ ] Credits opens.
- [ ] Menu closes after selection.
- [ ] Menu closes when tapping outside.
- [ ] Menu respects system-bar insets.

### Credits
- [ ] Prasad Dhodamanai is displayed.
- [ ] 11th Standard Student is displayed.
- [ ] @prasad.dhodamani is displayed.
- [ ] prasaddhodamani910@gmail.com is displayed.
- [ ] 13 September 2026 is displayed.

## 13. Final target

The final Math Grapher experience should feel like a proper graphing application rather than a prototype.

The core principles are:

**Correct system insets + correct graph clipping + one shared coordinate transform + clean navigation + restrained visual design.**

The graph remains the hero of the application.

### Developer credit

> **Math Grapher**  
> Made by **Prasad Dhodamanai**  
> **11th Standard Student**  
> Instagram: **@prasad.dhodamani**  
> Email: **prasaddhodamani910@gmail.com**  
> **13 September 2026**
