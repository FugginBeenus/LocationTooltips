package com.fugginbeenus.locationtooltip.server;

import com.fugginbeenus.locationtooltip.region.Region;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Sends HUD updates when a player enters a different region (innermost only). */
public final class RegionTicker {

    private static final Map<UUID, String> LAST = new HashMap<>();

    private RegionTicker() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(RegionTicker::onEndServerTick);
    }

    private static void onEndServerTick(MinecraftServer server) {
        RegionManager mgr = RegionManager.of(server);

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            var dim = p.getWorld().getRegistryKey().getValue();
            var pos = p.getBlockPos();

            // Innermost region only
            Region current = mgr.regionAt(dim, pos);
            String title = (current != null) ? current.name : "Wilderness";

            String prev = LAST.put(p.getUuid(), title);
            if (prev == null || !prev.equals(title)) {
                com.fugginbeenus.locationtooltip.net.LTPackets.sendRegionUpdate(p, title);
            }
        }
    }
}
