package com.fugginbeenus.locationtooltip.client;

/**
 * The Admin Compass needle on 1.21.11 and newer.
 *
 * Item predicates became data-driven item models, so the needle is now a model property that
 * the item's own model definition dispatches on. Extending vanilla's needle helper means the
 * easing and the aimless spin behave exactly like a real compass; all we supply is the target.
 */
//? if >=1.21.11 {
/*public final class AdminCompassAngle
        extends net.minecraft.client.renderer.item.properties.numeric.NeedleDirectionHelper
        implements net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty {

    public static final net.minecraft.resources.Identifier ID =
            com.fugginbeenus.locationtooltip.util.LTId.of("locationtooltip", "region_angle");

    public static final com.mojang.serialization.MapCodec<AdminCompassAngle> MAP_CODEC =
            com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance -> instance.group(
                    com.mojang.serialization.Codec.BOOL.optionalFieldOf("wobble", true)
                            .forGetter(p -> p.wobble)
            ).apply(instance, AdminCompassAngle::new));

    private final boolean wobble;

    public AdminCompassAngle(boolean wobble) {
        super(wobble);
        this.wobble = wobble;
    }

    @Override
    public com.mojang.serialization.MapCodec<AdminCompassAngle> type() {
        return MAP_CODEC;
    }

    @Override
    protected float calculate(net.minecraft.world.item.ItemStack stack,
                              net.minecraft.client.multiplayer.ClientLevel level,
                              int seed,
                              net.minecraft.world.entity.ItemOwner owner) {
        net.minecraft.world.phys.Vec3 pos = owner.position();
        net.minecraft.core.BlockPos target = AdminCompassModel.nearestRegionCenter(
                level.dimension().identifier(), net.minecraft.core.BlockPos.containing(pos));

        // No known region: let the needle spin, the way a compass does out of range.
        if (target == null) {
            return net.minecraft.util.Mth.positiveModulo(seed * 0.00390625F, 1.0F);
        }

        double facing = net.minecraft.util.Mth.positiveModulo(
                owner.getVisualRotationYInDegrees() / 360.0, 1.0);
        double toTarget = Math.atan2(target.getZ() - pos.z, target.getX() - pos.x) / (Math.PI * 2);
        return (float) net.minecraft.util.Mth.positiveModulo(0.5 - (facing - 0.25 - toTarget), 1.0);
    }
}
*///?} else {
public final class AdminCompassAngle {
    private AdminCompassAngle() {}
}
//?}
