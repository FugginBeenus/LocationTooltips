package com.fugginbeenus.locationtooltip;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import com.fugginbeenus.locationtooltip.region.SelectionManager;
import com.fugginbeenus.locationtooltip.registry.LTItems;
import net.fabricmc.api.ModInitializer;
import com.fugginbeenus.locationtooltip.server.RegionTicker;


public final class LocationTooltip implements ModInitializer {
    public static final String MOD_ID = "locationtooltip";

    @Override
    public void onInitialize() {
        LTPackets.register();
        SelectionManager.registerServerTicker();
        LTItems.init();
        RegionTicker.register();
        com.fugginbeenus.locationtooltip.server.RegionTicker.register();
    }
}
