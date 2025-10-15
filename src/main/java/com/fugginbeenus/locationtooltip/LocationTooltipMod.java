package com.fugginbeenus.locationtooltip;

import com.fugginbeenus.locationtooltip.data.RegionManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/** Common init: register items/blocks only. */
public class LocationTooltipMod implements ModInitializer {
    public static final String MODID = "locationtooltip";

    public static final RegionWandItem REGION_WAND =
            new RegionWandItem(new net.minecraft.item.Item.Settings().maxCount(1));

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, new Identifier(MODID, "region_wand"), REGION_WAND);
        RegionManager.load();
    }
}
