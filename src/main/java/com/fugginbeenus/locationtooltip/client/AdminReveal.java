package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.net.client.LTPacketsClient;
import net.minecraft.client.MinecraftClient;

public final class AdminReveal {
    private static boolean enabled = false;
    private static long lastRequestGameTime = 0;

    private AdminReveal(){}

    public static void setEnabled(boolean on) {
        enabled = on;
        if (!on) {
            AdminRegionRenderer.clearAll();
        }
    }

    public static boolean isEnabled(){ return enabled; }

    public static void clientTick() {
        if (!enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return;

        var world = mc.world;
        long t = world.getTime();

        if (t - lastRequestGameTime >= 20) {
            lastRequestGameTime = t;
            LTPacketsClient.requestAdminList(256);
        }

        var here = world.getRegistryKey().getValue();
        var rows = AdminClientCache.current();
        if (rows == null || rows.length == 0) {
            AdminRegionRenderer.clearAll();
            return;
        }

        AdminRegionRenderer.updateRegions(rows, here);
    }
}