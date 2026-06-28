package com.fugginbeenus.locationtooltip.server;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.RegionSource;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlag;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import com.fugginbeenus.locationtooltip.region.structure.StructureConfig;
import com.fugginbeenus.locationtooltip.region.structure.StructureRegionTagger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Command-based region creation for when the wand doesn't work
 * (spawn protection, other mods interfering, etc.)
 */
public class RegionCommands {

    /** Tab-completion for flag ids, sourced from the flag registry. */
    private static final SuggestionProvider<ServerCommandSource> FLAG_SUGGESTIONS = (ctx, builder) -> {
        for (RegionFlag f : RegionFlags.all()) builder.suggest(f.id);
        return builder.buildFuture();
    };

    /** Tab-completion for ALL structure registry ids (incl. modded), for `structures enable`. */
    private static final SuggestionProvider<ServerCommandSource> ALL_STRUCTURE_IDS = (ctx, builder) ->
            CommandSource.suggestIdentifiers(
                    ctx.getSource().getServer().getRegistryManager().get(RegistryKeys.STRUCTURE).getIds(),
                    builder);

    /** Tab-completion for currently-enabled structure ids, for `structures disable`. */
    private static final SuggestionProvider<ServerCommandSource> ENABLED_STRUCTURE_IDS = (ctx, builder) -> {
        for (String s : StructureConfig.get().structures) builder.suggest(s);
        return builder.buildFuture();
    };

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
                                            BlockPos b = com.fugginbeenus.locationtooltip.region.SelectionManager.getSecond(player);

                                            // Create the region with default settings
                                            RegionManager.of(player.getServer()).createRegion(player, name, a, b, java.util.Map.of());

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

                                                    RegionManager.of(player.getServer()).createRegion(player, name, a, b, java.util.Map.of());

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

                                                                                            RegionManager.of(player.getServer()).createRegion(player, name, a, b, java.util.Map.of());

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

                        // /ltregion structures <status|on|off|rescan> - Manage auto structure regions
                        .then(CommandManager.literal("structures")
                                .then(CommandManager.literal("status")
                                        .executes(ctx -> {
                                            ServerCommandSource src = ctx.getSource();
                                            int count = RegionManager.of(src.getServer()).countBySource(RegionSource.STRUCTURE);
                                            boolean on = StructureRegionTagger.isEnabled();
                                            src.sendFeedback(() -> Text.literal("Structure tagging: " + (on ? "ON" : "OFF")
                                                    + " | auto regions: " + count), false);
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("on")
                                        .executes(ctx -> {
                                            StructureRegionTagger.setEnabled(true);
                                            ctx.getSource().sendFeedback(() -> Text.literal(
                                                    "§aStructure tagging enabled. New chunks are tagged as they load."), false);
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("off")
                                        .executes(ctx -> {
                                            StructureRegionTagger.setEnabled(false);
                                            ctx.getSource().sendFeedback(() -> Text.literal(
                                                    "§eStructure tagging disabled. Existing structure regions are kept (use rescan to clear)."), false);
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("rescan")
                                        .executes(ctx -> {
                                            RegionManager.of(ctx.getSource().getServer()).rescanStructures();
                                            ctx.getSource().sendFeedback(() -> Text.literal(
                                                    "§aCleared auto structure regions. They will re-tag as chunks reload."), false);
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("list")
                                        .executes(ctx -> {
                                            var ids = StructureConfig.get().structures;
                                            ctx.getSource().sendFeedback(() -> Text.literal(
                                                    "§6Tagged structures (" + ids.size() + "): §f" + String.join(", ", ids)), false);
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("enable")
                                        .then(CommandManager.argument("id", IdentifierArgumentType.identifier())
                                                .suggests(ALL_STRUCTURE_IDS)
                                                .executes(ctx -> {
                                                    Identifier id = IdentifierArgumentType.getIdentifier(ctx, "id");
                                                    boolean added = StructureConfig.get().add(id.toString());
                                                    ctx.getSource().sendFeedback(() -> Text.literal(added
                                                            ? "§aNow tagging " + id + ". Use rescan + revisit to apply to existing chunks."
                                                            : "§e" + id + " is already tagged."), false);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("disable")
                                        .then(CommandManager.argument("id", IdentifierArgumentType.identifier())
                                                .suggests(ENABLED_STRUCTURE_IDS)
                                                .executes(ctx -> {
                                                    Identifier id = IdentifierArgumentType.getIdentifier(ctx, "id");
                                                    boolean removed = StructureConfig.get().remove(id.toString());
                                                    ctx.getSource().sendFeedback(() -> Text.literal(removed
                                                            ? "§aNo longer tagging " + id + ". Use rescan to remove existing ones."
                                                            : "§e" + id + " wasn't in the list."), false);
                                                    return 1;
                                                })
                                        )
                                )
                        )

                        // /ltregion flags - List flags + state for the region you're standing in
                        .then(CommandManager.literal("flags")
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                    RegionManager.of(player.getServer()).listFlagsAtPlayer(player);
                                    return 1;
                                })
                        )

                        // /ltregion flag <flag> <allow|deny|inherit> - Set a flag on the region you're in
                        .then(CommandManager.literal("flag")
                                .then(CommandManager.argument("flag", StringArgumentType.word())
                                        .suggests(FLAG_SUGGESTIONS)
                                        .then(CommandManager.literal("allow")
                                                .executes(ctx -> setFlag(ctx, Boolean.TRUE)))
                                        .then(CommandManager.literal("deny")
                                                .executes(ctx -> setFlag(ctx, Boolean.FALSE)))
                                        .then(CommandManager.literal("inherit")
                                                .executes(ctx -> setFlag(ctx, null)))
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

    /** Apply a flag value (true=allow, false=deny, null=inherit) to the region the player is in. */
    private static int setFlag(CommandContext<ServerCommandSource> ctx, Boolean value) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        String flag = StringArgumentType.getString(ctx, "flag");
        RegionManager.of(player.getServer()).setFlagAtPlayer(player, flag, value);
        return 1;
    }
}