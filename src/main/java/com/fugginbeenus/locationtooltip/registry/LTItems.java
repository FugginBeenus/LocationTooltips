package com.fugginbeenus.locationtooltip.registry;

import com.fugginbeenus.locationtooltip.item.AdminCompassItem;
import com.fugginbeenus.locationtooltip.item.RegionWandItem;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

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

    public static final Item REGION_WAND = Registry.register(
            BuiltInRegistries.ITEM, LTId.of(MODID, "region_wand"),
            new RegionWandItem(settings("region_wand"))
    );

    public static final Item ADMIN_COMPASS = Registry.register(
            BuiltInRegistries.ITEM, LTId.of(MODID, "admin_compass"),
            new AdminCompassItem(settings("admin_compass"))
    );

    @SuppressWarnings("unused")
    private static final net.minecraft.world.item.CreativeModeTab LT_GROUP = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB, LTId.of(MODID, "main"),
            //? if >=26.1 {
            /*FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ADMIN_COMPASS))
                    .title(Component.literal("Location Tooltip"))
                    .build()
            *///?} else {
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ADMIN_COMPASS))
                    .title(Component.literal("Location Tooltip"))
                    .displayItems((ctx, entries) -> {
                        entries.accept(ADMIN_COMPASS);
                        entries.accept(REGION_WAND);
                    })
                    .build()
            //?}
    );

    //? if >=26.1 {
    /*static {
        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.modifyOutputEvent(
                net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB,
                        LTId.of(MODID, "main"))
        ).register(out -> {
            out.accept(ADMIN_COMPASS);
            out.accept(REGION_WAND);
        });
    }
    *///?}

    private LTItems() {}

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

    public static void init() {
    }
}
