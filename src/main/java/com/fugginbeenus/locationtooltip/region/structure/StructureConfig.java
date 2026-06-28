package com.fugginbeenus.locationtooltip.region.structure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Server-side config for structure auto-tagging, stored at
 * {@code config/locationtooltip-structures.json}.
 *
 * Holds the master on/off toggle and the set of structure ids to tag. Because the set is
 * keyed by registry id strings (e.g. {@code "minecraft:village_plains"}), server owners can
 * add modded structures (e.g. {@code "mymod:castle"}) just by editing the file or using the
 * {@code /ltregion structures enable <id>} command — no code change needed.
 */
public final class StructureConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("locationtooltip-structures.json");

    /** Master switch for the whole structure-tagging system. */
    public boolean enabled = true;

    /** Registry ids of structures to tag (as strings, e.g. "minecraft:village_plains"). */
    public Set<String> structures = defaultStructures();

    private static Set<String> defaultStructures() {
        return new LinkedHashSet<>(List.of(
                "minecraft:village_plains", "minecraft:village_desert", "minecraft:village_savanna",
                "minecraft:village_snowy", "minecraft:village_taiga",
                "minecraft:pillager_outpost", "minecraft:mansion", "minecraft:monument", "minecraft:ancient_city",
                "minecraft:fortress", "minecraft:bastion_remnant", "minecraft:end_city", "minecraft:stronghold",
                "minecraft:desert_pyramid", "minecraft:jungle_pyramid", "minecraft:swamp_hut",
                "minecraft:igloo", "minecraft:trail_ruins"
        ));
    }

    // ---- singleton ----
    private static StructureConfig INSTANCE;
    private StructureConfig() {}

    public static synchronized StructureConfig get() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    private static StructureConfig load() {
        if (Files.exists(PATH)) {
            try (Reader r = Files.newBufferedReader(PATH)) {
                StructureConfig c = GSON.fromJson(r, StructureConfig.class);
                if (c != null) {
                    if (c.structures == null) c.structures = defaultStructures();
                    return c;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        StructureConfig fresh = new StructureConfig();
        fresh.save();
        return fresh;
    }

    public synchronized void save() {
        try {
            if (PATH.getParent() != null && !Files.exists(PATH.getParent())) {
                Files.createDirectories(PATH.getParent());
            }
            try (Writer w = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, w);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---- API ----
    public boolean isAllowed(Identifier id) {
        return structures.contains(id.toString());
    }

    public boolean add(String id) {
        boolean changed = structures.add(id);
        if (changed) save();
        return changed;
    }

    public boolean remove(String id) {
        boolean changed = structures.remove(id);
        if (changed) save();
        return changed;
    }

    public void setEnabled(boolean value) {
        enabled = value;
        save();
    }
}
