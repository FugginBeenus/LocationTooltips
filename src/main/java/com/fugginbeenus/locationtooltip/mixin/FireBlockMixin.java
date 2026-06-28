package com.fugginbeenus.locationtooltip.mixin;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.minecraft.block.FireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops fire from spreading to / burning blocks inside regions with the {@code fire-spread}
 * flag denied. {@code trySpreadingFire} is called for each neighbour fire tries to ignite;
 * we cancel it when the target position is protected.
 */
@Mixin(FireBlock.class)
public class FireBlockMixin {

    @Inject(method = "trySpreadingFire", at = @At("HEAD"), cancellable = true)
    private void locationtooltip$blockFireSpread(World world, BlockPos pos, int spreadFactor,
                                                 Random random, int currentAge, CallbackInfo ci) {
        if (world.isClient()) return;
        var server = world.getServer();
        if (server == null) return;

        var dim = world.getRegistryKey().getValue();
        if (!RegionManager.of(server).resolveFlag(dim, pos, RegionFlags.FIRE_SPREAD.id)) {
            ci.cancel();
        }
    }
}
