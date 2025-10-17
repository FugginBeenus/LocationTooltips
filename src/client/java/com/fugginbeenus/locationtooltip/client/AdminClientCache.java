package com.fugginbeenus.locationtooltip.client;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Stores the last region list received from the server so
 * the Admin Compass can render particle outlines locally.
 */
public class AdminClientCache {

    public static class Row {
        public final String id;
        public final String name;
        public final Identifier dim;
        public final BlockPos a;
        public final BlockPos b;

        public Row(String id, String name, Identifier dim, BlockPos a, BlockPos b) {
            this.id = id;
            this.name = name;
            this.dim = dim;
            this.a = a;
            this.b = b;
        }
    }

    /** Most recent list of regions for the active dimension */
    public static Row[] last = new Row[0];

    private AdminClientCache() {}
}
