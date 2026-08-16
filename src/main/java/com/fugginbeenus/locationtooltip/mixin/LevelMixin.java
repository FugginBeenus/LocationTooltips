package com.fugginbeenus.locationtooltip.mixin;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelMixin {
    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void locationtooltip$blockMobGriefing(BlockPos pos, boolean drop, Entity breakingEntity,
                                                  int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (!(breakingEntity instanceof Mob)) return;

        Level world = (Level) (Object) this;
        if (world.isClientSide()) return;
        var server = world.getServer();
        if (server == null) return;

        var dim = world.dimension().location();
        if (!RegionManager.of(server).resolveFlag(dim, pos, RegionFlags.MOB_GRIEFING.id)) {
            cir.setReturnValue(false);
        }
    }
}
