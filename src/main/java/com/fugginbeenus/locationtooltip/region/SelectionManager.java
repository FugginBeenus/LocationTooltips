package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;

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

    public static void setFirst(ServerPlayer p, BlockPos a)  {
        CURRENT.computeIfAbsent(p.getUUID(), id -> new Selection(a)).a = a;
    }

    public static void setSecond(ServerPlayer p, BlockPos b) {
        CURRENT.computeIfAbsent(p.getUUID(), id -> new Selection(null)).b = b;
    }

    public static BlockPos getFirst(ServerPlayer p) {
        var s = CURRENT.get(p.getUUID());
        return s == null ? null : s.a;
    }

    public static BlockPos getSecond(ServerPlayer p) {
        var s = CURRENT.get(p.getUUID());
        return s == null ? null : s.b;
    }

    public static boolean hasBoth(ServerPlayer p) {
        var s = CURRENT.get(p.getUUID());
        return s != null && s.ready();
    }

    public static void clear(ServerPlayer p) {
        CURRENT.remove(p.getUUID());
        LTPackets.sendSelectionClear(p);
    }

    public static void openNamingScreen(ServerPlayer p) {
        var s = CURRENT.get(p.getUUID());
        if (s == null || !s.ready()) return;
        LTPackets.openName(p, s.a, s.b);
    }

    public static void registerServerTicker() {
        ServerTickEvents.END_SERVER_TICK.register(SelectionManager::tick);
    }

    private static void tick(MinecraftServer server) {
        if (CURRENT.isEmpty()) return;

        for (var entry : CURRENT.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) continue;
            var sel = entry.getValue();
            if (!sel.ready()) continue;

            LTPackets.sendSelectionUpdate(player, sel.a, sel.b);
        }
    }
}
