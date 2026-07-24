package com.fugginbeenus.locationtooltip.net;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Server-side networking transport, isolated so it's the only place the version-specific
 * Fabric networking API is used. On 1.20.1 this is the Identifier + PacketByteBuf API; the
 * 1.21 port swaps the bodies for CustomPayload while keeping these signatures, so nothing
 * that sends or handles a packet has to change.
 *
 * Payloads are read on the network thread and handled on the server thread.
 */
public final class LTNet {
    private LTNet() {}

    @FunctionalInterface
    public interface ServerReceiver<T> {
        void receive(MinecraftServer server, ServerPlayerEntity player, T payload);
    }

    /** Common init hook (payload-type registration on 1.21; nothing to do on 1.20.1). */
    public static void init() {}

    public static <T> void registerReceiver(LTPayloads.Def<T> def, ServerReceiver<T> handler) {
        ServerPlayNetworking.registerGlobalReceiver(def.id(), (server, player, netHandler, buf, responseSender) -> {
            T payload = def.reader().apply(buf);
            server.execute(() -> handler.receive(server, player, payload));
        });
    }

    public static <T> void send(ServerPlayerEntity player, LTPayloads.Def<T> def, T payload) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        def.writer().accept(payload, buf);
        ServerPlayNetworking.send(player, def.id(), buf);
    }
}
