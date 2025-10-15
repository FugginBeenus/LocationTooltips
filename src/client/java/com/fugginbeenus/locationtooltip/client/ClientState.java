package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.data.Region;
import com.fugginbeenus.locationtooltip.data.RegionManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class ClientState {
    private ClientState() {}

    /** Lowest-volume region at the player's position. */
    public static Region current(MinecraftClient mc) {
        if (mc == null || mc.player == null || mc.world == null) return null;
        return RegionManager.getRegionAt(mc.world, mc.player.getBlockPos());
    }

    /** Convenience: current region name or "Wilderness" (no args). */
    public static String currentText() {
        return currentText(MinecraftClient.getInstance());
    }

    /** Convenience: current region name or "Wilderness". */
    public static String currentText(MinecraftClient mc) {
        Region r = current(mc);
        return (r != null && r.name() != null) ? r.name() : "Wilderness";
    }
}
