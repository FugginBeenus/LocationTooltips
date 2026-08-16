# Installation

## Which file do I download

Every Minecraft version gets its own file. Grab the one that matches the version you actually play, not the newest one on the list.

| Your Minecraft version | Download this build | Java you need |
|---|---|---|
| 1.20.1 | `1.20.1` | 17 |
| 1.21.1 | `1.21.1` | 21 |
| 1.21.11 | `1.21.11` | 21 |
| 26.1, 26.1.1, 26.1.2 | `26.1` | 25 |
| 26.2 | `26.2` | 25 |

The Java column is there in case you are running a server and picking a runtime. If you are just playing through the normal launcher, it sorts that out for you.

All releases live on [Modrinth](https://modrinth.com/mod/location-tooltip), and the same files are attached to each [GitHub release](https://github.com/FugginBeenus/LocationTooltips/releases).

## What else you need

**Fabric Loader and Fabric API.** Both required. Nothing works without them. Get Fabric API for your exact Minecraft version, same as the mod file.

That is the only hard requirement. Everything below is optional.

## Optional companions

**Cloth Config** gives you the settings screen. Without it the mod still runs perfectly well, you just have to edit the config file by hand instead of clicking things. If you want to change how the pills look without opening a text editor, install this.

**Mod Menu** puts a Settings button next to Location Tooltip in your mod list. It needs Cloth Config installed to be useful, since Cloth Config is what draws the actual screen. If you have Mod Menu but not Cloth Config, the button will not do anything.

**Waystones** is picked up automatically if you have it. When a waystone sits inside a village, the village takes the waystone's name. See [Villages and Structures](Villages-and-Structures) for how that plays with the automatic naming.

## Installing it

1. Put the mod file in your `mods` folder, alongside Fabric API.
2. Start the game once so the config files get written.
3. That is it.

On a server, the same file goes in the server's `mods` folder. Players need it installed too, because the pill is drawn on their screen and the config for how it looks is per player.

## Where the config files end up

Two files, and they do different jobs.

`config/locationtooltip.json` is yours. It controls how the pills look and where they sit on your screen. Each player has their own.

`config/locationtooltip-structures.json` lives on the server and controls automatic structure tagging and village naming. In single player, "the server" is your own game, so it is in the same place.

Region data itself is stored per world, under `<world>/locationtooltip/regions/`, split by dimension. It is plain JSON if you ever need to look at it, though the in game tools are easier.

## Updating

Drop the new file in and delete the old one. Your regions and your settings carry over. Settings that did not exist in your old config get filled in with sensible defaults the first time the new version starts.

## Removing it

Take the file out of `mods`. Your world is fine. The region data stays in the world folder doing nothing, so if you put the mod back later everything is still there.
