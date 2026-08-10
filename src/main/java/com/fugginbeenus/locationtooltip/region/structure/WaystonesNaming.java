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

/**
 * Optional Waystones integration, done via reflection so the mod carries no build- or
 * runtime-dependency on Waystones (and avoids a Loom version conflict). Only registered when
 * the {@code waystones} mod is present.
 *
 * <p>If a (preferably naturally-generated) named waystone sits inside a structure's bounding
 * box, its themed name is used as the region's name — so a village shows its waystone name
 * (e.g. "Restful Hamlet") instead of the generic "Plains Village".
 *
 * <p>Handles both API generations: 1.20.1 (Waystones 14.x, {@code IWaystone}, name is a String)
 * and 1.21+ (Waystones 21.x, {@code Waystone}, name is a Component). {@code getAllWaystones} and
 * {@code getPos/getDimension/getWaystoneUid/hasName/wasGenerated} are identical across both.
 */
public final class WaystonesNaming implements StructureNameProvider {

    /** A waystone's data, pulled out of the reflective API into something plain. */
    public record WaystoneInfo(String uid, String name, ResourceLocation dim, BlockPos pos, boolean generated) {}

    private final Method getAllWaystones; // static WaystonesAPI.getAllWaystones(MinecraftServer)
    private final Method getName;         // getName() -> String (1.20.1) or Component (1.21+)
    private final Method getPos;          // IWaystone.getPosition() -> BlockPos
    private final Method getDimension;    // IWaystone.getDimension() -> ResourceKey<Level>
    private final Method hasName;         // IWaystone.hasName() -> boolean
    private final Method wasGenerated;    // IWaystone.wasGenerated() -> boolean
    private final Method getWaystoneUid;  // IWaystone.getWaystoneUid() -> UUID
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
            // Waystones absent or API changed → provider stays inert.
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

    /**
     * Every currently-known named waystone. Note Waystones only registers a naturally
     * generated waystone once a player activates it, which is why region naming is synced
     * periodically rather than only when the structure is first tagged.
     */
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

            // Prefer naturally-generated waystones (themed names), else any named one.
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

    /** The waystone interface is Waystone on 1.21+ and IWaystone on 1.20.1. */
    private static Class<?> waystoneClass() throws ClassNotFoundException {
        try {
            return Class.forName("net.blay09.mods.waystones.api.Waystone");
        } catch (ClassNotFoundException e) {
            return Class.forName("net.blay09.mods.waystones.api.IWaystone");
        }
    }

    /** getName() is a String on 1.20.1 and a Component on 1.21+. */
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
