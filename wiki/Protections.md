# Protections

Every region carries a set of switches that decide what people can do inside it. They are called flags, and there are eleven of them.

The important thing about flags is that they belong to the place, not to the player. You are not giving a person permission to break blocks, you are saying blocks in this box cannot be broken. That applies to everyone.

![The protection grid on the region screen](images/naming-screen.png)

## Three states, not two

Each flag has three settings, and the third one is the one that makes nesting work.

**Allow** means yes, this is permitted here.

**Deny** means no, this is blocked here.

**Inherit** means this region has no opinion. Look at whatever region this one sits inside, and if there is nothing outside it either, fall back to the default.

Everything starts on Inherit. A brand new region changes nothing about how the world behaves until you actually set something.

Click a flag to cycle through the three.

## How a flag gets decided

When someone tries to break a block, the mod collects every region containing that block and works from the smallest outwards.

The first region with an actual opinion wins. Inherit is not an opinion, so those get skipped. If it runs out of regions without finding one, the default applies, and every flag defaults to allowed.

Worth sitting with for a second, because it is the opposite of what people often assume. A small region inside a big one overrides the big one. The inner region is more specific, so it gets the final say.

Here is the case that trips people up:

```
Spawn                 block breaking: Deny
  Community Garden    block breaking: Allow
```

Inside the garden, people can break blocks. The garden is smaller, so it wins. Everywhere else in spawn, they cannot. If you wanted the garden locked down too, you would leave it on Inherit and let the deny from spawn carry through.

## The flags

| Flag | What it covers |
|---|---|
| Allow PvP | Players damaging other players. Does not affect mobs hurting you. |
| Allow Block Breaking | Breaking any block. |
| Allow Block Placing | Placing any block. |
| Allow Interaction | Right clicking blocks. Doors, buttons, levers, crafting tables, that sort of thing. |
| Allow Container Access | Opening things that hold items. Chests, barrels, hoppers, furnaces. Separate from Interaction so you can let people use doors without letting them into the chests. |
| Allow Entity Interact | Right clicking entities. Trading with villagers, putting things in item frames, using boats. |
| Allow Explosions | Creepers, TNT, beds in the wrong dimension, end crystals. Stops the block damage. |
| Allow Fire Spread | Fire moving from block to block. The fire you light still burns, it just does not travel. |
| Allow Mob Spawning | Natural mob spawning. Does not touch spawners or spawn eggs. |
| Allow Mob Griefing | Mobs changing blocks. Endermen picking things up and putting them down, zombies breaking doors, ravagers wrecking crops and leaves, withers and the ender dragon chewing through walls. |
| Allow Item Pickup | Players picking dropped items up off the ground. The items still drop and still sit there, they just cannot be collected. |

Two of those overlap in a way worth knowing about. A creeper going off is handled by **Explosions**, not Mob Griefing, because from the region's point of view an explosion is an explosion whoever lit it. If you want a region where mobs cannot touch anything, deny both.

Admins bypass nothing here. If a region denies block breaking, it denies it for admins too. Change the flag or delete the region if you need to get in.

## Setting them

**On the screen.** Whenever you create or edit a region, the grid is right there. Click to cycle, then save.

**With commands.** `/ltregion flag <flag> allow`, `deny` or `inherit` sets a flag on the smallest region you are standing in.

`/ltregion flags` lists what is actually in effect where you are standing, which is not the same thing as what the region you are in has set. Each line shows the answer after nesting has been worked out, and tells you where that answer came from:

```
Flags in effect at Bob's Emporium:
  pvp: deny (from Spawn)
  block-break: deny
  mob-spawning: allow (default)
```

`deny` on its own means the region you are standing in decided it. `(from Spawn)` means a region further out decided it. `(default)` means nothing has an opinion anywhere, so the built in default applies.

## Common setups

**A shop nobody can wreck.** Block breaking and block placing to Deny, container access to Deny, interaction left on Inherit so customers can still open the door.

**A peaceful spawn.** PvP to Deny, mob spawning to Deny, explosions to Deny. Leave building alone if you want people to be able to build there.

**A protected build with a public path through it.** Make the big region and deny breaking and placing. Make a smaller region along the path and leave everything on Inherit, since the deny from outside carries through anyway. If you want the path to be diggable, set the inner region to Allow.

**An arena.** PvP to Allow inside a spawn area that otherwise denies it. The arena is smaller, so it wins.

## When something is blocked and you do not know why

Stand where it happened and run `/ltregion flags`. The line for the flag you care about tells you the answer and names the region responsible, which is normally the region you had forgotten about.
