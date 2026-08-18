# Location Tooltip

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%201.21.x%20%7C%2026.x-green.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-blue.svg)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Modrinth](https://img.shields.io/badge/Download-Modrinth-brightgreen.svg)](https://modrinth.com/mod/location-tooltip)

Puts the name of wherever you are standing on your screen, and gives you the tools to decide what those places are called.

Walk around and a small pill at the top tells you where you are. Cross into somewhere you have named and the name changes. Underneath that there is a region system, so you can mark out a box, call it something, and lock down what people are allowed to do inside it.

> Initially inspired by the Origin Realms location system. Open to collaboration if anyone's interested.

## What it does

**Names your locations.** A pill at the top of the screen showing your current area, with optional pills for coordinates, biome and the time. Each one can go wherever you want on the screen.

**Regions you draw yourself.** Two clicks with a wand marks out a box. Name it and it is live immediately. Regions can sit inside other regions, and the smallest one you are standing in is the one that shows.

**Protections that belong to the place.** Eleven switches covering block breaking, placing, PvP, explosions, container access, mob spawning, mob griefing and more. Set them per region, and leave the rest of the world alone.

**Structures named automatically.** Villages, fortresses, monuments, ancient cities and the rest get tagged as chunks load, so they show up without you doing anything. Modded structures too.

**Villages with real names.** Instead of "Plains Village" you get Winterdell or Frost Shire, leaning on where the village sits but never predictable. The name comes from the village's position, so it is the same every time you load the world. If you run Waystones, the waystone name wins instead.

## Supported versions

| Minecraft | Java |
|---|---|
| 1.20.1 | 17 |
| 1.21.1 | 21 |
| 1.21.11 | 21 |
| 26.1, 26.1.1, 26.1.2 | 25 |
| 26.2 | 25 |

Every version is a separate download. Pick the file that matches your game.

Fabric API is required. Cloth Config gives you the settings screen, Mod Menu puts a button next to it in your mod list, and Waystones is picked up automatically if you have it. All three are optional.

## Getting started

1. Craft a Region Wand from a compass and two sticks.
2. Right click one corner of the area you want, then right click the opposite corner.
3. Sneak and right click to name it.

That is a working region. The [Your First Region](https://github.com/FugginBeenus/LocationTooltips/wiki/Your-First-Region) page walks through it properly, including the mistake nearly everyone makes on their first try.

## Documentation

The [wiki](https://github.com/FugginBeenus/LocationTooltips/wiki) covers everything in detail.

- [Installation](https://github.com/FugginBeenus/LocationTooltips/wiki/Installation), including which file to download
- [The HUD](https://github.com/FugginBeenus/LocationTooltips/wiki/The-HUD), the pills and how to place them
- [Regions](https://github.com/FugginBeenus/LocationTooltips/wiki/Regions), creating, naming, nesting and ownership
- [Protections](https://github.com/FugginBeenus/LocationTooltips/wiki/Protections), all eleven flags and how they resolve
- [The Admin Compass](https://github.com/FugginBeenus/LocationTooltips/wiki/The-Admin-Compass), the management panel
- [Villages and Structures](https://github.com/FugginBeenus/LocationTooltips/wiki/Villages-and-Structures), automatic tagging and naming
- [Commands](https://github.com/FugginBeenus/LocationTooltips/wiki/Commands), the full reference
- [Configuration](https://github.com/FugginBeenus/LocationTooltips/wiki/Configuration), every setting in both config files
- [Permissions](https://github.com/FugginBeenus/LocationTooltips/wiki/Permissions), who is allowed to do what
- [Troubleshooting](https://github.com/FugginBeenus/LocationTooltips/wiki/Troubleshooting), when something is not behaving

## Links

- [Modrinth](https://modrinth.com/mod/location-tooltip)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/location-tooltip)
- [Discord](https://discord.gg/fMpb6retYA)

## Roadmap

- [x] Waystones integration
- [ ] Minimap integration, Xaero's and JourneyMap
- [ ] Finer permissions than operator level

## Building

One source tree covers every version, using [Stonecutter](https://stonecutter.kikugie.dev/) to switch the parts that differ.

```bash
./gradlew chiseledBuild
```

Jars land in each version's `build/libs`. To build or run one version on its own:

```bash
./gradlew :1.20.1:build
./gradlew :1.20.1:runClient
```

## Contributing

Bugs and feature requests are welcome as [issues](https://github.com/FugginBeenus/LocationTooltips/issues), and pull requests are welcome too. If you are reporting a bug, your Minecraft version and your log save a lot of back and forth.

## License

MIT. See [LICENSE](LICENSE).
