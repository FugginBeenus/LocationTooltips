package com.fugginbeenus.locationtooltip.region.structure;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
 * <p>API verified against Waystones 14.1.x (1.20.1):
 * {@code WaystonesAPI.getAllWaystones(MinecraftServer) -> Stream<IWaystone>};
 * {@code IWaystone.getName()/getPos()/getDimension()/hasName()/wasGenerated()}.
 */
public final class WaystonesNaming implements StructureNameProvider {

    private final Method getAllWaystones; // static WaystonesAPI.getAllWaystones(MinecraftServer)
    private final Method getName;         // IWaystone.getName() -> String
    private final Method getPos;          // IWaystone.getPos() -> BlockPos
    private final Method getDimension;    // IWaystone.getDimension() -> RegistryKey<World>
    private final Method hasName;         // IWaystone.hasName() -> boolean
    private final Method wasGenerated;    // IWaystone.wasGenerated() -> boolean
    private final boolean ready;

    public WaystonesNaming() {
        Method all = null, name = null, pos = null, dim = null, named = null, gen = null;
        boolean ok = false;
        try {
            Class<?> api = Class.forName("net.blay09.mods.waystones.api.WaystonesAPI");
            Class<?> iWaystone = Class.forName("net.blay09.mods.waystones.api.IWaystone");
            all = api.getMethod("getAllWaystones", MinecraftServer.class);
            name = iWaystone.getMethod("getName");
            pos = iWaystone.getMethod("getPos");
            dim = iWaystone.getMethod("getDimension");
            named = iWaystone.getMethod("hasName");
            gen = iWaystone.getMethod("wasGenerated");
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
        this.ready = ok;
    }

    public boolean isReady() {
        return ready;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> nameFor(MinecraftServer server, Identifier dim, Identifier structureId, BlockBox box) {
        if (!ready) return Optional.empty();
        try {
            Stream<Object> all = (Stream<Object>) getAllWaystones.invoke(null, server);
            if (all == null) return Optional.empty();

            List<Object> matches = new ArrayList<>();
            all.forEach(w -> {
                try {
                    if (!(boolean) hasName.invoke(w)) return;
                    RegistryKey<World> wDim = (RegistryKey<World>) getDimension.invoke(w);
                    if (wDim == null || !wDim.getValue().equals(dim)) return;
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
                String n = (String) getName.invoke(w);
                if (n != null && !n.isBlank()) return n;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static boolean inBox(BlockBox b, BlockPos p) {
        return p.getX() >= b.getMinX() && p.getX() <= b.getMaxX()
                && p.getY() >= b.getMinY() && p.getY() <= b.getMaxY()
                && p.getZ() >= b.getMinZ() && p.getZ() <= b.getMaxZ();
    }
}
