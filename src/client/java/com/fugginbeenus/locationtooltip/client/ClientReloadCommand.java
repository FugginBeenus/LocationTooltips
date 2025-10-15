package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.data.Region;
import com.fugginbeenus.locationtooltip.data.RegionManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public final class ClientReloadCommand {
    private ClientReloadCommand() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("lt")
                            .then(ClientCommandManager.literal("reload")
                                    .executes(ctx -> {
                                        LTConfig.load();
                                        RegionManager.load();
                                        ctx.getSource().sendFeedback(Text.literal("§aLocation Tooltip reloaded."));
                                        return 1;
                                    })
                            )
                            .then(ClientCommandManager.literal("layout")
                                    .then(ClientCommandManager.argument("mode", StringArgumentType.word())
                                            .suggests((c, b) -> { b.suggest("auto"); b.suggest("together"); b.suggest("split"); return b.buildFuture(); })
                                            .executes(ctx -> {
                                                String mode = StringArgumentType.getString(ctx, "mode").toLowerCase();
                                                if (!mode.equals("auto") && !mode.equals("together") && !mode.equals("split")) {
                                                    ctx.getSource().sendFeedback(Text.literal("§cInvalid mode. Use auto|together|split."));
                                                    return 0;
                                                }
                                                LTConfig.get().layoutMode = mode;
                                                LTConfig.save();
                                                ctx.getSource().sendFeedback(Text.literal("§aLayout set to §f" + mode));
                                                return 1;
                                            })
                                    )
                            )
                            .then(ClientCommandManager.literal("save")
                                    .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                String name = StringArgumentType.getString(ctx, "name").trim();
                                                if (name.isEmpty()) name = "Unnamed";
                                                RegionSelectionClient.saveRegion(MinecraftClient.getInstance(), name);
                                                ctx.getSource().sendFeedback(Text.literal("§aSaved region: §f" + name));
                                                return 1;
                                            })
                                    )
                            )
                            .then(ClientCommandManager.literal("delete")
                                    .then(ClientCommandManager.argument("idOrName", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                String key = StringArgumentType.getString(ctx, "idOrName").trim();
                                                Region r = RegionManager.findByIdOrName(key);
                                                if (r == null) {
                                                    ctx.getSource().sendFeedback(Text.literal("§cRegion not found: " + key));
                                                    return 0;
                                                }
                                                var mc = MinecraftClient.getInstance();
                                                boolean ok = RegionManager.deleteRegion(r, mc.player.getUuid());
                                                ctx.getSource().sendFeedback(Text.literal(ok ? "§eDeleted " + (r.name() != null ? r.name() : r.id())
                                                        : "§cNo permission to delete."));
                                                return ok ? 1 : 0;
                                            })
                                    )
                            )
            );
        });
    }
}
