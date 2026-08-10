package com.fugginbeenus.locationtooltip.server;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Map;

/**
 * Debug command for monitoring performance and statistics.
 * Usage: /ltdebug stats | /ltdebug reset
 */
public class DebugCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ltdebug")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("stats")
                                .executes(DebugCommands::showStats))
                        .then(Commands.literal("reset")
                                .executes(DebugCommands::resetStats))
        );
    }

    private static int showStats(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var server = source.getServer();

        try {
            // Get RegionManager stats
            RegionManager mgr = RegionManager.of(server);
            Map<String, Object> regionStats = mgr.getPerformanceStats();

            // Get RegionTicker stats
            Map<String, Object> tickerStats = RegionTicker.getStats();

            // Display header
            source.sendSuccess(() ->
                    Component.literal("=== Location Tooltip Performance Stats ===")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

            // Region stats
            source.sendSuccess(() ->
                    Component.literal("\n[Region Manager]").withStyle(ChatFormatting.YELLOW), false);

            source.sendSuccess(() ->
                    Component.literal("  Total Regions: " + regionStats.get("total_regions"))
                            .withStyle(ChatFormatting.WHITE), false);

            source.sendSuccess(() ->
                    Component.literal("  Dimensions: " + regionStats.get("dimensions"))
                            .withStyle(ChatFormatting.WHITE), false);

            if (regionStats.containsKey("avg_regions_per_dim")) {
                source.sendSuccess(() ->
                        Component.literal(String.format("  Avg Regions/Dim: %.1f",
                                        regionStats.get("avg_regions_per_dim")))
                                .withStyle(ChatFormatting.WHITE), false);
            }

            source.sendSuccess(() ->
                    Component.literal("  Indexed Chunks: " + regionStats.get("indexed_chunks"))
                            .withStyle(ChatFormatting.WHITE), false);

            if (regionStats.containsKey("avg_regions_per_chunk")) {
                source.sendSuccess(() ->
                        Component.literal(String.format("  Avg Regions/Chunk: %.2f",
                                        regionStats.get("avg_regions_per_chunk")))
                                .withStyle(ChatFormatting.WHITE), false);
            }

            // Lookup performance
            if (regionStats.containsKey("lookup_count")) {
                long lookups = (long) regionStats.get("lookup_count");
                source.sendSuccess(() ->
                        Component.literal("  Total Lookups: " + lookups)
                                .withStyle(ChatFormatting.WHITE), false);

                if (lookups > 0 && regionStats.containsKey("avg_lookup_micros")) {
                    double avgMicros = (double) regionStats.get("avg_lookup_micros");
                    ChatFormatting color = avgMicros < 10 ? ChatFormatting.GREEN :
                            avgMicros < 50 ? ChatFormatting.YELLOW : ChatFormatting.RED;

                    source.sendSuccess(() ->
                            Component.literal(String.format("  Avg Lookup Time: %.2f µs", avgMicros))
                                    .withStyle(color), false);
                }
            }

            // Ticker stats
            source.sendSuccess(() ->
                    Component.literal("\n[Region Ticker]").withStyle(ChatFormatting.YELLOW), false);

            source.sendSuccess(() ->
                    Component.literal("  Tracked Players: " + tickerStats.get("tracked_players"))
                            .withStyle(ChatFormatting.WHITE), false);

            source.sendSuccess(() ->
                    Component.literal("  Pending Tasks: " + tickerStats.get("pending_tasks"))
                            .withStyle(ChatFormatting.WHITE), false);

            source.sendSuccess(() ->
                    Component.literal("  Check Interval: " + tickerStats.get("check_interval_ticks") + " ticks")
                            .withStyle(ChatFormatting.WHITE), false);

            source.sendSuccess(() ->
                    Component.literal(String.format("  Min Movement: %.1f blocks",
                                    tickerStats.get("min_movement_blocks")))
                            .withStyle(ChatFormatting.WHITE), false);

            // Performance assessment
            source.sendSuccess(() ->
                    Component.literal("\n[Performance Assessment]").withStyle(ChatFormatting.YELLOW), false);

            if (regionStats.containsKey("avg_lookup_micros")) {
                double avgMicros = (double) regionStats.get("avg_lookup_micros");
                String assessment;
                ChatFormatting color;

                if (avgMicros < 10) {
                    assessment = "Excellent - No optimization needed";
                    color = ChatFormatting.GREEN;
                } else if (avgMicros < 50) {
                    assessment = "Good - Minor optimization possible";
                    color = ChatFormatting.YELLOW;
                } else if (avgMicros < 200) {
                    assessment = "Fair - Consider optimization";
                    color = ChatFormatting.GOLD;
                } else {
                    assessment = "Poor - Optimization recommended";
                    color = ChatFormatting.RED;
                }

                source.sendSuccess(() ->
                        Component.literal("  " + assessment).withStyle(color), false);
            }

            // Memory estimate
            int totalRegions = (int) regionStats.get("total_regions");
            long estimatedKB = (totalRegions * 200L) / 1024; // ~200 bytes per region
            source.sendSuccess(() ->
                    Component.literal(String.format("  Est. Memory: ~%d KB", estimatedKB))
                            .withStyle(ChatFormatting.WHITE), false);

            source.sendSuccess(() ->
                    Component.literal("\nUse /ltdebug reset to reset counters")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);

            return 1;

        } catch (Exception e) {
            source.sendFailure(Component.literal("Error gathering stats: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int resetStats(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var server = source.getServer();

        try {
            RegionManager mgr = RegionManager.of(server);
            mgr.resetStats();

            source.sendSuccess(() ->
                    Component.literal("Performance counters reset!")
                            .withStyle(ChatFormatting.GREEN), false);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error resetting stats: " + e.getMessage()));
            return 0;
        }
    }
}