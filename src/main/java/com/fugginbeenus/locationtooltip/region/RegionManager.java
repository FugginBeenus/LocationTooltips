package com.fugginbeenus.locationtooltip.region;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Manages all regions for a running server.
 * Persistence is handled via {@link RegionStorage} (JSON under the world save path).
 */
public final class RegionManager {

    // One RegionManager per running server instance.
    private static final WeakHashMap<MinecraftServer, RegionManager> BY_SERVER = new WeakHashMap<>();

    public static RegionManager of(MinecraftServer server) {
        return BY_SERVER.computeIfAbsent(server, s -> {
            RegionManager mgr = new RegionManager();
            mgr.load(s);
            return mgr;
        });
    }

    private final List<Region> regions = new ArrayList<>();
    private boolean loaded = false;

    private RegionManager() {}

    /* ---------------------------- Persistence ----------------------------- */

    public synchronized void load(MinecraftServer server) {
        if (loaded) return;
        regions.clear();
        regions.addAll(RegionStorage.load(server));
        loaded = true;
    }

    public synchronized void save(MinecraftServer server) {
        RegionStorage.save(server, regions);
    }

    /* ----------------------------- Mutations ------------------------------ */

    public synchronized Region createRegion(ServerPlayerEntity player, String name, Identifier dim, BlockPos a, BlockPos b) {
        // Make the region a full-height column (bottom->top of the dimension) so it persists over uneven terrain.
        var world = player.getWorld();
        int minY = world.getBottomY();
        int maxY = world.getTopY() - 1;

        BlockPos ca = new BlockPos(a.getX(), minY, a.getZ());
        BlockPos cb = new BlockPos(b.getX(), maxY, b.getZ());

        Region r = new Region(UUID.randomUUID().toString(), name, dim, ca, cb, player.getUuid());
        regions.add(r);

        // Stop selection particles, persist, and (optionally) celebrate on client
        SelectionManager.clear(player);
        save(player.getServer());

        // Send a little celebration packet (audio/visual polish) — see section 2 below
        com.fugginbeenus.locationtooltip.net.LTPackets.sendRegionCelebrate(player, name);

        return r;
    }

    /** Rename a region by ID; returns true if found. Saves on success. */
    public synchronized boolean renameRegion(MinecraftServer server, String id, String newName) {
        for (Region r : regions) {
            if (r.id.equals(id)) {
                r.name = newName;
                save(server);
                return true;
            }
        }
        return false;
    }

    /** Delete a region by ID; returns true if removed. Saves on success. */
    public synchronized boolean deleteRegion(MinecraftServer server, String id) {
        boolean ok = regions.removeIf(r -> r.id.equals(id));
        if (ok) save(server);
        return ok;
    }

    /* ------------------------------ Queries ------------------------------- */

    /** Returns the innermost region containing pos in dim (smallest volume), or null. */
    public synchronized Region regionAt(Identifier dim, BlockPos pos) {
        Region best = null;
        long bestVol = Long.MAX_VALUE;
        for (Region r : regions) {
            if (!r.dim.equals(dim)) continue;
            if (contains(r, pos)) {
                long vol = volume(r);
                if (vol < bestVol) {
                    bestVol = vol;
                    best = r;
                }
            }
        }
        return best;
    }

    /** All regions containing pos in dim, ordered outer → inner. */
    public synchronized List<Region> pathAt(Identifier dim, BlockPos pos) {
        List<Region> hits = new ArrayList<>();
        for (Region r : regions) {
            if (!r.dim.equals(dim)) continue;
            if (contains(r, pos)) hits.add(r);
        }
        // Outer first (bigger volume first)
        hits.sort((a, b) -> Long.compare(volume(b), volume(a)));
        return hits;
    }

    /** Pretty breadcrumb like "Wilderness - District - Square - Alice's Shop". */
    public synchronized String breadcrumbAt(Identifier dim, BlockPos pos) {
        List<Region> path = pathAt(dim, pos);
        if (path.isEmpty()) return "Wilderness";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append(" - ");
            sb.append(path.get(i).name);
        }
        return sb.toString();
    }

    /** Unmodifiable copy for admin GUIs, etc. */
    public synchronized List<Region> all() {
        return new ArrayList<>(regions);
    }

    /** Send the admin list of nearby regions (same dim, AABB intersects a radius-cube around player). */
    public synchronized void sendNearbyTo(net.minecraft.server.network.ServerPlayerEntity player, int radius) {
        net.minecraft.util.Identifier dim = player.getWorld().getRegistryKey().getValue();
        net.minecraft.util.math.BlockPos c = player.getBlockPos();

        int r = Math.max(0, radius);
        int minX = c.getX() - r, maxX = c.getX() + r;
        int minY = c.getY() - r, maxY = c.getY() + r;
        int minZ = c.getZ() - r, maxZ = c.getZ() + r;

        java.util.List<Region> list = new java.util.ArrayList<>();
        for (Region rg : regions) {
            if (!rg.dim.equals(dim)) continue;

            int aMinX = Math.min(rg.a.getX(), rg.b.getX());
            int aMaxX = Math.max(rg.a.getX(), rg.b.getX());
            int aMinY = Math.min(rg.a.getY(), rg.b.getY());
            int aMaxY = Math.max(rg.a.getY(), rg.b.getY());
            int aMinZ = Math.min(rg.a.getZ(), rg.b.getZ());
            int aMaxZ = Math.max(rg.a.getZ(), rg.b.getZ());

            // AABB vs AABB (axis-aligned) intersection
            boolean intersects =
                    aMinX <= maxX && aMaxX >= minX &&
                            aMinY <= maxY && aMaxY >= minY &&
                            aMinZ <= maxZ && aMaxZ >= minZ;

            if (intersects) list.add(rg);
        }

        // Hand off to your packet helper that the admin panel listens to:
        com.fugginbeenus.locationtooltip.net.LTPackets.sendAdminList(player, list.toArray(new Region[0]));
    }

    /* ------------------------------ Helpers ------------------------------- */

    private static boolean contains(Region r, BlockPos p) {
        int minX = Math.min(r.a.getX(), r.b.getX());
        int minY = Math.min(r.a.getY(), r.b.getY());
        int minZ = Math.min(r.a.getZ(), r.b.getZ());
        int maxX = Math.max(r.a.getX(), r.b.getX());
        int maxY = Math.max(r.a.getY(), r.b.getY());
        int maxZ = Math.max(r.a.getZ(), r.b.getZ());
        return p.getX() >= minX && p.getX() <= maxX
                && p.getY() >= minY && p.getY() <= maxY
                && p.getZ() >= minZ && p.getZ() <= maxZ;
    }

    private static long volume(Region r) {
        long dx = Math.abs(r.a.getX() - r.b.getX()) + 1L;
        long dy = Math.abs(r.a.getY() - r.b.getY()) + 1L;
        long dz = Math.abs(r.a.getZ() - r.b.getZ()) + 1L;
        return dx * dy * dz;
    }
}
