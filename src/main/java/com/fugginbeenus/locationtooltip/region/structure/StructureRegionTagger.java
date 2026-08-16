package com.fugginbeenus.locationtooltip.region.structure;

import com.fugginbeenus.locationtooltip.region.Region;
import com.fugginbeenus.locationtooltip.region.RegionManager;
import com.fugginbeenus.locationtooltip.region.RegionSource;
import com.fugginbeenus.locationtooltip.server.RegionTicker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class StructureRegionTagger {
    private StructureRegionTagger() {}

    private static int flushCounter = 0;

    public static boolean isEnabled() { return StructureConfig.get().enabled; }
    public static void setEnabled(boolean v) { StructureConfig.get().setEnabled(v); }

    public static void register() {
        StructureConfig.get();

        if (FabricLoader.getInstance().isModLoaded("waystones")) {
            WaystonesNaming waystones = new WaystonesNaming();
            if (waystones.isReady()) {
                StructureNaming.addProvider(waystones);

                WaystonesSync.register(waystones);
            }
        }

        StructureNaming.addProvider(new VillageNaming());

        //? if >=26.1 {
        /*ServerChunkEvents.CHUNK_LOAD.register((world, chunk, isNew) -> onChunkLoad(world, chunk));
        *///?} else {
        ServerChunkEvents.CHUNK_LOAD.register(StructureRegionTagger::onChunkLoad);
        //?}

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if ((++flushCounter % 40) == 0) RegionManager.of(server).flushDirty();
        });
    }

    private static void onChunkLoad(ServerLevel world, LevelChunk chunk) {
        if (!isEnabled()) return;

        Map<Structure, StructureStart> starts = chunk.getAllStarts();
        if (starts == null || starts.isEmpty()) return;

        Registry<Structure> structureReg = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ResourceLocation dim = world.dimension().location();

        List<Candidate> candidates = null;
        for (Map.Entry<Structure, StructureStart> e : starts.entrySet()) {
            StructureStart start = e.getValue();
            if (start == null || !start.isValid()) continue;

            ResourceLocation sid = structureReg.getKey(e.getKey());
            if (sid == null) continue;

            BoundingBox box = start.getBoundingBox();
            String regionId = "structure/" + dim.getPath() + "/" + sid.getPath()
                    + "@" + box.minX() + "_" + box.minZ();

            if (candidates == null) candidates = new ArrayList<>();
            candidates.add(new Candidate(regionId, sid, box));
        }
        if (candidates == null) return;

        final List<Candidate> toCreate = candidates;
        final MinecraftServer server = world.getServer();
        server.execute(() -> {
            StructureConfig cfg = StructureConfig.get();
            if (!cfg.enabled) return;
            RegionManager mgr = RegionManager.of(server);

            for (Candidate c : toCreate) {
                if (!cfg.isAllowed(c.sid)) continue;
                if (mgr.exists(c.regionId)) continue;

                int minY = Math.max(world.getMinBuildHeight(), c.box.minY() - 1);
                int maxY = Math.min(world.getMaxBuildHeight() - 1, c.box.maxY() + 8);
                BlockPos min = new BlockPos(c.box.minX(), minY, c.box.minZ());
                BlockPos max = new BlockPos(c.box.maxX(), maxY, c.box.maxZ());

                String name = StructureNaming.resolve(server, dim, c.sid, c.box);
                Region r = new Region(c.regionId, name, dim, min, max, null);
                r.source = RegionSource.STRUCTURE;
                r.category = c.sid.getPath();
                mgr.addStructureRegion(dim, r);

                scheduleNameRecheck(server, dim, r, c.sid, c.box);
            }
        });
    }

    private static void scheduleNameRecheck(MinecraftServer server, ResourceLocation dim, Region region,
                                            ResourceLocation sid, BoundingBox box) {
        if (!StructureNaming.hasProviders()) return;
        for (int delayTicks : new int[]{100, 1200}) {
            RegionTicker.later(server, delayTicks, () -> {
                RegionManager mgr = RegionManager.of(server);
                if (!mgr.exists(region.id)) return;
                if (region.source != RegionSource.STRUCTURE) return;
                StructureNaming.providerName(server, dim, sid, box).ifPresent(name -> {
                    if (!name.equals(region.name)) {
                        region.name = name;
                        mgr.touchDim(dim);
                    }
                });
            });
        }
    }

    private record Candidate(String regionId, ResourceLocation sid, BoundingBox box) {}
}
