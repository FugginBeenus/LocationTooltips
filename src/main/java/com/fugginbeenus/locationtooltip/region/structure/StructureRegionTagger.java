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

/**
 * Auto-creates regions for notable world structures as their chunks load, so places like
 * villages show up in the HUD with no manual setup. Which structures are tagged (and the
 * master on/off) come from {@link StructureConfig}, so server owners can edit the list and
 * add modded structures without a code change.
 *
 * <p>Detection rides on {@link ServerChunkEvents#CHUNK_LOAD}. A structure's start is stored
 * only in its anchor chunk, so each structure is seen once. Regions get a deterministic id
 * derived from the structure type + corner, which both de-dupes re-detection and lets admins
 * delete one without it instantly returning (it only re-tags if that chunk reloads later).
 *
 * <p>Structure-data reads happen on the chunk-load thread; the allow-list check and all
 * region/config mutation happen on the server thread (via {@code server.execute}).
 */
public final class StructureRegionTagger {
    private StructureRegionTagger() {}

    private static int flushCounter = 0;

    public static boolean isEnabled() { return StructureConfig.get().enabled; }
    public static void setEnabled(boolean v) { StructureConfig.get().setEnabled(v); }

    public static void register() {
        StructureConfig.get(); // ensure the config file exists on first run

        // Optional integrations (reflection-based; only active if the mod is present).
        if (FabricLoader.getInstance().isModLoaded("waystones")) {
            WaystonesNaming waystones = new WaystonesNaming();
            if (waystones.isReady()) {
                StructureNaming.addProvider(waystones);
                // Generated waystones only register once activated, and players can place or
                // rename one at any time — so keep structure names in sync continuously.
                WaystonesSync.register(waystones);
            }
        }

        //? if >=26.1 {
        /*ServerChunkEvents.CHUNK_LOAD.register((world, chunk, isNew) -> onChunkLoad(world, chunk));
        *///?} else {
        ServerChunkEvents.CHUNK_LOAD.register(StructureRegionTagger::onChunkLoad);
        //?}
        // Debounced flush of any structure regions added since the last write (~2s).
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

        // Read immutable structure data here; defer filtering + creation to the server thread.
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

                // A naturally-generated waystone may not be registered the instant its chunk
                // loads, so re-resolve the name shortly after to pick up its themed name.
                scheduleNameRecheck(server, dim, r, c.sid, c.box);
            }
        });
    }

    /** Re-resolve a structure region's name from providers (e.g. Waystones) a bit later. */
    private static void scheduleNameRecheck(MinecraftServer server, ResourceLocation dim, Region region,
                                            ResourceLocation sid, BoundingBox box) {
        if (!StructureNaming.hasProviders()) return;
        for (int delayTicks : new int[]{100, 1200}) { // ~5s and ~60s
            RegionTicker.later(server, delayTicks, () -> {
                RegionManager mgr = RegionManager.of(server);
                if (!mgr.exists(region.id)) return;                  // deleted in the meantime
                if (region.source != RegionSource.STRUCTURE) return; // manually edited → leave its name alone
                StructureNaming.providerName(server, dim, sid, box).ifPresent(name -> {
                    if (!name.equals(region.name)) {
                        region.name = name;
                        mgr.touchDim(dim); // persist on next flush; HUD auto-updates on move
                    }
                });
            });
        }
    }

    private record Candidate(String regionId, ResourceLocation sid, BoundingBox box) {}
}
