package com.fugginbeenus.locationtooltip.server;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Command-based region creation for when the wand doesn't work
 * (spawn protection, other mods interfering, etc.)
 */
public class RegionCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
                CommandManager.literal("ltregion")
                        .requires(source -> source.hasPermissionLevel(2)) // OP level 2

                        // /ltregion pos1 - Set first corner at current position
                        .then(CommandManager.literal("pos1")
                                .executes(ctx -> {
                                    ServerCommandSource source = ctx.getSource();
                                    ServerPlayerEntity player = source.getPlayerOrThrow();
                                    BlockPos pos = player.getBlockPos();

                                    com.fugginbeenus.locationtooltip.region.SelectionManager.setFirst(player, pos);
                                    player.sendMessage(Text.literal("§aFirst corner set at " + pos.toShortString()), false);

                                    return 1;
                                })
                        )

                        // /ltregion pos2 - Set second corner at current position
                        .then(CommandManager.literal("pos2")
                                .executes(ctx -> {
                                    ServerCommandSource source = ctx.getSource();
                                    ServerPlayerEntity player = source.getPlayerOrThrow();
                                    BlockPos pos = player.getBlockPos();

                                    com.fugginbeenus.locationtooltip.region.SelectionManager.setSecond(player, pos);
                                    player.sendMessage(Text.literal("§aSecond corner set at " + pos.toShortString()), false);

                                    // Check if both are set
                                    if (com.fugginbeenus.locationtooltip.region.SelectionManager.hasBoth(player)) {
                                        player.sendMessage(Text.literal("§6Both corners set! Use §e/ltregion create <name> §6to create the region."), false);
                                    }

                                    return 1;
                                })
                        )

                        // /ltregion create <name> - Create region from pos1/pos2
                        .then(CommandManager.literal("create")
                                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerCommandSource source = ctx.getSource();
                                            ServerPlayerEntity player = source.getPlayerOrThrow();
                                            String name = StringArgumentType.getString(ctx, "name");

                                            if (!com.fugginbeenus.locationtooltip.region.SelectionManager.hasBoth(player)) {
                                                player.sendMessage(Text.literal("§cYou must set both corners first! Use /ltregion pos1 and /ltregion pos2"), false);
                                                return 0;
                                            }

                                            BlockPos a = com.fugginbeenus.locationtooltip.region.SelectionManager.getFirst(player);
                                            BlockPos b = com.fugginbeenus.locationtooltip.region.SelectionManager.getFirst(player);

                                            // Create the region with default settings
                                            RegionManager.of(player.getServer()).createRegion(player, name, a, b, true, true);

                                            return 1;
                                        })
                                )
                        )

                        // /ltregion createhere <name> <radius> - Create region around current position
                        .then(CommandManager.literal("createhere")
                                .then(CommandManager.argument("name", StringArgumentType.string())
                                        .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, 1000))
                                                .executes(ctx -> {
                                                    ServerCommandSource source = ctx.getSource();
                                                    ServerPlayerEntity player = source.getPlayerOrThrow();
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    int radius = IntegerArgumentType.getInteger(ctx, "radius");

                                                    BlockPos center = player.getBlockPos();
                                                    BlockPos a = center.add(-radius, -10, -radius);
                                                    BlockPos b = center.add(radius, 10, radius);

                                                    RegionManager.of(player.getServer()).createRegion(player, name, a, b, true, true);

                                                    return 1;
                                                })
                                        )
                                )
                        )

                        // /ltregion createbox <name> <x1> <y1> <z1> <x2> <y2> <z2> - Create region by coordinates
                        .then(CommandManager.literal("createbox")
                                .then(CommandManager.argument("name", StringArgumentType.string())
                                        .then(CommandManager.argument("x1", IntegerArgumentType.integer())
                                                .then(CommandManager.argument("y1", IntegerArgumentType.integer())
                                                        .then(CommandManager.argument("z1", IntegerArgumentType.integer())
                                                                .then(CommandManager.argument("x2", IntegerArgumentType.integer())
                                                                        .then(CommandManager.argument("y2", IntegerArgumentType.integer())
                                                                                .then(CommandManager.argument("z2", IntegerArgumentType.integer())
                                                                                        .executes(ctx -> {
                                                                                            ServerCommandSource source = ctx.getSource();
                                                                                            ServerPlayerEntity player = source.getPlayerOrThrow();
                                                                                            String name = StringArgumentType.getString(ctx, "name");

                                                                                            int x1 = IntegerArgumentType.getInteger(ctx, "x1");
                                                                                            int y1 = IntegerArgumentType.getInteger(ctx, "y1");
                                                                                            int z1 = IntegerArgumentType.getInteger(ctx, "z1");
                                                                                            int x2 = IntegerArgumentType.getInteger(ctx, "x2");
                                                                                            int y2 = IntegerArgumentType.getInteger(ctx, "y2");
                                                                                            int z2 = IntegerArgumentType.getInteger(ctx, "z2");

                                                                                            BlockPos a = new BlockPos(x1, y1, z1);
                                                                                            BlockPos b = new BlockPos(x2, y2, z2);

                                                                                            RegionManager.of(player.getServer()).createRegion(player, name, a, b, true, true);

                                                                                            return 1;
                                                                                        })
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )

                        // /ltregion clear - Clear current selection
                        .then(CommandManager.literal("clear")
                                .executes(ctx -> {
                                    ServerCommandSource source = ctx.getSource();
                                    ServerPlayerEntity player = source.getPlayerOrThrow();

                                    com.fugginbeenus.locationtooltip.region.SelectionManager.clear(player);
                                    player.sendMessage(Text.literal("§aSelection cleared."), false);

                                    return 1;
                                })
                        )
        );
    }
}