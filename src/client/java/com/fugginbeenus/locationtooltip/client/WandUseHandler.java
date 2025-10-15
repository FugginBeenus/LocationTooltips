package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.LocationTooltipMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

@Environment(EnvType.CLIENT)
public final class WandUseHandler {
    private WandUseHandler() {}

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            var held = player.getStackInHand(hand);
            if (!held.isOf(LocationTooltipMod.REGION_WAND)) return ActionResult.PASS;

            // Client-side only
            if (!world.isClient) return ActionResult.SUCCESS;

            MinecraftClient mc = MinecraftClient.getInstance();
            var pos = ((BlockHitResult) hit).getBlockPos();

            if (player.isSneaking()) {
                // If both corners selected → open naming; else rename region under cursor
                if (RegionSelectionClient.hasBothCorners()) {
                    mc.execute(() -> mc.setScreen(new RegionNameScreen()));
                } else {
                    RegionSelectionClient.onWandSneak(mc, pos);
                }
            } else {
                // Set A/B and show a quick actionbar hint
                boolean setB = RegionSelectionClient.onWandUse(mc, pos);
                if (mc.player != null) {
                    mc.player.sendMessage(Text.literal(
                            setB ? "§7Second corner set. §fShift-Right-Click §7to name & save."
                                    : "§7First corner set."
                    ), true);
                }
            }
            return ActionResult.SUCCESS;
        });
    }
}
