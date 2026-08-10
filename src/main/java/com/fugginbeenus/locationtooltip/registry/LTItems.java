package com.fugginbeenus.locationtooltip.registry;

import com.fugginbeenus.locationtooltip.item.AdminCompassItem;
import com.fugginbeenus.locationtooltip.item.RegionWandItem;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
// If you later want to also add to vanilla tabs for *your* version, you can
// re-enable these imports and code, but keeping it version-agnostic for now.
// import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import com.fugginbeenus.locationtooltip.util.LTId;
import net.minecraft.world.item.CreativeModeTabs;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class LTItems {

    private static final String MODID = "locationtooltip";

    // 1) Items
    public static final Item REGION_WAND = Registry.register(
            BuiltInRegistries.ITEM, LTId.of(MODID, "region_wand"),
            new RegionWandItem(settings("region_wand"))
    );

    public static final Item ADMIN_COMPASS = Registry.register(
            BuiltInRegistries.ITEM, LTId.of(MODID, "admin_compass"),
            new AdminCompassItem(settings("admin_compass"))
    );

    // 2) Our own always-present creative tab (no version-specific constants)
    //    Shows both items so you can grab them even if vanilla tabs change.
    @SuppressWarnings("unused")
    private static final net.minecraft.world.item.CreativeModeTab LT_GROUP = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB, LTId.of(MODID, "main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ADMIN_COMPASS))
                    .title(Component.literal("Location Tooltip"))
                    .displayItems((ctx, entries) -> {
                        entries.accept(ADMIN_COMPASS);
                        entries.accept(REGION_WAND);
                    })
                    .build()
    );

    private LTItems() {}

    // Fabric folded its item settings into vanilla Item.Properties in 1.21; FabricItemSettings is
    // gone. From 1.21.2 an item also has to know its own registry key before it is constructed.
    private static Item.Properties settings(String path) {
        //? if >=1.21.11 {
        /*return new Item.Properties()
                .setId(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.ITEM, LTId.of(MODID, path)))
                .stacksTo(1);
        *///?} elif >=1.21 {
        /*return new Item.Properties().stacksTo(1);
        *///?} else {
        return new net.fabricmc.fabric.api.item.v1.FabricItemSettings().maxCount(1);
        //?}
    }

    // Call this from your main mod initializer (onInitialize). Nothing else needed here.
    public static void init() {
        // If you later want to also add items to vanilla tabs for a specific MC version,
        // put that version-locked code here so this class still compiles everywhere.
        // Example (uncomment & adjust for your version):
        // ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS).register(entries -> {
        //     entries.accept(ADMIN_COMPASS);
        //     entries.accept(REGION_WAND);
        // });
    }
}
