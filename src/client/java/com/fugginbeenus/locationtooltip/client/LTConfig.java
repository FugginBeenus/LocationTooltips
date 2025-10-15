package com.fugginbeenus.locationtooltip.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/** JSON config at config/locationtooltip/config.json */
public class LTConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "config.json";

    // Basic toggles
    public boolean showClock = true;
    /** auto | split | together */
    public String layoutMode = "auto";

    // Simple placement (top-centered anchor)
    /** px from horizontal center (+ right, - left) */
    public int offsetX = 0;
    /** px from top of the screen */
    public int offsetY = 18;
    /** global scale 0.50..1.50 */
    public float scale = 0.90f;

    // Small layout knobs
    /** gap between location & clock when together */
    public int togetherGap = 8;
    /** empty space between left & right bars when split (center area for Jade) */
    public int centerGap = 220;

    // Visual style
    public boolean useNineSlice = true;
    public boolean matchJadeMetrics = true;   // icon/text padding/height like Jade
    /** panel alpha 0.10..1.00 */
    public float panelAlpha = 0.35f;
    /** text shadow (disable to remove ghosty outline) */
    public boolean textShadow = false;
    /** font scale (text only) */
    public float fontScale = 1.00f;

    // ---- Region selection settings (RESTORED so RegionSelectionClient compiles) ----
    /** If true, selection Y is placed one block above the clicked block. */
    public boolean placeOnTop = false;   // default back to "in the block" per your preference
    /** Extend selection upward by this many blocks when saving. */
    public int padAbove = 2;
    /** Extend selection downward by this many blocks when saving. */
    public int padBelow = 1;

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
            try (FileReader r = new FileReader(file)) {
                INSTANCE = GSON.fromJson(r, LTConfig.class);
                if (INSTANCE == null) INSTANCE = new LTConfig();
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
            try (FileWriter w = new FileWriter(file)) {
                GSON.toJson(INSTANCE, w);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
