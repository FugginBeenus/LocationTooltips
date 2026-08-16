package com.fugginbeenus.locationtooltip.adv;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public final class AdvancementUtil {
    private AdvancementUtil() {}

    public static void grant(ServerPlayer player, ResourceLocation id) {
        if (player == null || player.level().getServer() == null) return;

        //? if >=1.21 {
        /*var adv = player.level().getServer().getAdvancements().get(id);
        *///?} else {
        var adv = player.level().getServer().getAdvancements().getAdvancement(id);
        //?}
        if (adv == null) return;

        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(adv);
        if (progress.isDone()) return;

        for (String crit : progress.getRemainingCriteria()) {
            player.getAdvancements().award(adv, crit);
        }
    }
}
