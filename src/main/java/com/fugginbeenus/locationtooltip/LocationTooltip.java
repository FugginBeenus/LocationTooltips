package com.fugginbeenus.locationtooltip;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import com.fugginbeenus.locationtooltip.registry.LTItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fugginbeenus.locationtooltip.region.SelectionManager;

/**
 * Main server/common initializer for the Location Tooltip mod.
 * - Registers items/creative tab
 * - Wires up server-side networking
 */
public class LocationTooltip implements ModInitializer {
    public static final String MODID = "locationtooltip";
    public static final Logger LOG = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        // Items & creative tab
        LTItems.init();

        // Packets (server receivers, client->server handlers)
        LTPackets.init();

        com.fugginbeenus.locationtooltip.server.RegionTicker.register();


        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(SelectionManager::tickAll);


        LOG.info("[{}] Initialized.", MODID);
    }
}
