package com.fugginbeenus.locationtooltip.server;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/** Sends HUD updates when a player enters a different region + tiny delayed task scheduler. */
public final class RegionTicker {
    private static final Map<UUID, String> LAST = new HashMap<>();
    private RegionTicker(){}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(RegionTicker::onEndServerTick);
    }

    private static void onEndServerTick(MinecraftServer server) {
        RegionManager mgr = RegionManager.of(server);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            var dim = p.getWorld().getRegistryKey().getValue();
            var pos = p.getBlockPos();

            String name = mgr.currentRegionName(dim, pos);
            String prev = LAST.put(p.getUuid(), name);
            if (prev == null || !prev.equals(name)) {
                LTPackets.sendRegionUpdate(p, name);
            }
        }

        // Run any due delayed tasks
        runDue(server);
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