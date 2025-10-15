package com.fugginbeenus.locationtooltip.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.*;

/** Persistent region list + ownership + lookups. */
public class RegionManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Region> REGIONS = new ArrayList<>();
    private static volatile boolean ADMIN_OVERRIDE = false;

    public static void setAdminOverride(boolean isAdmin) { ADMIN_OVERRIDE = isAdmin; }
    public static boolean canAdmin() { return ADMIN_OVERRIDE; }

    private static File dataFile() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        File dir = gameDir.resolve("config").resolve("locationtooltip").toFile();
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "regions.json");
    }

    public static void load() {
        REGIONS.clear();
        try {
            File f = dataFile();
            if (!f.exists()) { save(); return; }
            try (FileReader r = new FileReader(f)) {
                Region[] arr = GSON.fromJson(r, Region[].class);
                if (arr != null) REGIONS.addAll(Arrays.asList(arr));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void save() {
        try (FileWriter w = new FileWriter(dataFile())) {
            GSON.toJson(REGIONS, w);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static List<Region> get() { return REGIONS; }

    public static boolean canRename(UUID requester, Region r) {
        if (r == null) return false; if (canAdmin()) return true;
        return r.ownerUuid() != null && r.ownerUuid().equals(requester);
    }

    public static Region createAndAdd(String id, String name, Bounds bounds, UUID owner, String ownerName, String worldKey) {
        Region r = new Region(id, name, worldKey, bounds, owner, ownerName);
        REGIONS.add(r); save(); return r;
    }

    public static boolean renameRegion(Region r, String newName, UUID requester) {
        if (r == null) return false; if (!canRename(requester, r)) return false;
        r.setName(newName); save(); return true;
    }

    public static boolean deleteRegion(Region r, UUID requester) {
        if (r == null) return false; if (!canRename(requester, r)) return false;
        boolean ok = REGIONS.remove(r); if (ok) save(); return ok;
    }

    public static Region findByIdOrName(String key) {
        if (key == null || key.isEmpty()) return null;
        for (Region r : REGIONS) {
            if (key.equalsIgnoreCase(r.id()) || key.equalsIgnoreCase(r.name())) return r;
        }
        return null;
    }

    public static Region getRegionAt(World world, BlockPos pos) {
        String wk = worldKey(world);
        Region best = null; long bestVol = Long.MAX_VALUE;
        for (Region r : REGIONS) {
            if (r.worldKey() != null && !r.worldKey().equals(wk)) continue;
            if (!r.contains(pos)) continue;
            long vol = r.bounds().volume();
            if (vol < bestVol) { bestVol = vol; best = r; }
        }
        return best;
    }

    public static Region getDeepestAt(World world, BlockPos pos) { return getRegionAt(world, pos); }

    public static String worldKey(World world) {
        Identifier id = world.getRegistryKey().getValue(); // e.g. minecraft:overworld
        return id.toString();
    }
    public static boolean rename(Region target, String newName, java.util.UUID actorUuid) {
        if (target == null) return false;
        if (newName == null) return false;
        newName = newName.trim();
        if (newName.isEmpty()) newName = "Unnamed";

        // Require owner or admin
        if (!canRename(actorUuid, target)) return false;

        // Apply & persist
        target.setName(newName);
        save(); // ensure this exists; most projects already have RegionManager.save()
        return true;
    }
}
