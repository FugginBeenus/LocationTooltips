package com.fugginbeenus.locationtooltip.util;

import net.minecraft.resources.ResourceLocation;

/**
 * Builds a namespaced id. The way you construct one keeps moving (public constructor on 1.20.1,
 * a static factory once the constructor was hidden), so every call site goes through here and
 * only this file carries the version switch.
 */
public final class LTId {
    private LTId() {}

    public static ResourceLocation of(String namespace, String path) {
        //? if >=1.21 {
        /*return ResourceLocation.fromNamespaceAndPath(namespace, path);
        *///?} else {
        return new ResourceLocation(namespace, path);
        //?}
    }
}
