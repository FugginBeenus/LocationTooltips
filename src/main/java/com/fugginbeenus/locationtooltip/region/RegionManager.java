package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.adv.AdvancementUtil;
import com.fugginbeenus.locationtooltip.net.LTPackets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.fugginbeenus.locationtooltip.LocationTooltip.MOD_ID;
import com.fugginbeenus.locationtooltip.adv.AdvancementUtil;

/**
 * Region manager (per server). Stores regions per-dimension, provides CRUD and queries.
 * Compatible with Region having fields { id, name, dim, min, max } and a contains() method.
 */
public final class RegionManager {

    // ----- instance per server -----
    private static final Map<UUID, RegionManager> BY_SERVER = new ConcurrentHashMap<>();

    public static RegionManager of(MinecraftServer server) {
        // stable key without relying on getServerUuid mappings
        UUID key = new UUID(0L, System.identityHashCode(server));
        return BY_SERVER.computeIfAbsent(key, k -> new RegionManager(server));
    }

    // ----- state -----
    private final MinecraftServer server;
    private final Map<Identifier, List<Region>> byDim = new HashMap<>();

    private RegionManager(MinecraftServer server) {
        this.server = server;
        loadAll();
    }

    // ===== persistence =====

    private void loadAll() {
        byDim.clear();
        server.getWorlds().forEach(sw -> {
            Identifier dim = sw.getRegistryKey().getValue();
            List<Region> list = RegionStorage.load(server, dim);
            byDim.put(dim, new ArrayList<>(list));
        });
    }

    private void saveDim(Identifier dim) {
        List<Region> list = byDim.get(dim);
        if (list == null) list = Collections.emptyList();
        RegionStorage.save(server, dim, list);
    }

    // ===== helpers =====

    private List<Region> listFor(Identifier dim) {
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
        long dx = (long) (r.max.getX() - r.min.getX() + 1);
        long dy = (long) (r.max.getY() - r.min.getY() + 1);
        long dz = (long) (r.max.getZ() - r.min.getZ() + 1);
        return Math.max(1L, dx) * Math.max(1L, dy) * Math.max(1L, dz);
    }

    // ===== API =====

    /** Send regions in caller's dimension within radius, sorted by distance then name. */
    public void sendNearbyTo(ServerPlayerEntity player, int radius) {
        Identifier dim = player.getWorld().getRegistryKey().getValue();
        BlockPos p = player.getBlockPos();

        List<Region> all = listFor(dim);
        List<Region> near = new ArrayList<>();
        int r2 = radius * radius;

        for (Region r : all) {
            double d2 = distance2ToBox(p, boxOf(r));
            if (d2 <= r2) near.add(r);
        }

        near.sort(
                Comparator
                        .comparingDouble((Region r) -> distance2ToBox(p, boxOf(r)))
                        .thenComparing((Region r) -> r.name, String.CASE_INSENSITIVE_ORDER)
        );

        // LTPackets send method should accept List<Region> (client builds rows)
        LTPackets.sendAdminList(player, near);
    }

    /** Create a new region in the player's current dimension. */
    public void createRegion(ServerPlayerEntity player, String name, BlockPos a, BlockPos b) {
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

        Region region = new Region(java.util.UUID.randomUUID().toString(), name, dim, na, nb);
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

    /** Rename by id; persists and refreshes the caller's list and HUD. */
    public void renameRegion(ServerPlayerEntity player, String id, String newName) {
        Region r = findById(id);
        if (r == null) return;
        r.name = newName;
        saveDim(r.dim);
        sendNearbyTo(player, 512);
        pushNameTo(player);
    }

    /** Delete by id; persists and refreshes the caller's list and HUD. */
    public void deleteRegion(ServerPlayerEntity player, String id) {
        Region r = findById(id);
        if (r == null) return;
        List<Region> list = listFor(r.dim);
        list.removeIf(x -> x.id.equals(id));
        saveDim(r.dim);
        sendNearbyTo(player, 512);
        pushNameTo(player);
    }

    /** Smallest-volume region containing pos in dim (nested → inner wins). */
    public @Nullable Region smallestContaining(Identifier dim, BlockPos pos) {
        var list = byDim.get(dim);
        if (list == null || list.isEmpty()) return null;
        Region best = null;
        long bestVol = Long.MAX_VALUE;
        for (Region r : list) {
            if (!r.contains(pos)) continue;
            long vol = volume(r);
            if (vol < bestVol) { bestVol = vol; best = r; }
        }
        return best;
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
}
