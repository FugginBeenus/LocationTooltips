package com.fugginbeenus.locationtooltip.region.structure;

import com.fugginbeenus.locationtooltip.region.Region;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public final class WaystonesSync {
    private WaystonesSync() {}

    private static final int INTERVAL_TICKS = 100;
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
            }
        });
    }

    private static void sync(MinecraftServer server) {
        List<WaystonesNaming.WaystoneInfo> waystones = provider.listNamedWaystones(server);
        if (waystones.isEmpty()) return;

        RegionManager mgr = RegionManager.of(server);

        for (WaystonesNaming.WaystoneInfo w : waystones) {
            Region r = mgr.smallestStructureContaining(w.dim(), w.pos());
            if (r == null) continue;

            if (r.waystoneUid != null && !r.waystoneUid.equals(w.uid())) continue;

            if (w.name().equals(r.name)) {
                if (r.waystoneUid == null) {
                    r.waystoneUid = w.uid();
                    mgr.touchDim(r.dim);
                }
                continue;
            }

            String previous = r.name;
            r.name = w.name();
            r.waystoneUid = w.uid();
            mgr.touchDim(r.dim);
            announce(server, r, previous);
        }
    }

    private static void announce(MinecraftServer server, Region r, String previous) {
        Component msg = Component.literal("")
                .append(Component.literal(previous).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" is now known as ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(r.name).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" (named after its waystone)").withStyle(ChatFormatting.DARK_GRAY));

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!p.level().dimension().location().equals(r.dim)) continue;
            if (!r.contains(p.blockPosition())) continue;
            com.fugginbeenus.locationtooltip.util.LTChat.tell(p, msg, false);
        }
    }
}
