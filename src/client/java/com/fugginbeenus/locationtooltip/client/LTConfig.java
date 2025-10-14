package com.fugginbeenus.locationtooltip.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Simple JSON-based config for Location Tooltip.
 * Stored at: config/locationtooltip/config.json
 */
public class LTConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "config.json";

    // Default values
    public boolean showClock = true;
    public int centerOffset = 140;
    public int gutterY = 42;
    public int fadeTicks = 40;

    private static LTConfig INSTANCE;

    public static LTConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        try {
            File dir = new File(MinecraftClient.getInstance().runDirectory, "config/locationtooltip");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, FILE_NAME);
            if (!file.exists()) {
                INSTANCE = new LTConfig();
                save();
                return;
            }

            try (FileReader reader = new FileReader(file)) {
                INSTANCE = GSON.fromJson(reader, LTConfig.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            INSTANCE = new LTConfig();
        }
    }

    public static void save() {
        try {
            File dir = new File(MinecraftClient.getInstance().runDirectory, "config/locationtooltip");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, FILE_NAME);
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
