package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.region.flag.RegionFlag;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
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

    // Per-region flag OVERRIDES (sparse): a present entry is an explicit allow/deny;
    // an absent entry means "inherit from a containing region, else the flag's default".
    // See RegionFlags for the registry of known flags and their defaults.
    private final Map<String, Boolean> flagOverrides = new HashMap<>();

    // Where this region came from (player, server/admin, or auto-generated structure).
    public RegionSource source = RegionSource.PLAYER;

    // Optional category/type tag (e.g. "village", "shop"); used for HUD styling + structure tagging.
    public String category = null;

    // Owner system
    public UUID owner;  // Player UUID who created this region (null = admin-created)

    /** Create a new region with a fresh id, auto-normalizing a/b into min/max. */
    public Region(String name, Identifier dim, BlockPos a, BlockPos b) {
        this(UUID.randomUUID().toString(), name, dim, a, b);
    }

    /** Create a region with explicit id, auto-normalizing a/b into min/max. */
    public Region(String id, String name, Identifier dim, BlockPos a, BlockPos b) {
        this(id, name, dim, a, b, null);  // null owner by default
    }

    /** Create a region with explicit id and owner, auto-normalizing a/b into min/max. */
    public Region(String id, String name, Identifier dim, BlockPos a, BlockPos b, UUID owner) {
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
        this.owner = owner;

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

    /**
     * Check if a player owns this region
     */
    public boolean isOwnedBy(UUID playerUuid) {
        if (owner == null) return false;  // Admin-created regions have no owner
        return owner.equals(playerUuid);
    }

    /**
     * Check if this region can be edited by a player
     */
    public boolean canBeEditedBy(UUID playerUuid, boolean isOp) {
        if (isOp) return true;  // Admins can edit anything
        return isOwnedBy(playerUuid);  // Players can only edit their own
    }

    // ===== Flags =====

    /** This region's explicit override for a flag, or null if it doesn't set one (inherits). */
    public Boolean getFlagOverride(String flagId) {
        return flagOverrides.get(flagId);
    }

    /** True if this region explicitly sets the flag (vs. inheriting it). */
    public boolean hasFlagOverride(String flagId) {
        return flagOverrides.containsKey(flagId);
    }

    /** Set an explicit allow/deny for a flag on this region. */
    public void setFlag(String flagId, boolean value) {
        flagOverrides.put(flagId, value);
    }

    /** Remove this region's override so the flag inherits / falls back to its default. */
    public void clearFlag(String flagId) {
        flagOverrides.remove(flagId);
    }

    /**
     * Effective value for THIS region alone: its override if set, otherwise the flag's
     * registered default. Note this ignores nested-region inheritance — for the value
     * that actually applies at a position, use {@code RegionManager.resolveFlag(...)}.
     */
    public boolean flagOrDefault(String flagId) {
        Boolean v = flagOverrides.get(flagId);
        if (v != null) return v;
        RegionFlag f = RegionFlags.byId(flagId);
        return f != null ? f.defaultValue : true;
    }

    /** Live view of this region's overrides (used by storage + networking). */
    public Map<String, Boolean> flagOverrides() {
        return flagOverrides;
    }
}