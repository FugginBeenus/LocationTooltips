package com.fugginbeenus.locationtooltip.region.structure;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class WaystonesNaming implements StructureNameProvider {
    public record WaystoneInfo(String uid, String name, ResourceLocation dim, BlockPos pos, boolean generated) {}

    private final Method getAllWaystones;
    private final Method getName;
    private final Method getPos;
    private final Method getDimension;
    private final Method hasName;
    private final Method wasGenerated;
    private final Method getWaystoneUid;
    private final boolean ready;

    public WaystonesNaming() {
        Method all = null, name = null, pos = null, dim = null, named = null, gen = null, uid = null;
        boolean ok = false;
        try {
            Class<?> api = Class.forName("net.blay09.mods.waystones.api.WaystonesAPI");
            Class<?> waystone = waystoneClass();
            all = api.getMethod("getAllWaystones", MinecraftServer.class);
            name = waystone.getMethod("getName");
            pos = waystone.getMethod("getPos");
            dim = waystone.getMethod("getDimension");
            named = waystone.getMethod("hasName");
            gen = waystone.getMethod("wasGenerated");
            uid = waystone.getMethod("getWaystoneUid");
            ok = true;
        } catch (Throwable t) {
        }
        this.getAllWaystones = all;
        this.getName = name;
        this.getPos = pos;
        this.getDimension = dim;
        this.hasName = named;
        this.wasGenerated = gen;
        this.getWaystoneUid = uid;
        this.ready = ok;
    }

    @SuppressWarnings("unchecked")
    public List<WaystoneInfo> listNamedWaystones(MinecraftServer server) {
        if (!ready) return List.of();
        try {
            Stream<Object> all = (Stream<Object>) getAllWaystones.invoke(null, server);
            if (all == null) return List.of();

            List<WaystoneInfo> out = new ArrayList<>();
            all.forEach(w -> {
                try {
                    if (!(boolean) hasName.invoke(w)) return;
                    String name = waystoneName(getName.invoke(w));
                    if (name == null || name.isBlank()) return;

                    ResourceKey<Level> wDim = (ResourceKey<Level>) getDimension.invoke(w);
                    BlockPos pos = (BlockPos) getPos.invoke(w);
                    Object uid = getWaystoneUid.invoke(w);
                    if (wDim == null || pos == null || uid == null) return;

                    out.add(new WaystoneInfo(uid.toString(), name, wDim.location(), pos,
                            (boolean) wasGenerated.invoke(w)));
                } catch (Throwable ignored) {
                }
            });
            return out;
        } catch (Throwable t) {
            return List.of();
        }
    }

    public boolean isReady() {
        return ready;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> nameFor(MinecraftServer server, ResourceLocation dim, ResourceLocation structureId, BoundingBox box) {
        if (!ready) return Optional.empty();
        try {
            Stream<Object> all = (Stream<Object>) getAllWaystones.invoke(null, server);
            if (all == null) return Optional.empty();

            List<Object> matches = new ArrayList<>();
            all.forEach(w -> {
                try {
                    if (!(boolean) hasName.invoke(w)) return;
                    ResourceKey<Level> wDim = (ResourceKey<Level>) getDimension.invoke(w);
                    if (wDim == null || !wDim.location().equals(dim)) return;
                    BlockPos pos = (BlockPos) getPos.invoke(w);
                    if (pos == null || !inBox(box, pos)) return;
                    matches.add(w);
                } catch (Throwable ignored) {
                }
            });
            if (matches.isEmpty()) return Optional.empty();

            String generated = pickName(matches, true);
            if (generated != null) return Optional.of(generated);
            return Optional.ofNullable(pickName(matches, false));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    private String pickName(List<Object> matches, boolean generatedOnly) {
        for (Object w : matches) {
            try {
                if (generatedOnly && !(boolean) wasGenerated.invoke(w)) continue;
                String n = waystoneName(getName.invoke(w));
                if (n != null && !n.isBlank()) return n;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static Class<?> waystoneClass() throws ClassNotFoundException {
        try {
            return Class.forName("net.blay09.mods.waystones.api.Waystone");
        } catch (ClassNotFoundException e) {
            return Class.forName("net.blay09.mods.waystones.api.IWaystone");
        }
    }

    private static String waystoneName(Object result) {
        if (result instanceof String s) return s;
        if (result instanceof net.minecraft.network.chat.Component t) return t.getString();
        return null;
    }

    private static boolean inBox(BoundingBox b, BlockPos p) {
        return p.getX() >= b.minX() && p.getX() <= b.maxX()
                && p.getY() >= b.minY() && p.getY() <= b.maxY()
                && p.getZ() >= b.minZ() && p.getZ() <= b.maxZ();
    }
}
