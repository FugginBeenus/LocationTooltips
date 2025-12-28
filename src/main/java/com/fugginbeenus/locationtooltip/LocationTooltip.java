package com.fugginbeenus.locationtooltip;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import com.fugginbeenus.locationtooltip.registry.LTItems;
import com.fugginbeenus.locationtooltip.server.RegionTicker;
import com.fugginbeenus.locationtooltip.server.DebugCommands;
import com.fugginbeenus.locationtooltip.region.SelectionManager;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.RegionProtection;

public final class LocationTooltip implements ModInitializer {
    public static final String MOD_ID = "locationtooltip";
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("locationtooltip");

    @Override
    public void onInitialize() {
        // COMMON/SERVER-SAFE ONLY. No client classes here.
        LTItems.init();  // Initialize items (wand, compass)
        LTPackets.register();
        RegionTicker.register();
        SelectionManager.registerServerTicker();
        RegionProtection.register();  // NEW: Register protection handlers

        // OPTIMIZATION: Register cleanup events to prevent memory leaks
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            RegionTicker.onPlayerDisconnect(handler.player.getUuid());
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            RegionManager.cleanup(server);
        });

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DebugCommands.register(dispatcher);
            com.fugginbeenus.locationtooltip.server.RegionCommands.register(dispatcher, registryAccess);
        });

        LOG.info("[LT] onInitialize() complete - optimizations active + region protection enabled");
    }
}