# Regions

A region is a box in the world with a name on it. That is genuinely all it is. Everything else the mod does hangs off that.

## Making one

### With the wand

The usual way, and the one covered step by step in [Your First Region](Your-First-Region).

Right click a block for the first corner, right click another for the second, then sneak and right click to open the naming screen. Right clicking a third time when you already have both corners throws the selection away and starts over from that block.

You can also right click into thin air once both corners are set, and that opens the naming screen too.

### With commands

Handy when you want exact numbers rather than clicking blocks.

`/ltregion pos1` and `/ltregion pos2` set the corners to wherever you are standing, then `/ltregion create <name>` makes the region from them.

`/ltregion createhere <name> <radius>` builds a box around you without any of the corner marking. Good for a quick spawn protection.

`/ltregion createbox <name> <x1> <y1> <z1> <x2> <y2> <z2>` takes the coordinates directly.

These all need admin. Full details in [Commands](Commands).

## Height matters

The most common mistake is a region that is one block tall.

If you mark both corners by clicking the floor, that is exactly what you get, and you will walk over the top of it without the name ever appearing. Click something high for your second corner, or use `createbox` and give it a Y range you actually want.

A region covering a building usually wants to start a block or two below the floor and end a few blocks above the roof, so people walking on the top floor or standing in the doorway are still inside it.

## Regions inside regions

Regions can overlap freely, and nesting is the useful case.

When you are standing in more than one, the smallest one wins for the name on your screen. Smallest means smallest by volume, not most recently made.

So you can do this:

```
Market District
  Bob's Emporium
    Bob's Back Room
  Alice's Bakery
```

Walk through the market and it says Market District. Step into Bob's shop and it says Bob's Emporium. Go through the door at the back and it says Bob's Back Room. Walk out and it works back up the chain.

There is no separate parent setting to configure. If one box is inside another, that is the nesting.

Protections nest too, and they follow the same smallest first rule, though with an extra wrinkle. See [Protections](Protections).

## Naming

Names can be up to 48 characters and can contain spaces.

Renaming is done from the Admin Compass, covered in [The Admin Compass](The-Admin-Compass).

One thing worth knowing: renaming a region that was created automatically for a structure converts it into a normal region. It stops being managed by the automatic tagger, so nothing will overwrite your name later. That is deliberate, and it is how you take a village or a fortress and make it properly yours.

## Ownership

Whoever creates a region owns it. The owner can edit and delete it, and so can any admin. Nobody else can touch it.

Regions created automatically for structures have no owner. They belong to the server, so only admins can edit them.

There is one narrow exception, which is players naming villages. That gives them the name and nothing else, no ownership and no control over protections. It is off by default and is covered in [Villages and Structures](Villages-and-Structures).

## Where regions live

Region data is saved in the world folder, at `<world>/locationtooltip/regions/<namespace>/<dimension>.json`. One file per dimension.

It is readable JSON. You can back it up by copying the folder, and you can move regions between worlds by copying the files, as long as the coordinates still make sense in the destination.

Regions are saved as soon as you change them, so you do not need to worry about losing work if the server stops unexpectedly.

## Deleting

From the Admin Compass, hit Delete on the row. There is no confirmation step, so be a little careful with it.

Deleting a region does not affect anything inside it. Blocks stay, nested regions stay and keep working, and the only thing that changes is that the name and any protections that region was providing stop applying.
