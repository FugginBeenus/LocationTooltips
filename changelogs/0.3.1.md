## Location Tooltip 0.3.1

Fixes from tester feedback on 0.3.0.

### Fixed
- **Modded structures now get named.** Structures added by mods are tagged automatically — including modded versions of vanilla structures. Vanilla stays curated so you don't get a region for every mineshaft. Turn it off with `/ltregion structures modded off`, or exclude one with `/ltregion structures disable <id>`.
- **Waystones now name their village.** A waystone inside a village gives the village its name — whether it's activated, freshly placed, or renamed later — and anyone standing there sees a message. (Waystones only registers a naturally-generated waystone once a player activates it, which is why this didn't work before.) Renaming the waystone renames the village too, and a village you've renamed by hand is left alone.
- **The Master Compass needle works.** It now points to the nearest region, and spins freely when there's nothing nearby.
- **The HUD no longer covers boss bars.** When it's positioned at the top-centre it hides itself while a boss bar is on screen.
- **Dimensions have their own names.** The Nether now reads "The Nether" and the End reads "The End" instead of "Wilderness". Modded dimensions (Ad Astra and friends) are named automatically from their dimension id.

### Thanks
- Everyone who tested 0.3.0 and sent detailed reports.

---
*Requires Fabric API. Mod Menu, Cloth Config, and Waystones are optional.*
