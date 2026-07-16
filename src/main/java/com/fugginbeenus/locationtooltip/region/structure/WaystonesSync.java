package com.fugginbeenus.locationtooltip.region.structure;

import com.fugginbeenus.locationtooltip.region.Region;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Keeps auto-tagged structure regions named after the waystone inside them.
 *
 * <p>Waystones only registers a naturally-generated waystone once a player <em>activates</em>
 * it, and a player can place or rename a waystone at any time — so a one-shot check when the
 * structure is first tagged misses both cases. Instead we sweep periodically: for each named
 * waystone, find the structure region containing it and adopt the waystone's name.
 *
 * <p>Claim rules: a region records the waystone UID that named it, so that waystone's later
 * renames keep following, other waystones can't steal an already-named region, and a region
 * manually renamed by an admin (which flips its source away from STRUCTURE) is left alone.
 */
public final class WaystonesSync {
    private WaystonesSync() {}

    private static final int INTERVAL_TICKS = 100; // ~5s
    private static WaystonesNaming provider;
    private static int ticks;

    public static void register(WaystonesNaming waystones) {
        provider = waystones;
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (provider == null) return;
            if ((++ticks % INTERVAL_TICKS) != 0) return;
            try {
                sync(server);
            } catch (Throwable ignored) {
                // never let an integration hiccup break the server tick
            }
        });
    }

    private static void sync(MinecraftServer server) {
        List<WaystonesNaming.WaystoneInfo> waystones = provider.listNamedWaystones(server);
        if (waystones.isEmpty()) return;

        RegionManager mgr = RegionManager.of(server);

        for (WaystonesNaming.WaystoneInfo w : waystones) {
            // Only auto-generated structure regions are eligible (admin-renamed ones aren't STRUCTURE).
            Region r = mgr.smallestStructureContaining(w.dim(), w.pos());
            if (r == null) continue;

            // Already claimed by a different waystone → leave it be.
            if (r.waystoneUid != null && !r.waystoneUid.equals(w.uid())) continue;

            if (w.name().equals(r.name)) {
                if (r.waystoneUid == null) {           // same name already, just record the claim
                    r.waystoneUid = w.uid();
                    mgr.touchDim(r.dim);
                }
                continue;
            }

            String previous = r.name;
            r.name = w.name();
            r.waystoneUid = w.uid();
            mgr.touchDim(r.dim);                        // persist on the next flush
            announce(server, r, previous);
        }
    }

    /** Tell anyone standing in the region that it just got its name. */
    private static void announce(MinecraftServer server, Region r, String previous) {
        Text msg = Text.literal("")
                .append(Text.literal(previous).formatted(Formatting.GRAY))
                .append(Text.literal(" is now known as ").formatted(Formatting.GRAY))
                .append(Text.literal(r.name).formatted(Formatting.AQUA))
                .append(Text.literal(" (named after its waystone)").formatted(Formatting.DARK_GRAY));

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (!p.getWorld().getRegistryKey().getValue().equals(r.dim)) continue;
            if (!r.contains(p.getBlockPos())) continue;
            p.sendMessage(msg, false);
        }
    }
}
