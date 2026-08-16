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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

public class RegionCommands {
    private static final SuggestionProvider<CommandSourceStack> FLAG_SUGGESTIONS = (ctx, builder) -> {
        for (RegionFlag f : RegionFlags.all()) builder.suggest(f.id);
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> ALL_STRUCTURE_IDS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(
                    ctx.getSource().getServer().registryAccess().registryOrThrow(Registries.STRUCTURE).keySet(),
                    builder);

    private static final SuggestionProvider<CommandSourceStack> ENABLED_STRUCTURE_IDS = (ctx, builder) -> {
        for (String s : StructureConfig.get().structures) builder.suggest(s);
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
                Commands.literal("ltregion")
                        .requires(com.fugginbeenus.locationtooltip.util.LTPerms::isAdmin)

                        .then(Commands.literal("pos1")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    ServerPlayer player = source.getPlayerOrException();
                                    BlockPos pos = player.blockPosition();

                                    com.fugginbeenus.locationtooltip.region.SelectionManager.setFirst(player, pos);
                                    com.fugginbeenus.locationtooltip.util.LTChat.tell(player, Component.literal("§aFirst corner set at " + pos.toShortString()), false);

                                    return 1;
                                })
                        )

                        .then(Commands.literal("pos2")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    ServerPlayer player = source.getPlayerOrException();
                                    BlockPos pos = player.blockPosition();

                                    com.fugginbeenus.locationtooltip.region.SelectionManager.setSecond(player, pos);
                                    com.fugginbeenus.locationtooltip.util.LTChat.tell(player, Component.literal("§aSecond corner set at " + pos.toShortString()), false);

                                    if (com.fugginbeenus.locationtooltip.region.SelectionManager.hasBoth(player)) {
                                        com.fugginbeenus.locationtooltip.util.LTChat.tell(player, Component.literal("§6Both corners set! Use §e/ltregion create <name> §6to create the region."), false);
                                    }

                                    return 1;
                                })
                        )

                        .then(Commands.literal("create")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            ServerPlayer player = source.getPlayerOrException();
                                            String name = StringArgumentType.getString(ctx, "name");

                                            if (!com.fugginbeenus.locationtooltip.region.SelectionManager.hasBoth(player)) {
                                                com.fugginbeenus.locationtooltip.util.LTChat.tell(player, Component.literal("§cYou must set both corners first! Use /ltregion pos1 and /ltregion pos2"), false);
                                                return 0;
                                            }

                                            BlockPos a = com.fugginbeenus.locationtooltip.region.SelectionManager.getFirst(player);
                                            BlockPos b = com.fugginbeenus.locationtooltip.region.SelectionManager.getSecond(player);

                                            RegionManager.of(player.level().getServer()).createRegion(player, name, a, b, java.util.Map.of());

                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("createhere")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 1000))
                                                .executes(ctx -> {
                                                    CommandSourceStack source = ctx.getSource();
                                                    ServerPlayer player = source.getPlayerOrException();
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    int radius = IntegerArgumentType.getInteger(ctx, "radius");

                                                    BlockPos center = player.blockPosition();
                                                    BlockPos a = center.offset(-radius, -10, -radius);
                                                    BlockPos b = center.offset(radius, 10, radius);

                                                    RegionManager.of(player.level().getServer()).createRegion(player, name, a, b, java.util.Map.of());

                                                    return 1;
                                                })
                                        )
                                )
                        )

                        .then(Commands.literal("createbox")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("x1", IntegerArgumentType.integer())
                                                .then(Commands.argument("y1", IntegerArgumentType.integer())
                                                        .then(Commands.argument("z1", IntegerArgumentType.integer())
                                                                .then(Commands.argument("x2", IntegerArgumentType.integer())
                                                                        .then(Commands.argument("y2", IntegerArgumentType.integer())
                                                                                .then(Commands.argument("z2", IntegerArgumentType.integer())
                                                                                        .executes(ctx -> {
                                                                                            CommandSourceStack source = ctx.getSource();
                                                                                            ServerPlayer player = source.getPlayerOrException();
                                                                                            String name = StringArgumentType.getString(ctx, "name");

                                                                                            int x1 = IntegerArgumentType.getInteger(ctx, "x1");
                                                                                            int y1 = IntegerArgumentType.getInteger(ctx, "y1");
                                                                                            int z1 = IntegerArgumentType.getInteger(ctx, "z1");
                                                                                            int x2 = IntegerArgumentType.getInteger(ctx, "x2");
                                                                                            int y2 = IntegerArgumentType.getInteger(ctx, "y2");
                                                                                            int z2 = IntegerArgumentType.getInteger(ctx, "z2");

                                                                                            BlockPos a = new BlockPos(x1, y1, z1);
                                                                                            BlockPos b = new BlockPos(x2, y2, z2);

                                                                                            RegionManager.of(player.level().getServer()).createRegion(player, name, a, b, java.util.Map.of());

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

                        .then(Commands.literal("structures")
                                .then(Commands.literal("status")
                                        .executes(ctx -> {
                                            CommandSourceStack src = ctx.getSource();
                                            int count = RegionManager.of(src.getServer()).countBySource(RegionSource.STRUCTURE);
                                            boolean on = StructureRegionTagger.isEnabled();
                                            StructureConfig cfg = StructureConfig.get();
                                            src.sendSuccess(() -> Component.literal("Structure tagging: " + (on ? "§aON" : "§cOFF")
                                                    + "§r | auto-tag modded: " + (cfg.tagModdedStructures ? "§aON" : "§cOFF")
                                                    + "§r | vanilla listed: " + cfg.structures.size()
                                                    + " | excluded: " + cfg.denied.size()
                                                    + " | auto regions: " + count), false);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("on")
                                        .executes(ctx -> {
                                            StructureRegionTagger.setEnabled(true);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§aStructure tagging enabled. New chunks are tagged as they load."), false);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("off")
                                        .executes(ctx -> {
                                            StructureRegionTagger.setEnabled(false);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§eStructure tagging disabled. Existing structure regions are kept (use rescan to clear)."), false);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("modded")
                                        .then(Commands.literal("on")
                                                .executes(ctx -> {
                                                    StructureConfig.get().setTagModdedStructures(true);
                                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                                            "§aAuto-tagging modded structures enabled. Use rescan + revisit to apply to existing chunks."), false);
                                                    return 1;
                                                })
                                        )
                                        .then(Commands.literal("off")
                                                .executes(ctx -> {
                                                    StructureConfig.get().setTagModdedStructures(false);
                                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                                            "§eAuto-tagging modded structures disabled. Only listed structures will be tagged."), false);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(Commands.literal("rescan")
                                        .executes(ctx -> {
                                            RegionManager.of(ctx.getSource().getServer()).rescanStructures();
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§aCleared auto structure regions. They will re-tag as chunks reload."), false);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("list")
                                        .executes(ctx -> {
                                            StructureConfig cfg = StructureConfig.get();
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§6Vanilla tagged (" + cfg.structures.size() + "): §f" + String.join(", ", cfg.structures)), false);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§6Auto-tag modded: §f" + (cfg.tagModdedStructures
                                                            ? "on (any non-minecraft structure)" : "off")), false);
                                            if (!cfg.denied.isEmpty()) {
                                                ctx.getSource().sendSuccess(() -> Component.literal(
                                                        "§6Excluded (" + cfg.denied.size() + "): §f" + String.join(", ", cfg.denied)), false);
                                            }
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("enable")
                                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                                .suggests(ALL_STRUCTURE_IDS)
                                                .executes(ctx -> {
                                                    ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
                                                    boolean added = StructureConfig.get().allow(id.toString());
                                                    ctx.getSource().sendSuccess(() -> Component.literal(added
                                                            ? "§aNow tagging " + id + ". Use rescan + revisit to apply to existing chunks."
                                                            : "§e" + id + " is already tagged."), false);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(Commands.literal("disable")
                                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                                .suggests(ENABLED_STRUCTURE_IDS)
                                                .executes(ctx -> {
                                                    ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
                                                    boolean removed = StructureConfig.get().deny(id.toString());
                                                    ctx.getSource().sendSuccess(() -> Component.literal(removed
                                                            ? "§aNo longer tagging " + id + ". Use rescan to remove existing ones."
                                                            : "§e" + id + " was already excluded."), false);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(Commands.literal("villagenames")
                                        .executes(ctx -> {
                                            StructureConfig cfg = StructureConfig.get();
                                            ctx.getSource().sendSuccess(() -> Component.literal("Automatic village names: "
                                                    + (cfg.nameVillages ? "§aON" : "§cOFF")
                                                    + "§r | players may name villages: "
                                                    + (cfg.allowPlayerVillageNaming ? "§aON" : "§cOFF")), false);
                                            return 1;
                                        })
                                        .then(Commands.literal("on")
                                                .executes(ctx -> {
                                                    StructureConfig.get().setNameVillages(true);
                                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                                            "§aVillages will be given a place name when nothing else has named them."), false);
                                                    return 1;
                                                })
                                        )
                                        .then(Commands.literal("off")
                                                .executes(ctx -> {
                                                    StructureConfig.get().setNameVillages(false);
                                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                                            "§eVillages will keep their plain structure name."), false);
                                                    return 1;
                                                })
                                        )
                                        .then(Commands.literal("players")
                                                .then(Commands.literal("on")
                                                        .executes(ctx -> {
                                                            StructureConfig.get().setAllowPlayerVillageNaming(true);
                                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                                    "§aPlayers can now name a village they are standing in with /ltname. "
                                                                            + "This does not give them any control over its flags."), false);
                                                            return 1;
                                                        })
                                                )
                                                .then(Commands.literal("off")
                                                        .executes(ctx -> {
                                                            StructureConfig.get().setAllowPlayerVillageNaming(false);
                                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                                    "§ePlayers can no longer name villages. Names already set are kept."), false);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )

                        .then(Commands.literal("flags")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    RegionManager.of(player.level().getServer()).listFlagsAtPlayer(player);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("flag")
                                .then(Commands.argument("flag", StringArgumentType.word())
                                        .suggests(FLAG_SUGGESTIONS)
                                        .then(Commands.literal("allow")
                                                .executes(ctx -> setFlag(ctx, Boolean.TRUE)))
                                        .then(Commands.literal("deny")
                                                .executes(ctx -> setFlag(ctx, Boolean.FALSE)))
                                        .then(Commands.literal("inherit")
                                                .executes(ctx -> setFlag(ctx, null)))
                                )
                        )

                        .then(Commands.literal("clear")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    ServerPlayer player = source.getPlayerOrException();

                                    com.fugginbeenus.locationtooltip.region.SelectionManager.clear(player);
                                    com.fugginbeenus.locationtooltip.util.LTChat.tell(player, Component.literal("§aSelection cleared."), false);

                                    return 1;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("ltname")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String name = StringArgumentType.getString(ctx, "name");
                                    return RegionManager.of(player.level().getServer()).nameVillageAt(player, name) ? 1 : 0;
                                })
                        )
        );
    }

    private static int setFlag(CommandContext<CommandSourceStack> ctx, Boolean value) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String flag = StringArgumentType.getString(ctx, "flag");
        RegionManager.of(player.level().getServer()).setFlagAtPlayer(player, flag, value);
        return 1;
    }
}
