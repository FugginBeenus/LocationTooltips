package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.adv.AdvancementUtil;
import com.fugginbeenus.locationtooltip.net.LTPackets;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlag;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import com.fugginbeenus.locationtooltip.util.LTId;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.fugginbeenus.locationtooltip.LocationTooltip.MOD_ID;

public final class RegionManager {
    private static final Map<UUID, RegionManager> BY_SERVER = new ConcurrentHashMap<>();

    public static RegionManager of(MinecraftServer server) {
        UUID key = new UUID(0L, System.identityHashCode(server));
        return BY_SERVER.computeIfAbsent(key, k -> new RegionManager(server));
    }

    public static void cleanup(MinecraftServer server) {
        UUID key = new UUID(0L, System.identityHashCode(server));
        RegionManager mgr = BY_SERVER.remove(key);
        if (mgr != null) {
            mgr.flushDirty();
            mgr.spatialIndex.clear();
            mgr.byDim.clear();
        }
    }

    private final MinecraftServer server;
    private final Map<ResourceLocation, List<Region>> byDim = new HashMap<>();

    private final Map<ResourceLocation, Map<ChunkPos, List<Region>>> spatialIndex = new HashMap<>();

    private final Set<ResourceLocation> dirtyDims = new HashSet<>();

    private long lookupCount = 0;
    private long lookupTimeNanos = 0;

    private RegionManager(MinecraftServer server) {
        this.server = server;
        loadAll();
    }

    private void loadAll() {
        byDim.clear();
        spatialIndex.clear();
        server.getAllLevels().forEach(sw -> {
            ResourceLocation dim = sw.dimension().location();
            List<Region> list = RegionStorage.load(server, dim);
            byDim.put(dim, new ArrayList<>(list));
            rebuildSpatialIndex(dim);
        });
    }

    private void saveDim(ResourceLocation dim) {
        List<Region> list = byDim.get(dim);
        if (list == null) list = Collections.emptyList();
        RegionStorage.save(server, dim, list);
        rebuildSpatialIndex(dim);
    }

    private void rebuildSpatialIndex(ResourceLocation dim) {
        Map<ChunkPos, List<Region>> index = new HashMap<>();
        List<Region> regions = byDim.get(dim);

        if (regions != null) {
            for (Region r : regions) {
                int minChunkX = r.min.getX() >> 4;
                int maxChunkX = r.max.getX() >> 4;
                int minChunkZ = r.min.getZ() >> 4;
                int maxChunkZ = r.max.getZ() >> 4;

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

    private void indexRegionIncremental(ResourceLocation dim, Region r) {
        Map<ChunkPos, List<Region>> index = spatialIndex.computeIfAbsent(dim, d -> new HashMap<>());
        int minCX = r.min.getX() >> 4, maxCX = r.max.getX() >> 4;
        int minCZ = r.min.getZ() >> 4, maxCZ = r.max.getZ() >> 4;
        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                index.computeIfAbsent(new ChunkPos(cx, cz), k -> new ArrayList<>()).add(r);
            }
        }
    }

    private List<Region> listFor(@Nullable ResourceLocation dim) {
        if (dim == null) {
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

    private static AABB boxOf(Region r) {
        return new AABB(
                r.min.getX(), r.min.getY(), r.min.getZ(),
                r.max.getX() + 1, r.max.getY() + 1, r.max.getZ() + 1
        );
    }

    private static double distance2ToBox(BlockPos p, AABB bb) {
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
        return r.volume();
    }

    public void sendNearbyTo(ServerPlayer player, int radius) {
        ResourceLocation dim = player.level().dimension().location();
        BlockPos p = player.blockPosition();
        boolean isOp = com.fugginbeenus.locationtooltip.util.LTPerms.isAdmin(player);

        List<Region> all = listFor(dim);
        List<Region> near = new ArrayList<>();
        int r2 = radius * radius;

        for (Region r : all) {
            double d2 = distance2ToBox(p, boxOf(r));
            if (d2 <= r2) {
                if (isOp || r.isOwnedBy(player.getUUID())) {
                    near.add(r);
                }
            }
        }

        near.sort(
                Comparator
                        .comparingDouble((Region r) -> distance2ToBox(p, boxOf(r)))
                        .thenComparing((Region r) -> r.name, String.CASE_INSENSITIVE_ORDER)
        );

        LTPackets.sendAdminList(player, near, isOp);
    }

    public void sendAllTo(ServerPlayer player, @Nullable ResourceLocation dim) {
        boolean isOp = com.fugginbeenus.locationtooltip.util.LTPerms.isAdmin(player);

        List<Region> all = new ArrayList<>();
        for (Region r : listFor(dim)) {
            if (isOp || r.isOwnedBy(player.getUUID())) all.add(r);
        }
        all.sort(Comparator.comparing((Region r) -> r.name, String.CASE_INSENSITIVE_ORDER));

        LTPackets.sendAdminList(player, all, isOp);
    }

    public void createRegion(ServerPlayer player, String name, BlockPos a, BlockPos b, Map<String, Boolean> flags) {
        ResourceLocation dim = player.level().dimension().location();

        int minX = Math.min(a.getX(), b.getX());
        int maxX = Math.max(a.getX(), b.getX());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxZ = Math.max(a.getZ(), b.getZ());
        int minY = Math.min(a.getY(), b.getY()) - 1;
        int maxY = Math.max(a.getY(), b.getY()) + 4;

        var sw = player.level();
        minY = Math.max(sw.getMinBuildHeight(), minY);
        maxY = Math.min(sw.getMaxBuildHeight() - 1, maxY);

        BlockPos na = new BlockPos(minX, minY, minZ);
        BlockPos nb = new BlockPos(maxX, maxY, maxZ);

        Region region = new Region(java.util.UUID.randomUUID().toString(), name, dim, na, nb, player.getUUID());

        region.source = RegionSource.PLAYER;
        if (flags != null) {
            for (Map.Entry<String, Boolean> e : flags.entrySet()) {
                region.setFlag(e.getKey(), e.getValue());
            }
        }

        listFor(dim).add(region);
        saveDim(dim);

        LTPackets.sendRegionCreatedCelebrate(player, region.name, region.min, region.max);
        SelectionManager.clear(player);
        pushNameTo(player);
        grantFirstRegion(player);

        final UUID who = player.getUUID();
        final var advId = LTId.of(MOD_ID, "first_region");

        com.fugginbeenus.locationtooltip.server.RegionTicker.later(player.level().getServer(), 250, () -> {
            ServerPlayer again = player.level().getServer().getPlayerList().getPlayer(who);
            if (again == null) return;

            com.fugginbeenus.locationtooltip.adv.AdvancementUtil.grant(again, advId);
        });
    }

    private boolean canEdit(ServerPlayer player, Region r) {
        boolean isOp = com.fugginbeenus.locationtooltip.util.LTPerms.isAdmin(player);
        return r.canBeEditedBy(player.getUUID(), isOp);
    }

    private static void denyEdit(ServerPlayer player) {
        com.fugginbeenus.locationtooltip.util.LTChat.tell(player, 
                Component.literal("You don't have permission to modify this region.").withStyle(ChatFormatting.RED),
                true /* action bar */
        );
    }

    public void renameRegion(ServerPlayer player, String id, String newName, Map<String, Boolean> flags) {
        Region r = findById(id);
        if (r == null) return;
        if (!canEdit(player, r)) { denyEdit(player); return; }
        r.name = newName;

        if (r.source == RegionSource.STRUCTURE) r.source = RegionSource.PLAYER;

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

    public void deleteRegion(ServerPlayer player, String id) {
        Region r = findById(id);
        if (r == null) return;
        if (!canEdit(player, r)) { denyEdit(player); return; }
        List<Region> list = listFor(r.dim);
        list.removeIf(x -> x.id.equals(id));
        saveDim(r.dim);
        sendNearbyTo(player, 512);
        pushNameTo(player);
    }

    public void setFlagAtPlayer(ServerPlayer player, String flagId, Boolean value) {
        var dim = player.level().dimension().location();
        Region r = smallestContaining(dim, player.blockPosition());
        if (r == null) {
            com.fugginbeenus.locationtooltip.util.LTChat.tell(player, Component.literal("You're not standing in a region.").withStyle(ChatFormatting.RED), false);
            return;
        }
        if (!canEdit(player, r)) { denyEdit(player); return; }

        RegionFlag f = RegionFlags.byId(flagId);
        if (f == null) {
            com.fugginbeenus.locationtooltip.util.LTChat.tell(player, Component.literal("Unknown flag: " + flagId).withStyle(ChatFormatting.RED), false);
            return;
        }

        if (value == null) r.clearFlag(flagId); else r.setFlag(flagId, value);
        saveDim(r.dim);

        String state = (value == null) ? "inherit" : (value ? "allow" : "deny");
        com.fugginbeenus.locationtooltip.util.LTChat.tell(player, 
                Component.literal("Set ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(f.displayName).withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" → " + state + " for ").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(r.name).withStyle(ChatFormatting.AQUA)),
                false);
    }

    public void listFlagsAtPlayer(ServerPlayer player) {
        var dim = player.level().dimension().location();
        Region r = smallestContaining(dim, player.blockPosition());
        if (r == null) {
            com.fugginbeenus.locationtooltip.util.LTChat.tell(player, Component.literal("You're not standing in a region.").withStyle(ChatFormatting.RED), false);
            return;
        }

        com.fugginbeenus.locationtooltip.util.LTChat.tell(player, 
                Component.literal("Flags for ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(r.name).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(":").withStyle(ChatFormatting.GOLD)),
                false);

        for (RegionFlag f : RegionFlags.all()) {
            Boolean ov = r.getFlagOverride(f.id);
            String state = (ov == null)
                    ? "inherit (default " + (f.defaultValue ? "allow" : "deny") + ")"
                    : (ov ? "allow" : "deny");
            ChatFormatting color = (ov == null) ? ChatFormatting.GRAY : (ov ? ChatFormatting.GREEN : ChatFormatting.RED);
            com.fugginbeenus.locationtooltip.util.LTChat.tell(player, 
                    Component.literal("  " + f.id + ": ").withStyle(ChatFormatting.WHITE)
                            .append(Component.literal(state).withStyle(color)),
                    false);
        }
    }

    public boolean exists(String id) {
        return findById(id) != null;
    }

    public @Nullable Region smallestStructureContaining(ResourceLocation dim, BlockPos pos) {
        for (Region r : allContaining(dim, pos)) {
            if (r.source == RegionSource.STRUCTURE) return r;
        }
        return null;
    }

    public void addStructureRegion(ResourceLocation dim, Region r) {
        listFor(dim).add(r);
        indexRegionIncremental(dim, r);
        dirtyDims.add(dim);
    }

    public void touchDim(ResourceLocation dim) {
        dirtyDims.add(dim);
    }

    public void flushDirty() {
        if (dirtyDims.isEmpty()) return;
        for (ResourceLocation dim : dirtyDims) {
            RegionStorage.save(server, dim, byDim.getOrDefault(dim, Collections.emptyList()));
        }
        dirtyDims.clear();
    }

    public int countBySource(RegionSource src) {
        int n = 0;
        for (var list : byDim.values()) {
            for (Region r : list) if (r.source == src) n++;
        }
        return n;
    }

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

    public @Nullable Region smallestContaining(ResourceLocation dim, BlockPos pos) {
        long startTime = System.nanoTime();
        lookupCount++;

        try {
            Map<ChunkPos, List<Region>> index = spatialIndex.get(dim);
            if (index == null) return null;

            ChunkPos cp = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
            List<Region> candidates = index.get(cp);
            if (candidates == null || candidates.isEmpty()) return null;

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

    public List<Region> allContaining(ResourceLocation dim, BlockPos pos) {
        Map<ChunkPos, List<Region>> index = spatialIndex.get(dim);
        if (index == null) return Collections.emptyList();
        List<Region> candidates = index.get(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
        if (candidates == null || candidates.isEmpty()) return Collections.emptyList();

        List<Region> out = new ArrayList<>();
        for (Region r : candidates) {
            if (r.contains(pos)) out.add(r);
        }
        out.sort(Comparator.comparingLong(Region::volume));
        return out;
    }

    public boolean resolveFlag(ResourceLocation dim, BlockPos pos, String flagId) {
        for (Region r : allContaining(dim, pos)) {
            Boolean v = r.getFlagOverride(flagId);
            if (v != null) return v;
        }
        RegionFlag f = RegionFlags.byId(flagId);
        return f != null ? f.defaultValue : true;
    }

    public String currentRegionName(ResourceLocation dim, BlockPos pos) {
        Region r = smallestContaining(dim, pos);
        return (r != null) ? r.name : DimensionNames.wilderness(dim);
    }

    public void pushNameTo(ServerPlayer player) {
        var dim = player.level().dimension().location();
        var pos = player.blockPosition();
        String name = currentRegionName(dim, pos);
        LTPackets.sendRegionUpdate(player, name);
    }

    private static void grantFirstRegion(ServerPlayer player) {
        com.fugginbeenus.locationtooltip.adv.AdvancementUtil.grant(
                player, LTId.of("locationtooltip", "first_region"));
    }

    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();

        int totalRegions = byDim.values().stream().mapToInt(List::size).sum();
        stats.put("total_regions", totalRegions);

        stats.put("dimensions", byDim.size());
        stats.put("avg_regions_per_dim", byDim.isEmpty() ? 0 : totalRegions / (double) byDim.size());

        stats.put("lookup_count", lookupCount);
        if (lookupCount > 0) {
            stats.put("avg_lookup_micros", lookupTimeNanos / lookupCount / 1000.0);
        }

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

    public void resetStats() {
        lookupCount = 0;
        lookupTimeNanos = 0;
    }
}
