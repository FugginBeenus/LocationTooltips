# Configuration

There are two config files and they belong to different people. One is yours and controls what you see. One is the server's and controls how the world gets tagged.

## Your settings

`config/locationtooltip.json`

This is per player. Changing it on a server changes nothing for anyone else, because the pills are drawn on your screen out of your own settings.

If you have Cloth Config installed you never need to open this file, since the settings screen covers all of it. Without Cloth Config, edit it by hand and restart the game.

### Which pills show

| Setting | Default | What it does |
|---|---|---|
| `showRegionName` | `true` | The region name pill. |
| `showClock` | `true` | The clock pill. |
| `showCoords` | `false` | The coordinates pill. |
| `showBiome` | `false` | The biome pill. |
| `time24h` | `false` | 14:30 instead of 2:30 PM. |

### Where they sit

Each pill has its own anchor and its own pair of offsets. Anchors are `TOP_LEFT`, `TOP_CENTER`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_CENTER` and `BOTTOM_RIGHT`.

| Setting | Default | What it does |
|---|---|---|
| `position` | `TOP_CENTER` | Anchor for the region pill. |
| `xOffset` | `0` | Region pill, sideways from the anchor. |
| `yOffset` | `8` | Region pill, away from the top or bottom edge. |
| `coordsPosition` | `TOP_LEFT` | Anchor for the coordinates pill. |
| `coordsXOffset` | `8` | |
| `coordsYOffset` | `8` | |
| `biomePosition` | `TOP_LEFT` | Anchor for the biome pill. |
| `biomeXOffset` | `8` | |
| `biomeYOffset` | `30` | |

The clock does not have its own anchor. It sits with the region pill.

Offsets always push the pill away from the edge it is anchored to, so you never need negative numbers to move something inward.

Pills sharing an anchor line up in a row and take their position from the first one in that row.

### How they look

| Setting | Default | Range | What it does |
|---|---|---|---|
| `backgroundOpacity` | `0.85` | 0 to 1 | How solid the pill behind the text is. 0 leaves floating text. |
| `textScale` | `1.0` | 0.5 to 3 | Text size. The pill grows to fit. |
| `iconSize` | `16` | 8 to 64 | The icons either side of the text. |
| `pillPadding` | `6` | 0 to 24 | Space between the text and the pill edge. |
| `pillHeightScale` | `1.0` | 0.5 to 2.5 | Makes pills taller or shorter without changing text size. |
| `pillExtraWidth` | `0` | 0 to 64 | Extra width on every pill. |
| `spacing` | `8` | 0 to 48 | Gap between pills sharing an anchor. |
| `verticalNudge` | `1` | -16 to 16 | Shifts the text up or down inside the pill. |
| `shadow` | `true` | | Drop shadow on the text. |
| `cornerStyle` | `ROUND` | | `ROUND`, `PILL` or `SQUIRCLE`. |
| `cornerRadius` | `1` | 0 to 32 | How rounded the corners are in `ROUND`. |
| `cornerExponent` | `4.0` | | Tunes the curve in `SQUIRCLE`. Higher is squarer. |
| `borderWidth` | `0` | | Outline around the pill. |
| `gradientSheen` | `0` | | Subtle gradient across the pill. |
| `separator` | `" • "` | | Used when elements share one pill. |
| `splitElements` | `true` | | Separate pills rather than one combined pill. |

### Skinning the pills

If you would rather draw your own pill background than use the built in shapes, turn on `useTexturedPills` and the pill is drawn from an image using nine slice scaling, so the corners stay sharp while the middle stretches.

| Setting | Default | What it does |
|---|---|---|
| `useTexturedPills` | `false` | Use an image instead of the drawn shape. |
| `texW` / `texH` | `64` / `32` | Size of your source image. |
| `sliceLeft` / `sliceRight` | `8` / `8` | How much of each side is corner rather than stretch. |
| `sliceTop` / `sliceBottom` | `8` / `8` | Same for top and bottom. |

Slices are clamped to half the texture size, so you cannot accidentally set them so large they overlap.

## Server settings

`config/locationtooltip-structures.json`

This one lives with the server and applies to everybody. In single player your own game is the server, so it is in the same config folder.

Everything here can be set with commands instead, which is usually easier because the changes take effect immediately and you do not have to restart. See [Commands](Commands).

| Setting | Default | What it does |
|---|---|---|
| `enabled` | `true` | Automatic structure tagging as a whole. |
| `tagModdedStructures` | `true` | Whether structures from other mods get tagged. |
| `nameVillages` | `true` | Whether villages get a generated place name. |
| `allowPlayerVillageNaming` | `false` | Whether players can name villages they are standing in. |
| `structures` | a list | Vanilla structure ids that get tagged. |
| `denied` | empty | Structure ids explicitly excluded, even if they would otherwise be tagged. |

`denied` wins over `structures`. If an id is in both, it is not tagged.

## Region data

Not really configuration, but worth knowing where it is.

`<world>/locationtooltip/regions/<namespace>/<dimension>.json`

One file per dimension, in readable JSON. Copy the folder to back your regions up. It is written as soon as anything changes, so it stays current even if the server stops badly.
