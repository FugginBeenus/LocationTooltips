package com.fugginbeenus.locationtooltip.mixin;

/**
 * Reaches the registry of range-select item model properties, which is private and has no
 * Fabric wrapper, so the Admin Compass can register the property its model dispatches on.
 *
 * Client-only, and only present from 1.21.11, where item predicates became data-driven.
 */
//? if >=1.21.11 {
/*@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties.class)
public interface RangeSelectPropertiesAccessor {
    @org.spongepowered.asm.mixin.gen.Accessor("ID_MAPPER")
    static net.minecraft.util.ExtraCodecs.LateBoundIdMapper<
            net.minecraft.resources.Identifier,
            com.mojang.serialization.MapCodec<? extends net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty>>
    locationtooltip$idMapper() {
        throw new AssertionError();
    }
}
*///?} else {
public interface RangeSelectPropertiesAccessor {
}
//?}
