package com.fugginbeenus.locationtooltip.registry;

import com.fugginbeenus.locationtooltip.LocationTooltip;
import com.fugginbeenus.locationtooltip.item.AdminCompassItem;
import com.fugginbeenus.locationtooltip.item.RegionWandItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Registers mod items and creative tab.
 */
public class LTItems {

    public static Item REGION_WAND;
    public static Item ADMIN_COMPASS;

    public static void init() {
        REGION_WAND = register("region_wand",
                new RegionWandItem(new Item.Settings().maxCount(1)));
        ADMIN_COMPASS = register("admin_compass",
                new AdminCompassItem(new Item.Settings().maxCount(1)));

        // Custom creative tab
        Registry.register(Registries.ITEM_GROUP, id("group"), FabricItemGroup.builder()
                .icon(() -> new ItemStack(REGION_WAND))
                .displayName(Text.literal("Location Tooltip"))
                .entries((displayContext, entries) -> {
                    entries.add(REGION_WAND);
                    entries.add(ADMIN_COMPASS);
                })
                .build());

        LocationTooltip.LOG.info("[Items] Registered Region Wand + Admin Compass.");
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, id(name), item);
    }

    private static Identifier id(String path) {
        return new Identifier(LocationTooltip.MODID, path);
    }

    private LTItems() {}
}
