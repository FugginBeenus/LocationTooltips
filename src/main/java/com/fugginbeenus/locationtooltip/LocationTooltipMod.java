package com.fugginbeenus.locationtooltip;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class LocationTooltipMod implements ModInitializer {
    public static final String MODID = "locationtooltip";
    public static Item REGION_WAND;

    @Override
    public void onInitialize() {
        REGION_WAND = Registry.register(
                Registries.ITEM,
                new Identifier(MODID, "region_wand"),
                new Item(new Item.Settings())
        );
        // No creative tab registration (keeps 1.20.1 happy). Use /give to obtain the item.
    }
}
