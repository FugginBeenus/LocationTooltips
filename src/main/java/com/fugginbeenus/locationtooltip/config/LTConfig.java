package com.fugginbeenus.locationtooltip.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Central config for Location Tooltip.
 * - Vector pills by default; optional 9-slice textures supported.
 * - Values are clamped on load/save so bad edits can't break the HUD.
 * - Thread-safe singleton; call LTConfig.get() anywhere.
 */
public final class LTConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("locationtooltip.json");

    // ---------------- Visibility ----------------
    public boolean showRegionName = true;
    public boolean showClock      = true;
    public boolean time24h        = false;
    public String  separator      = " • ";
    /** When true, region and time use two independent pills. */
    public boolean splitElements  = true;

    // ---------------- Appearance (vector pills) ----------------
    /** Background alpha ONLY used by solid-color pills (not textured). */
    public float  backgroundOpacity = 0.85f; // 0..1
    public int    iconSize          = 16;    // px
    public int    pillPadding       = 6;     // px inside pill around content
    public float  textScale         = 1.0f;  // >= 0.5
    public float  pillHeightScale   = 1.0f;  // 0.5 .. 2.5
    /** Rounded-corner fallback when not using textures. */
    public int    cornerRadius      = 1;     // px default for “vanilla-ish” rounded corners
    /** Gap between split pills. */
    public int    spacing           = 8;     // px
    /** Extra width for single pill only (ignored when splitElements=true). */
    public int    pillExtraWidth    = 0;     // px
    public boolean shadow           = true;
    /** Nudge text vertically inside pill. */
    public int    verticalNudge     = 1;     // px

    // Corner styling (for vector pills)
    public CornerStyle cornerStyle = CornerStyle.ROUND; // ROUND, PILL, SQUIRCLE
    public float cornerExponent = 4.0f; // Squircle exponent "n": 2 = oval, 4 = soft square, higher = squarer
    public int borderWidth = 0; // px

    public enum CornerStyle { ROUND, PILL, SQUIRCLE }

    // ---------------- Positioning ----------------
    public Position position = Position.TOP_CENTER;
    public int xOffset = 0;
    public int yOffset = 8;

    // ---------------- Textured pills (optional 9-slice) ----------------
    /** Master switch for using 9-slice textures. */
    public boolean useTexturedPills = false;

    /** Source texture size for the pills (e.g., 64x32). */
    public int texW = 64;
    public int texH = 32;

    /** Nine-slice caps. */
    public int sliceLeft   = 8;
    public int sliceRight  = 8;
    public int sliceTop    = 8;
    public int sliceBottom = 8;

    /** Legacy/compat – kept so old configs still deserialize safely. */
    public float gradientSheen = 0f;

    // ---------------- Singleton plumbing ----------------
    private static LTConfig INSTANCE;

    private LTConfig() {}

    public static synchronized LTConfig get() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    /** Optional helper if your UI wants to force a fresh read from disk. */
    public static synchronized LTConfig reload() {
        INSTANCE = load();
        return INSTANCE;
    }

    // ---------------- Persistence ----------------
    private static LTConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                LTConfig cfg = GSON.fromJson(r, LTConfig.class);
                if (cfg != null) {
                    cfg.clamp();
                    return cfg;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LTConfig fresh = new LTConfig();
        fresh.clamp();
        fresh.save();
        return fresh;
    }

    /** Persist current config (values are clamped before writing). */
    public synchronized void save() {
        clamp();
        try {
            if (!Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // If you have a live preview bridge, you can notify it here.
        // try { com.fugginbeenus.locationtooltip.config.ui.ConfigLiveBridge.onConfigChanged(this); } catch (Throwable ignored) {}
    }

    /** Keep all values in safe bounds to avoid rendering glitches. */
    private void clamp() {
        backgroundOpacity = clamp(backgroundOpacity, 0f, 1f);
        iconSize          = clamp(iconSize, 8, 64);
        pillPadding       = clamp(pillPadding, 0, 24);
        textScale         = clamp(textScale, 0.5f, 3.0f);
        pillHeightScale   = clamp(pillHeightScale, 0.5f, 2.5f);
        cornerRadius      = clamp(cornerRadius, 0, 32);
        spacing           = clamp(spacing, 0, 48);
        pillExtraWidth    = clamp(pillExtraWidth, 0, 64);
        verticalNudge     = clamp(verticalNudge, -16, 16);

        texW = Math.max(1, texW);
        texH = Math.max(1, texH);
        sliceLeft   = clamp(sliceLeft,   0, texW / 2);
        sliceRight  = clamp(sliceRight,  0, texW / 2);
        sliceTop    = clamp(sliceTop,    0, texH / 2);
        sliceBottom = clamp(sliceBottom, 0, texH / 2);

        if (separator == null) separator = " • ";
        // If split pills are used, extra width on single pill has no effect—but keep it persisted as-is.
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static float clamp(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }

    // ---------------- Types ----------------
    public enum Position {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }
}
