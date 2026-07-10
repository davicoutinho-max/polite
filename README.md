# CivicPulse

Institutional transparency platform — **Angular 21 + PrimeNG 21**, standalone components,
signal-based reactive state, fully componentized around a shared design system.

> Built to run with the portable **Node 24** at
> `C:\Users\davi.pires\node\node-v24.18.0-win-x64` (no global Node install required).

## Running (PowerShell)

Add the portable Node to your `PATH` for the current session, then use the npm scripts:

```powershell
$env:Path = "C:\Users\davi.pires\node\node-v24.18.0-win-x64;" + $env:Path

npm start        # dev server  -> http://localhost:4200
npm run build    # production build -> dist/civicpulse
npm test         # unit tests (vitest)
```

## Architecture

```
src/app/
  core/                     Framework-agnostic domain
    models/                 Typed interfaces (Post, Bill, Politician, NavItem, …)
    services/               Signal stores (feed, bills, trending, session, navigation)
    theme/civic-preset.ts   PrimeNG preset mapped to the design tokens
  shared/
    ui/                     Generic, parameter-driven components
      ui-icon, ui-avatar, ui-tag, ui-card, ui-button,
      ui-icon-button, ui-progress, ui-nav-item
    pipes/                  compactNumber (1.2k / 45.2K)
  layout/
    shell/                  App layout (composes navigation + routed content)
    side-nav/               Desktop sidebar
    mobile-top-bar/         Mobile header
    mobile-bottom-nav/      Mobile bottom navigation
  features/
    feed/                   Feed page + composer, post-card, sort, sidebar widgets
    profile/                Profile page + header, tabs, bill-card
```

### Principles

- **Standalone + zoneless** — no NgModules, no `zone.js`; change detection is signal-driven.
- **Generic components** — every `ui-*` is presentational and driven purely by `input()`/`output()`,
  so you just drop it in and pass parameters (`<ui-tag label="Passed" severity="success" />`).
- **Reactive stores** — services expose read-only signals; components mutate via intent methods.
- **Single source of design truth** — all colors/spacing/typography live as CSS custom properties
  in `src/styles/_tokens.scss`, and the PrimeNG preset reads from the same palette.
- **OnPush everywhere** — deterministic, fast rendering.

## Routes

| Path            | Page               |
| --------------- | ------------------ |
| `/feed`         | Activity feed      |
| `/profile/:id`  | Politician profile |
