package com.fugginbeenus.locationtooltip.net.client;

import com.fugginbeenus.locationtooltip.net.LTPayloads;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

/**
 * Client-side networking transport — the client counterpart to {@link LTNet}, and the only
 * client file that touches the version-specific Fabric networking API.
 *
 * Payloads are read on the network thread and handled on the client thread.
 */
public final class LTNetClient {
    private LTNetClient() {}

    @FunctionalInterface
    public interface ClientReceiver<T> {
        void receive(MinecraftClient client, T payload);
    }

    public static void init() {}

    public static <T> void registerReceiver(LTPayloads.Def<T> def, ClientReceiver<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(def.id(), (client, netHandler, buf, responseSender) -> {
            T payload = def.reader().apply(buf);
            client.execute(() -> handler.receive(client, payload));
        });
    }

    public static <T> void send(LTPayloads.Def<T> def, T payload) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        def.writer().accept(payload, buf);
        ClientPlayNetworking.send(def.id(), buf);
    }
}
