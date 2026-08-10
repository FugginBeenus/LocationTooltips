package com.fugginbeenus.locationtooltip.mixin;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Strips blocks inside regions with the {@code explosions} flag denied from an explosion's
 * affected-block list, so creeper / TNT / wither / ghast blasts can't grief protected areas.
 *
 * We inject at the TAIL of {@code explode()} (which fills the affected list) and remove
 * protected positions before they are destroyed. Entity damage is left untouched.
 */
@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow @Final private Level level;

    @Inject(method = "explode", at = @At("TAIL"))
    private void locationtooltip$filterProtectedBlocks(CallbackInfo ci) {
        if (level == null || level.isClientSide()) return;
        var server = level.getServer();
        if (server == null) return;

        var dim = level.dimension().location();
        RegionManager mgr = RegionManager.of(server);
        Explosion self = (Explosion) (Object) this;
        self.getToBlow().removeIf(pos -> !mgr.resolveFlag(dim, pos, RegionFlags.EXPLOSIONS.id));
    }
}
