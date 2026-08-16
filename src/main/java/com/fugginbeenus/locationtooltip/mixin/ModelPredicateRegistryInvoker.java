package com.fugginbeenus.locationtooltip.mixin;

//? if >=1.21.11 {
/*public interface ModelPredicateRegistryInvoker {
}
*///?} else {
@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.renderer.item.ItemProperties.class)
public interface ModelPredicateRegistryInvoker {
    @org.spongepowered.asm.mixin.gen.Invoker("register")
    static void locationtooltip$register(net.minecraft.world.item.Item item,
                                         net.minecraft.resources.ResourceLocation id,
                                         net.minecraft.client.renderer.item.ClampedItemPropertyFunction provider) {
        throw new AssertionError();
    }
}
//?}
