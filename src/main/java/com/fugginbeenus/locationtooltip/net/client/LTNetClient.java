package com.fugginbeenus.locationtooltip.net.client;

import com.fugginbeenus.locationtooltip.net.LTPayloads;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

public final class LTNetClient {
    private LTNetClient() {}

    @FunctionalInterface
    public interface ClientReceiver<T> {
        void receive(Minecraft client, T payload);
    }

    //? if >=1.21 {
    /*private static final java.util.Map<net.minecraft.resources.ResourceLocation, LTPayloads.Def<?>> DEFS = new java.util.HashMap<>();
    private static final java.util.Map<net.minecraft.resources.ResourceLocation, ClientReceiver<?>> HANDLERS = new java.util.HashMap<>();

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(com.fugginbeenus.locationtooltip.net.LTNet.LTCarrier.ID,
                (payload, context) -> dispatch(context.client(), payload));
    }

    public static <T> void registerReceiver(LTPayloads.Def<T> def, ClientReceiver<T> handler) {
        DEFS.put(def.id(), def);
        HANDLERS.put(def.id(), handler);
    }

    @SuppressWarnings("unchecked")
    private static void dispatch(Minecraft client, com.fugginbeenus.locationtooltip.net.LTNet.LTCarrier carrier) {
        LTPayloads.Def<Object> def = (LTPayloads.Def<Object>) DEFS.get(carrier.channel());
        ClientReceiver<Object> handler = (ClientReceiver<Object>) HANDLERS.get(carrier.channel());
        if (def == null || handler == null) return;
        Object value = def.reader().apply(new FriendlyByteBuf(Unpooled.wrappedBuffer(carrier.data())));
        client.execute(() -> handler.receive(client, value));
    }

    public static <T> void send(LTPayloads.Def<T> def, T payload) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        def.writer().accept(payload, buf);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        ClientPlayNetworking.send(new com.fugginbeenus.locationtooltip.net.LTNet.LTCarrier(def.id(), data));
    }
    *///?} else {
    public static void init() {}

    public static <T> void registerReceiver(LTPayloads.Def<T> def, ClientReceiver<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(def.id(), (client, netHandler, buf, responseSender) -> {
            T payload = def.reader().apply(buf);
            client.execute(() -> handler.receive(client, payload));
        });
    }

    public static <T> void send(LTPayloads.Def<T> def, T payload) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        def.writer().accept(payload, buf);
        ClientPlayNetworking.send(def.id(), buf);
    }
    //?}
}
