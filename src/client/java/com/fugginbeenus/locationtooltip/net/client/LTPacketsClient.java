package com.fugginbeenus.locationtooltip.net.client;

import com.fugginbeenus.locationtooltip.client.AdminClientCache;
import com.fugginbeenus.locationtooltip.client.AdminPanelScreen;
import com.fugginbeenus.locationtooltip.client.NameRegionScreen;
import com.fugginbeenus.locationtooltip.hud.LocationHudOverlay;
import com.fugginbeenus.locationtooltip.net.LTPackets;

import io.netty.buffer.Unpooled;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;




public class LTPacketsClient {

    /** Register all client-side packet receivers. Call from LocationTooltipClient.onInitializeClient(). */
    public static void initClient() {
        // Server -> Client: open naming UI for a selection
        ClientPlayNetworking.registerGlobalReceiver(LTPackets.OPEN_NAME, (client, handler, buf, rs) -> {
            BlockPos a = buf.readBlockPos();
            BlockPos b = buf.readBlockPos();
            client.execute(() -> MinecraftClient.getInstance().setScreen(new NameRegionScreen(a, b)));
        });

        // Server -> Client: open Admin Panel UI
        ClientPlayNetworking.registerGlobalReceiver(LTPackets.OPEN_ADMIN_PANEL, (client, handler, buf, rs) -> {
            client.execute(() -> {
                net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
                if (mc != null) {
                    mc.setScreen(new com.fugginbeenus.locationtooltip.client.AdminPanelScreen());
                    // immediately request region list (default radius)
                    LTPacketsClient.requestAdminList(256);
                }
            });
        });

        // Server -> Client: active HUD region name changed
        ClientPlayNetworking.registerGlobalReceiver(LTPackets.REGION_UPDATE, (client, handler, buf, rs) -> {
            String name = buf.readString(128);
            client.execute(() -> LocationHudOverlay.setCurrentRegion(name));
        });

        // Server -> Client: admin list of nearby regions
        ClientPlayNetworking.registerGlobalReceiver(LTPackets.ADMIN_LIST, (client, handler, buf, rs) -> {
            int n = buf.readVarInt();
            AdminPanelScreen.RegionRow[] rows = new AdminPanelScreen.RegionRow[n];
            for (int i = 0; i < n; i++) {
                String id = buf.readString(64);
                String name = buf.readString(128);
                Identifier dim = buf.readIdentifier();
                BlockPos a = buf.readBlockPos();
                BlockPos b = buf.readBlockPos();
                rows[i] = new AdminPanelScreen.RegionRow(id, name, dim, a, b);
            }

            ClientPlayNetworking.registerGlobalReceiver(
                    com.fugginbeenus.locationtooltip.net.LTPackets.REGION_CELEBRATE,
                    (mcClient, netHandler, byteBuf, resp3) -> {
                        String name = byteBuf.readString();
                        mcClient.execute(() -> {
                            MinecraftClient mc = MinecraftClient.getInstance();
                            if (mc == null || mc.player == null) return;

                            // Title / subtitle
                            mc.inGameHud.setTitle(Text.literal("Region Created"));
                            mc.inGameHud.setSubtitle(Text.literal(name));

                            // Sound
                            mc.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);

                            // Particle puff
                            if (mc.world != null) {
                                var w = mc.world;
                                var p = mc.player.getPos();
                                for (int i = 0; i < 20; i++) {
                                    double ox = (w.random.nextDouble() - 0.5) * 1.2;
                                    double oy = w.random.nextDouble() * 0.8 + 0.2;
                                    double oz = (w.random.nextDouble() - 0.5) * 1.2;
                                    w.addParticle(ParticleTypes.GLOW, p.x + ox, p.y + 1.0 + oy, p.z + oz, 0, 0.02, 0);
                                }
                            }
                        });
                    }
            );

            client.execute(() -> {
                // update client cache (used for Master Compass particle render)
                AdminClientCache.Row[] cache = new AdminClientCache.Row[n];
                for (int i = 0; i < n; i++) {
                    var r = rows[i];
                    cache[i] = new AdminClientCache.Row(r.id, r.name, r.dim, r.a, r.b);
                }
                AdminClientCache.last = cache;

                // refresh the Admin Panel UI (if open)
                AdminPanelScreen.receiveList(rows);
            });
        });
    }

    // -------------------- client -> server helpers --------------------

    public static void requestAdminList(int radius) {
        PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
        out.writeVarInt(radius);
        ClientPlayNetworking.send(LTPackets.ADMIN_REQUEST, out);
    }

    public static void sendAdminRename(String id, String newName) {
        PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
        out.writeString(id, 64);
        out.writeString(newName, 128);
        ClientPlayNetworking.send(LTPackets.ADMIN_RENAME, out);
    }

    public static void sendAdminDelete(String id) {
        PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
        out.writeString(id, 64);
        ClientPlayNetworking.send(LTPackets.ADMIN_DELETE, out);
    }

    public static void sendCreate(String name, BlockPos a, BlockPos b) {
        PacketByteBuf out = new PacketByteBuf(Unpooled.buffer());
        out.writeString(name, 128);
        out.writeBlockPos(a);
        out.writeBlockPos(b);
        ClientPlayNetworking.send(LTPackets.CREATE_REGION, out);
    }
}
