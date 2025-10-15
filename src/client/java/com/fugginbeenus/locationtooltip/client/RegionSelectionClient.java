package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.data.Bounds;
import com.fugginbeenus.locationtooltip.data.Region;
import com.fugginbeenus.locationtooltip.data.RegionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class RegionSelectionClient {
    static BlockPos a, b;

    public static void init() { a = null; b = null; }

    /** Normal right-click: set first/second corner & spawn outline; returns true when B is set. */
    public static boolean onWandUse(MinecraftClient mc, BlockPos hit) {
        var cfg = LTConfig.get();
        int y = cfg.placeOnTop ? (hit.getY() + 1) : hit.getY();
        BlockPos adjusted = new BlockPos(hit.getX(), y, hit.getZ());

        boolean setBNow = false;
        if (a == null) {
            a = adjusted;
            toast(mc, "§7First corner set.");
        } else if (b == null) {
            b = adjusted;
            setBNow = true;
            toast(mc, "§7Second corner set. §fShift-Right-Click §7to name & save.");
        } else {
            a = adjusted;
            b = null;
            toast(mc, "§7First corner reset.");
        }

        outlinePickParticles(mc);
        return setBNow;
    }

    /** Sneak-right-click: if A+B → name screen; else try rename region under cursor (owner/admin). */
    public static void onWandSneak(MinecraftClient mc, BlockPos hit) {
        if (a != null && b != null) {
            mc.execute(() -> mc.setScreen(new RegionNameScreen()));
            return;
        }
        Region target = RegionManager.getDeepestAt(mc.world, hit);
        if (target == null) { toast(mc, "§cNo region here to rename."); return; }
        if (!RegionManager.canRename(mc.player.getUuid(), target)) {
            toast(mc, "§cYou can’t rename this region."); return;
        }
        mc.execute(() -> mc.setScreen(new RegionRenameScreen(target)));
    }

    /** Name/save region from A+B; applies vertical padding to reduce jump flicker. */
    public static void saveRegion(MinecraftClient mc, String name) {
        if (a == null || b == null) { toast(mc, "§cSelect two corners first."); return; }
        var cfg = LTConfig.get();

        int minX = Math.min(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxX = Math.max(a.getX(), b.getX());
        int maxY = Math.max(a.getY(), b.getY());
        int maxZ = Math.max(a.getZ(), b.getZ());

        minY = Math.max(mc.world.getBottomY(), minY - cfg.padBelow);
        maxY = Math.min(mc.world.getTopY(),    maxY + cfg.padAbove);

        Bounds boxed = new Bounds(minX, minY, minZ, maxX, maxY, maxZ);

        UUID owner = mc.player.getUuid();
        String ownerName = mc.player.getName().getString();
        String id = java.util.UUID.randomUUID().toString();
        String worldKey = RegionManager.worldKey(mc.world);

        RegionManager.createAndAdd(id, name, boxed, owner, ownerName, worldKey);

        sparkle(mc, boxed);
        toast(mc, "§aSaved region: §f" + name);

        a = b = null;
    }

    /** Bottom-outline particles so the selection is always visible. */
    private static void outlinePickParticles(MinecraftClient mc) {
        if (mc.world == null) return;
        if (a == null && b == null) return;

        BlockPos p1 = (a != null) ? a : b;
        BlockPos p2 = (b != null) ? b : a;

        if (p1 == null || p2 == null) {
            var p = (p1 != null) ? p1 : p2;
            if (p != null) mc.world.addParticle(ParticleTypes.HAPPY_VILLAGER, p.getX()+0.5, p.getY()+0.01, p.getZ()+0.5, 0,0.01,0);
            return;
        }

        int minX = Math.min(p1.getX(), p2.getX());
        int minY = Math.min(p1.getY(), p2.getY());
        int minZ = Math.min(p1.getZ(), p2.getZ());
        int maxX = Math.max(p1.getX(), p2.getX());
        int maxZ = Math.max(p1.getZ(), p2.getZ());

        for (int x = minX; x <= maxX; x++) {
            mc.world.addParticle(ParticleTypes.HAPPY_VILLAGER, x + 0.5, minY + 0.01, minZ + 0.5, 0, 0.005, 0);
            mc.world.addParticle(ParticleTypes.HAPPY_VILLAGER, x + 0.5, minY + 0.01, maxZ + 0.5, 0, 0.005, 0);
        }
        for (int z = minZ; z <= maxZ; z++) {
            mc.world.addParticle(ParticleTypes.HAPPY_VILLAGER, minX + 0.5, minY + 0.01, z + 0.5, 0, 0.005, 0);
            mc.world.addParticle(ParticleTypes.HAPPY_VILLAGER, maxX + 0.5, minY + 0.01, z + 0.5, 0, 0.005, 0);
        }
    }

    private static void sparkle(MinecraftClient mc, Bounds b) {
        if (mc.world == null) return;
        double cx = (b.minX + b.maxX + 1) / 2.0;
        double cy = (b.minY + b.maxY + 1) / 2.0;
        double cz = (b.minZ + b.maxZ + 1) / 2.0;
        for (int i = 0; i < 20; i++) {
            double vx = (mc.world.random.nextDouble() - 0.5) * 0.1;
            double vy = mc.world.random.nextDouble() * 0.1;
            double vz = (mc.world.random.nextDouble() - 0.5) * 0.1;
            mc.world.addParticle(ParticleTypes.GLOW, cx, cy, cz, vx, vy, vz);
        }
        if (mc.player != null) mc.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.0f);
    }

    private static void toast(MinecraftClient mc, String msg) {
        if (mc.player != null) mc.player.sendMessage(Text.literal(msg), true);
        if (mc.player != null) mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.6f, 1.15f);
    }

    public static boolean hasBothCorners() { return a != null && b != null; }
    public static void clear(MinecraftClient mc) { a = b = null; }
}
