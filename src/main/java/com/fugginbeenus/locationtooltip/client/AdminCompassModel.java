package com.fugginbeenus.locationtooltip.client;

//? if <1.21.11 {
import com.fugginbeenus.locationtooltip.mixin.ModelPredicateRegistryInvoker;
//?}
import com.fugginbeenus.locationtooltip.registry.LTItems;
import com.fugginbeenus.locationtooltip.util.LTId;
//? if <1.21.11 {
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
//?}
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;

public final class AdminCompassModel {
    private AdminCompassModel() {}

    public static void register() {
        //? if >=1.21.11 {
        /*com.fugginbeenus.locationtooltip.mixin.RangeSelectPropertiesAccessor.locationtooltip$idMapper()
                .put(AdminCompassAngle.ID, AdminCompassAngle.MAP_CODEC);
        *///?}
        //? if <1.21.11 {
        ModelPredicateRegistryInvoker.locationtooltip$register(
                LTItems.ADMIN_COMPASS,
                LTId.of("minecraft", "angle"),
                new CompassItemPropertyFunction((world, stack, entity) -> {
                    if (world == null || entity == null) return null;
                    BlockPos target = nearestRegionCenter(world.dimension().location(), entity.blockPosition());
                    return (target == null) ? null : GlobalPos.of(world.dimension(), target);
                })
        );
        //?}
    }

    static BlockPos nearestRegionCenter(ResourceLocation dim, BlockPos from) {
        AdminClientCache.Row[] rows = AdminClientCache.current();
        if (rows == null || rows.length == 0) return null;

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (AdminClientCache.Row r : rows) {
            if (r.dim == null || !r.dim.equals(dim) || r.min == null || r.max == null) continue;
            BlockPos center = new BlockPos(
                    (r.min.getX() + r.max.getX()) / 2,
                    (r.min.getY() + r.max.getY()) / 2,
                    (r.min.getZ() + r.max.getZ()) / 2);
            double d = center.distSqr(from);
            if (d < bestDist) {
                bestDist = d;
                best = center;
            }
        }
        return best;
    }
}
