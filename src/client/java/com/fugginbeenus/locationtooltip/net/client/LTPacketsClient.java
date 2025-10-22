package com.fugginbeenus.locationtooltip.net.client;

import com.fugginbeenus.locationtooltip.client.AdminClientCache;
import com.fugginbeenus.locationtooltip.client.AdminPanelScreen;
import com.fugginbeenus.locationtooltip.client.NameRegionScreen;
import com.fugginbeenus.locationtooltip.net.LTPackets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Client receivers & client->server senders.
 * Uses server-declared identifiers (LTPackets.*) to keep both sides in sync.
 */
public final class LTPacketsClient {
    private LTPacketsClient() {}

    // Local copies for readability when registering receivers
    private static final Identifier OPEN_NAME            = LTPackets.OPEN_NAME;
    private static final Identifier ADMIN_LIST           = LTPackets.ADMIN_LIST;
    private static final Identifier REGION_UPDATE        = LTPackets.REGION_UPDATE;
    private static final Identifier REGION_CREATED_TOAST = LTPackets.REGION_CREATED_TOAST;
    private static final Identifier OPEN_ADMIN_PANEL = LTPackets.OPEN_ADMIN_PANEL;

    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(OPEN_ADMIN_PANEL, (client, handler, buf, rs) -> {
            client.execute(() -> {
                var mc = MinecraftClient.getInstance();
                if (mc == null) return;
                mc.setScreen(new AdminPanelScreen());
                // pull initial data right away; radius 256 matches your defaults
                requestAdminList(256);
            });
        });

        // Open the textured naming screen when server asks
        ClientPlayNetworking.registerGlobalReceiver(OPEN_NAME, (client, handler, buf, rs) -> {
            BlockPos a = buf.readBlockPos();
            BlockPos b = buf.readBlockPos();
            client.execute(() -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc != null) mc.setScreen(new NameRegionScreen(a, b));
            });
        });

        // Admin list for compass + admin screen
        ClientPlayNetworking.registerGlobalReceiver(ADMIN_LIST, (client, handler, buf, rs) -> {
            AdminPanelScreen.RegionRow[] panelRows = readPanelRows(buf);
            AdminClientCache.Row[] cacheRows = toCacheRows(panelRows);

            client.execute(() -> {
                // 1) Update the admin panel (if it’s open)
                AdminPanelScreen.receiveList(panelRows);
                // 2) Update reveal/compass particle cache
                AdminClientCache.set(cacheRows);
            });
        });

        // Update the pill HUD text
        ClientPlayNetworking.registerGlobalReceiver(REGION_UPDATE, (client, handler, buf, rs) -> {
            String name = buf.readString(32767);
            client.execute(() -> updateHudTitle(name));
            client.execute(() -> com.fugginbeenus.locationtooltip.hud.LocationHudOverlay.setRegionTitle(name));
        });

        // Celebration: particles + sound + quick outline flash
        ClientPlayNetworking.registerGlobalReceiver(REGION_CREATED_TOAST, (client, handler, buf, rs) -> {
            String name = buf.readString(32767);
            BlockPos min = buf.readBlockPos();
            BlockPos max = buf.readBlockPos();

            client.execute(() -> {
                var mc = MinecraftClient.getInstance();
                if (mc == null || mc.world == null || mc.player == null) return;

                var w = mc.world;
                var p = mc.player.getBlockPos();

                // sparkle around player
                for (int i = 0; i < 60; i++) {
                    w.addParticle(ParticleTypes.HAPPY_VILLAGER,
                            p.getX() + 0.5 + (w.random.nextDouble() - 0.5) * 2.0,
                            p.getY() + 1.2 + w.random.nextDouble(),
                            p.getZ() + 0.5 + (w.random.nextDouble() - 0.5) * 2.0,
                            0, 0.02, 0);
                }
                mc.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 1.0f);

                // quick top outline flash
                int minX = Math.min(min.getX(), max.getX());
                int minY = Math.min(min.getY(), max.getY());
                int minZ = Math.min(min.getZ(), max.getZ());
                int maxX = Math.max(min.getX(), max.getX());
                int maxY = Math.max(min.getY(), max.getY());
                int maxZ = Math.max(min.getZ(), max.getZ());
                int y = maxY + 1;

                for (int x = minX; x <= maxX; x += 2) {
                    w.addParticle(ParticleTypes.GLOW, x + 0.5, y + 0.1, minZ + 0.5, 0, 0, 0);
                    w.addParticle(ParticleTypes.GLOW, x + 0.5, y + 0.1, maxZ + 0.5, 0, 0, 0);
                }
                for (int z = minZ; z <= maxZ; z += 2) {
                    w.addParticle(ParticleTypes.GLOW, minX + 0.5, y + 0.1, z + 0.5, 0, 0, 0);
                    w.addParticle(ParticleTypes.GLOW, maxX + 0.5, y + 0.1, z + 0.5, 0, 0, 0);
                }
            });
        });
    }

    // ---------------- client -> server ----------------

    public static void requestAdminList(int radius) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(radius);
        ClientPlayNetworking.send(LTPackets.REQUEST_ADMIN_LIST, buf);
    }

    public static void sendAdminRename(String id, String newName) {
        PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
        out.writeString(id);
        out.writeString(newName);
        ClientPlayNetworking.send(LTPackets.ADMIN_RENAME, out);
    }

    public static void sendAdminDelete(String id) {
        PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
        out.writeString(id);
        ClientPlayNetworking.send(LTPackets.ADMIN_DELETE, out);
    }

    public static void sendCreate(String name, BlockPos a, BlockPos b) {
        PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
        out.writeString(name);
        out.writeBlockPos(a);
        out.writeBlockPos(b);
        ClientPlayNetworking.send(LTPackets.CREATE_REGION, out);
    }

    // ---------------- helpers ----------------

    private static AdminPanelScreen.RegionRow[] readPanelRows(PacketByteBuf buf) {
        int n = Math.max(0, buf.readVarInt());
        var out = new AdminPanelScreen.RegionRow[n];
        for (int i = 0; i < n; i++) {
            String id = buf.readString(32767);
            String name = buf.readString(32767);
            Identifier dim = buf.readIdentifier();
            BlockPos min = buf.readBlockPos();
            BlockPos max = buf.readBlockPos();
            out[i] = new AdminPanelScreen.RegionRow(id, name, dim, min, max);
        }
        return out;
    }

    private static AdminClientCache.Row[] toCacheRows(AdminPanelScreen.RegionRow[] in) {
        var out = new AdminClientCache.Row[in.length];
        for (int i = 0; i < in.length; i++) {
            var r = in[i];
            out[i] = new AdminClientCache.Row(r.id, r.name, r.dim, r.a, r.b);
        }
        return out;
    }

    /** Best-effort bridge for whichever HUD setter exists. */
    private static void updateHudTitle(String name) {
        try {
            Class<?> cls = Class.forName("com.fugginbeenus.locationtooltip.hud.LocationHudOverlay");
            // common method names
            for (String mname : new String[]{"setRegionTitle", "setTitle", "updateTitle", "setText", "setName"}) {
                try {
                    var m = cls.getMethod(mname, String.class);
                    m.invoke(null, name);
                    return;
                } catch (NoSuchMethodException ignored) {}
            }
            // fallback to a public static String field if present
            for (String fname : new String[]{"CURRENT", "TITLE", "currentTitle", "current"}) {
                try {
                    var f = cls.getField(fname);
                    if (f.getType() == String.class) {
                        f.set(null, name);
                        return;
                    }
                } catch (NoSuchFieldException ignored) {}
            }
        } catch (Throwable ignored) {}
    }
}
