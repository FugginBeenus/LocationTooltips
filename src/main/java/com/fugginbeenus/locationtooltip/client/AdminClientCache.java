package com.fugginbeenus.locationtooltip.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

public final class AdminClientCache {
    private static Row[] CACHE = new Row[0];

    public static void set(Row[] rows) { CACHE = rows != null ? rows : new Row[0]; }

    public static Row[] current() { return CACHE; }

    public static void update(Row[] rows) { set(rows); }
    public static Row[] get() { return current(); }

    public static final class Row {
        public final String id;
        public final String name;
        public final ResourceLocation dim;

        public final BlockPos min;
        public final BlockPos max;

        public final BlockPos a;
        public final BlockPos b;

        public final java.util.Map<String, Boolean> flags;

        public final String ownerName;

        public final String source;

        public Row(String id, String name, ResourceLocation dim, BlockPos min, BlockPos max,
                   java.util.Map<String, Boolean> flags, String ownerName, String source) {
            this.id = id;
            this.name = name;
            this.dim = dim;
            this.min = min;
            this.max = max;
            this.flags = flags;
            this.ownerName = ownerName;
            this.source = source;

            this.a = min;
            this.b = max;
        }

        public boolean isStructure() {
            return "STRUCTURE".equals(source);
        }
    }
}
