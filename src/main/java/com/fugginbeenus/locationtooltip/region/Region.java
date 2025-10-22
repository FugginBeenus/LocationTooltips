package com.fugginbeenus.locationtooltip.region;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.UUID;

/** Immutable, axis-aligned rectangular region in a single dimension. */
public final class Region {
    public final String id;          // stable unique id (string form)
    public String name;              // editable display name
    public final Identifier dim;     // dimension id
    public final BlockPos min;       // normalized min corner (<=)
    public final BlockPos max;       // normalized max corner (>=)

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
    }

    public boolean contains(BlockPos p) {
        return p.getX() >= min.getX() && p.getX() <= max.getX()
                && p.getY() >= min.getY() && p.getY() <= max.getY()
                && p.getZ() >= min.getZ() && p.getZ() <= max.getZ();
    }

    public int sizeX() { return max.getX() - min.getX() + 1; }
    public int sizeY() { return max.getY() - min.getY() + 1; }
    public int sizeZ() { return max.getZ() - min.getZ() + 1; }
}
