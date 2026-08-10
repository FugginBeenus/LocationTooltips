package com.fugginbeenus.locationtooltip.mixin;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code ItemProperties.register(Item, ResourceLocation, ClampedItemPropertyFunction)}
 * is private in vanilla and Fabric API (0.92.x) ships no public wrapper, so we reach it with an
 * invoker. Used to give the Admin Compass a working "angle" predicate (a moving needle).
 *
 * Client-only — registered in the "client" list of locationtooltip.mixins.json.
 */
@Mixin(ItemProperties.class)
public interface ModelPredicateRegistryInvoker {
    @Invoker("register")
    static void locationtooltip$register(Item item, ResourceLocation id, ClampedItemPropertyFunction provider) {
        throw new AssertionError();
    }
}
