package com.fugginbeenus.locationtooltip;

import net.fabricmc.api.ModInitializer;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import com.fugginbeenus.locationtooltip.registry.LTItems;
import com.fugginbeenus.locationtooltip.server.RegionTicker;
import com.fugginbeenus.locationtooltip.region.SelectionManager;

public final class LocationTooltip implements ModInitializer {
    public static final String MOD_ID = "locationtooltip";

    @Override
    public void onInitialize() {
        // COMMON/SERVER-SAFE ONLY. No client classes here.
        LTPackets.register();
        RegionTicker.register();
        SelectionManager.registerServerTicker();
        LOG.info("[LT] onInitialize() start");
    }
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("locationtooltip");
}
