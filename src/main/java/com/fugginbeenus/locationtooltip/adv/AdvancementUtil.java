package com.fugginbeenus.locationtooltip.adv;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public final class AdvancementUtil {
    private AdvancementUtil() {}

    /** Grants all remaining criteria on the given advancement id (no-ops if missing or already done). */
    public static void grant(ServerPlayer player, ResourceLocation id) {
        if (player == null || player.server == null) return;
        // The lookup is named getAdvancement on 1.20.1 and get on 1.21+, and returns an
        // Advancement vs an AdvancementHolder, which var absorbs.
        //? if >=1.21 {
        /*var adv = player.server.getAdvancements().get(id);
        *///?} else {
        var adv = player.server.getAdvancements().getAdvancement(id);
        //?}
        if (adv == null) return;

        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(adv);
        if (progress.isDone()) return;

        for (String crit : progress.getRemainingCriteria()) {
            player.getAdvancements().award(adv, crit);
        }
    }
}
