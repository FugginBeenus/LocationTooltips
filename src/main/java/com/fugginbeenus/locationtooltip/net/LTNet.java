package com.fugginbeenus.locationtooltip.net;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Server-side networking transport — the only server file that touches the version-specific
 * Fabric networking API. 1.20.1 uses the Identifier + PacketByteBuf API that was removed in
 * 1.20.5+; the 1.21 branch multiplexes every channel through a single {@code CustomPayload}
 * and dispatches internally by id, so the {@link LTPayloads.Def} abstraction is unchanged.
 */
public final class LTNet {
    private LTNet() {}

    @FunctionalInterface
    public interface ServerReceiver<T> {
        void receive(MinecraftServer server, ServerPlayerEntity player, T payload);
    }

    //? if >=1.21 {
    /*private static final java.util.Map<net.minecraft.util.Identifier, LTPayloads.Def<?>> DEFS = new java.util.HashMap<>();
    private static final java.util.Map<net.minecraft.util.Identifier, ServerReceiver<?>> HANDLERS = new java.util.HashMap<>();

    public record LTCarrier(net.minecraft.util.Identifier channel, byte[] data) implements net.minecraft.network.packet.CustomPayload {
        public static final net.minecraft.network.packet.CustomPayload.Id<LTCarrier> ID =
                new net.minecraft.network.packet.CustomPayload.Id<>(net.minecraft.util.Identifier.of("locationtooltip", "carrier"));
        public static final net.minecraft.network.codec.PacketCodec<PacketByteBuf, LTCarrier> CODEC =
                net.minecraft.network.codec.PacketCodec.of(
                        (value, buf) -> { buf.writeIdentifier(value.channel()); buf.writeByteArray(value.data()); },
                        buf -> new LTCarrier(buf.readIdentifier(), buf.readByteArray()));
        public net.minecraft.network.packet.CustomPayload.Id<? extends net.minecraft.network.packet.CustomPayload> getId() { return ID; }
    }

    public static void init() {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(LTCarrier.ID, LTCarrier.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(LTCarrier.ID, LTCarrier.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(LTCarrier.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            dispatch(player.getServer(), player, payload);
        });
    }

    public static <T> void registerReceiver(LTPayloads.Def<T> def, ServerReceiver<T> handler) {
        DEFS.put(def.id(), def);
        HANDLERS.put(def.id(), handler);
    }

    @SuppressWarnings("unchecked")
    private static void dispatch(MinecraftServer server, ServerPlayerEntity player, LTCarrier carrier) {
        LTPayloads.Def<Object> def = (LTPayloads.Def<Object>) DEFS.get(carrier.channel());
        ServerReceiver<Object> handler = (ServerReceiver<Object>) HANDLERS.get(carrier.channel());
        if (def == null || handler == null) return;
        Object value = def.reader().apply(new PacketByteBuf(Unpooled.wrappedBuffer(carrier.data())));
        server.execute(() -> handler.receive(server, player, value));
    }

    public static <T> void send(ServerPlayerEntity player, LTPayloads.Def<T> def, T payload) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        def.writer().accept(payload, buf);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        ServerPlayNetworking.send(player, new LTCarrier(def.id(), data));
    }
    *///?} else {
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
    //?}
}
