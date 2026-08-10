package com.fugginbeenus.locationtooltip.mixin;

/**
 * {@code ItemProperties.register(Item, ResourceLocation, ClampedItemPropertyFunction)}
 * is private in vanilla and Fabric API ships no public wrapper, so we reach it with an
 * invoker. Used to give the Admin Compass a working "angle" predicate (a moving needle).
 *
 * Client-only — registered in the "client" list of locationtooltip.mixins.json.
 *
 * 1.21.11 replaced item predicates with data-driven item models, so there is nothing to
 * target there: the build drops this from the mixin config and leaves the stub below.
 */
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
