package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.data.Region;
import com.fugginbeenus.locationtooltip.data.RegionManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Handles /lt commands (save, reload, admin, delete, highlight) — client-side.
 */
public final class ClientReloadCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("lt")
                            // /lt save <name>
                            .then(ClientCommandManager.literal("save")
                                    .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                String name = StringArgumentType.getString(ctx, "name");
                                                MinecraftClient mc = MinecraftClient.getInstance();
                                                RegionSelectionClient.saveRegion(mc, name, 0);
                                                ctx.getSource().sendFeedback(Text.literal("§aSaved region: §f" + name));
                                                return 1;
                                            })
                                    )
                            )

                            // /lt reload (regions + config)
                            .then(ClientCommandManager.literal("reload")
                                    .executes(ctx -> {
                                        RegionManager.load();
                                        LTConfig.load();
                                        ctx.getSource().sendFeedback(Text.literal("§aReloaded regions and config."));
                                        return 1;
                                    })
                            )

                            // /lt admin (open GUI) — singleplayer only on client
                            .then(ClientCommandManager.literal("admin")
                                    .executes(ctx -> {
                                        if (!RegionManager.canAdmin()) {
                                            ctx.getSource().sendFeedback(Text.literal("§cYou do not have permission to use this command."));
                                            return 0;
                                        }
                                        MinecraftClient.getInstance().execute(() ->
                                                MinecraftClient.getInstance().setScreen(new RegionAdminScreen())
                                        );
                                        return 1;
                                    })
                            )

                            // /lt delete <id|name>
                            .then(ClientCommandManager.literal("delete")
                                    .then(ClientCommandManager.argument("idOrName", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                if (!RegionManager.canAdmin()) {
                                                    ctx.getSource().sendFeedback(Text.literal("§cYou do not have permission to delete regions."));
                                                    return 0;
                                                }
                                                String s = StringArgumentType.getString(ctx, "idOrName");
                                                Region r = RegionManager.findByIdOrName(s);
                                                if (r == null) {
                                                    ctx.getSource().sendFeedback(Text.literal("§cRegion not found."));
                                                    return 0;
                                                }
                                                boolean ok = RegionManager.deleteById(r.id());
                                                ctx.getSource().sendFeedback(Text.literal(ok
                                                        ? "§eDeleted region: §f" + (r.name() != null ? r.name() : r.id())
                                                        : "§cFailed to delete region."));
                                                return ok ? 1 : 0;
                                            })
                                    )
                            )

                            // /lt highlight <id|name>
                            .then(ClientCommandManager.literal("highlight")
                                    .then(ClientCommandManager.argument("idOrName", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                if (!RegionManager.canAdmin()) {
                                                    ctx.getSource().sendFeedback(Text.literal("§cYou do not have permission to highlight regions."));
                                                    return 0;
                                                }
                                                String s = StringArgumentType.getString(ctx, "idOrName");
                                                Region r = RegionManager.findByIdOrName(s);
                                                if (r == null) {
                                                    ctx.getSource().sendFeedback(Text.literal("§cRegion not found."));
                                                    return 0;
                                                }
                                                MinecraftClient.getInstance().execute(() -> {
                                                    RegionAdminScreen.highlight(r);
                                                    ctx.getSource().sendFeedback(Text.literal("§bHighlighted region: §f" + (r.name() != null ? r.name() : r.id())));
                                                });
                                                return 1;
                                            })
                                    )
                            )
            );
        });
    }
}
