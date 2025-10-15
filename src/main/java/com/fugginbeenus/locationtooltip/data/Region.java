package com.fugginbeenus.locationtooltip.data;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/** Region with owner metadata and world key. Gson-friendly POJO. */
public class Region {
    private String id;
    private String name;
    private String worldKey;  // e.g. "minecraft:overworld"
    private Bounds bounds;
    private UUID ownerUuid;
    private String ownerName;

    public Region() { }

    public Region(String id, String name, String worldKey, Bounds bounds, UUID ownerUuid, String ownerName) {
        this.id = id; this.name = name; this.worldKey = worldKey;
        this.bounds = bounds; this.ownerUuid = ownerUuid; this.ownerName = ownerName;
    }

    // record-like accessors
    public String id() { return id; }
    public String name() { return name; }
    public String worldKey() { return worldKey; }
    public Bounds bounds() { return bounds; }
    public UUID ownerUuid() { return ownerUuid; }
    public String ownerName() { return ownerName; }

    // setters for Gson & edits
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setWorldKey(String worldKey) { this.worldKey = worldKey; }
    public void setBounds(Bounds bounds) { this.bounds = bounds; }
    public void setOwner(UUID ownerUuid, String ownerName) { this.ownerUuid = ownerUuid; this.ownerName = ownerName; }

    // helpers
    public boolean contains(BlockPos pos) { return bounds != null && bounds.contains(pos); }

    public boolean containsBox(Bounds other) {
        if (bounds == null || other == null) return false;
        return other.minX >= bounds.minX && other.maxX <= bounds.maxX
                && other.minY >= bounds.minY && other.maxY <= bounds.maxY
                && other.minZ >= bounds.minZ && other.maxZ <= bounds.maxZ;


    }


    @Override public String toString() {
        return "Region{" + id + "," + name + "," + worldKey + "," + bounds + "," + ownerUuid + "," + ownerName + "}";
    }
}
