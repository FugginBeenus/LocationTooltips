package com.fugginbeenus.locationtooltip.mixin;

import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanLeaveBlockGoal")
public class EndermanLeaveBlockGoalMixin {
    @Shadow @Final private EnderMan enderman;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void locationtooltip$blockEndermanLeave(CallbackInfo ci) {
        if (MobGriefingCheck.denied(enderman)) ci.cancel();
    }
}
