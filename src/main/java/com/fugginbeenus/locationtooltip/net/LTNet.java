package com.fugginbeenus.locationtooltip.net;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.fugginbeenus.locationtooltip.util.LTId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side networking transport — the only server file that touches the version-specific
 * Fabric networking API. 1.20.1 uses the ResourceLocation + FriendlyByteBuf API that was removed in
 * 1.20.5+; the 1.21 branch multiplexes every channel through a single {@code CustomPacketPayload}
 * and dispatches internally by id, so the {@link LTPayloads.Def} abstraction is unchanged.
 */
public final class LTNet {
    private LTNet() {}

    @FunctionalInterface
    public interface ServerReceiver<T> {
        void receive(MinecraftServer server, ServerPlayer player, T payload);
    }

    //? if >=1.21 {
    /*private static final java.util.Map<net.minecraft.resources.ResourceLocation, LTPayloads.Def<?>> DEFS = new java.util.HashMap<>();
    private static final java.util.Map<net.minecraft.resources.ResourceLocation, ServerReceiver<?>> HANDLERS = new java.util.HashMap<>();

    public record LTCarrier(net.minecraft.resources.ResourceLocation channel, byte[] data) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
        public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<LTCarrier> ID =
                new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(LTId.of("locationtooltip", "carrier"));
        public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, LTCarrier> CODEC =
                net.minecraft.network.codec.StreamCodec.of(
                        (buf, value) -> { buf.writeResourceLocation(value.channel()); buf.writeByteArray(value.data()); },
                        buf -> new LTCarrier(buf.readResourceLocation(), buf.readByteArray()));
        public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() { return ID; }
    }

    public static void init() {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(LTCarrier.ID, LTCarrier.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(LTCarrier.ID, LTCarrier.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(LTCarrier.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            dispatch(player.level().getServer(), player, payload);
        });
    }

    public static <T> void registerReceiver(LTPayloads.Def<T> def, ServerReceiver<T> handler) {
        DEFS.put(def.id(), def);
        HANDLERS.put(def.id(), handler);
    }

    @SuppressWarnings("unchecked")
    private static void dispatch(MinecraftServer server, ServerPlayer player, LTCarrier carrier) {
        LTPayloads.Def<Object> def = (LTPayloads.Def<Object>) DEFS.get(carrier.channel());
        ServerReceiver<Object> handler = (ServerReceiver<Object>) HANDLERS.get(carrier.channel());
        if (def == null || handler == null) return;
        Object value = def.reader().apply(new FriendlyByteBuf(Unpooled.wrappedBuffer(carrier.data())));
        server.execute(() -> handler.receive(server, player, value));
    }

    public static <T> void send(ServerPlayer player, LTPayloads.Def<T> def, T payload) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
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

    public static <T> void send(ServerPlayer player, LTPayloads.Def<T> def, T payload) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        def.writer().accept(payload, buf);
        ServerPlayNetworking.send(player, def.id(), buf);
    }
    //?}
}
