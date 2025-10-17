package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.net.LTPackets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.particle.DustParticleEffect;
import org.joml.Vector3f;

import java.util.*;

/**
 * Handles the player's active selection with the Region Wand.
 */
public class SelectionManager {

    private static final Map<UUID, BlockPos> FIRST = new HashMap<>();
    private static final Map<UUID, BlockPos> SECOND = new HashMap<>();

    public static void setFirst(ServerPlayerEntity player, BlockPos pos) { FIRST.put(player.getUuid(), pos); }
    public static void setSecond(ServerPlayerEntity player, BlockPos pos) { SECOND.put(player.getUuid(), pos); }
    public static BlockPos getFirst(ServerPlayerEntity player) { return FIRST.get(player.getUuid()); }
    public static BlockPos getSecond(ServerPlayerEntity player) { return SECOND.get(player.getUuid()); }
    public static boolean hasBoth(ServerPlayerEntity player) { return FIRST.containsKey(player.getUuid()) && SECOND.containsKey(player.getUuid()); }
    public static void clear(ServerPlayerEntity player) { FIRST.remove(player.getUuid()); SECOND.remove(player.getUuid()); }

    public static void openNamingScreen(ServerPlayerEntity player) {
        BlockPos a = FIRST.get(player.getUuid());
        BlockPos b = SECOND.get(player.getUuid());
        if (a != null && b != null) LTPackets.sendOpenName(player, a, b);
    }

    /** Draw selection border particles (server-side). */
    public static void showSelectionParticles(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        BlockPos a = FIRST.get(player.getUuid());
        BlockPos b = SECOND.get(player.getUuid());
        if (a == null || b == null) return;

        int minX = Math.min(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY()) - 1;
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxX = Math.max(a.getX(), b.getX());
        int maxY = Math.max(a.getY(), b.getY()) + 4;
        int maxZ = Math.max(a.getZ(), b.getZ());

        var effect = new DustParticleEffect(new Vector3f(0.2f, 0.6f, 1.0f), 1.0f);
        int step = 1;

        for (int x = minX; x <= maxX; x += step) {
            for (int z = minZ; z <= maxZ; z += step) {
                if (x == minX || x == maxX || z == minZ || z == maxZ) {
                    world.spawnParticles(effect, x + 0.5, (double) minY, z + 0.5, 1, 0, 0, 0, 0);
                    world.spawnParticles(effect, x + 0.5, (double) maxY, z + 0.5, 1, 0, 0, 0, 0);
                }
            }
        }
        for (int y = minY; y <= maxY; y += step) {
            world.spawnParticles(effect, minX + 0.5, (double) y, minZ + 0.5, 1, 0, 0, 0, 0);
            world.spawnParticles(effect, minX + 0.5, (double) y, maxZ + 0.5, 1, 0, 0, 0, 0);
            world.spawnParticles(effect, maxX + 0.5, (double) y, minZ + 0.5, 1, 0, 0, 0, 0);
            world.spawnParticles(effect, maxX + 0.5, (double) y, maxZ + 0.5, 1, 0, 0, 0, 0);
        }
    }

    /** Server tick update for all selections. Call this each tick. */
    public static void tickAll(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (hasBoth(player)) showSelectionParticles(player);
        }
    }
}
