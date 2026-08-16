package com.fugginbeenus.locationtooltip.mixin;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanTakeBlockGoal")
public class EndermanTakeBlockGoalMixin {
    @Shadow @Final private EnderMan enderman;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void locationtooltip$blockEndermanTake(CallbackInfo ci) {
        if (MobGriefingCheck.denied(enderman)) ci.cancel();
    }
}
