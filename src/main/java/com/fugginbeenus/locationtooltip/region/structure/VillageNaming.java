package com.fugginbeenus.locationtooltip.region.structure;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class VillageNaming implements StructureNameProvider {

    private static final List<String> SUFFIXES = List.of(
            "dell", "brook", "ford", "hollow", "wick", "stead", "mere", "fell",
            "holt", "gate", "wood", "field", "bury", "ton", "march", "crest");

    private static final List<String> NOUNS = List.of(
            "Shire", "Hamlet", "Crossing", "Rest", "Watch", "Hollow", "Reach",
            "Landing", "Wells", "Row", "Green", "End", "Barrow", "Court", "Vale");

    private static final List<String> GENERIC = List.of(
            "Iron", "Old", "Still", "Raven", "Stone", "Elder", "Silver", "Black",
            "High", "Red", "Grey", "Long", "Bell", "Fox", "Crow", "Hearth");

    private record Palette(List<String> heads, List<String> suffixes, List<String> nouns) {
        Palette(List<String> heads) { this(heads, SUFFIXES, NOUNS); }
    }

    private static final Palette SNOWY = new Palette(List.of(
            "Frost", "Winter", "Rime", "Snow", "Pale", "North", "Hoar", "Chill", "White"));

    private static final Palette DESERT = new Palette(List.of(
            "Dune", "Sun", "Amber", "Dust", "Sand", "Dry", "Copper", "Scorch", "Bone"));

    private static final Palette BADLANDS = new Palette(List.of(
            "Clay", "Rust", "Ochre", "Mesa", "Ember", "Kiln", "Sienna", "Gulch"));

    private static final Palette TAIGA = new Palette(List.of(
            "Pine", "Spruce", "Fern", "Timber", "Elk", "Moss", "Bear", "Cedar"));

    private static final Palette FOREST = new Palette(List.of(
            "Oak", "Birch", "Bramble", "Thorn", "Bloom", "Glade", "Aspen", "Ivy"));

    private static final Palette SAVANNA = new Palette(List.of(
            "Acacia", "Gold", "Wide", "Dry", "Grass", "Amber", "Sun", "Wind"));

    private static final Palette PLAINS = new Palette(List.of(
            "Green", "Meadow", "Wheat", "Hill", "Fair", "Bright", "Willow", "Apple"));

    private static final Palette JUNGLE = new Palette(List.of(
            "Vine", "Emerald", "Canopy", "Cocoa", "Parrot", "Mist", "Bamboo", "Fern"));

    private static final Palette SWAMP = new Palette(
            List.of("Bog", "Murk", "Reed", "Willow", "Marsh", "Mire", "Peat", "Toad"),
            List.of("mire", "fen", "marsh", "bog", "hollow", "mere", "brook", "holt"),
            List.of("Fen", "Mire", "Bog", "Hollow", "Rest", "Crossing", "Reach"));

    private static final Palette OCEAN = new Palette(
            List.of("Salt", "Tide", "Coral", "Pearl", "Wave", "Kelp", "Drift", "Gull", "Brine", "Foam"),
            List.of("haven", "port", "cove", "reef", "shoal", "wharf", "strand", "mere"),
            List.of("Harbour", "Cove", "Landing", "Wharf", "Anchorage", "Bay", "Shoals", "Reach"));

    private static final Palette MUSHROOM = new Palette(List.of(
            "Spore", "Cap", "Violet", "Shroud", "Glow", "Gill", "Ruby"));

    private static final Palette FLEET = new Palette(
            List.of("Salt", "Storm", "Gale", "Anchor", "Mast", "Keel", "Prow", "Lantern",
                    "Wake", "Tempest", "Grey", "Iron"),
            List.of("fleet", "wake", "moor", "berth", "hold", "watch", "run", "reach"),
            List.of("Fleet", "Armada", "Flotilla", "Convoy", "Moorings", "Anchorage", "Company", "Wake"));

    private static final List<String> SHIP_VILLAGES = List.of(
            "towns_and_towers:village_ocean",
            "t_and_t:village_ocean");

    private static final Palette NETHER = new Palette(
            List.of("Ash", "Ember", "Soul", "Cinder", "Scorch", "Char", "Gloom", "Blaze", "Basalt"),
            List.of("forge", "pyre", "hollow", "gate", "reach", "fell", "crest", "march"),
            List.of("Forge", "Pyre", "Hollow", "Watch", "Gate", "Reach", "End", "Rest"));

    @Override
    public Optional<String> nameFor(MinecraftServer server, ResourceLocation dim,
                                    ResourceLocation structureId, BoundingBox box) {
        if (!StructureConfig.get().nameVillages) return Optional.empty();
        if (structureId == null || box == null) return Optional.empty();
        if (!structureId.getPath().contains("village")) return Optional.empty();
        if (has(structureId.getPath(), "trader", "camp")) {
            return Optional.of(plainName(structureId.getPath()));
        }

        Random random = new Random(seedFor(dim, box));
        return Optional.of(build(random, paletteFor(server, dim, structureId, box)));
    }

    private static long seedFor(ResourceLocation dim, BoundingBox box) {
        long seed = dim == null ? 0L : dim.toString().hashCode();
        seed = seed * 31L + box.minX();
        seed = seed * 31L + box.minZ();
        return seed;
    }

    private static Palette paletteFor(MinecraftServer server, ResourceLocation dim,
                                      ResourceLocation structureId, BoundingBox box) {
        if (SHIP_VILLAGES.contains(structureId.toString())) return FLEET;

        Palette fromId = match(structureId.getPath());
        if (fromId != null) return fromId;

        Palette fromBiome = match(biomePathAt(server, dim, box));
        if (fromBiome != null) return fromBiome;

        return null;
    }

    private static Palette match(String path) {
        if (path == null || path.isEmpty()) return null;
        if (has(path, "snow", "frozen", "ice", "grove", "slopes")) return SNOWY;
        if (has(path, "desert")) return DESERT;
        if (has(path, "badlands", "mesa")) return BADLANDS;
        if (has(path, "ocean", "beach", "shore", "coast", "reef", "tidal")) return OCEAN;
        if (has(path, "jungle", "bamboo")) return JUNGLE;
        if (has(path, "swamp", "mangrove")) return SWAMP;
        if (has(path, "mushroom", "fungus")) return MUSHROOM;
        if (has(path, "nether", "crimson", "warped", "soul", "basalt", "piglin")) return NETHER;
        if (has(path, "savanna")) return SAVANNA;
        if (has(path, "plains", "meadow", "sunflower")) return PLAINS;
        if (has(path, "taiga", "spruce", "pine")) return TAIGA;
        if (has(path, "forest", "birch", "wooded", "cherry", "flower")) return FOREST;
        return null;
    }

    private static String plainName(String path) {
        String tail = path.substring(path.lastIndexOf('/') + 1);
        if (tail.startsWith("village_")) tail = tail.substring("village_".length());

        StringBuilder sb = new StringBuilder();
        for (String word : tail.replace('_', ' ').trim().split(" ")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    private static boolean has(String path, String... keys) {
        for (String k : keys) {
            if (path.contains(k)) return true;
        }
        return false;
    }

    private static String biomePathAt(MinecraftServer server, ResourceLocation dim, BoundingBox box) {
        if (server == null || dim == null) return null;
        try {
            ServerLevel level = null;
            for (ServerLevel candidate : server.getAllLevels()) {
                if (candidate.dimension().location().equals(dim)) {
                    level = candidate;
                    break;
                }
            }
            if (level == null) return null;

            int x = (box.minX() + box.maxX()) / 2;
            int y = (box.minY() + box.maxY()) / 2;
            int z = (box.minZ() + box.maxZ()) / 2;

            return level.getUncachedNoiseBiome(x >> 2, y >> 2, z >> 2)
                    .unwrapKey()
                    .map(key -> key.location().getPath())
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static String build(Random random, Palette flavour) {
        List<String> heads = flavour == null ? GENERIC : flavour.heads();
        List<String> suffixes = flavour == null ? SUFFIXES : flavour.suffixes();
        List<String> nouns = flavour == null ? NOUNS : flavour.nouns();

        List<String> pool = random.nextInt(10) < 7 ? heads : GENERIC;
        String head = pool.get(random.nextInt(pool.size()));

        if (random.nextBoolean()) {
            return head + suffixes.get(random.nextInt(suffixes.size()));
        }
        return head + " " + nouns.get(random.nextInt(nouns.size()));
    }
}
