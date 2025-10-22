package com.fugginbeenus.locationtooltip.net;

import com.fugginbeenus.locationtooltip.LocationTooltip;
import com.fugginbeenus.locationtooltip.region.Region;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.SelectionManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static com.fugginbeenus.locationtooltip.LocationTooltip.MOD_ID;


public final class LTPackets {
    private LTPackets() {}

    // ---------- Channel IDs ----------
    public static final Identifier OPEN_NAME            = id("open_name");
    public static final Identifier CREATE_REGION        = id("create_region");
    public static final Identifier REGION_UPDATE        = id("region_update");
    public static final Identifier REGION_CREATED_TOAST = id("region_created_celebrate");

    public static final Identifier REQUEST_ADMIN_LIST   = id("request_admin_list");
    public static final Identifier ADMIN_LIST           = id("admin_list");
    public static final Identifier ADMIN_RENAME         = id("admin_rename");
    public static final Identifier ADMIN_DELETE         = id("admin_delete");

    // NEW: tell the client to open the admin panel
    public static final Identifier OPEN_ADMIN_PANEL     = id("open_admin_panel");

    private static Identifier id(String path) { return new Identifier(MOD_ID, path); }

    // Keep old call-sites working
    public static void register() { init(); }

    public static void init() {
        // Client → Server: create a region
        ServerPlayNetworking.registerGlobalReceiver(CREATE_REGION, (server, player, handler, buf, rs) -> {
            String name = buf.readString(32767);
            BlockPos a  = buf.readBlockPos();
            BlockPos b  = buf.readBlockPos();

            server.execute(() -> {
                RegionManager.of(server).createRegion(player, name, a, b);
                // selection is cleared inside createRegion; HUD/name push handled there too
            });
        });

        // Client → Server: ask for a list of nearby regions (admin panel & compass)
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_ADMIN_LIST, (server, player, handler, buf, rs) -> {
            int radius = buf.readVarInt();
            server.execute(() -> RegionManager.of(server).sendNearbyTo(player, radius));
        });

        // Client → Server: admin rename
        ServerPlayNetworking.registerGlobalReceiver(ADMIN_RENAME, (server, player, handler, buf, rs) -> {
            String id  = buf.readString(32767);
            String newName = buf.readString(32767);
            server.execute(() -> RegionManager.of(server).renameRegion(player, id, newName));
        });

        // Client → Server: admin delete
        ServerPlayNetworking.registerGlobalReceiver(ADMIN_DELETE, (server, player, handler, buf, rs) -> {
            String id = buf.readString(32767);
            server.execute(() -> RegionManager.of(server).deleteRegion(player, id));
        });
    }

    // -------- Server → Client helpers

    /** Open the naming screen with the current selection corners. */

    public static void openName(ServerPlayerEntity player, BlockPos a, BlockPos b) {
        var out = PacketByteBufs.create();
        out.writeBlockPos(a);
        out.writeBlockPos(b);
        ServerPlayNetworking.send(player, OPEN_NAME, out);
    }
    public static void openAdminPanel(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, OPEN_ADMIN_PANEL, PacketByteBufs.empty());
    }

    /** Update the client HUD region title. */
    public static void sendRegionUpdate(ServerPlayerEntity player, String name) {
        PacketByteBuf out = PacketByteBufs.create();
        out.writeString(name);
        ServerPlayNetworking.send(player, REGION_UPDATE, out);
    }

    /** Send the admin list back to a single player. */
    public static void sendAdminList(ServerPlayerEntity player, List<Region> regions) {
        PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
        out.writeVarInt(regions.size());
        for (Region r : regions) {
            out.writeString(r.id);
            out.writeString(r.name);
            out.writeIdentifier(r.dim);
            out.writeBlockPos(r.min);
            out.writeBlockPos(r.max);
        }
        ServerPlayNetworking.send(player, ADMIN_LIST, out);
    }

    /** Celebration payload used after a successful create. */
    public static void sendRegionCreatedCelebrate(ServerPlayerEntity player, String name, BlockPos min, BlockPos max) {
        PacketByteBuf out = PacketByteBufs.create();
        out.writeString(name);
        out.writeBlockPos(min);
        out.writeBlockPos(max);
        ServerPlayNetworking.send(player, REGION_CREATED_TOAST, out);
    }
}
