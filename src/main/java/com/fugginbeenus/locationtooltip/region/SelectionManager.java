package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;
import net.minecraft.world.Heightmap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SelectionManager {

    private static final class Selection {
        BlockPos a, b;
        Selection(BlockPos a) { this.a = a; }
        void setB(BlockPos b) { this.b = b; }
        boolean ready() { return a != null && b != null; }
    }

    private static final Map<UUID, Selection> CURRENT = new HashMap<>();
    private static final DustParticleEffect OUTLINE =
            new DustParticleEffect(new Vector3f(0.2f, 0.9f, 0.9f), 1.0f);

    private SelectionManager() {}

    // ---------- API used by RegionWandItem / screens ----------

    public static void setFirst(ServerPlayerEntity p, BlockPos a)  {
        CURRENT.computeIfAbsent(p.getUuid(), id -> new Selection(a)).a = a;
    }

    public static void setSecond(ServerPlayerEntity p, BlockPos b) {
        CURRENT.computeIfAbsent(p.getUuid(), id -> new Selection(null)).b = b;
    }

    public static BlockPos getFirst(ServerPlayerEntity p) {
        var s = CURRENT.get(p.getUuid());
        return s == null ? null : s.a;
    }

    public static boolean hasBoth(ServerPlayerEntity p) {
        var s = CURRENT.get(p.getUuid());
        return s != null && s.ready();
    }

    /** Clear the selection and stop the outlines. */
    public static void clear(ServerPlayerEntity p) { CURRENT.remove(p.getUuid()); }

    /** Open the naming screen for the player's current selection. */
    public static void openNamingScreen(ServerPlayerEntity p) {
        var s = CURRENT.get(p.getUuid());
        if (s == null || !s.ready()) return;
        LTPackets.openName(p, s.a, s.b);
    }

    // ---------- Ticking / visuals ----------

    public static void registerServerTicker() {
        ServerTickEvents.END_SERVER_TICK.register(SelectionManager::tick);
    }

    private static void tick(MinecraftServer server) {
        if (CURRENT.isEmpty()) return;

        for (var entry : CURRENT.entrySet()) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player == null) continue;
            var sel = entry.getValue();
            if (!sel.ready()) continue;

            ServerWorld w = (ServerWorld) player.getWorld();
            BlockPos a = sel.a, b = sel.b;

            int minX = Math.min(a.getX(), b.getX());
            int minY = Math.min(a.getY(), b.getY());
            int minZ = Math.min(a.getZ(), b.getZ());
            int maxX = Math.max(a.getX(), b.getX());
            int maxY = Math.max(a.getY(), b.getY());
            int maxZ = Math.max(a.getZ(), b.getZ());

            // Throttle particle count roughly with perimeter
            int step = Math.max(1, (maxX - minX + maxZ - minZ) / 80);

            // vertical edges
            for (int y = minY; y <= maxY; y += step) {
                w.spawnParticles(OUTLINE, minX + 0.5, y + 0.1, minZ + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, minX + 0.5, y + 0.1, maxZ + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, maxX + 0.5, y + 0.1, minZ + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, maxX + 0.5, y + 0.1, maxZ + 0.5, 1, 0, 0, 0, 0);
            }

            // bottom/top perimeters
            for (int x = minX; x <= maxX; x += step) {
                w.spawnParticles(OUTLINE, x + 0.5, minY + 0.1, minZ + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, x + 0.5, minY + 0.1, maxZ + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, x + 0.5, maxY + 0.1, minZ + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, x + 0.5, maxY + 0.1, maxZ + 0.5, 1, 0, 0, 0, 0);
            }
            for (int z = minZ; z <= maxZ; z += step) {
                w.spawnParticles(OUTLINE, minX + 0.5, minY + 0.1, z + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, maxX + 0.5, minY + 0.1, z + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, minX + 0.5, maxY + 0.1, z + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, maxX + 0.5, maxY + 0.1, z + 0.5, 1, 0, 0, 0, 0);
            }

            // surface layer along heightmap (ensures visibility above terrain)
            for (int x = minX; x <= maxX; x += 2) {
                int yA = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, minZ);
                int yB = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, maxZ);
                w.spawnParticles(OUTLINE, x + 0.5, yA + 0.2, minZ + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, x + 0.5, yB + 0.2, maxZ + 0.5, 1, 0, 0, 0, 0);
            }
            for (int z = minZ; z <= maxZ; z += 2) {
                int yA = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, minX, z);
                int yB = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, maxX, z);
                w.spawnParticles(OUTLINE, minX + 0.5, yA + 0.2, z + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, maxX + 0.5, yB + 0.2, z + 0.5, 1, 0, 0, 0, 0);
            }

            // “-1y” layer (just below minY so underground boxes still read)
            double yBelow = minY - 0.9;
            for (int x = minX; x <= maxX; x += 2) {
                w.spawnParticles(OUTLINE, x + 0.5, yBelow, minZ + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, x + 0.5, yBelow, maxZ + 0.5, 1, 0, 0, 0, 0);
            }
            for (int z = minZ; z <= maxZ; z += 2) {
                w.spawnParticles(OUTLINE, minX + 0.5, yBelow, z + 0.5, 1, 0, 0, 0, 0);
                w.spawnParticles(OUTLINE, maxX + 0.5, yBelow, z + 0.5, 1, 0, 0, 0, 0);
            }
        }
    }
}
