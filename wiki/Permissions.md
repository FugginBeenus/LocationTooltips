# Permissions

Short version: admins can do everything, players can manage what they made, and nobody else can touch it.

## Who counts as an admin

Operator level 2 or higher. On 1.21.11 and newer that is the game's own gamemaster permission, which comes to the same thing for a normal server.

There is no separate permission node and no integration with permission plugins. If you need finer control than operator level, that would be a good issue to open.

## What each person can do

| | Player | Admin |
|---|---|---|
| See the location pill | Yes | Yes |
| Create a region | Yes | Yes |
| Edit or delete a region they made | Yes | Yes |
| Edit or delete somebody else's region | No | Yes |
| See other people's regions in the compass | No | Yes |
| Use `/ltregion` | No | Yes |
| Name a village they are standing in | Only if the server turns it on | Yes |
| Change server settings | No | Yes |

## Ownership

Whoever creates a region owns it, and that is recorded permanently. The owner and any admin can edit or delete it. Nobody else can, and nobody else even sees it in their Admin Compass.

Regions created automatically for structures have no owner. They belong to the server, so only admins can change them.

## Admins do not bypass protections

This catches people out, so it is worth saying plainly.

If a region denies block breaking, admins cannot break blocks there either. The flags apply to the place, and the place does not care who you are.

If you need to get in, change the flag, then change it back. Or delete the region if you are done with it.

## Village naming is the one exception

There is exactly one case where a player can change something they do not own, and it is deliberately narrow.

When a server turns on player village naming, a player standing inside a village can set its name. That is the whole of it. They do not become the owner, they get no control over the protections, and they cannot delete it. The region still belongs to the server.

Whoever names a village is remembered, so nobody else can rename it over them afterwards. Admins can still rename it and doing so takes it over for good.

It is off by default, so a server that never touches it behaves exactly as it always did. Full details in [Villages and Structures](Villages-and-Structures).

## Anyone can craft the tools

Both the Region Wand and the Admin Compass are craftable by any player, and that is on purpose.

The wand is how players make their own regions, so it needs to be available.

The Admin Compass shows you what you are allowed to see. A player opening it gets a list of their own regions, and nothing else. It is called the Admin Compass because admins get the most out of it, not because it is restricted to them.

If you would rather players could not make regions at all on your server, there is no setting for that yet. Removing the recipes with a datapack is the current answer.
