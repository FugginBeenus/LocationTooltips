package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.adv.AdvancementUtil;
import com.fugginbeenus.locationtooltip.net.LTPackets;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlag;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.fugginbeenus.locationtooltip.LocationTooltip.MOD_ID;

/**
 * Region manager (per server). Stores regions per-dimension, provides CRUD and queries.
 *
 * OPTIMIZED with spatial indexing:
 * - Chunk-based spatial index for O(1) region candidate selection
 * - Reduced linear searches from O(n) to O(k) where k = regions per chunk
 * - Performance monitoring capabilities
 * - Proper cleanup to prevent memory leaks
 */
public final class RegionManager {

    // ----- instance per server -----
    private static final Map<UUID, RegionManager> BY_SERVER = new ConcurrentHashMap<>();

    public static RegionManager of(MinecraftServer server) {
        // stable key without relying on getServerUuid mappings
        UUID key = new UUID(0L, System.identityHashCode(server));
        return BY_SERVER.computeIfAbsent(key, k -> new RegionManager(server));
    }

    /**
     * Clean up when server stops to prevent memory leaks.
     * Call from ServerLifecycleEvents.SERVER_STOPPING
     */
    public static void cleanup(MinecraftServer server) {
        UUID key = new UUID(0L, System.identityHashCode(server));
        RegionManager mgr = BY_SERVER.remove(key);
        if (mgr != null) {
            mgr.flushDirty();   // persist any pending structure regions before shutdown
            mgr.spatialIndex.clear();
            mgr.byDim.clear();
        }
    }

    // ----- state -----
    private final MinecraftServer server;
    private final Map<Identifier, List<Region>> byDim = new HashMap<>();

    // OPTIMIZATION: Spatial index - maps chunks to regions that intersect them
    private final Map<Identifier, Map<ChunkPos, List<Region>>> spatialIndex = new HashMap<>();

    // Dimensions touched by structure auto-tagging since the last debounced disk flush.
    private final Set<Identifier> dirtyDims = new HashSet<>();

    // Performance tracking
    private long lookupCount = 0;
    private long lookupTimeNanos = 0;

    private RegionManager(MinecraftServer server) {
        this.server = server;
        loadAll();
    }

    // ===== persistence =====

    private void loadAll() {
        byDim.clear();
        spatialIndex.clear();
        server.getWorlds().forEach(sw -> {
            Identifier dim = sw.getRegistryKey().getValue();
            List<Region> list = RegionStorage.load(server, dim);
            byDim.put(dim, new ArrayList<>(list));
            rebuildSpatialIndex(dim);
        });
    }

    private void saveDim(Identifier dim) {
        List<Region> list = byDim.get(dim);
        if (list == null) list = Collections.emptyList();
        RegionStorage.save(server, dim, list);
        rebuildSpatialIndex(dim); // Rebuild index after save
    }

    /**
     * OPTIMIZATION: Rebuild spatial index for a dimension.
     * Called after loading or modifying regions.
     */
    private void rebuildSpatialIndex(Identifier dim) {
        Map<ChunkPos, List<Region>> index = new HashMap<>();
        List<Region> regions = byDim.get(dim);

        if (regions != null) {
            for (Region r : regions) {
                // Calculate which chunks this region spans
                int minChunkX = r.min.getX() >> 4;
                int maxChunkX = r.max.getX() >> 4;
                int minChunkZ = r.min.getZ() >> 4;
                int maxChunkZ = r.max.getZ() >> 4;

                // Register region in all chunks it overlaps
                for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                    for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                        ChunkPos cp = new ChunkPos(cx, cz);
                        index.computeIfAbsent(cp, k -> new ArrayList<>()).add(r);
                    }
                }
            }
        }

        spatialIndex.put(dim, index);
    }

    /** Add a single region to the spatial index without rebuilding the whole thing. */
    private void indexRegionIncremental(Identifier dim, Region r) {
        Map<ChunkPos, List<Region>> index = spatialIndex.computeIfAbsent(dim, d -> new HashMap<>());
        int minCX = r.min.getX() >> 4, maxCX = r.max.getX() >> 4;
        int minCZ = r.min.getZ() >> 4, maxCZ = r.max.getZ() >> 4;
        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                index.computeIfAbsent(new ChunkPos(cx, cz), k -> new ArrayList<>()).add(r);
            }
        }
    }

    // ===== helpers =====

    private List<Region> listFor(@Nullable Identifier dim) {
        if (dim == null) {
            // null = flatten regions across all dimensions
            List<Region> all = new ArrayList<>();
            for (List<Region> list : byDim.values()) all.addAll(list);
            return all;
        }
        return byDim.computeIfAbsent(dim, d -> new ArrayList<>());
    }

    private Region findById(String id) {
        for (var list : byDim.values()) {
            for (var r : list) if (r.id.equals(id)) return r;
        }
        return null;
    }

    private static Box boxOf(Region r) {
        // +1 because BlockPos are voxel corners; Box is min..max as doubles
        return new Box(
                r.min.getX(), r.min.getY(), r.min.getZ(),
                r.max.getX() + 1, r.max.getY() + 1, r.max.getZ() + 1
        );
    }

    private static double distance2ToBox(BlockPos p, Box bb) {
        double px = p.getX() + 0.5, py = p.getY() + 0.5, pz = p.getZ() + 0.5;
        double dx = clamp(px, bb.minX, bb.maxX) - px;
        double dy = clamp(py, bb.minY, bb.maxY) - py;
        double dz = clamp(pz, bb.minZ, bb.maxZ) - pz;
        return dx*dx + dy*dy + dz*dz;
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private static long volume(Region r) {
        return r.volume(); // Delegate to Region's optimized method
    }

    // ===== API =====

    /** Send regions in caller's dimension within radius, sorted by distance then name. */
    public void sendNearbyTo(ServerPlayerEntity player, int radius) {
        Identifier dim = player.getWorld().getRegistryKey().getValue();
        BlockPos p = player.getBlockPos();
        boolean isOp = player.server.getPlayerManager().isOperator(player.getGameProfile());

        List<Region> all = listFor(dim);
        List<Region> near = new ArrayList<>();
        int r2 = radius * radius;

        for (Region r : all) {
            double d2 = distance2ToBox(p, boxOf(r));
            if (d2 <= r2) {
                // Filter: admins see all, players see only their own
                if (isOp || r.isOwnedBy(player.getUuid())) {
                    near.add(r);
                }
            }
        }

        near.sort(
                Comparator
                        .comparingDouble((Region r) -> distance2ToBox(p, boxOf(r)))
                        .thenComparing((Region r) -> r.name, String.CASE_INSENSITIVE_ORDER)
        );

        // LTPackets send method should accept List<Region> (client builds rows)
        LTPackets.sendAdminList(player, near, isOp);
    }

    /**
     * Send ALL regions to a player (no distance filtering), optionally limited to one
     * dimension (null = every dimension). Honours the same owner/op visibility filter as
     * {@link #sendNearbyTo}. Originally contributed by GambaPVP (all-locations-packet).
     */
    public void sendAllTo(ServerPlayerEntity player, @Nullable Identifier dim) {
        boolean isOp = player.server.getPlayerManager().isOperator(player.getGameProfile());

        List<Region> all = new ArrayList<>();
        for (Region r : listFor(dim)) {
            if (isOp || r.isOwnedBy(player.getUuid())) all.add(r);
        }
        all.sort(Comparator.comparing((Region r) -> r.name, String.CASE_INSENSITIVE_ORDER));

        LTPackets.sendAdminList(player, all, isOp);
    }

    /** Create a new region in the player's current dimension. */
    public void createRegion(ServerPlayerEntity player, String name, BlockPos a, BlockPos b, Map<String, Boolean> flags) {
        Identifier dim = player.getWorld().getRegistryKey().getValue();

        // Expand Y so regions always “catch” the player (see section 3 below)
        int minX = Math.min(a.getX(), b.getX());
        int maxX = Math.max(a.getX(), b.getX());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxZ = Math.max(a.getZ(), b.getZ());
        int minY = Math.min(a.getY(), b.getY()) - 1;   // -1y like you wanted
        int maxY = Math.max(a.getY(), b.getY()) + 4;   // +4y like you wanted

        // Clamp to build limits so huge/void selections don’t explode
        var sw = player.getWorld();
        minY = Math.max(sw.getBottomY(), minY);
        maxY = Math.min(sw.getTopY() - 1, maxY);

        BlockPos na = new BlockPos(minX, minY, minZ);
        BlockPos nb = new BlockPos(maxX, maxY, maxZ);

        Region region = new Region(java.util.UUID.randomUUID().toString(), name, dim, na, nb, player.getUuid());

        // Apply protection flag overrides from the GUI (empty/absent = inherit defaults).
        region.source = RegionSource.PLAYER;
        if (flags != null) {
            for (Map.Entry<String, Boolean> e : flags.entrySet()) {
                region.setFlag(e.getKey(), e.getValue());
            }
        }

        listFor(dim).add(region);
        saveDim(dim);

        // Celebration & clear selection (unchanged)
        LTPackets.sendRegionCreatedCelebrate(player, region.name, region.min, region.max);
        SelectionManager.clear(player);
        pushNameTo(player);
        grantFirstRegion(player);

        // --- NEW: delay the advancement grant so the toast isn't hidden by the celebration ---
        final UUID who = player.getUuid();
        final var advId = new Identifier(MOD_ID, "first_region");
        // 60 ticks (~3 seconds) feels great; use 40 for ~2s if you prefer
        com.fugginbeenus.locationtooltip.server.RegionTicker.later(player.server, 250, () -> {
            ServerPlayerEntity again = player.server.getPlayerManager().getPlayer(who);
            if (again == null) return;
            // safe utility you already added
            com.fugginbeenus.locationtooltip.adv.AdvancementUtil.grant(again, advId);
        });
    }

    /** True if this player may modify the given region (ops always; otherwise only the owner). */
    private boolean canEdit(ServerPlayerEntity player, Region r) {
        boolean isOp = player.server.getPlayerManager().isOperator(player.getGameProfile());
        return r.canBeEditedBy(player.getUuid(), isOp);
    }

    private static void denyEdit(ServerPlayerEntity player) {
        player.sendMessage(
                Text.literal("You don't have permission to modify this region.").formatted(Formatting.RED),
                true /* action bar */
        );
    }

    /** Rename by id; persists and refreshes the caller's list and HUD. */
    public void renameRegion(ServerPlayerEntity player, String id, String newName, Map<String, Boolean> flags) {
        Region r = findById(id);
        if (r == null) return;
        if (!canEdit(player, r)) { denyEdit(player); return; }
        r.name = newName;
        // A manually-edited structure region becomes a normal region: it renders orange and is
        // no longer auto-managed (won't be re-named by the structure/Waystones logic).
        if (r.source == RegionSource.STRUCTURE) r.source = RegionSource.PLAYER;
        // Replace overrides entirely so clearing a flag in the GUI returns it to inherit.
        r.flagOverrides().clear();
        if (flags != null) {
            for (Map.Entry<String, Boolean> e : flags.entrySet()) {
                r.setFlag(e.getKey(), e.getValue());
            }
        }
        saveDim(r.dim);
        sendNearbyTo(player, 512);
        pushNameTo(player);
    }

    /** Delete by id; persists and refreshes the caller's list and HUD. */
    public void deleteRegion(ServerPlayerEntity player, String id) {
        Region r = findById(id);
        if (r == null) return;
        if (!canEdit(player, r)) { denyEdit(player); return; }
        List<Region> list = listFor(r.dim);
        list.removeIf(x -> x.id.equals(id));
        saveDim(r.dim);
        sendNearbyTo(player, 512);
        pushNameTo(player);
    }

    /**
     * Set (value = true/false) or clear (value = null → inherit) a flag on the smallest
     * region the player is standing in. Used by the {@code /ltregion flag} command.
     */
    public void setFlagAtPlayer(ServerPlayerEntity player, String flagId, Boolean value) {
        var dim = player.getWorld().getRegistryKey().getValue();
        Region r = smallestContaining(dim, player.getBlockPos());
        if (r == null) {
            player.sendMessage(Text.literal("You're not standing in a region.").formatted(Formatting.RED), false);
            return;
        }
        if (!canEdit(player, r)) { denyEdit(player); return; }

        RegionFlag f = RegionFlags.byId(flagId);
        if (f == null) {
            player.sendMessage(Text.literal("Unknown flag: " + flagId).formatted(Formatting.RED), false);
            return;
        }

        if (value == null) r.clearFlag(flagId); else r.setFlag(flagId, value);
        saveDim(r.dim);

        String state = (value == null) ? "inherit" : (value ? "allow" : "deny");
        player.sendMessage(
                Text.literal("Set ").formatted(Formatting.GREEN)
                        .append(Text.literal(f.displayName).formatted(Formatting.YELLOW))
                        .append(Text.literal(" → " + state + " for ").formatted(Formatting.GREEN))
                        .append(Text.literal(r.name).formatted(Formatting.AQUA)),
                false);
    }

    /** List every flag and its effective state for the region the player is standing in. */
    public void listFlagsAtPlayer(ServerPlayerEntity player) {
        var dim = player.getWorld().getRegistryKey().getValue();
        Region r = smallestContaining(dim, player.getBlockPos());
        if (r == null) {
            player.sendMessage(Text.literal("You're not standing in a region.").formatted(Formatting.RED), false);
            return;
        }

        player.sendMessage(
                Text.literal("Flags for ").formatted(Formatting.GOLD)
                        .append(Text.literal(r.name).formatted(Formatting.AQUA))
                        .append(Text.literal(":").formatted(Formatting.GOLD)),
                false);

        for (RegionFlag f : RegionFlags.all()) {
            Boolean ov = r.getFlagOverride(f.id);
            String state = (ov == null)
                    ? "inherit (default " + (f.defaultValue ? "allow" : "deny") + ")"
                    : (ov ? "allow" : "deny");
            Formatting color = (ov == null) ? Formatting.GRAY : (ov ? Formatting.GREEN : Formatting.RED);
            player.sendMessage(
                    Text.literal("  " + f.id + ": ").formatted(Formatting.WHITE)
                            .append(Text.literal(state).formatted(color)),
                    false);
        }
    }

    // ===== structure auto-tagging support =====

    /** True if a region with this id already exists (used to de-dupe structure tagging). */
    public boolean exists(String id) {
        return findById(id) != null;
    }

    /**
     * Add an auto-generated structure region: updates memory + the spatial index immediately
     * (so the HUD reflects it right away) and marks the dimension dirty for a debounced save.
     */
    public void addStructureRegion(Identifier dim, Region r) {
        listFor(dim).add(r);
        indexRegionIncremental(dim, r);
        dirtyDims.add(dim);
    }

    /** Mark a dimension dirty so its regions persist on the next flush (e.g. after an in-place rename). */
    public void touchDim(Identifier dim) {
        dirtyDims.add(dim);
    }

    /** Persist any dimensions touched by structure tagging since the last flush. */
    public void flushDirty() {
        if (dirtyDims.isEmpty()) return;
        for (Identifier dim : dirtyDims) {
            RegionStorage.save(server, dim, byDim.getOrDefault(dim, Collections.emptyList()));
        }
        dirtyDims.clear();
    }

    /** Count regions from a particular source (e.g. how many auto structure regions exist). */
    public int countBySource(RegionSource src) {
        int n = 0;
        for (var list : byDim.values()) {
            for (Region r : list) if (r.source == src) n++;
        }
        return n;
    }

    /** Remove all auto structure regions so they can be re-tagged as chunks reload. */
    public void rescanStructures() {
        for (var entry : byDim.entrySet()) {
            boolean changed = entry.getValue().removeIf(r -> r.source == RegionSource.STRUCTURE);
            if (changed) {
                rebuildSpatialIndex(entry.getKey());
                RegionStorage.save(server, entry.getKey(), entry.getValue());
            }
        }
        dirtyDims.clear();
    }

    /**
     * OPTIMIZED: Smallest-volume region containing pos in dim (nested → inner wins).
     * Uses spatial index for fast lookup - O(k) instead of O(n) where k = regions per chunk.
     */
    public @Nullable Region smallestContaining(Identifier dim, BlockPos pos) {
        long startTime = System.nanoTime();
        lookupCount++;

        try {
            Map<ChunkPos, List<Region>> index = spatialIndex.get(dim);
            if (index == null) return null;

            // Get candidate regions from the chunk containing this position
            ChunkPos cp = new ChunkPos(pos);
            List<Region> candidates = index.get(cp);
            if (candidates == null || candidates.isEmpty()) return null;

            // Find smallest containing region among candidates
            Region best = null;
            long bestVol = Long.MAX_VALUE;

            for (Region r : candidates) {
                if (!r.contains(pos)) continue;
                long vol = volume(r);
                if (vol < bestVol) {
                    bestVol = vol;
                    best = r;
                }
            }
            return best;
        } finally {
            lookupTimeNanos += System.nanoTime() - startTime;
        }
    }

    /**
     * All regions containing pos in dim, sorted innermost first (smallest volume).
     * Uses the spatial index, so it only scans regions registered to pos's chunk.
     */
    public List<Region> allContaining(Identifier dim, BlockPos pos) {
        Map<ChunkPos, List<Region>> index = spatialIndex.get(dim);
        if (index == null) return Collections.emptyList();
        List<Region> candidates = index.get(new ChunkPos(pos));
        if (candidates == null || candidates.isEmpty()) return Collections.emptyList();

        List<Region> out = new ArrayList<>();
        for (Region r : candidates) {
            if (r.contains(pos)) out.add(r);
        }
        out.sort(Comparator.comparingLong(Region::volume));
        return out;
    }

    /**
     * Resolve a flag's effective value at a position, honouring nested-region inheritance:
     * the innermost region with an explicit override wins; if none override it, the flag's
     * registered default applies. This is what protection handlers should call.
     */
    public boolean resolveFlag(Identifier dim, BlockPos pos, String flagId) {
        for (Region r : allContaining(dim, pos)) {
            Boolean v = r.getFlagOverride(flagId);
            if (v != null) return v;
        }
        RegionFlag f = RegionFlags.byId(flagId);
        return f != null ? f.defaultValue : true;
    }

    /** Best region name at a position in a dimension — smallest-volume match wins; “Wilderness” if none. */
    public String currentRegionName(Identifier dim, BlockPos pos) {
        Region r = smallestContaining(dim, pos);
        return (r != null) ? r.name : "Wilderness";
    }

    /** Push the active region name to the client HUD for this player. */
    public void pushNameTo(ServerPlayerEntity player) {
        var dim = player.getWorld().getRegistryKey().getValue();
        var pos = player.getBlockPos();
        String name = currentRegionName(dim, pos);
        LTPackets.sendRegionUpdate(player, name);
    }

    private static void grantFirstRegion(ServerPlayerEntity player) {
        // Grant: locationtooltip:first_region
        final net.minecraft.util.Identifier advId =
                new net.minecraft.util.Identifier("locationtooltip", "first_region");
        var adv = player.server.getAdvancementLoader().get(advId);
        if (adv == null) {
            // If you ever see this in logs, your JSON path/namespace is wrong.
            player.server.sendMessage(net.minecraft.text.Text.literal("[LocationTooltip] Missing advancement: " + advId));
            return;
        }
        var tracker = player.getAdvancementTracker();
        var progress = tracker.getProgress(adv);
        if (!progress.isDone()) {
            for (String crit : progress.getUnobtainedCriteria()) {
                tracker.grantCriterion(adv, crit);
            }
        }
    }

    // ===== Performance Monitoring =====

    /**
     * Get performance statistics for debugging and optimization
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();

        // Count total regions across all dimensions
        int totalRegions = byDim.values().stream().mapToInt(List::size).sum();
        stats.put("total_regions", totalRegions);

        // Calculate average regions per dimension
        stats.put("dimensions", byDim.size());
        stats.put("avg_regions_per_dim", byDim.isEmpty() ? 0 : totalRegions / (double) byDim.size());

        // Lookup performance
        stats.put("lookup_count", lookupCount);
        if (lookupCount > 0) {
            stats.put("avg_lookup_micros", lookupTimeNanos / lookupCount / 1000.0);
        }

        // Spatial index stats
        long totalChunks = spatialIndex.values().stream().mapToLong(Map::size).sum();
        stats.put("indexed_chunks", totalChunks);

        if (totalChunks > 0) {
            long totalEntries = spatialIndex.values().stream()
                    .flatMap(map -> map.values().stream())
                    .mapToLong(List::size)
                    .sum();
            stats.put("avg_regions_per_chunk", totalEntries / (double) totalChunks);
        }

        return stats;
    }

    /**
     * Reset performance counters
     */
    public void resetStats() {
        lookupCount = 0;
        lookupTimeNanos = 0;
    }
}