package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SelectionManager {

    private static final class Selection {
        BlockPos a, b;
        Selection(BlockPos a) { this.a = a; }
        void setB(BlockPos b) { this.b = b; }
        boolean ready() { return a != null && b != null; }
    }

    private static final Map<UUID, Selection> CURRENT = new HashMap<>();

    private SelectionManager() {}

    public static void setFirst(ServerPlayerEntity p, BlockPos a)  {
        CURRENT.computeIfAbsent(p.getUuid(), id -> new Selection(a)).a = a;
    }

    public static void setSecond(ServerPlayerEntity p, BlockPos b) {
        CURRENT.computeIfAbsent(p.getUuid(), id -> new Selection(null)).b = b;
    }

    public static BlockPos getFirst(ServerPlayerEntity p) {
        var s = CURRENT.get(p.getUuid());
        return s == null ? null : s.a;
    }

    public static boolean hasBoth(ServerPlayerEntity p) {
        var s = CURRENT.get(p.getUuid());
        return s != null && s.ready();
    }

    public static void clear(ServerPlayerEntity p) {
        CURRENT.remove(p.getUuid());
        LTPackets.sendSelectionClear(p);
    }

    public static void openNamingScreen(ServerPlayerEntity p) {
        var s = CURRENT.get(p.getUuid());
        if (s == null || !s.ready()) return;
        LTPackets.openName(p, s.a, s.b);
    }

    public static void registerServerTicker() {
        ServerTickEvents.END_SERVER_TICK.register(SelectionManager::tick);
    }

    private static void tick(MinecraftServer server) {
        if (CURRENT.isEmpty()) return;

        for (var entry : CURRENT.entrySet()) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player == null) continue;
            var sel = entry.getValue();
            if (!sel.ready()) continue;

            LTPackets.sendSelectionUpdate(player, sel.a, sel.b);
        }
    }
}