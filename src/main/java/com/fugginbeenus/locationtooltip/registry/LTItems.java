package com.fugginbeenus.locationtooltip.registry;

import com.fugginbeenus.locationtooltip.item.AdminCompassItem;
import com.fugginbeenus.locationtooltip.item.RegionWandItem;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
// If you later want to also add to vanilla tabs for *your* version, you can
// re-enable these imports and code, but keeping it version-agnostic for now.
// import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
// import net.minecraft.item.ItemGroups;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

public final class LTItems {

    private static final String MODID = "locationtooltip";

    // 1) Items
    public static final Item REGION_WAND = Registry.register(
            Registries.ITEM, new Identifier(MODID, "region_wand"),
            new RegionWandItem(new FabricItemSettings().maxCount(1))
    );

    public static final Item ADMIN_COMPASS = Registry.register(
            Registries.ITEM, new Identifier(MODID, "admin_compass"),
            new AdminCompassItem(new FabricItemSettings().maxCount(1))
    );

    // 2) Our own always-present creative tab (no version-specific constants)
    //    Shows both items so you can grab them even if vanilla tabs change.
    @SuppressWarnings("unused")
    private static final net.minecraft.item.ItemGroup LT_GROUP = Registry.register(
            Registries.ITEM_GROUP, new Identifier(MODID, "main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ADMIN_COMPASS))
                    .displayName(Text.literal("Location Tooltip"))
                    .entries((ctx, entries) -> {
                        entries.add(ADMIN_COMPASS);
                        entries.add(REGION_WAND);
                    })
                    .build()
    );

    private LTItems() {}

    // Call this from your main mod initializer (onInitialize). Nothing else needed here.
    public static void init() {
        // If you later want to also add items to vanilla tabs for a specific MC version,
        // put that version-locked code here so this class still compiles everywhere.
        // Example (uncomment & adjust for your version):
        // ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
        //     entries.add(ADMIN_COMPASS);
        //     entries.add(REGION_WAND);
        // });
    }
}
