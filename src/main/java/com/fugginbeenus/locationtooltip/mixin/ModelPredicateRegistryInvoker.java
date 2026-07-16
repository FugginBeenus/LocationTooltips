package com.fugginbeenus.locationtooltip.mixin;

import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code ModelPredicateProviderRegistry.register(Item, Identifier, ClampedModelPredicateProvider)}
 * is private in vanilla and Fabric API (0.92.x) ships no public wrapper, so we reach it with an
 * invoker. Used to give the Admin Compass a working "angle" predicate (a moving needle).
 *
 * Client-only — registered in the "client" list of locationtooltip.mixins.json.
 */
@Mixin(ModelPredicateProviderRegistry.class)
public interface ModelPredicateRegistryInvoker {
    @Invoker("register")
    static void locationtooltip$register(Item item, Identifier id, ClampedModelPredicateProvider provider) {
        throw new AssertionError();
    }
}
