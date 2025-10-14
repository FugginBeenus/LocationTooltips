package com.fugginbeenus.locationtooltip.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Handles region storage, loading/saving, and nesting priority logic.
 * Purely common-side — no client-only imports.
 */
public final class RegionManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "regions.json";

    private static List<Region> regions = new ArrayList<>();

    /* ------------------------------------------------------ */
    /*                     LOAD / SAVE                        */
    /* ------------------------------------------------------ */

    public static void load() {
        try {
            File file = getFile();
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                saveAll(new ArrayList<>());
            }

            Region[] arr = GSON.fromJson(new FileReader(file), Region[].class);
            regions = (arr != null) ? new ArrayList<>(Arrays.asList(arr)) : new ArrayList<>();

            boolean mutated = false;
            for (Region r : regions) {
                if (r.id() == null || r.id().isBlank()) {
                    try {
                        var f = Region.class.getDeclaredField("id");
                        f.setAccessible(true);
                        f.set(r, UUID.randomUUID().toString());
                        mutated = true;
                    } catch (Exception ignored) {}
                }
            }
            if (mutated) saveAll(regions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveAll(List<Region> list) {
        try (FileWriter writer = new FileWriter(getFile())) {
            GSON.toJson(list, writer);
            regions = new ArrayList<>(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** getFile must be called client-side (runDirectory is client) */
    public static File getFile() {
        File dir = new File("config/locationtooltip");
        return new File(dir, FILE_NAME);
    }

    public static List<Region> all() {
        return Collections.unmodifiableList(regions);
    }

    /* ------------------------------------------------------ */
    /*                  PRIORITY / NESTING                    */
    /* ------------------------------------------------------ */

    public static int autoAssignPriority(Region newRegion) {
        int maxParent = -1;

        for (Region r : regions) {
            if (!Objects.equals(r.world(), newRegion.world())) continue;

            if (r.containsBox(newRegion.bounds())) {
                if (r.priority() > maxParent) maxParent = r.priority();
            }
        }

        int assigned = Math.max(0, maxParent + 1);
        try {
            var f = Region.class.getDeclaredField("priority");
            f.setAccessible(true);
            f.setInt(newRegion, assigned);
        } catch (Exception ignored) {}
        return assigned;
    }

    /* ------------------------------------------------------ */
    /*                REGION QUERY HELPERS                    */
    /* ------------------------------------------------------ */

    public static Region findByIdOrName(String query) {
        for (Region r : regions) {
            if (r.id() != null && r.id().equalsIgnoreCase(query)) return r;
            if (r.name() != null && r.name().equalsIgnoreCase(query)) return r;
        }
        return null;
    }

    public static boolean deleteById(String id) {
        ArrayList<Region> copy = new ArrayList<>(regions);
        boolean removed = copy.removeIf(r -> id.equalsIgnoreCase(r.id()));
        if (removed) saveAll(copy);
        return removed;
    }

    public static List<Region> allMutable() {
        return new ArrayList<>(regions);
    }

    /** Returns highest-priority region at given position */
    public static Region getRegionAt(World world, BlockPos pos) {
        if (world == null || pos == null) return null;
        String worldKey = world.getRegistryKey().getValue().toString();
        Vec3d p = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.001, pos.getZ() + 0.5);

        Region best = null;
        int bestPriority = Integer.MIN_VALUE;

        for (Region r : regions) {
            if (!Objects.equals(worldKey, r.world())) continue;
            if (r.contains(p) && r.priority() > bestPriority) {
                best = r;
                bestPriority = r.priority();
            }
        }
        return best;
    }

    /* ------------------------------------------------------ */
    /*               REGION CREATION / SAVING                 */
    /* ------------------------------------------------------ */

    public static void add(Region region) {
        autoAssignPriority(region);
        ArrayList<Region> copy = new ArrayList<>(regions);
        copy.add(region);
        saveAll(copy);
    }

    /* ------------------------------------------------------ */
    /*              STUBS FOR CLIENT HELPERS                  */
    /* ------------------------------------------------------ */

    /** always true in singleplayer; client wrapper decides */
    public static boolean canAdmin() {
        return true;
    }

    public static void notifyPlayer(String msg) {
        System.out.println("[LocationTooltip] " + msg);
    }
}
