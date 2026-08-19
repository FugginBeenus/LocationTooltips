# Troubleshooting

## Nothing shows on screen at all

Check Fabric API is installed and matches your Minecraft version. It is by far the most common cause, and the mod cannot do anything without it.

Check `showRegionName` has not been set to false in `config/locationtooltip.json`.

If you are on a server, the mod has to be installed on your client too. The pill is drawn on your screen, so a server only install gives you the protections and none of the display.

## The pill says Wilderness inside my region

Nine times out of ten the region is one block tall.

If you marked both corners by clicking the floor, that is what you get, and you walk straight over the top of it. Delete it and remake it with the second corner somewhere high up.

Otherwise, move a few blocks. The pill updates when you move, so if you created the region and then stood perfectly still, nothing has told it to change yet.

## The name is not updating when I cross a boundary

Same thing. Movement is what triggers the check, and it needs a small amount of actual movement rather than a step. Walk properly and it will catch up.

## I cannot see any regions in the Admin Compass

If you are not an admin, you only see regions you created yourself. An empty list means you have not made any, and that is working as intended rather than broken.

If you are an admin and the list is still empty, you may be a long way from all of them. The list is built from regions near you.

## Something is blocked and I do not know why

Stand exactly where it happened and run `/ltregion flags`. Each line shows what is in effect and names the region deciding it, including regions further out that you might have forgotten covered this spot.

Remember that admins do not bypass protections. If the flag says deny, it denies for you too.

## The village name changed on its own

If you run Waystones, that is almost certainly it. When a waystone inside a village gets named, the village takes the waystone's name, and everyone gets a message saying so.

If you want a name that nothing will overwrite, rename the region from the Admin Compass. That converts it out of automatic management for good.

## Structures are not being tagged

Structure tagging happens as chunks load, so it only applies to chunks you visit after it was turned on. Areas you explored beforehand keep whatever they had.

Run `/ltregion structures rescan` and then go back to the area. The chunks reload and get tagged properly.

Check `/ltregion structures status` to confirm tagging is actually on and that the structure you are expecting is in the list.

## Village naming is not working for players

Check it is turned on with `/ltregion structures villagenames`. It is off by default, so a fresh server will not have it.

The player has to be standing inside the village, not near it. If the village region is short, they may be above it.

If somebody else has already named that village, only they and admins can change it. That is deliberate.

## Region borders look wrong with shaders

If borders come out skewed and slide around as you move, update to 0.5.3 or newer. Older versions asked the render pipeline for their drawing buffer later than the game guarantees it. Vanilla and Sodium let that pass, Iris did not, so the boxes drew against the wrong view.

If you are on 0.5.3 or newer and still see the borders shift by a pixel or two while shaders are running, that is your shaderpack's own anti aliasing jittering the picture between frames. It affects anything drawn outside the shader's own pipeline and is not something the mod can correct.

Borders sit behind water and stained glass from 0.5.3 onwards. That is deliberate. They used to draw over the top of water regardless of what was in front.

## The config screen will not open

You need Cloth Config installed. Mod Menu on its own gives you the button but Cloth Config is what draws the screen behind it.

Without either, everything is still configurable by editing `config/locationtooltip.json` and restarting.

## The screens run off the edge of my window

That was fixed in 0.5.1. If you are still seeing it, you are on an older build.

## My regions disappeared

They are stored in the world folder at `<world>/locationtooltip/regions/`. If the files are still there, the regions are still there, and something is stopping them loading rather than deleting them. Check the server log around startup.

If you moved a world between computers, make sure that folder came with it.

## Still stuck

Open an issue on [GitHub](https://github.com/FugginBeenus/LocationTooltips/issues) with your Minecraft version, the mod file you are using and your log. Or come and ask on [Discord](https://discord.gg/fMpb6retYA).
