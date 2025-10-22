package com.fugginbeenus.locationtooltip.client;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class AdminClientCache {
    private static Row[] CACHE = new Row[0];

    /** Preferred setter — called from LTPacketsClient when a fresh list arrives. */
    public static void set(Row[] rows) { CACHE = rows != null ? rows : new Row[0]; }

    /** Preferred getter for current cached rows. */
    public static Row[] current() { return CACHE; }

    // --- Back-compat aliases (safe no-ops if legacy calls exist) ---
    public static void update(Row[] rows) { set(rows); }
    public static Row[] get() { return current(); }

    /** Cached row describing a region. Provides both (min,max) and (a,b) for callers. */
    public static final class Row {
        public final String id;
        public final String name;
        public final Identifier dim;

        // Canonical corners:
        public final BlockPos min;
        public final BlockPos max;

        // Aliases for callers that expect (a,b):
        public final BlockPos a;
        public final BlockPos b;

        public Row(String id, String name, Identifier dim, BlockPos min, BlockPos max) {
            this.id = id;
            this.name = name;
            this.dim = dim;
            this.min = min;
            this.max = max;
            // aliases so code using r.a / r.b compiles:
            this.a = min;
            this.b = max;
        }
    }
}
