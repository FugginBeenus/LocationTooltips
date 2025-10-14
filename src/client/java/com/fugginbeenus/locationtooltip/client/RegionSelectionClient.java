package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.data.Region;
import com.fugginbeenus.locationtooltip.data.RegionManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public final class RegionSelectionClient {
    private static BlockPos a, b; // selection corners
    private static Axis axis = Axis.X;
    private static Face face = Face.MAX;

    private static KeyBinding kbCycleAxis, kbToggleFace, kbGrow, kbShrink;

    enum Axis { X, Y, Z }
    enum Face { MIN, MAX }

    public static void init() {
        kbCycleAxis = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.locationtooltip.cycle_axis", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.locationtooltip"));
        kbToggleFace = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.locationtooltip.toggle_face", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.locationtooltip"));
        kbGrow = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.locationtooltip.grow", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories.locationtooltip"));
        kbShrink = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.locationtooltip.shrink", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.locationtooltip"));

        ClientTickEvents.END_CLIENT_TICK.register(RegionSelectionClient::tick);

        // Draw edge particles while there is an active selection
        WorldRenderEvents.END.register(ctx -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world != null && a != null && b != null) {
                spawnParticles(mc);
            }
        });
    }

    /** First click sets A; second click sets B. Both placed one block above the clicked block. */
    public static void onWandUse(MinecraftClient mc, BlockPos hit) {
        if (a == null) {
            a = hit.up();
            toast(mc, "Set corner A: " + a.toShortString());
        } else if (b == null) {
            b = hit.up();
            toast(mc, "Set corner B: " + b.toShortString());
        } else {
            a = hit.up();
            b = null;
            toast(mc, "Reset A, now click B");
        }
    }

    public static void clear(MinecraftClient mc) {
        a = null;
        b = null;
        toast(mc, "Cleared selection");
    }

    public static boolean hasBothCorners() {
        return a != null && b != null;
    }

    private static void tick(MinecraftClient mc) {
        if (mc.player == null) return;

        if (kbCycleAxis.wasPressed()) {
            axis = switch (axis) { case X -> Axis.Y; case Y -> Axis.Z; case Z -> Axis.X; };
            toast(mc, "Axis: " + axis);
        }
        if (kbToggleFace.wasPressed()) {
            face = (face == Face.MIN) ? Face.MAX : Face.MIN;
            toast(mc, "Face: " + face);
        }
        if (a != null && b != null) {
            if (kbGrow.wasPressed()) tweak(+1);
            if (kbShrink.wasPressed()) tweak(-1);
        }
    }

    /** Grow/shrink along the selected axis + face by 1 block. */
    private static void tweak(int dir) {
        BlockPos min = new BlockPos(Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ()));
        BlockPos max = new BlockPos(Math.max(a.getX(), b.getX()),
                Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ()));

        switch (axis) {
            case X -> { if (face == Face.MIN) min = min.add(dir, 0, 0); else max = max.add(dir, 0, 0); }
            case Y -> { if (face == Face.MIN) min = min.add(0, dir, 0); else max = max.add(0, dir, 0); }
            case Z -> { if (face == Face.MIN) min = min.add(0, 0, dir); else max = max.add(0, 0, dir); }
        }

        a = new BlockPos(min.getX(), min.getY(), min.getZ());
        b = new BlockPos(max.getX(), max.getY(), max.getZ());
    }

    /**
     * Save a region using the current selection.
     * - Lifts max edges by +1 so the box includes whole blocks
     * - Assigns UUID & world, color default
     * - Auto-assigns priority based on nesting (RegionManager.add)
     * - Plays sparkle burst & clears selection
     */
    public static void saveRegion(MinecraftClient mc, String name, int ignoredPriorityParam) {
        if (a == null || b == null || mc.world == null) {
            if (mc.player != null) mc.player.sendMessage(Text.literal("Select two corners first"), true);
            return;
        }

        Region.Bounds bounds = new Region.Bounds();
        bounds.min = new double[] {
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ())
        };
        bounds.max = new double[] {
                Math.max(a.getX(), b.getX()) + 1,
                Math.max(a.getY(), b.getY()) + 1,
                Math.max(a.getZ(), b.getZ()) + 1
        };

        Region r = new Region();
        try {
            var f = Region.class.getDeclaredField("id");     f.setAccessible(true); f.set(r, UUID.randomUUID().toString());
            f = Region.class.getDeclaredField("name");       f.setAccessible(true); f.set(r, name);
            f = Region.class.getDeclaredField("world");      f.setAccessible(true); f.set(r, mc.world.getRegistryKey().getValue().toString());
            f = Region.class.getDeclaredField("bounds");     f.setAccessible(true); f.set(r, bounds);
            f = Region.class.getDeclaredField("colorHex");   f.setAccessible(true); f.set(r, "#FFFFFF");
            f = Region.class.getDeclaredField("priority");   f.setAccessible(true); f.setInt(r, 0);
        } catch (Exception ignored) {}

        // Add (auto-assigns priority + saves)
        RegionManager.add(r);

        // Sparkle burst above center
        var world = mc.world;
        if (world != null) {
            double cx = (bounds.min[0] + bounds.max[0]) * 0.5;
            double cy = bounds.max[1] + 1.0;
            double cz = (bounds.min[2] + bounds.max[2]) * 0.5;

            var color = new Vector3f(0.85f, 0.85f, 1.0f);
            var sparkle = new DustParticleEffect(color, 1.5f);
            for (int i = 0; i < 40; i++) {
                double vx = (world.random.nextDouble() - 0.5) * 0.25;
                double vy = world.random.nextDouble() * 0.35;
                double vz = (world.random.nextDouble() - 0.5) * 0.25;
                world.addParticle(sparkle, cx, cy, cz, vx, vy, vz);
            }
        }

        // Clear selection so the border stops drawing
        a = null;
        b = null;

        if (mc.player != null) mc.player.sendMessage(Text.literal("Saved region: " + name), true);
    }

    /** Draw a thin white/yellow border of dust particles around the selection. */
    private static void spawnParticles(MinecraftClient mc) {
        var world = mc.world;
        if (world == null || a == null || b == null) return;

        int minX = Math.min(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxX = Math.max(a.getX(), b.getX()) + 1; // +1 to include full block
        int maxY = Math.max(a.getY(), b.getY()) + 1;
        int maxZ = Math.max(a.getZ(), b.getZ()) + 1;

        var color = new Vector3f(1.0f, 1.0f, 0.3f);
        var effect = new DustParticleEffect(color, 1.0f);

        // X edges
        for (double x = minX; x <= maxX; x += 0.5) {
            world.addParticle(effect, x, minY, minZ, 0, 0, 0);
            world.addParticle(effect, x, minY, maxZ, 0, 0, 0);
            world.addParticle(effect, x, maxY, minZ, 0, 0, 0);
            world.addParticle(effect, x, maxY, maxZ, 0, 0, 0);
        }
        // Z edges
        for (double z = minZ; z <= maxZ; z += 0.5) {
            world.addParticle(effect, minX, minY, z, 0, 0, 0);
            world.addParticle(effect, maxX, minY, z, 0, 0, 0);
            world.addParticle(effect, minX, maxY, z, 0, 0, 0);
            world.addParticle(effect, maxX, maxY, z, 0, 0, 0);
        }
        // Y edges
        for (double y = minY; y <= maxY; y += 0.5) {
            world.addParticle(effect, minX, y, minZ, 0, 0, 0);
            world.addParticle(effect, maxX, y, minZ, 0, 0, 0);
            world.addParticle(effect, minX, y, maxZ, 0, 0, 0);
            world.addParticle(effect, maxX, y, maxZ, 0, 0, 0);
        }
    }

    private static void toast(MinecraftClient mc, String msg) {
        if (mc.player != null) mc.player.sendMessage(Text.literal(msg), true);
    }
}
