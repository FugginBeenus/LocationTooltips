package com.fugginbeenus.locationtooltip.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public final class ClientWandBridge {
    private ClientWandBridge() {}

    public static void handleUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (world == null || !world.isClient) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        // Quick ping so we *know* the handler fired (actionbar)
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§8[LT] §7Wand used"), true);
        }

        var pos = hit.getBlockPos();
        if (player.isSneaking()) {
            if (RegionSelectionClient.hasBothCorners()) {
                mc.execute(() -> mc.setScreen(new RegionNameScreen()));
            } else {
                RegionSelectionClient.onWandSneak(mc, pos);
            }
        } else {
            RegionSelectionClient.onWandUse(mc, pos);
        }
    }
}
