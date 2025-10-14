package com.fugginbeenus.locationtooltip.data;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Data model for a named, axis-aligned region in a specific world.
 * - Serialized to/from JSON via Gson (fields annotated).
 * - Fields are private; expose read-only accessors.
 * - Priority is auto-assigned by RegionManager based on nesting depth.
 */
public final class Region {
    // Stable UUID string (assigned on load if missing)
    @SerializedName("id")       private String id;

    // Display name
    @SerializedName("name")     private String name;

    // World registry key string, e.g. "minecraft:overworld"
    @SerializedName("world")    private String world;

    // Bounding box (min/max corners). JSON stores arrays; getters normalize order.
    @SerializedName("bounds")   Bounds bounds;

    // Optional UI color (hex like "#FFFFFF")
    @SerializedName("color")    private String colorHex;

    // Nesting priority (0 = outermost). Higher = deeper (more specific) region.
    @SerializedName("priority") private int priority = 0;

    /* ---------- Accessors ---------- */

    public String id()        { return id; }
    public String name()      { return name; }
    public String world()     { return world; }
    public int    priority()  { return priority; }
    public Bounds bounds()    { return bounds; }

    /** Convert the optional hex color to ARGB with a provided alpha. */
    public int colorARGB(int alpha) {
        int rgb = 0xFFFFFF;
        try {
            if (colorHex != null && !colorHex.isBlank()) {
                rgb = Integer.parseInt(colorHex.replace("#",""), 16);
            }
        } catch (Exception ignored) {}
        return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF);
    }

    /** Point containment check (client-side). */
    public boolean contains(Vec3d pos) {
        if (bounds == null || !bounds.valid()) return false;
        Box box = new Box(bounds.minX(), bounds.minY(), bounds.minZ(),
                bounds.maxX(), bounds.maxY(), bounds.maxZ());
        return box.contains(pos);
    }

    /** Full box containment: does this region fully contain the other region's bounds? */
    public boolean containsBox(Bounds other) {
        if (bounds == null || other == null || !bounds.valid() || !other.valid()) return false;
        return bounds.minX() <= other.minX() && bounds.minY() <= other.minY() && bounds.minZ() <= other.minZ()
                && bounds.maxX() >= other.maxX() && bounds.maxY() >= other.maxY() && bounds.maxZ() >= other.maxZ();
    }

    /* ---------- Nested type for bounds ---------- */

    /**
     * Axis-aligned bounds with two corners:
     *  - Stored as arrays in JSON: [x, y, z]
     *  - Accessor methods normalize min/max ordering
     */
    public static final class Bounds {
        @SerializedName("min") public double[] min; // [x, y, z]
        @SerializedName("max") public double[] max; // [x, y, z]

        public boolean valid() {
            return min != null && max != null && min.length == 3 && max.length == 3;
        }

        public double minX(){ return Math.min(min[0], max[0]); }
        public double minY(){ return Math.min(min[1], max[1]); }
        public double minZ(){ return Math.min(min[2], max[2]); }
        public double maxX(){ return Math.max(min[0], max[0]); }
        public double maxY(){ return Math.max(min[1], max[1]); }
        public double maxZ(){ return Math.max(min[2], max[2]); }
    }
}
