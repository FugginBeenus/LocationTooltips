package com.fugginbeenus.locationtooltip.region;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable, axis-aligned rectangular region in a single dimension.
 *
 * OPTIMIZED: Cached position components for faster contains() checks
 */
public final class Region {
    public final String id;          // stable unique id (string form)
    public String name;              // editable display name
    public final Identifier dim;     // dimension id
    public final BlockPos min;       // normalized min corner (<=)
    public final BlockPos max;       // normalized max corner (>=)

    // OPTIMIZATION: Cached bounds for faster contains() checks (avoids 6 method calls per check)
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    // Functional flags
    public boolean allowPvP = true;           // Can players damage each other?
    public boolean allowMobSpawning = true;   // Can mobs naturally spawn?

    /** Create a new region with a fresh id, auto-normalizing a/b into min/max. */
    public Region(String name, Identifier dim, BlockPos a, BlockPos b) {
        this(UUID.randomUUID().toString(), name, dim, a, b);
    }

    /** Create a region with explicit id, auto-normalizing a/b into min/max. */
    public Region(String id, String name, Identifier dim, BlockPos a, BlockPos b) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(dim, "dim");
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");

        int minX = Math.min(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxX = Math.max(a.getX(), b.getX());
        int maxY = Math.max(a.getY(), b.getY());
        int maxZ = Math.max(a.getZ(), b.getZ());

        this.id = id;
        this.name = name;
        this.dim = dim;
        this.min = new BlockPos(minX, minY, minZ);
        this.max = new BlockPos(maxX, maxY, maxZ);

        // Cache bounds for faster contains() checks
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    /**
     * OPTIMIZED: Check if a position is contained in this region.
     * Uses cached bounds to avoid repeated BlockPos method calls.
     */
    public boolean contains(BlockPos p) {
        int x = p.getX();
        int y = p.getY();
        int z = p.getZ();

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public int sizeX() { return maxX - minX + 1; }
    public int sizeY() { return maxY - minY + 1; }
    public int sizeZ() { return maxZ - minZ + 1; }

    /**
     * Calculate the volume of this region (used for nested region priority)
     */
    public long volume() {
        long dx = (long) (maxX - minX + 1);
        long dy = (long) (maxY - minY + 1);
        long dz = (long) (maxZ - minZ + 1);
        return Math.max(1L, dx) * Math.max(1L, dy) * Math.max(1L, dz);
    }
}