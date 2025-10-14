package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.data.Region;
import com.fugginbeenus.locationtooltip.data.RegionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class ClientState {
    private static Region current;

    public static void initOnce() { RegionManager.load(); }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        var dim = mc.world.getRegistryKey();
        var pos = mc.player.getPos();
        current = chooseRegion(dim.getValue().toString(), pos); // just set; no timers
    }

    private static Region chooseRegion(String dimId, Vec3d pos) {
        Region best = null;
        for (Region r : RegionManager.all()) {
            if (r.world() != null && !r.world().equals(dimId)) continue;
            if (!r.contains(pos)) continue;
            if (best == null || r.priority() > best.priority()) best = r;
            else if (r.priority() == best.priority()) {
                // smaller box wins
                best = (volume(r) < volume(best)) ? r : best;
            }
        }
        return best;
    }

    private static double volume(Region r) {
        // defensive bounds extraction omitted for brevity (use your existing helper if you like)
        try {
            var f = r.getClass().getDeclaredField("bounds");
            f.setAccessible(true);
            var b = (com.fugginbeenus.locationtooltip.data.Region.Bounds) f.get(r);
            return (b.maxX()-b.minX())*(b.maxY()-b.minY())*(b.maxZ()-b.minZ());
        } catch (Exception e) { return Double.MAX_VALUE; }
    }

    public static String currentText() { return (current != null) ? current.name() : "Wilderness"; }
    public static int currentTextColor() { return 0xFFFFFF; } // keep white for now (your regions can override later)
}