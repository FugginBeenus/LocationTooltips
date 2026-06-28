## Location Tooltip 0.3.0

A big update — region protections, automatic structure naming, optional Waystones support, and a fully redesigned UI.

### New
- **Region protection flags** — guard a region with per-flag control (Allow / Deny / Inherit): PvP, block breaking, block placing, interaction, container access, entity interaction, mob spawning, explosions, and fire spread. Flags inherit through nested regions, so an inner region can override the one around it.
- **Automatic structure regions** — villages and other landmarks name themselves on your HUD as you explore (Plains Village, Desert Village, Pillager Outpost, Woodland Mansion, Ocean Monument, Ancient City, and more). Fully server-configurable, and it works with modded structures too.
- **Waystones integration** (optional) — if you run Waystones, a named waystone inside a structure lends its themed name to that region.
- **Redesigned admin panel** — a sleek, searchable region browser with flag icons, color-coded region types, and smooth scrolling. Matching create/edit screens with a visual flag editor.
- **New commands** — `/ltregion flag`, `/ltregion flags`, and `/ltregion structures`.

### Fixed
- Mob-spawning protection now actually works (its mixin was never being loaded).
- Region edit and delete now properly check permissions (operators or the region owner).
- `/ltregion create` now uses both selected corners.
- HUD corner-style and border options now render correctly.

### Thanks
- GambaPVP for the "view all regions" feature.

---
*Requires Fabric API. Mod Menu, Cloth Config, and Waystones are optional.*
