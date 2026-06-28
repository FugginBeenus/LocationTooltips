package com.fugginbeenus.locationtooltip.mixin;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
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
 * We inject at the TAIL of {@code collectBlocksAndDamageEntities()} (which fills the affected
 * list) and remove protected positions before {@code affectWorld()} destroys them. Entity
 * damage from the blast is intentionally left untouched.
 */
@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow @Final private World world;

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("TAIL"))
    private void locationtooltip$filterProtectedBlocks(CallbackInfo ci) {
        if (world == null || world.isClient()) return;
        var server = world.getServer();
        if (server == null) return;

        var dim = world.getRegistryKey().getValue();
        RegionManager mgr = RegionManager.of(server);
        Explosion self = (Explosion) (Object) this;
        self.getAffectedBlocks().removeIf(pos -> !mgr.resolveFlag(dim, pos, RegionFlags.EXPLOSIONS.id));
    }
}
