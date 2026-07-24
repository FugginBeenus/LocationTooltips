package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.mixin.ModelPredicateRegistryInvoker;
import com.fugginbeenus.locationtooltip.registry.LTItems;
import net.minecraft.client.item.CompassAnglePredicateProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

/**
 * Gives the Admin Compass a real, moving needle — it points at the nearest known region
 * instead of world spawn.
 *
 * Uses vanilla's {@link CompassAnglePredicateProvider} (so the needle eases/wobbles exactly
 * like a normal compass) behind an "angle" model predicate, with the 32 admin_compass_XX
 * frames supplying the art. Returning {@code null} from the target makes vanilla fall back to
 * its "aimless" spin, which is what we want when no regions are known yet.
 *
 * Region positions come from {@link AdminClientCache}, which refreshes while the compass is
 * held (and whenever the admin panel is open).
 */
public final class AdminCompassModel {
    private AdminCompassModel() {}

    public static void register() {
        ModelPredicateRegistryInvoker.locationtooltip$register(
                LTItems.ADMIN_COMPASS,
                Identifier.of("minecraft", "angle"),
                new CompassAnglePredicateProvider((world, stack, entity) -> {
                    if (world == null || entity == null) return null;
                    BlockPos target = nearestRegionCenter(world.getRegistryKey().getValue(), entity.getBlockPos());
                    return (target == null) ? null : GlobalPos.create(world.getRegistryKey(), target);
                })
        );
    }

    /** Centre of the closest cached region in this dimension, or null if none are known. */
    private static BlockPos nearestRegionCenter(Identifier dim, BlockPos from) {
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
            double d = center.getSquaredDistance(from);
            if (d < bestDist) {
                bestDist = d;
                best = center;
            }
        }
        return best;
    }
}
