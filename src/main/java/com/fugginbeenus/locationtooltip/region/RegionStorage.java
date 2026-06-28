package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class RegionStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private RegionStorage() {}

    static List<Region> load(MinecraftServer server, Identifier dim) {
        Path path = fileFor(server, dim);
        if (!Files.exists(path)) return new ArrayList<>();

        try (Reader r = Files.newBufferedReader(path)) {
            JsonElement root = JsonParser.parseReader(r);
            if (!root.isJsonArray()) return new ArrayList<>();
            JsonArray arr = root.getAsJsonArray();
            List<Region> list = new ArrayList<>(arr.size());
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();

                String id   = o.has("id")   ? o.get("id").getAsString()   : null;
                String name = o.has("name") ? o.get("name").getAsString() : "Region";
                Identifier d = dim; // stored per-file; ignore if present in older files

                BlockPos min = fromObj(o.getAsJsonObject("min"));
                BlockPos max = fromObj(o.getAsJsonObject("max"));

                if (min == null || max == null) {
                    // Back-compat with very old files that used "a"/"b"
                    if (o.has("a") && o.has("b")) {
                        min = fromObj(o.getAsJsonObject("a"));
                        max = fromObj(o.getAsJsonObject("b"));
                    }
                }
                if (min == null || max == null) continue;

                // Load owner UUID (new field)
                UUID owner = null;
                if (o.has("owner")) {
                    try {
                        String ownerStr = o.get("owner").getAsString();
                        if (ownerStr != null && !ownerStr.isEmpty()) {
                            owner = UUID.fromString(ownerStr);
                        }
                    } catch (Exception ignored) {}
                }

                Region rgn;
                if (id != null && !id.isEmpty()) {
                    rgn = new Region(id, name, d, min, max, owner);
                } else {
                    rgn = new Region(name, d, min, max); // new id
                    rgn.owner = owner;
                }

                // Load flags. Prefer the new "flags" object; otherwise migrate the legacy
                // boolean keys. Anything absent simply inherits / falls back to defaults.
                if (o.has("flags") && o.get("flags").isJsonObject()) {
                    for (var e : o.getAsJsonObject("flags").entrySet()) {
                        try { rgn.setFlag(e.getKey(), e.getValue().getAsBoolean()); } catch (Exception ignored) {}
                    }
                } else {
                    if (o.has("allowPvP"))         rgn.setFlag(RegionFlags.PVP.id,          o.get("allowPvP").getAsBoolean());
                    if (o.has("allowMobSpawning")) rgn.setFlag(RegionFlags.MOB_SPAWNING.id,  o.get("allowMobSpawning").getAsBoolean());
                }

                // Source / category (new, optional)
                if (o.has("source")) {
                    try { rgn.source = RegionSource.valueOf(o.get("source").getAsString()); } catch (Exception ignored) {}
                }
                if (o.has("category") && !o.get("category").isJsonNull()) {
                    rgn.category = o.get("category").getAsString();
                }

                list.add(rgn);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    static void save(MinecraftServer server, Identifier dim, List<Region> list) {
        Path path = fileFor(server, dim);
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException ignored) {}

        JsonArray arr = new JsonArray();
        for (Region r : list) {
            JsonObject o = new JsonObject();
            o.addProperty("id", r.id);
            o.addProperty("name", r.name);
            // dimension is implied by file name; we don’t redundantly store it
            o.add("min", toObj(r.min));
            o.add("max", toObj(r.max));

            // Save flag overrides (sparse: only what this region explicitly sets)
            JsonObject flags = new JsonObject();
            for (var e : r.flagOverrides().entrySet()) {
                flags.addProperty(e.getKey(), e.getValue());
            }
            o.add("flags", flags);

            // Save source + optional category
            o.addProperty("source", r.source.name());
            if (r.category != null) {
                o.addProperty("category", r.category);
            }

            // Save owner UUID
            if (r.owner != null) {
                o.addProperty("owner", r.owner.toString());
            }

            arr.add(o);
        }

        try (Writer w = Files.newBufferedWriter(path)) {
            GSON.toJson(arr, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ---------------- helpers ---------------- */

    private static Path fileFor(MinecraftServer server, Identifier dim) {
        // Per-dimension file under the world folder:
        //   <world>/locationtooltip/regions/<namespace>/<path>.json
        Path root = server.getSavePath(WorldSavePath.ROOT);
        Path dir = root.resolve("locationtooltip")
                .resolve("regions")
                .resolve(dim.getNamespace());
        String file = dim.getPath().replace('/', '_') + ".json";
        return dir.resolve(file);
    }

    private static JsonObject toObj(BlockPos p) {
        JsonObject o = new JsonObject();
        o.addProperty("x", p.getX());
        o.addProperty("y", p.getY());
        o.addProperty("z", p.getZ());
        return o;
    }

    private static BlockPos fromObj(JsonObject o) {
        if (o == null) return null;
        try {
            int x = o.get("x").getAsInt();
            int y = o.get("y").getAsInt();
            int z = o.get("z").getAsInt();
            return new BlockPos(x, y, z);
        } catch (Exception e) {
            return null;
        }
    }
}