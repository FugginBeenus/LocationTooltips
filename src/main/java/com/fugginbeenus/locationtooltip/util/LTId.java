package com.fugginbeenus.locationtooltip.util;

import net.minecraft.resources.ResourceLocation;

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
