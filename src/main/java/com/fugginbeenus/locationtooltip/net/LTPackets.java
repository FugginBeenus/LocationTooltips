package com.fugginbeenus.locationtooltip.net;

import com.fugginbeenus.locationtooltip.region.Region;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class LTPackets {
    private LTPackets() {}

    public static void register() { init(); }

    public static void init() {
        LTNet.init();

        LTNet.registerReceiver(LTPayloads.CREATE_REGION, (server, player, p) ->
                RegionManager.of(server).createRegion(player, p.name(), p.a(), p.b(), p.flags()));

        LTNet.registerReceiver(LTPayloads.REQUEST_ADMIN_LIST, (server, player, p) -> {
            if (p.radius() < 0) {
                RegionManager.of(server).sendAllTo(player, null);
            } else {
                RegionManager.of(server).sendNearbyTo(player, p.radius());
            }
        });

        LTNet.registerReceiver(LTPayloads.ADMIN_RENAME, (server, player, p) ->
                RegionManager.of(server).renameRegion(player, p.id(), p.newName(), p.flags()));

        LTNet.registerReceiver(LTPayloads.ADMIN_DELETE, (server, player, p) ->
                RegionManager.of(server).deleteRegion(player, p.id()));

        LTNet.registerReceiver(LTPayloads.PLAYER_NAME_REGION, (server, player, p) ->
                RegionManager.of(server).nameVillage(player, p.id(), p.newName()));
    }

    public static void openName(ServerPlayer player, BlockPos a, BlockPos b) {
        LTNet.send(player, LTPayloads.OPEN_NAME, new LTPayloads.OpenName(a, b));
    }

    public static void openAdminPanel(ServerPlayer player) {
        LTNet.send(player, LTPayloads.OPEN_ADMIN_PANEL, new LTPayloads.OpenAdminPanel());
    }

    public static void sendRegionUpdate(ServerPlayer player, String name) {
        LTNet.send(player, LTPayloads.REGION_UPDATE, new LTPayloads.RegionUpdate(name));
    }

    public static void sendAdminList(ServerPlayer player, List<Region> regions, boolean isOp) {
        sendAdminList(player, regions, isOp, null);
    }

    public static void sendAdminList(ServerPlayer player, List<Region> regions, boolean isOp, String nameableId) {
        List<LTPayloads.RegionEntry> entries = new ArrayList<>(regions.size());
        for (Region r : regions) {
            String ownerName;
            if (isOp && r.owner != null) {
                String name = getPlayerName(player.level().getServer(), r.owner);
                ownerName = (name != null) ? name : "Unknown";
            } else if (isOp) {
                ownerName = "Server";
            } else {
                ownerName = "";
            }
            entries.add(new LTPayloads.RegionEntry(
                    r.id, r.name, r.dim, r.min, r.max, r.flagOverrides(), ownerName, r.source.name(),
                    r.id.equals(nameableId)));
        }
        LTNet.send(player, LTPayloads.ADMIN_LIST, new LTPayloads.AdminList(entries));
    }

    public static void sendRegionCreatedCelebrate(ServerPlayer player, String name, BlockPos min, BlockPos max) {
        LTNet.send(player, LTPayloads.REGION_CREATED, new LTPayloads.RegionCreated(name, min, max));
    }

    public static void sendSelectionUpdate(ServerPlayer player, BlockPos a, BlockPos b) {
        LTNet.send(player, LTPayloads.SELECTION_UPDATE, new LTPayloads.SelectionUpdate(a, b));
    }

    public static void sendSelectionClear(ServerPlayer player) {
        LTNet.send(player, LTPayloads.SELECTION_CLEAR, new LTPayloads.SelectionClear());
    }

    private static String getPlayerName(MinecraftServer server, UUID uuid) {
        //? if >=1.21.11 {
        /*GameProfile profile = server.services().profileResolver().fetchById(uuid).orElse(null);
        *///?} else {
        GameProfile profile = server.getProfileCache().get(uuid).orElse(null);
        //?}
        if (profile == null) return null;
        //? if >=1.21.11 {
        /*return profile.name();
        *///?} else {
        return profile.getName();
        //?}
    }
}
