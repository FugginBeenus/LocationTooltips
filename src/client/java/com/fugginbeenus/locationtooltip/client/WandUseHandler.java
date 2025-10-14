package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.LocationTooltipMod;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

public final class WandUseHandler {
    public static void register() {
        // 1) Right-clicking a BLOCK (sets A/B corners or opens GUI if sneaking)
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!world.isClient()) return ActionResult.PASS;
            if (!player.getStackInHand(hand).isOf(LocationTooltipMod.REGION_WAND)) return ActionResult.PASS;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (player.isSneaking()) {
                if (RegionSelectionClient.hasBothCorners()) {
                    mc.setScreen(new NameRegionScreen(0)); // open naming GUI
                } else {
                    RegionSelectionClient.clear(mc);
                }
                return ActionResult.SUCCESS;
            }

            // Normal right-click: set corner A/B
            RegionSelectionClient.onWandUse(mc, hit.getBlockPos());
            return ActionResult.SUCCESS;
        });

        // 2) Right-clicking the AIR (also opens GUI if sneaking)
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient()) return TypedActionResult.pass(ItemStack.EMPTY);
            if (!player.getStackInHand(hand).isOf(LocationTooltipMod.REGION_WAND))
                return TypedActionResult.pass(ItemStack.EMPTY);

            MinecraftClient mc = MinecraftClient.getInstance();
            if (player.isSneaking()) {
                if (RegionSelectionClient.hasBothCorners()) {
                    mc.setScreen(new NameRegionScreen(0));
                } else {
                    RegionSelectionClient.clear(mc);
                }
                return TypedActionResult.success(player.getStackInHand(hand));
            }

            return TypedActionResult.pass(player.getStackInHand(hand));
        });
    }
}
