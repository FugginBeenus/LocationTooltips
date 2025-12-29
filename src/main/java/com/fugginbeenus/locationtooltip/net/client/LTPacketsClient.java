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

public final class LTPacketsClient {
    private LTPacketsClient() {}

    private static final Identifier OPEN_NAME            = LTPackets.OPEN_NAME;
    private static final Identifier ADMIN_LIST           = LTPackets.ADMIN_LIST;
    private static final Identifier REGION_UPDATE        = LTPackets.REGION_UPDATE;
    private static final Identifier REGION_CREATED_TOAST = LTPackets.REGION_CREATED_TOAST;
    private static final Identifier OPEN_ADMIN_PANEL     = LTPackets.OPEN_ADMIN_PANEL;
    private static final Identifier SELECTION_UPDATE     = LTPackets.SELECTION_UPDATE;
    private static final Identifier SELECTION_CLEAR      = LTPackets.SELECTION_CLEAR;

    public static void initClient() {
        // Register selection renderers
        com.fugginbeenus.locationtooltip.client.SelectionRenderer.register();
        com.fugginbeenus.locationtooltip.client.AdminRegionRenderer.register();

        ClientPlayNetworking.registerGlobalReceiver(SELECTION_UPDATE, (client, handler, buf, rs) -> {
            BlockPos a = buf.readBlockPos();
            BlockPos b = buf.readBlockPos();
            client.execute(() -> {
                com.fugginbeenus.locationtooltip.client.SelectionRenderer.setCorners(a, b);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SELECTION_CLEAR, (client, handler, buf, rs) -> {
            client.execute(() -> {
                com.fugginbeenus.locationtooltip.client.SelectionRenderer.clear();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OPEN_ADMIN_PANEL, (client, handler, buf, rs) ->
                client.execute(() -> {
                    var mc = MinecraftClient.getInstance();
                    if (mc == null) return;
                    mc.setScreen(new AdminPanelScreen());
                    requestAdminList(256);
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(OPEN_NAME, (client, handler, buf, rs) -> {
            BlockPos a = buf.readBlockPos();
            BlockPos b = buf.readBlockPos();
            client.execute(() -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc != null) mc.setScreen(new NameRegionScreen(a, b));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ADMIN_LIST, (client, handler, buf, rs) -> {
            AdminPanelScreen.RegionRow[] panelRows = readPanelRows(buf);
            AdminClientCache.Row[] cacheRows = toCacheRows(panelRows);
            client.execute(() -> {
                AdminPanelScreen.receiveList(panelRows);
                AdminClientCache.set(cacheRows);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(REGION_UPDATE, (client, handler, buf, rs) -> {
            String name = buf.readString(32767);
            client.execute(() -> updateHudTitle(name));
            client.execute(() -> com.fugginbeenus.locationtooltip.hud.LocationHudOverlay.setRegionTitle(name));
        });

        ClientPlayNetworking.registerGlobalReceiver(REGION_CREATED_TOAST, (client, handler, buf, rs) ->
                client.execute(() -> {
                    var mc = MinecraftClient.getInstance();
                    if (mc == null || mc.world == null || mc.player == null) return;

                    var w = mc.world;
                    var p = mc.player.getBlockPos();

                    for (int i = 0; i < 60; i++) {
                        w.addParticle(ParticleTypes.HAPPY_VILLAGER,
                                p.getX() + 0.5 + (w.random.nextDouble() - 0.5) * 2.0,
                                p.getY() + 1.2 + w.random.nextDouble(),
                                p.getZ() + 0.5 + (w.random.nextDouble() - 0.5) * 2.0,
                                0, 0.02, 0);
                    }
                    mc.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 1.0f);

                    // top outline flash omitted for brevity; keep yours if you like
                })
        );
    }

    // -------- client → server --------
    public static void requestAdminList(int radius) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(radius);
        ClientPlayNetworking.send(LTPackets.REQUEST_ADMIN_LIST, buf);
    }

    /** Convenience callback for querying all existing regions [GambaPVP] */
    public static void requestAllAdminList() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(-1); // -1 means "all regions"
        ClientPlayNetworking.send(LTPackets.REQUEST_ADMIN_LIST, buf);
    }

    public static void sendAdminRename(String id, String newName, boolean allowPvP, boolean allowMobSpawning) {
        PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
        out.writeString(id);
        out.writeString(newName);
        out.writeBoolean(allowPvP);
        out.writeBoolean(allowMobSpawning);
        ClientPlayNetworking.send(LTPackets.ADMIN_RENAME, out);
    }

    public static void sendAdminDelete(String id) {
        PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
        out.writeString(id);
        ClientPlayNetworking.send(LTPackets.ADMIN_DELETE, out);
    }

    public static void sendCreate(String name, BlockPos a, BlockPos b, boolean allowPvP, boolean allowMobSpawning) {
        PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
        out.writeString(name);
        out.writeBlockPos(a);
        out.writeBlockPos(b);
        out.writeBoolean(allowPvP);
        out.writeBoolean(allowMobSpawning);
        ClientPlayNetworking.send(LTPackets.CREATE_REGION, out);
    }

    // -------- helpers --------
    private static AdminPanelScreen.RegionRow[] readPanelRows(PacketByteBuf buf) {
        int n = Math.max(0, buf.readVarInt());
        var out = new AdminPanelScreen.RegionRow[n];
        for (int i = 0; i < n; i++) {
            String id = buf.readString(32767);
            String name = buf.readString(32767);
            Identifier dim = buf.readIdentifier();
            BlockPos min = buf.readBlockPos();
            BlockPos max = buf.readBlockPos();
            boolean allowPvP = buf.readBoolean();
            boolean allowMobSpawning = buf.readBoolean();
            out[i] = new AdminPanelScreen.RegionRow(id, name, dim, min, max, allowPvP, allowMobSpawning);
        }
        return out;
    }

    private static AdminClientCache.Row[] toCacheRows(AdminPanelScreen.RegionRow[] in) {
        var out = new AdminClientCache.Row[in.length];
        for (int i = 0; i < in.length; i++) {
            var r = in[i];
            out[i] = new AdminClientCache.Row(r.id, r.name, r.dim, r.a, r.b, r.allowPvP);
        }
        return out;
    }

    private static void updateHudTitle(String name) {
        try {
            Class<?> cls = Class.forName("com.fugginbeenus.locationtooltip.hud.LocationHudOverlay");
            for (String mname : new String[]{"setRegionTitle","setTitle","updateTitle","setText","setName"}) {
                try {
                    var m = cls.getMethod(mname, String.class);
                    m.invoke(null, name);
                    return;
                } catch (NoSuchMethodException ignored) {}
            }
            for (String fname : new String[]{"CURRENT","TITLE","currentTitle","current"}) {
                try {
                    var f = cls.getField(fname);
                    if (f.getType() == String.class) { f.set(null, name); return; }
                } catch (NoSuchFieldException ignored) {}
            }
        } catch (Throwable ignored) {}
    }
}