package com.fugginbeenus.locationtooltip.mixin;

import com.fugginbeenus.locationtooltip.region.RegionProtection;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobEntityMixin {
    @Inject(method = "checkMobSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void onCanMobSpawn(
            EntityType<? extends Mob> type,
            LevelAccessor world,
            MobSpawnType spawnReason,
            BlockPos pos,
            net.minecraft.util.RandomSource random,
            CallbackInfoReturnable<Boolean> cir) {
        if (spawnReason != MobSpawnType.NATURAL && spawnReason != MobSpawnType.CHUNK_GENERATION) return;
        if (!(world instanceof ServerLevelAccessor server)) return;

        if (!RegionProtection.canMobSpawn(server.getLevel(), pos)) {
            cir.setReturnValue(false);
        }
    }
}
