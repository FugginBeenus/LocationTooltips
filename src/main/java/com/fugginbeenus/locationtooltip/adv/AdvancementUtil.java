package com.fugginbeenus.locationtooltip.adv;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class AdvancementUtil {
    private AdvancementUtil() {}

    /** Grants all remaining criteria on the given advancement id (no-ops if missing or already done). */
    public static void grant(ServerPlayerEntity player, Identifier id) {
        if (player == null || player.server == null) return;
        // 1.20.1: get(...) returns Advancement; 1.21: AdvancementEntry. var absorbs the difference.
        var adv = player.server.getAdvancementLoader().get(id);
        if (adv == null) return;

        AdvancementProgress progress = player.getAdvancementTracker().getProgress(adv);
        if (progress.isDone()) return;

        for (String crit : progress.getUnobtainedCriteria()) {
            player.getAdvancementTracker().grantCriterion(adv, crit);
        }
    }
}
