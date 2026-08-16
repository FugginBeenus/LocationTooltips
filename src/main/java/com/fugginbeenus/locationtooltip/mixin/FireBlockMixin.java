package com.fugginbeenus.locationtooltip.mixin;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    @Inject(method = "checkBurnOut", at = @At("HEAD"), cancellable = true)
    private void locationtooltip$blockFireSpread(Level world, BlockPos pos, int spreadFactor,
                                                 RandomSource random, int currentAge, CallbackInfo ci) {
        if (world.isClientSide()) return;
        var server = world.getServer();
        if (server == null) return;

        var dim = world.dimension().location();
        if (!RegionManager.of(server).resolveFlag(dim, pos, RegionFlags.FIRE_SPREAD.id)) {
            ci.cancel();
        }
    }
}
