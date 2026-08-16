package com.fugginbeenus.locationtooltip.mixin;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

//? if >=1.21.11 {
/*@Mixin(net.minecraft.world.level.ServerExplosion.class)
public abstract class ExplosionMixin {
    @Inject(method = "calculateExplodedPositions", at = @At("RETURN"), cancellable = true)
    private void locationtooltip$filterProtectedBlocks(
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<java.util.List<net.minecraft.core.BlockPos>> cir) {
        net.minecraft.world.level.ServerExplosion self = (net.minecraft.world.level.ServerExplosion) (Object) this;
        net.minecraft.server.level.ServerLevel level = self.level();
        if (level == null) return;
        var server = level.getServer();
        if (server == null) return;

        var dim = level.dimension().location();
        RegionManager mgr = RegionManager.of(server);
        var filtered = new java.util.ArrayList<>(cir.getReturnValue());
        filtered.removeIf(pos -> !mgr.resolveFlag(dim, pos, RegionFlags.EXPLOSIONS.id));
        cir.setReturnValue(filtered);
    }
}
*///?} else {
@Mixin(net.minecraft.world.level.Explosion.class)
public abstract class ExplosionMixin {
    @org.spongepowered.asm.mixin.Shadow @org.spongepowered.asm.mixin.Final
    private net.minecraft.world.level.Level level;

    @Inject(method = "explode", at = @At("TAIL"))
    private void locationtooltip$filterProtectedBlocks(
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (level == null || level.isClientSide()) return;
        var server = level.getServer();
        if (server == null) return;

        var dim = level.dimension().location();
        RegionManager mgr = RegionManager.of(server);
        net.minecraft.world.level.Explosion self = (net.minecraft.world.level.Explosion) (Object) this;
        self.getToBlow().removeIf(pos -> !mgr.resolveFlag(dim, pos, RegionFlags.EXPLOSIONS.id));
    }
}
//?}
