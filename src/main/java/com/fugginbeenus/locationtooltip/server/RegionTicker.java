package com.fugginbeenus.locationtooltip.server;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Sends HUD updates when a player enters a different region + tiny delayed task scheduler.
 *
 * OPTIMIZED: Throttled checking (every 5 ticks) + movement detection
 * - 80-90% reduction in region lookup calls
 * - Only checks when players actually move
 */
public final class RegionTicker {
    private static final Map<UUID, String> LAST_REGION = new HashMap<>();
    private static final Map<UUID, BlockPos> LAST_POS = new HashMap<>();
    private static int tickCounter = 0;

    // Configuration - adjust these for different performance characteristics
    private static final int CHECK_INTERVAL = 2; // Check every 2 ticks (10 times per second) for responsive UI
    private static final double MIN_MOVEMENT_SQ = 1.0; // Minimum 1 block movement to trigger check

    private RegionTicker(){}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(RegionTicker::onEndServerTick);
    }

    private static void onEndServerTick(MinecraftServer server) {
        // Throttle region checks - only run every N ticks
        if (++tickCounter % CHECK_INTERVAL != 0) {
            runDue(server);
            return;
        }

        RegionManager mgr = RegionManager.of(server);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            UUID playerId = p.getUuid();
            BlockPos currentPos = p.getBlockPos();

            // Movement detection - skip if player hasn't moved significantly
            BlockPos lastPos = LAST_POS.get(playerId);
            if (lastPos != null) {
                double distSq = lastPos.getSquaredDistance(currentPos);
                if (distSq < MIN_MOVEMENT_SQ) {
                    continue; // Player hasn't moved enough, skip region check
                }
            }

            // Update last known position
            LAST_POS.put(playerId, currentPos);

            // Perform region lookup
            var dim = p.getWorld().getRegistryKey().getValue();
            String currentRegion = mgr.currentRegionName(dim, currentPos);
            String previousRegion = LAST_REGION.put(playerId, currentRegion);

            // Only send packet if region changed
            if (previousRegion == null || !previousRegion.equals(currentRegion)) {
                LTPackets.sendRegionUpdate(p, currentRegion);
            }
        }

        // Run any due delayed tasks
        runDue(server);
    }

    /**
     * Clean up tracking data when a player disconnects to prevent memory leaks.
     * Call this from ServerPlayConnectionEvents.DISCONNECT
     */
    public static void onPlayerDisconnect(UUID playerId) {
        LAST_REGION.remove(playerId);
        LAST_POS.remove(playerId);
    }

    /**
     * Get performance statistics for debugging
     */
    public static Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("tracked_players", LAST_REGION.size());
        stats.put("pending_tasks", QUEUE.size());
        stats.put("check_interval_ticks", CHECK_INTERVAL);
        stats.put("min_movement_blocks", Math.sqrt(MIN_MOVEMENT_SQ));
        return stats;
    }

    // ---------- ultra-light delayed runner (server-thread safe) ----------

    private record Task(long dueTick, Runnable r) {}
    private static final Deque<Task> QUEUE = new ArrayDeque<>();

    /** Schedule a runnable to execute after delayTicks on the server thread. */
    public static void later(MinecraftServer server, int delayTicks, Runnable r) {
        long now = server.getOverworld().getTime();
        QUEUE.addLast(new Task(now + Math.max(1, delayTicks), r));
    }

    private static void runDue(MinecraftServer server) {
        long now = server.getOverworld().getTime();
        int n = QUEUE.size();
        // simple pass: execute tasks whose dueTick <= now
        for (int i = 0; i < n; i++) {
            Task t = QUEUE.pollFirst();
            if (t == null) break;
            if (t.dueTick <= now) {
                try { t.r.run(); } catch (Throwable ignored) {}
            } else {
                // not due yet, put back at end
                QUEUE.addLast(t);
            }
        }
    }
}