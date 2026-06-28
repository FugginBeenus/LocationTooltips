package com.fugginbeenus.locationtooltip.region.structure;

import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Resolves a friendly display name for a structure id. Vanilla structures get a curated
 * label; anything else (incl. modded structures) falls back to a prettified path, so the
 * system works for mods without per-mod code.
 */
public final class StructureNames {
    private StructureNames() {}

    private static final Map<String, String> NICE = Map.ofEntries(
            // Villages — show the biome variant
            Map.entry("village_plains",        "Plains Village"),
            Map.entry("village_desert",        "Desert Village"),
            Map.entry("village_savanna",       "Savanna Village"),
            Map.entry("village_snowy",         "Snowy Village"),
            Map.entry("village_taiga",         "Taiga Village"),
            // Landmarks
            Map.entry("pillager_outpost",      "Pillager Outpost"),
            Map.entry("mansion",               "Woodland Mansion"),
            Map.entry("monument",              "Ocean Monument"),
            Map.entry("ancient_city",          "Ancient City"),
            Map.entry("fortress",              "Nether Fortress"),
            Map.entry("bastion_remnant",       "Bastion Remnant"),
            Map.entry("end_city",              "End City"),
            Map.entry("stronghold",            "Stronghold"),
            Map.entry("desert_pyramid",        "Desert Pyramid"),
            Map.entry("jungle_pyramid",        "Jungle Temple"),
            Map.entry("swamp_hut",             "Witch Hut"),
            Map.entry("igloo",                 "Igloo"),
            Map.entry("trail_ruins",           "Trail Ruins"),
            // Names ready for if/when coverage expands beyond the landmark list
            Map.entry("shipwreck",             "Shipwreck"),
            Map.entry("shipwreck_beached",     "Beached Shipwreck"),
            Map.entry("ocean_ruin_cold",       "Cold Ocean Ruins"),
            Map.entry("ocean_ruin_warm",       "Warm Ocean Ruins"),
            Map.entry("buried_treasure",       "Buried Treasure"),
            Map.entry("mineshaft",             "Mineshaft"),
            Map.entry("mineshaft_mesa",        "Badlands Mineshaft"),
            Map.entry("nether_fossil",         "Nether Fossil"),
            Map.entry("ruined_portal",         "Ruined Portal"),
            Map.entry("ruined_portal_desert",  "Ruined Portal"),
            Map.entry("ruined_portal_jungle",  "Ruined Portal"),
            Map.entry("ruined_portal_swamp",   "Ruined Portal"),
            Map.entry("ruined_portal_mountain","Ruined Portal"),
            Map.entry("ruined_portal_ocean",   "Ruined Portal"),
            Map.entry("ruined_portal_nether",  "Ruined Portal")
    );

    /** Friendly name for a structure id (curated for vanilla, prettified path otherwise). */
    public static String displayName(Identifier structureId) {
        String nice = NICE.get(structureId.getPath());
        return (nice != null) ? nice : prettify(structureId.getPath());
    }

    private static String prettify(String path) {
        String[] parts = path.replace('/', ' ').replace('_', ' ').trim().split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        String out = sb.toString().trim();
        return out.isEmpty() ? "Structure" : out;
    }
}
