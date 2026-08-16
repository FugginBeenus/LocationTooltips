package com.fugginbeenus.locationtooltip.mixin;

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
