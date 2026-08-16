package com.fugginbeenus.locationtooltip.server;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;

import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public final class RegionTicker {
    private static final Map<UUID, String> LAST_REGION = new HashMap<>();
    private static final Map<UUID, BlockPos> LAST_POS = new HashMap<>();
    private static int tickCounter = 0;

    private static final int CHECK_INTERVAL = 2;
    private static final double MIN_MOVEMENT_SQ = 1.0;

    private RegionTicker(){}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(RegionTicker::onEndServerTick);
    }

    private static void onEndServerTick(MinecraftServer server) {
        if (++tickCounter % CHECK_INTERVAL != 0) {
            runDue(server);
            return;
        }

        RegionManager mgr = RegionManager.of(server);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            UUID playerId = p.getUUID();
            BlockPos currentPos = p.blockPosition();

            BlockPos lastPos = LAST_POS.get(playerId);
            if (lastPos != null) {
                double distSq = lastPos.distSqr(currentPos);
                if (distSq < MIN_MOVEMENT_SQ) {
                    continue;
                }
            }

            LAST_POS.put(playerId, currentPos);

            var dim = p.level().dimension().location();
            String currentRegion = mgr.currentRegionName(dim, currentPos);
            String previousRegion = LAST_REGION.put(playerId, currentRegion);

            if (previousRegion == null || !previousRegion.equals(currentRegion)) {
                LTPackets.sendRegionUpdate(p, currentRegion);
            }
        }

        runDue(server);
    }

    public static void onPlayerDisconnect(UUID playerId) {
        LAST_REGION.remove(playerId);
        LAST_POS.remove(playerId);
    }

    public static Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("tracked_players", LAST_REGION.size());
        stats.put("pending_tasks", QUEUE.size());
        stats.put("check_interval_ticks", CHECK_INTERVAL);
        stats.put("min_movement_blocks", Math.sqrt(MIN_MOVEMENT_SQ));
        return stats;
    }

    private record Task(long dueTick, Runnable r) {}
    private static final Deque<Task> QUEUE = new ArrayDeque<>();

    public static void later(MinecraftServer server, int delayTicks, Runnable r) {
        long now = server.overworld().getGameTime();
        QUEUE.addLast(new Task(now + Math.max(1, delayTicks), r));
    }

    private static void runDue(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        int n = QUEUE.size();

        for (int i = 0; i < n; i++) {
            Task t = QUEUE.pollFirst();
            if (t == null) break;
            if (t.dueTick <= now) {
                try { t.r.run(); } catch (Throwable ignored) {}
            } else {
                QUEUE.addLast(t);
            }
        }
    }
}
