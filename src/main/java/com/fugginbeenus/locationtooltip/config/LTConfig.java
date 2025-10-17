package com.fugginbeenus.locationtooltip.config;

/**
 * Stores all client-side appearance and display settings for the Location Tooltip HUD.
 * (Will later support Cloth Config / ModMenu integration.)
 */
public class LTConfig {

    // Visual
    public float backgroundOpacity = 0.7f;
    public float textScale = 1.0f;
    public int pillPadding = 6;
    public int cornerRadius = 10;
    public boolean shadow = true;
    public float gradientSheen = 0.28f; // 0..1

    // Elements
    public boolean showRegionName = true;
    public boolean showClock = true;
    public boolean time24h = false;
    public String separator = " • ";

    // Layout
    public Position position = Position.TOP_CENTER;
    public int xOffset = 0;
    public int yOffset = 8;
    public int iconSize = 10;
    public int spacing = 8;      // spacing between split pills
    public int verticalNudge = 1; // nudge text down inside pill
    public float pillHeightScale = 1.00f; // multiply computed height
    public int   pillExtraWidth  = 0;     // add N pixels horizontally


    // Split
    public boolean splitElements = false; // if true, region + clock render as separate pills

    public enum Position { TOP_LEFT, TOP_CENTER, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT }

    // Runtime singleton
    private static LTConfig INSTANCE;
    public static LTConfig get() { if (INSTANCE == null) INSTANCE = new LTConfig(); return INSTANCE; }
    private LTConfig() {}
}