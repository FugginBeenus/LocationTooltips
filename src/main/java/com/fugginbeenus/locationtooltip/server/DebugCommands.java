package com.fugginbeenus.locationtooltip.server;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

/**
 * Debug command for monitoring performance and statistics.
 * Usage: /ltdebug stats | /ltdebug reset
 */
public class DebugCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("ltdebug")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("stats")
                                .executes(DebugCommands::showStats))
                        .then(CommandManager.literal("reset")
                                .executes(DebugCommands::resetStats))
        );
    }

    private static int showStats(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        var server = source.getServer();

        try {
            // Get RegionManager stats
            RegionManager mgr = RegionManager.of(server);
            Map<String, Object> regionStats = mgr.getPerformanceStats();

            // Get RegionTicker stats
            Map<String, Object> tickerStats = RegionTicker.getStats();

            // Display header
            source.sendFeedback(() ->
                    Text.literal("=== Location Tooltip Performance Stats ===")
                            .formatted(Formatting.GOLD, Formatting.BOLD), false);

            // Region stats
            source.sendFeedback(() ->
                    Text.literal("\n[Region Manager]").formatted(Formatting.YELLOW), false);

            source.sendFeedback(() ->
                    Text.literal("  Total Regions: " + regionStats.get("total_regions"))
                            .formatted(Formatting.WHITE), false);

            source.sendFeedback(() ->
                    Text.literal("  Dimensions: " + regionStats.get("dimensions"))
                            .formatted(Formatting.WHITE), false);

            if (regionStats.containsKey("avg_regions_per_dim")) {
                source.sendFeedback(() ->
                        Text.literal(String.format("  Avg Regions/Dim: %.1f",
                                        regionStats.get("avg_regions_per_dim")))
                                .formatted(Formatting.WHITE), false);
            }

            source.sendFeedback(() ->
                    Text.literal("  Indexed Chunks: " + regionStats.get("indexed_chunks"))
                            .formatted(Formatting.WHITE), false);

            if (regionStats.containsKey("avg_regions_per_chunk")) {
                source.sendFeedback(() ->
                        Text.literal(String.format("  Avg Regions/Chunk: %.2f",
                                        regionStats.get("avg_regions_per_chunk")))
                                .formatted(Formatting.WHITE), false);
            }

            // Lookup performance
            if (regionStats.containsKey("lookup_count")) {
                long lookups = (long) regionStats.get("lookup_count");
                source.sendFeedback(() ->
                        Text.literal("  Total Lookups: " + lookups)
                                .formatted(Formatting.WHITE), false);

                if (lookups > 0 && regionStats.containsKey("avg_lookup_micros")) {
                    double avgMicros = (double) regionStats.get("avg_lookup_micros");
                    Formatting color = avgMicros < 10 ? Formatting.GREEN :
                            avgMicros < 50 ? Formatting.YELLOW : Formatting.RED;

                    source.sendFeedback(() ->
                            Text.literal(String.format("  Avg Lookup Time: %.2f µs", avgMicros))
                                    .formatted(color), false);
                }
            }

            // Ticker stats
            source.sendFeedback(() ->
                    Text.literal("\n[Region Ticker]").formatted(Formatting.YELLOW), false);

            source.sendFeedback(() ->
                    Text.literal("  Tracked Players: " + tickerStats.get("tracked_players"))
                            .formatted(Formatting.WHITE), false);

            source.sendFeedback(() ->
                    Text.literal("  Pending Tasks: " + tickerStats.get("pending_tasks"))
                            .formatted(Formatting.WHITE), false);

            source.sendFeedback(() ->
                    Text.literal("  Check Interval: " + tickerStats.get("check_interval_ticks") + " ticks")
                            .formatted(Formatting.WHITE), false);

            source.sendFeedback(() ->
                    Text.literal(String.format("  Min Movement: %.1f blocks",
                                    tickerStats.get("min_movement_blocks")))
                            .formatted(Formatting.WHITE), false);

            // Performance assessment
            source.sendFeedback(() ->
                    Text.literal("\n[Performance Assessment]").formatted(Formatting.YELLOW), false);

            if (regionStats.containsKey("avg_lookup_micros")) {
                double avgMicros = (double) regionStats.get("avg_lookup_micros");
                String assessment;
                Formatting color;

                if (avgMicros < 10) {
                    assessment = "Excellent - No optimization needed";
                    color = Formatting.GREEN;
                } else if (avgMicros < 50) {
                    assessment = "Good - Minor optimization possible";
                    color = Formatting.YELLOW;
                } else if (avgMicros < 200) {
                    assessment = "Fair - Consider optimization";
                    color = Formatting.GOLD;
                } else {
                    assessment = "Poor - Optimization recommended";
                    color = Formatting.RED;
                }

                source.sendFeedback(() ->
                        Text.literal("  " + assessment).formatted(color), false);
            }

            // Memory estimate
            int totalRegions = (int) regionStats.get("total_regions");
            long estimatedKB = (totalRegions * 200L) / 1024; // ~200 bytes per region
            source.sendFeedback(() ->
                    Text.literal(String.format("  Est. Memory: ~%d KB", estimatedKB))
                            .formatted(Formatting.WHITE), false);

            source.sendFeedback(() ->
                    Text.literal("\nUse /ltdebug reset to reset counters")
                            .formatted(Formatting.GRAY, Formatting.ITALIC), false);

            return 1;

        } catch (Exception e) {
            source.sendError(Text.literal("Error gathering stats: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int resetStats(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        var server = source.getServer();

        try {
            RegionManager mgr = RegionManager.of(server);
            mgr.resetStats();

            source.sendFeedback(() ->
                    Text.literal("Performance counters reset!")
                            .formatted(Formatting.GREEN), false);

            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("Error resetting stats: " + e.getMessage()));
            return 0;
        }
    }
}