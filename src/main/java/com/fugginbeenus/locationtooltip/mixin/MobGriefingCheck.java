package com.fugginbeenus.locationtooltip.mixin;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.minecraft.world.entity.Entity;

final class MobGriefingCheck {
    private MobGriefingCheck() {}

    static boolean denied(Entity entity) {
        if (entity == null) return false;
        var world = entity.level();
        if (world.isClientSide()) return false;
        var server = world.getServer();
        if (server == null) return false;

        var dim = world.dimension().location();
        return !RegionManager.of(server).resolveFlag(dim, entity.blockPosition(), RegionFlags.MOB_GRIEFING.id);
    }
}
