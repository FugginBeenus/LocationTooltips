package com.fugginbeenus.locationtooltip.region;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Represents a rectangular 3D region in a specific dimension.
 * Each region has a unique id, name, dimension, and two corner positions.
 */
public class Region {
    public final String id;
    public String name;
    public final Identifier dim;
    public final BlockPos a;
    public final BlockPos b;
    public final UUID owner;

    public Region(String id, String name, Identifier dim, BlockPos a, BlockPos b, UUID owner) {
        this.id = id;
        this.name = name;
        this.dim = dim;
        this.a = a;
        this.b = b;
        this.owner = owner;
    }

    public boolean contains(BlockPos pos, Identifier dim) {
        if (!this.dim.equals(dim)) return false;
        int minX = Math.min(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxX = Math.max(a.getX(), b.getX());
        int maxY = Math.max(a.getY(), b.getY());
        int maxZ = Math.max(a.getZ(), b.getZ());
        return pos.getX() >= minX && pos.getX() <= maxX
                && pos.getY() >= minY && pos.getY() <= maxY
                && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    public boolean overlaps(Region other) {
        if (!this.dim.equals(other.dim)) return false;
        return !(other.a.getX() > Math.max(a.getX(), b.getX())
                || other.b.getX() < Math.min(a.getX(), b.getX())
                || other.a.getY() > Math.max(a.getY(), b.getY())
                || other.b.getY() < Math.min(a.getY(), b.getY())
                || other.a.getZ() > Math.max(a.getZ(), b.getZ())
                || other.b.getZ() < Math.min(a.getZ(), b.getZ()));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", dim, name, id);
    }
}
