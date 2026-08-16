package com.fugginbeenus.locationtooltip.mixin;

import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void locationtooltip$blockItemPickup(Player player, CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        var world = self.level();
        if (world.isClientSide()) return;
        var server = world.getServer();
        if (server == null) return;

        var dim = world.dimension().location();
        if (!RegionManager.of(server).resolveFlag(dim, self.blockPosition(), RegionFlags.ITEM_PICKUP.id)) {
            ci.cancel();
        }
    }
}
