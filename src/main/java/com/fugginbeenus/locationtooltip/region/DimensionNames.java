package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.util.LTId;
import net.minecraft.resources.ResourceLocation;

public final class DimensionNames {
    private DimensionNames() {}

    private static final ResourceLocation OVERWORLD = LTId.of("minecraft", "overworld");
    private static final ResourceLocation THE_NETHER = LTId.of("minecraft", "the_nether");
    private static final ResourceLocation THE_END = LTId.of("minecraft", "the_end");

    public static String wilderness(ResourceLocation dim) {
        if (dim == null) return "Wilderness";
        if (OVERWORLD.equals(dim)) return "Wilderness";
        if (THE_NETHER.equals(dim)) return "The Nether";
        if (THE_END.equals(dim)) return "The End";
        return prettify(dim.getPath());
    }

    private static String prettify(String path) {
        String[] parts = path.replace('/', ' ').replace('_', ' ').trim().split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        String out = sb.toString().trim();
        return out.isEmpty() ? "Wilderness" : out;
    }
}
