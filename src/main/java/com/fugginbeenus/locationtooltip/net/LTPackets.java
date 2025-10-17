package com.fugginbeenus.locationtooltip.net;

import com.fugginbeenus.locationtooltip.LocationTooltip;
import com.fugginbeenus.locationtooltip.region.Region;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

/**
 * Handles all Fabric networking for both directions:
 *  - client -> server (region creation, admin actions)
 *  - server -> client (HUD updates, admin panel, etc.)
 */
public class LTPackets {
    public static final Identifier CREATE_REGION   = id("create_region");
    public static final Identifier OPEN_NAME       = id("open_name");
    public static final Identifier REGION_UPDATE   = id("region_update");
    public static final Identifier ADMIN_REQUEST   = id("admin_request");
    public static final Identifier ADMIN_LIST      = id("admin_list");
    public static final Identifier ADMIN_RENAME    = id("admin_rename");
    public static final Identifier ADMIN_DELETE    = id("admin_delete");
    public static final Identifier OPEN_ADMIN_PANEL = id("open_admin_panel");
    public static final net.minecraft.util.Identifier REGION_CELEBRATE =
            new net.minecraft.util.Identifier("locationtooltip", "region_celebrate");
    public static void sendRegionCelebrate(net.minecraft.server.network.ServerPlayerEntity player, String name) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(name);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, REGION_CELEBRATE, buf);
    }




    private static Identifier id(String path) {
        return new Identifier(LocationTooltip.MODID, path);
    }

    // Called from LocationTooltip.onInitialize()
    public static void init() {
        // Client -> Server: create region
        ServerPlayNetworking.registerGlobalReceiver(CREATE_REGION, (server, player, handler, buf, responseSender) -> {
            String name = buf.readString(128);
            BlockPos a = buf.readBlockPos();
            BlockPos b = buf.readBlockPos();

            server.execute(() -> {
                RegionManager mgr = RegionManager.of(server);
                mgr.createRegion(player, name, player.getWorld().getRegistryKey().getValue(), a, b);
            });
        });

        // Admin requests nearby regions
        ServerPlayNetworking.registerGlobalReceiver(ADMIN_REQUEST, (server, player, handler, buf, responseSender) -> {
            int radius = buf.readVarInt();
            server.execute(() -> RegionManager.of(server).sendNearbyTo(player, radius));
        });

        // Admin renames region
        ServerPlayNetworking.registerGlobalReceiver(ADMIN_RENAME, (server, player, handler, buf, responseSender) -> {
            String id = buf.readString(64);
            String newName = buf.readString(128);
            server.execute(() -> RegionManager.of(server).renameRegion(server, id, newName));
        });

        // Admin deletes region
        ServerPlayNetworking.registerGlobalReceiver(ADMIN_DELETE, (server, player, handler, buf, responseSender) -> {
            String id = buf.readString(64);
            server.execute(() -> RegionManager.of(server).deleteRegion(server, id));
        });

        LocationTooltip.LOG.info("[Networking] Registered server packet handlers.");
    }

    // ------------- SERVER -> CLIENT UTILITY SENDERS -------------

    public static void sendOpenName(ServerPlayerEntity player, BlockPos a, BlockPos b) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(a);
        buf.writeBlockPos(b);
        ServerPlayNetworking.send(player, OPEN_NAME, buf);
    }

    public static void sendRegionUpdate(ServerPlayerEntity player, String name) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeString(name, 128);
        ServerPlayNetworking.send(player, REGION_UPDATE, buf);
    }

    public static void sendAdminList(ServerPlayerEntity player, Region[] list) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeVarInt(list.length);
        for (Region r : list) {
            buf.writeString(r.id, 64);
            buf.writeString(r.name, 128);
            buf.writeIdentifier(r.dim);
            buf.writeBlockPos(r.a);
            buf.writeBlockPos(r.b);
        }
        ServerPlayNetworking.send(player, ADMIN_LIST, buf);
    }

    public static void sendOpenAdminPanel(ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        ServerPlayNetworking.send(player, OPEN_ADMIN_PANEL, buf);
    }


    private LTPackets() {}
}
