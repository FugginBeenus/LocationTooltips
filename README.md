# Location Display Mod

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.92.6-blue.svg)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Server-Side](https://img.shields.io/badge/Type-Server%20%26%20Client-purple.svg)](https://fabricmc.net/)

A location display system for Minecraft that shows players their current area at the top of their screen. Works well for servers with shopping districts, themed areas, player shops, and points of interest.

> Heavily inspired by the Origin Realms location system. The core concept isn't mine, but all the code and assets are written from scratch. I'm still pretty new to modding so development may be slow — open to collaboration if anyone's interested.

**All releases on [Modrinth](https://modrinth.com/mod/location-tooltip).**

## Overview

Adds a location indicator to the HUD that automatically updates as you move between defined regions — from "Wilderness" to "Shopping District" to individual shop names.

## Features

### Location Display
- HUD element at the top of the screen showing your current area
- Fade transitions when crossing region boundaries
- Adjustable color, size, position, and style

### Region Management
- Click-and-drag visual editor for defining region boundaries
- Full XYZ boundary support (not just 2D)
- Nested/hierarchical regions with priority-based display
- Region type categories (city, shop, wilderness, dungeon, etc.)

### Permissions
- Players can create and name their own regions
- Admin controls for full region management and overrides
- Region ownership tracking

### Nested Region Example

    Shopping District (Priority: 1)
      ├── Bob's Emporium (Priority: 2)
      │   ├── Weapons Section (Priority: 3)
      │   └── Armor Section (Priority: 3)
      └── Alice's Bakery (Priority: 2)

## Links

- [Modrinth](https://modrinth.com/mod/location-tooltip)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/location-tooltip)
- [Discord](https://discord.gg/fMpb6retYA)

## Roadmap

- [ ] Waypoint system integration
- [ ] Minimap compatibility

## Support

If you find a bug or have a feature request, open an issue. If you want to contribute, PRs are welcome.
