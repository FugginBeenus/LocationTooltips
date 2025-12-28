package com.fugginbeenus.locationtooltip.mixin;

import com.fugginbeenus.locationtooltip.region.RegionProtection;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to prevent natural mob spawning in regions where it's disabled
 */
@Mixin(MobEntity.class)
public class MobEntityMixin {

    /**
     * Inject into the canSpawn method to check region permissions
     */
    @Inject(method = "canMobSpawn", at = @At("HEAD"), cancellable = true)
    private static void onCanMobSpawn(
            EntityType<? extends MobEntity> type,
            ServerWorldAccess world,
            SpawnReason spawnReason,
            BlockPos pos,
            net.minecraft.util.math.random.Random random,
            CallbackInfoReturnable<Boolean> cir) {

        // Only prevent NATURAL spawns (not spawners, eggs, commands, etc.)
        if (spawnReason == SpawnReason.NATURAL || spawnReason == SpawnReason.CHUNK_GENERATION) {
            // Check if mob spawning is allowed at this location
            if (!RegionProtection.canMobSpawn(world.toServerWorld(), pos)) {
                cir.setReturnValue(false);  // Prevent spawn
            }
        }
    }
}