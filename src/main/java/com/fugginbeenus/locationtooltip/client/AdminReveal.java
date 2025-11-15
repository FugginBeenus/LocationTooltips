package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.registry.LTItems;
import com.fugginbeenus.locationtooltip.net.client.LTPacketsClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

public final class AdminReveal {
    private static boolean enabled = false;
    private static long lastRequestGameTime = 0;
    private static final DustParticleEffect EFFECT = new DustParticleEffect(new Vector3f(0.2f, 0.9f, 0.9f), 1f);

    private AdminReveal(){}

    public static void setEnabled(boolean on) { enabled = on; }
    public static boolean isEnabled(){ return enabled; }

    public static void clientTick() {
        if (!enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return;

        var world = mc.world;
        long t = world.getTime();

        // Refresh admin list every 20 ticks while enabled (keeps borders up-to-date)
        if (t - lastRequestGameTime >= 20) {
            lastRequestGameTime = t;
            LTPacketsClient.requestAdminList(256);
        }

        var here = world.getRegistryKey().getValue();
        var rows = AdminClientCache.current(); // latest from server
        if (rows == null || rows.length == 0) return;

        int step = 3;
        for (var r : rows) {
            if (!r.dim.equals(here)) continue;
            int minX = Math.min(r.a.getX(), r.b.getX());
            int minY = Math.min(r.a.getY(), r.b.getY());
            int minZ = Math.min(r.a.getZ(), r.b.getZ());
            int maxX = Math.max(r.a.getX(), r.b.getX());
            int maxY = Math.max(r.a.getY(), r.b.getY());
            int maxZ = Math.max(r.a.getZ(), r.b.getZ());

            for (int y = minY; y <= maxY; y += step) {
                world.addParticle(EFFECT, minX + 0.5, y + 0.1, minZ + 0.5, 0, 0, 0);
                world.addParticle(EFFECT, minX + 0.5, y + 0.1, maxZ + 0.5, 0, 0, 0);
                world.addParticle(EFFECT, maxX + 0.5, y + 0.1, minZ + 0.5, 0, 0, 0);
                world.addParticle(EFFECT, maxX + 0.5, y + 0.1, maxZ + 0.5, 0, 0, 0);
            }
        }
    }

    static {
        // Safety: disable when leaving world to avoid “stuck on”
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> enabled = false);
    }
}
