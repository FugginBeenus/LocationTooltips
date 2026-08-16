package com.fugginbeenus.locationtooltip.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LTConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("locationtooltip.json");

    public boolean showRegionName = true;
    public boolean showClock      = true;
    public boolean time24h        = false;
    public String  separator      = " • ";
    public boolean splitElements  = true;

    public float  backgroundOpacity = 0.25f;
    public int    iconSize          = 9;
    public int    pillPadding       = 2;
    public float  textScale         = 0.6f;
    public float  pillHeightScale   = 1.0f;
    public int    cornerRadius      = 1;
    public int    spacing           = 5;
    public int    pillExtraWidth    = 0;
    public boolean shadow           = true;
    public int    verticalNudge     = 0;

    public CornerStyle cornerStyle = CornerStyle.ROUND;
    public float cornerExponent = 4.0f;
    public int borderWidth = 0;

    public enum CornerStyle { ROUND, PILL, SQUIRCLE }

    public Position position = Position.TOP_CENTER;
    public int xOffset = 0;
    public int yOffset = 4;

    public boolean showCoords = true;
    public Position coordsPosition = Position.TOP_CENTER;
    public int coordsXOffset = 8;
    public int coordsYOffset = 4;

    public boolean showBiome = true;
    public Position biomePosition = Position.TOP_CENTER;
    public int biomeXOffset = 8;
    public int biomeYOffset = 4;

    public boolean useTexturedPills = false;

    public int texW = 64;
    public int texH = 32;

    public int sliceLeft   = 8;
    public int sliceRight  = 8;
    public int sliceTop    = 8;
    public int sliceBottom = 8;

    public float gradientSheen = 0f;

    private static LTConfig INSTANCE;

    private LTConfig() {}

    public static synchronized LTConfig get() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    public static synchronized LTConfig reload() {
        INSTANCE = load();
        return INSTANCE;
    }

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
    }

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
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static float clamp(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }

    public enum Position {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }
}
