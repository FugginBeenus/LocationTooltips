package com.fugginbeenus.locationtooltip.region.structure;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class VillageNaming implements StructureNameProvider {

    private static final List<String> GENERIC = List.of(
            "Iron", "Old", "Still", "Raven", "Stone", "Elder", "Silver", "Black",
            "High", "Red", "Grey", "Long", "Bell", "Fox", "Crow", "Hearth");

    private static final List<String> SNOWY = List.of(
            "Frost", "Winter", "Rime", "Snow", "Pale", "North", "Hoar", "Chill", "White");

    private static final List<String> DESERT = List.of(
            "Dune", "Sun", "Amber", "Dust", "Sand", "Dry", "Copper", "Scorch", "Bone");

    private static final List<String> TAIGA = List.of(
            "Pine", "Spruce", "Fern", "Timber", "Elk", "Moss", "Bear", "Cedar");

    private static final List<String> SAVANNA = List.of(
            "Acacia", "Gold", "Wide", "Dry", "Grass", "Amber", "Sun", "Wind");

    private static final List<String> PLAINS = List.of(
            "Green", "Meadow", "Wheat", "Hill", "Fair", "Bright", "Willow", "Apple");

    private static final List<String> SUFFIXES = List.of(
            "dell", "brook", "ford", "hollow", "wick", "stead", "mere", "fell",
            "holt", "gate", "wood", "field", "bury", "ton", "march", "crest");

    private static final List<String> NOUNS = List.of(
            "Shire", "Hamlet", "Crossing", "Rest", "Watch", "Hollow", "Reach",
            "Landing", "Wells", "Row", "Green", "End", "Barrow", "Court", "Vale");

    @Override
    public Optional<String> nameFor(MinecraftServer server, ResourceLocation dim,
                                    ResourceLocation structureId, BoundingBox box) {
        if (!StructureConfig.get().nameVillages) return Optional.empty();
        if (structureId == null || box == null) return Optional.empty();
        if (!structureId.getPath().contains("village")) return Optional.empty();

        Random random = new Random(seedFor(dim, box));
        return Optional.of(build(random, flavourFor(structureId)));
    }

    private static long seedFor(ResourceLocation dim, BoundingBox box) {
        long seed = dim == null ? 0L : dim.toString().hashCode();
        seed = seed * 31L + box.minX();
        seed = seed * 31L + box.minZ();
        return seed;
    }

    private static List<String> flavourFor(ResourceLocation structureId) {
        String path = structureId.getPath();
        if (path.contains("snow") || path.contains("frozen")) return SNOWY;
        if (path.contains("desert")) return DESERT;
        if (path.contains("taiga") || path.contains("forest")) return TAIGA;
        if (path.contains("savanna")) return SAVANNA;
        if (path.contains("plains") || path.contains("meadow")) return PLAINS;
        return GENERIC;
    }

    private static String build(Random random, List<String> flavour) {
        List<String> pool = random.nextInt(10) < 7 ? flavour : GENERIC;
        String head = pool.get(random.nextInt(pool.size()));

        if (random.nextBoolean()) {
            return head + SUFFIXES.get(random.nextInt(SUFFIXES.size()));
        }
        return head + " " + NOUNS.get(random.nextInt(NOUNS.size()));
    }
}
