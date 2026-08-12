package com.fugginbeenus.locationtooltip.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;

/**
 * Traces region edges with dust particles.
 *
 * 26.x rewrote world rendering and Fabric has not shipped a hook for drawing into it yet, so
 * the boxes cannot be drawn there. Particles need no such hook, so the edges are still visible
 * while the Admin Compass is held — coarser than the drawn box, but it does the same job.
 */
public final class RegionEdgeParticles {
    private RegionEdgeParticles() {}

    private static final int EVERY_N_TICKS = 4;
    private static final double SPACING = 1.0;    // blocks between particles along an edge
    private static final double RANGE = 48.0;     // don't bother with edges further out than this

    private static int ticks;

    /** Call each client tick while the compass is held. */
    public static void tick(java.util.List<double[]> edges) {
        if (edges.isEmpty()) return;
        if ((++ticks % EVERY_N_TICKS) != 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        BlockPos eye = mc.player.blockPosition();
        double rangeSq = RANGE * RANGE;

        for (double[] e : edges) {
            // e = {x1, y1, z1, x2, y2, z2, r, g, b}
            double dx = e[3] - e[0], dy = e[4] - e[1], dz = e[5] - e[2];
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length <= 0) continue;

            int steps = (int) Math.ceil(length / SPACING);
            DustParticleOptions dust = dust((float) e[6], (float) e[7], (float) e[8]);

            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                double x = e[0] + dx * t, y = e[1] + dy * t, z = e[2] + dz * t;
                if (eye.distSqr(BlockPos.containing(x, y, z)) > rangeSq) continue;
                mc.level.addParticle(dust, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }

    /** Dust took a colour vector before 26.x and a packed colour after. */
    private static DustParticleOptions dust(float r, float g, float b) {
        //? if >=1.21.11 {
        /*int packed = ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
        return new DustParticleOptions(packed, 1.0f);
        *///?} else {
        return new DustParticleOptions(new org.joml.Vector3f(r, g, b), 1.0f);
        //?}
    }
}
