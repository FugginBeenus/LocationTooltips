# Commands

There are two command roots. `/ltregion` is for admins and does everything. `/ltname` is open to everyone and does exactly one thing.

## /ltname

```
/ltname <name>
```

Names the village you are standing in. Anyone can run it, but it only does anything if the server has turned player naming on, and only if you are actually inside a village.

The name can contain spaces. Everything after `/ltname` is taken as the name.

Covered properly in [Villages and Structures](Villages-and-Structures).

## /ltregion

Everything below needs admin, which means operator level 2 or the equivalent permission on newer versions. Normal players will not see these in tab completion at all.

### Making regions

```
/ltregion pos1
/ltregion pos2
/ltregion create <name>
```

`pos1` and `pos2` set the two corners to wherever you are standing. `create` then builds the region from them. The name can contain spaces.

```
/ltregion createhere <name> <radius>
```

Builds a box around you with the given radius, between 1 and 1000. Quickest way to protect a spawn point. The name here is a single word, or quoted if you want spaces.

```
/ltregion createbox <name> <x1> <y1> <z1> <x2> <y2> <z2>
```

Builds a region from exact coordinates. Useful when you know the numbers and do not want to go and stand in two places.

```
/ltregion clear
```

Throws away your current corner selection without making anything.

### Protections

```
/ltregion flags
```

Lists what is in effect where you are standing, and names the region responsible for each answer. This is the one to reach for when something is being blocked and you cannot work out why.

```
/ltregion flag <flag> allow
/ltregion flag <flag> deny
/ltregion flag <flag> inherit
```

Sets a flag on the smallest region you are standing in. Flag names tab complete. See [Protections](Protections) for what each one covers.

### Structures

```
/ltregion structures status
```

One line summary. Whether tagging is on, whether modded structures are included, how many structures are listed and how many automatic regions exist.

```
/ltregion structures on
/ltregion structures off
```

Turns automatic structure tagging on and off. Turning it off keeps the regions already made.

```
/ltregion structures list
```

Everything currently being tagged, plus anything explicitly excluded.

```
/ltregion structures enable <id>
/ltregion structures disable <id>
```

Adds or removes a single structure. Ids tab complete from the structures actually registered in your game, so modded ones show up too.

```
/ltregion structures modded on
/ltregion structures modded off
```

Whether structures from other mods get tagged automatically. On by default.

```
/ltregion structures rescan
```

Clears out the automatic regions so they get rebuilt as chunks reload. Run this after changing what is enabled. Regions you made yourself are not touched.

### Village names

```
/ltregion structures villagenames
```

Shows whether automatic village naming is on, and whether players are allowed to name villages.

```
/ltregion structures villagenames on
/ltregion structures villagenames off
```

Turns the automatic place names on and off. Off means villages fall back to plain structure names.

```
/ltregion structures villagenames players on
/ltregion structures villagenames players off
```

Whether players can name villages they are standing in. Off by default. Turning it off later keeps any names players already set.

## Notes

Anything that changes settings saves straight away, so there is nothing to run afterwards to make it stick.

Region and structure commands all work from the console as well as in game, except the ones that need to know where you are standing. `pos1`, `pos2`, `createhere`, `flags`, `flag` and `ltname` all need a player.
