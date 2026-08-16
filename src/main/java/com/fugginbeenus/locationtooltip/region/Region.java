package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.region.flag.RegionFlag;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class Region {
    public final String id;
    public String name;
    public final ResourceLocation dim;
    public final BlockPos min;
    public final BlockPos max;

    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    private final Map<String, Boolean> flagOverrides = new HashMap<>();

    public RegionSource source = RegionSource.PLAYER;

    public String category = null;

    public String waystoneUid = null;

    public UUID owner;

    public Region(String name, ResourceLocation dim, BlockPos a, BlockPos b) {
        this(UUID.randomUUID().toString(), name, dim, a, b);
    }

    public Region(String id, String name, ResourceLocation dim, BlockPos a, BlockPos b) {
        this(id, name, dim, a, b, null);
    }

    public Region(String id, String name, ResourceLocation dim, BlockPos a, BlockPos b, UUID owner) {
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

        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

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

    public long volume() {
        long dx = (long) (maxX - minX + 1);
        long dy = (long) (maxY - minY + 1);
        long dz = (long) (maxZ - minZ + 1);
        return Math.max(1L, dx) * Math.max(1L, dy) * Math.max(1L, dz);
    }

    public boolean isOwnedBy(UUID playerUuid) {
        if (owner == null) return false;
        return owner.equals(playerUuid);
    }

    public boolean canBeEditedBy(UUID playerUuid, boolean isOp) {
        if (isOp) return true;
        return isOwnedBy(playerUuid);
    }

    public Boolean getFlagOverride(String flagId) {
        return flagOverrides.get(flagId);
    }

    public boolean hasFlagOverride(String flagId) {
        return flagOverrides.containsKey(flagId);
    }

    public void setFlag(String flagId, boolean value) {
        flagOverrides.put(flagId, value);
    }

    public void clearFlag(String flagId) {
        flagOverrides.remove(flagId);
    }

    public boolean flagOrDefault(String flagId) {
        Boolean v = flagOverrides.get(flagId);
        if (v != null) return v;
        RegionFlag f = RegionFlags.byId(flagId);
        return f != null ? f.defaultValue : true;
    }

    public Map<String, Boolean> flagOverrides() {
        return flagOverrides;
    }
}
