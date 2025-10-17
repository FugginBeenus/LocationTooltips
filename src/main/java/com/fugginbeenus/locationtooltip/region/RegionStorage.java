package com.fugginbeenus.locationtooltip.region;

import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.*;
import java.util.*;

public final class RegionStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE = "locationtooltip/regions.json";

    public static void ensureDir(MinecraftServer server) throws IOException {
        Path root = server.getSavePath(WorldSavePath.ROOT).resolve("locationtooltip");
        if (!Files.exists(root)) Files.createDirectories(root);
    }

    public static List<Region> load(MinecraftServer server) {
        try {
            ensureDir(server);
            Path p = server.getSavePath(WorldSavePath.ROOT).resolve(FILE);
            if (!Files.exists(p)) return new ArrayList<>();
            try (Reader r = Files.newBufferedReader(p)) {
                JsonArray arr = JsonParser.parseReader(r).getAsJsonArray();
                List<Region> list = new ArrayList<>(arr.size());
                for (JsonElement e : arr) {
                    JsonObject o = e.getAsJsonObject();
                    String id = o.get("id").getAsString();
                    String name = o.get("name").getAsString();
                    Identifier dim = new Identifier(o.get("dim").getAsString());
                    BlockPos a = fromObj(o.getAsJsonObject("a"));
                    BlockPos b = fromObj(o.getAsJsonObject("b"));
                    UUID owner = UUID.fromString(o.get("owner").getAsString());
                    list.add(new Region(id, name, dim, a, b, owner));
                }
                return list;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void save(MinecraftServer server, Collection<Region> regions) {
        try {
            ensureDir(server);
            Path p = server.getSavePath(WorldSavePath.ROOT).resolve(FILE);
            JsonArray arr = new JsonArray();
            for (Region r : regions) {
                JsonObject o = new JsonObject();
                o.addProperty("id", r.id);
                o.addProperty("name", r.name);
                o.addProperty("dim", r.dim.toString());
                o.add("a", toObj(r.a));
                o.add("b", toObj(r.b));
                o.addProperty("owner", r.owner.toString());
                arr.add(o);
            }
            try (Writer w = Files.newBufferedWriter(p)) {
                GSON.toJson(arr, w);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static JsonObject toObj(BlockPos p) {
        JsonObject o = new JsonObject();
        o.addProperty("x", p.getX());
        o.addProperty("y", p.getY());
        o.addProperty("z", p.getZ());
        return o;
    }
    private static BlockPos fromObj(JsonObject o) {
        return new BlockPos(o.get("x").getAsInt(), o.get("y").getAsInt(), o.get("z").getAsInt());
    }
}

