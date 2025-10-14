package com.fugginbeenus.locationtooltip.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;

public final class LTConfig {
    public enum Layout { CENTER, SPLIT, LEFT, RIGHT }

    public Layout layout = Layout.CENTER;
    public boolean preferSplitWhenJade = true;
    public int topMargin = 8;      // px from top
    public int sideMargin = 8;     // px from edges
    public int gap = 10;           // inner gap between icon/text

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static LTConfig INSTANCE = new LTConfig();

    public static LTConfig get() { return INSTANCE; }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("locationtooltip/config.json");
    }

    public static void load() {
        try {
            Path p = path();
            Files.createDirectories(p.getParent());
            if (Files.notExists(p)) {
                saveDefault();
            }
            try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                LTConfig cfg = GSON.fromJson(r, LTConfig.class);
                INSTANCE = Objects.requireNonNullElseGet(cfg, LTConfig::new);
            }
        } catch (Exception e) {
            e.printStackTrace();
            INSTANCE = new LTConfig();
        }
    }

    private static void saveDefault() {
        try (BufferedWriter w = Files.newBufferedWriter(path(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write(GSON.toJson(new LTConfig()));
        } catch (Exception ignored) {}
    }
}
