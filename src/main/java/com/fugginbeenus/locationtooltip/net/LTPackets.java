package com.fugginbeenus.locationtooltip.net;

import com.fugginbeenus.locationtooltip.region.Region;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static com.fugginbeenus.locationtooltip.LocationTooltip.MOD_ID;

public final class LTPackets {
    private LTPackets() {}

    public static final Identifier OPEN_NAME            = id("open_name");
    public static final Identifier CREATE_REGION        = id("create_region");
    public static final Identifier REGION_UPDATE        = id("region_update");
    public static final Identifier REGION_CREATED_TOAST = id("region_created_celebrate");

    public static final Identifier REQUEST_ADMIN_LIST   = id("request_admin_list");
    public static final Identifier ADMIN_LIST           = id("admin_list");
    public static final Identifier ADMIN_RENAME         = id("admin_rename");
    public static final Identifier ADMIN_DELETE         = id("admin_delete");

    public static final Identifier OPEN_ADMIN_PANEL     = id("open_admin_panel");

    public static final Identifier SELECTION_UPDATE     = id("selection_update");
    public static final Identifier SELECTION_CLEAR      = id("selection_clear");

    private static Identifier id(String path) { return new Identifier(MOD_ID, path); }

    public static void register() { init(); }

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(CREATE_REGION, (server, player, handler, buf, rs) -> {
            String name = buf.readString(32767);
            BlockPos a  = buf.readBlockPos();
            BlockPos b  = buf.readBlockPos();
            boolean allowPvP = buf.readBoolean();
            boolean allowMobSpawning = buf.readBoolean();
            server.execute(() -> RegionManager.of(server).createRegion(player, name, a, b, allowPvP, allowMobSpawning));
        });

        ServerPlayNetworking.registerGlobalReceiver(REQUEST_ADMIN_LIST, (server, player, handler, buf, rs) -> {
            int radius = buf.readVarInt();
            server.execute(() -> RegionManager.of(server).sendNearbyTo(player, radius));
        });

        ServerPlayNetworking.registerGlobalReceiver(ADMIN_RENAME, (server, player, handler, buf, rs) -> {
            String id  = buf.readString(32767);
            String newName = buf.readString(32767);
            boolean allowPvP = buf.readBoolean();
            boolean allowMobSpawning = buf.readBoolean();
            server.execute(() -> RegionManager.of(server).renameRegion(player, id, newName, allowPvP, allowMobSpawning));
        });

        ServerPlayNetworking.registerGlobalReceiver(ADMIN_DELETE, (server, player, handler, buf, rs) -> {
            String id = buf.readString(32767);
            server.execute(() -> RegionManager.of(server).deleteRegion(player, id));
        });
    }

    public static void openName(ServerPlayerEntity player, BlockPos a, BlockPos b) {
        var out = PacketByteBufs.create();
        out.writeBlockPos(a);
        out.writeBlockPos(b);
        ServerPlayNetworking.send(player, OPEN_NAME, out);
    }

    public static void openAdminPanel(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, OPEN_ADMIN_PANEL, PacketByteBufs.empty());
    }

    public static void sendRegionUpdate(ServerPlayerEntity player, String name) {
        PacketByteBuf out = PacketByteBufs.create();
        out.writeString(name);
        ServerPlayNetworking.send(player, REGION_UPDATE, out);
    }

    public static void sendAdminList(ServerPlayerEntity player, List<Region> regions) {
        PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
        out.writeVarInt(regions.size());
        for (Region r : regions) {
            out.writeString(r.id);
            out.writeString(r.name);
            out.writeIdentifier(r.dim);
            out.writeBlockPos(r.min);
            out.writeBlockPos(r.max);
            out.writeBoolean(r.allowPvP);
            out.writeBoolean(r.allowMobSpawning);
        }
        ServerPlayNetworking.send(player, ADMIN_LIST, out);
    }

    public static void sendRegionCreatedCelebrate(ServerPlayerEntity player, String name, BlockPos min, BlockPos max) {
        PacketByteBuf out = PacketByteBufs.create();
        out.writeString(name);
        out.writeBlockPos(min);
        out.writeBlockPos(max);
        ServerPlayNetworking.send(player, REGION_CREATED_TOAST, out);
    }

    public static void sendSelectionUpdate(ServerPlayerEntity player, BlockPos a, BlockPos b) {
        PacketByteBuf out = PacketByteBufs.create();
        out.writeBlockPos(a);
        out.writeBlockPos(b);
        ServerPlayNetworking.send(player, SELECTION_UPDATE, out);
    }

    public static void sendSelectionClear(ServerPlayerEntity player) {
        PacketByteBuf out = PacketByteBufs.create();
        ServerPlayNetworking.send(player, SELECTION_CLEAR, out);
    }
}