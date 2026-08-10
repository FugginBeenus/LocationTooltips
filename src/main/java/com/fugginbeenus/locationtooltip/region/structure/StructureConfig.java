package com.fugginbeenus.locationtooltip.region.structure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

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

    /**
     * Auto-tag every structure that comes from a mod (any non-"minecraft" namespace) without
     * having to list it. Vanilla stays curated via {@link #structures} so the world isn't
     * flooded with mineshafts/ruined portals, while modded structures — including modded
     * versions of vanilla ones — work out of the box.
     */
    public boolean tagModdedStructures = true;

    /** Registry ids of structures to tag (as strings, e.g. "minecraft:village_plains"). */
    public Set<String> structures = defaultStructures();

    /** Explicit exclusions, checked first — works for vanilla and modded ids alike. */
    public Set<String> denied = new LinkedHashSet<>();

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
                    if (c.denied == null) c.denied = new LinkedHashSet<>(); // added after 0.3.0
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

    /** Deny list wins; then the explicit list; then modded structures if auto-tagging is on. */
    public boolean isAllowed(ResourceLocation id) {
        String key = id.toString();
        if (denied.contains(key)) return false;
        if (structures.contains(key)) return true;
        return tagModdedStructures && !"minecraft".equals(id.getNamespace());
    }

    /** Tag this structure (also clears any deny entry). */
    public boolean allow(String id) {
        boolean changed = denied.remove(id);
        changed |= structures.add(id);
        if (changed) save();
        return changed;
    }

    /** Stop tagging this structure — works for modded ids that were auto-tagged. */
    public boolean deny(String id) {
        boolean changed = structures.remove(id);
        changed |= denied.add(id);
        if (changed) save();
        return changed;
    }

    public void setEnabled(boolean value) {
        enabled = value;
        save();
    }

    public void setTagModdedStructures(boolean value) {
        tagModdedStructures = value;
        save();
    }
}
