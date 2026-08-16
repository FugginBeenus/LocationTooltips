package com.fugginbeenus.locationtooltip.region;

import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.core.BlockPos;

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

    static List<Region> load(MinecraftServer server, ResourceLocation dim) {
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
                ResourceLocation d = dim;

                BlockPos min = fromObj(o.getAsJsonObject("min"));
                BlockPos max = fromObj(o.getAsJsonObject("max"));

                if (min == null || max == null) {
                    if (o.has("a") && o.has("b")) {
                        min = fromObj(o.getAsJsonObject("a"));
                        max = fromObj(o.getAsJsonObject("b"));
                    }
                }
                if (min == null || max == null) continue;

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
                    rgn = new Region(name, d, min, max);
                    rgn.owner = owner;
                }

                if (o.has("flags") && o.get("flags").isJsonObject()) {
                    for (var e : o.getAsJsonObject("flags").entrySet()) {
                        try { rgn.setFlag(e.getKey(), e.getValue().getAsBoolean()); } catch (Exception ignored) {}
                    }
                } else {
                    if (o.has("allowPvP"))         rgn.setFlag(RegionFlags.PVP.id,          o.get("allowPvP").getAsBoolean());
                    if (o.has("allowMobSpawning")) rgn.setFlag(RegionFlags.MOB_SPAWNING.id,  o.get("allowMobSpawning").getAsBoolean());
                }

                if (o.has("source")) {
                    try { rgn.source = RegionSource.valueOf(o.get("source").getAsString()); } catch (Exception ignored) {}
                }
                if (o.has("category") && !o.get("category").isJsonNull()) {
                    rgn.category = o.get("category").getAsString();
                }
                if (o.has("waystoneUid") && !o.get("waystoneUid").isJsonNull()) {
                    rgn.waystoneUid = o.get("waystoneUid").getAsString();
                }
                if (o.has("namedBy") && !o.get("namedBy").isJsonNull()) {
                    try { rgn.namedBy = UUID.fromString(o.get("namedBy").getAsString()); } catch (Exception ignored) {}
                }

                list.add(rgn);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    static void save(MinecraftServer server, ResourceLocation dim, List<Region> list) {
        Path path = fileFor(server, dim);
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException ignored) {}

        JsonArray arr = new JsonArray();
        for (Region r : list) {
            JsonObject o = new JsonObject();
            o.addProperty("id", r.id);
            o.addProperty("name", r.name);

            o.add("min", toObj(r.min));
            o.add("max", toObj(r.max));

            JsonObject flags = new JsonObject();
            for (var e : r.flagOverrides().entrySet()) {
                flags.addProperty(e.getKey(), e.getValue());
            }
            o.add("flags", flags);

            o.addProperty("source", r.source.name());
            if (r.category != null) {
                o.addProperty("category", r.category);
            }
            if (r.waystoneUid != null) {
                o.addProperty("waystoneUid", r.waystoneUid);
            }

            if (r.owner != null) {
                o.addProperty("owner", r.owner.toString());
            }
            if (r.namedBy != null) {
                o.addProperty("namedBy", r.namedBy.toString());
            }

            arr.add(o);
        }

        try (Writer w = Files.newBufferedWriter(path)) {
            GSON.toJson(arr, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path fileFor(MinecraftServer server, ResourceLocation dim) {
        Path root = server.getWorldPath(LevelResource.ROOT);
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
