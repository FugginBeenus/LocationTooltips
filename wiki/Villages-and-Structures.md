# Villages and Structures

You do not have to mark out every landmark by hand. As chunks load, the mod notices structures generating in them and quietly makes a region for each one.

Walk up to a village and the pill says the village's name. Walk into a nether fortress and it says Fortress. Nobody had to do anything.

## What gets tagged

Out of the box, the vanilla structures worth naming:

Villages of all five kinds, pillager outposts, woodland mansions, ocean monuments, ancient cities, nether fortresses, bastion remnants, end cities, strongholds, desert pyramids, jungle pyramids, swamp huts, igloos and trail ruins.

Modded structures are tagged too, on the assumption that if another mod went to the trouble of adding a structure, it is probably somewhere you want named. Turn that off with `/ltregion structures modded off` if it gets noisy.

## Managing the list

`/ltregion structures status` gives you the current state in one line.

`/ltregion structures list` shows everything currently being tagged.

`/ltregion structures enable <id>` and `/ltregion structures disable <id>` add and remove individual structures, with tab completion for the ids.

`/ltregion structures off` stops tagging entirely. Regions already made are kept.

`/ltregion structures rescan` clears out the automatic regions so they can be built again as chunks reload. Useful after changing what is enabled. It only removes automatic ones, so anything you made yourself is safe.

Structures are tagged as chunks load, so changes only apply to chunks you visit afterwards. In an existing world, the areas you have already explored keep whatever they had until you rescan and go back.

## Village names

A region called "Plains Village" is accurate and completely forgettable. So villages get a proper place name instead.

You get names like Winterdell, Frost Shire, Ironbrook and Pine Hollow. Some are one word run together, some are two words, and it varies from village to village.

The name leans on where the village is. Snowy villages lean towards cold words, desert villages towards dry ones, taiga towards trees. It is a lean rather than a rule, so a generic name can still turn up in the snow, which keeps it from feeling like a formula once you have seen a few.

The name comes from the village's position in the world, which means it is the same every single time. Reload the world, restart the server, come back in six months, still Winterdell.

Turn it off with `/ltregion structures villagenames off` and villages go back to plain structure names.

## If you run Waystones

Waystones wins.

When a waystone sits inside a village and someone has named that waystone, the village takes the waystone's name. That is almost always what you want, because a player naming a waystone has effectively named the place.

The automatic naming only fills the gap where Waystones has not. If you do not run Waystones, it fills every village.

When a waystone renames a village, everyone gets a message saying what it used to be called and what it is now.

## Letting players name villages

Off by default. Turn it on and a player who finds a village can give it a name.

```
/ltregion structures villagenames players on
```

Once it is on, a player standing in a village opens their Admin Compass and the village is in the list with a Name button on it. Click it, type a name, done. There is also `/ltname <name>` for anyone who would rather type.

### What it does not give them

This is the part worth being clear about, because on a public server it matters.

Naming a village gives the player the name and nothing else. They do not own it. They get no control over its protections. They cannot decide whether people can build there or fight there or open the chests. All of that stays exactly where it was, with the server.

It is a signpost, not a deed.

### Who can rename it afterwards

Whoever names a village is remembered. After that, only that player and admins can change it, so two people cannot sit there renaming it back and forth at each other.

The automatic namer also leaves it alone from then on, and so does the Waystones sync. A name a person chose deliberately is not going to get overwritten by something generated.

Admins can always rename it through the Admin Compass, and doing that takes it over permanently.

### Guard rails

The player has to actually be standing inside the village. Names are capped at 48 characters, and formatting codes are stripped out, so nobody can push coloured text into everybody else's screen.

The button is only a convenience. Everything is checked again on the server when the name arrives, so there is nothing to gain by trying to fake the message.

### Turning it back off

```
/ltregion structures villagenames players off
```

Names players already set are kept. They just cannot set any more.

## Making a structure region properly yours

Rename any automatic region from the Admin Compass and it stops being automatic. It becomes a normal region, the tagger leaves it alone, and you can set protections on it like anything else.

That is the intended route for turning a village into a proper town on a server, or locking down a monument so people stop mining the prismarine.
