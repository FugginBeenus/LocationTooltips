package com.fugginbeenus.locationtooltip.data;

import net.minecraft.util.math.BlockPos;

/** Inclusive integer AABB for regions. Gson-friendly POJO. */
public class Bounds {
    public int minX, minY, minZ;
    public int maxX, maxY, maxZ;

    public Bounds() { }

    public Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public boolean contains(BlockPos pos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public long volume() {
        long dx = (long)(maxX - minX + 1);
        long dy = (long)(maxY - minY + 1);
        long dz = (long)(maxZ - minZ + 1);
        return dx * dy * dz;
    }

    @Override public String toString() {
        return "Bounds[" + minX + "," + minY + "," + minZ + " -> " + maxX + "," + maxY + "," + maxZ + "]";
    }
}
