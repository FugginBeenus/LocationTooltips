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

public final class StructureConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("locationtooltip-structures.json");

    public boolean enabled = true;

    public boolean tagModdedStructures = true;

    public boolean nameVillages = true;

    public boolean allowPlayerVillageNaming = false;

    public Set<String> structures = defaultStructures();

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
                    if (c.denied == null) c.denied = new LinkedHashSet<>();
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

    public boolean isAllowed(ResourceLocation id) {
        String key = id.toString();
        if (denied.contains(key)) return false;
        if (structures.contains(key)) return true;
        return tagModdedStructures && !"minecraft".equals(id.getNamespace());
    }

    public boolean allow(String id) {
        boolean changed = denied.remove(id);
        changed |= structures.add(id);
        if (changed) save();
        return changed;
    }

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

    public void setNameVillages(boolean value) {
        this.nameVillages = value;
        save();
    }

    public void setAllowPlayerVillageNaming(boolean value) {
        this.allowPlayerVillageNaming = value;
        save();
    }

    public void setTagModdedStructures(boolean value) {
        tagModdedStructures = value;
        save();
    }
}
