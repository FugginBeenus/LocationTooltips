package com.fugginbeenus.locationtooltip.config.ui;

import com.fugginbeenus.locationtooltip.config.LTConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import com.fugginbeenus.locationtooltip.config.ui.ConfigLiveBridge;
import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

/**
 * ClothConfig screen for Location Tooltip (LIVE).
 * - Uses native int sliders; floats are mapped to step sliders.
 * - Live updates: entries are polled each tick while this screen is open.
 * - Still saves on "Done".
 */
public final class LTClothConfig {
    private LTClothConfig() {}

    public static Screen create(Screen parent) {
        final LTConfig cfg = LTConfig.get();
        final List<ConfigLiveBridge.Tracked<?>> tracked = new ArrayList<>();

        final var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Location Tooltip (Advanced)"))
                .setTransparentBackground(true);

        builder.setSavingRunnable(cfg::save);
        final var eb = builder.entryBuilder();

        // ---- General ----
        {
            var cat = builder.getOrCreateCategory(Text.literal("General"));

            var eShowRegion = eb.startBooleanToggle(Text.literal("Show Region Name"), cfg.showRegionName)
                    .setSaveConsumer(v -> { cfg.showRegionName = v; cfg.save(); })
                    .build();
            cat.addEntry(eShowRegion);
            tracked.add(new ConfigLiveBridge.Tracked<>(eShowRegion, v -> cfg.showRegionName = v));

            var eShowClock = eb.startBooleanToggle(Text.literal("Show Clock"), cfg.showClock)
                    .setSaveConsumer(v -> { cfg.showClock = v; cfg.save(); })
                    .build();
            cat.addEntry(eShowClock);
            tracked.add(new ConfigLiveBridge.Tracked<>(eShowClock, v -> cfg.showClock = v));

            var e24h = eb.startBooleanToggle(Text.literal("24h Time"), cfg.time24h)
                    .setSaveConsumer(v -> { cfg.time24h = v; cfg.save(); })
                    .build();
            cat.addEntry(e24h);
            tracked.add(new ConfigLiveBridge.Tracked<>(e24h, v -> cfg.time24h = v));

            var eSep = eb.startStrField(Text.literal("Single Pill Separator"), cfg.separator)
                    .setSaveConsumer(v -> { cfg.separator = (v == null || v.isEmpty()) ? " • " : v; cfg.save(); })
                    .build();
            cat.addEntry(eSep);
            tracked.add(new ConfigLiveBridge.Tracked<>(eSep, v -> cfg.separator = (v == null || v.isEmpty()) ? " • " : v));

            var eSplit = eb.startBooleanToggle(Text.literal("Split Elements (Two Pills)"), cfg.splitElements)
                    .setSaveConsumer(v -> { cfg.splitElements = v; cfg.save(); })
                    .build();
            cat.addEntry(eSplit);
            tracked.add(new ConfigLiveBridge.Tracked<>(eSplit, v -> cfg.splitElements = v));
        }

        // ---- Appearance ----
        {
            var cat = builder.getOrCreateCategory(Text.literal("Appearance"));

            var eOpacity = eb.startIntSlider(Text.literal("Background Opacity"), toPct(cfg.backgroundOpacity), 0, 100)
                    .setTooltip(Text.literal("Opacity of pill background (0–100%)"))
                    .setTextGetter(pct -> Text.literal(toPctLabel(pct)))
                    .setSaveConsumer(pct -> { cfg.backgroundOpacity = clampF(fromPct(pct), 0f, 1f); cfg.save(); })
                    .build();
            cat.addEntry(eOpacity);
            tracked.add(new ConfigLiveBridge.Tracked<>(eOpacity, pct -> cfg.backgroundOpacity = clampF(fromPct(pct), 0f, 1f)));

            var eIcon = eb.startIntSlider(Text.literal("Icon Size (px)"), cfg.iconSize, 8, 64)
                    .setSaveConsumer(v -> { cfg.iconSize = clamp(v, 8, 64); cfg.save(); })
                    .build();
            cat.addEntry(eIcon);
            tracked.add(new ConfigLiveBridge.Tracked<>(eIcon, v -> cfg.iconSize = clamp(v, 8, 64)));

            var ePad = eb.startIntSlider(Text.literal("Pill Padding (px)"), cfg.pillPadding, 0, 24)
                    .setSaveConsumer(v -> { cfg.pillPadding = clamp(v, 0, 24); cfg.save(); })
                    .build();
            cat.addEntry(ePad);
            tracked.add(new ConfigLiveBridge.Tracked<>(ePad, v -> cfg.pillPadding = clamp(v, 0, 24)));

            var eTextScale = eb.startIntSlider(Text.literal("Text Scale"),
                            toSteps(cfg.textScale, 0.50f, 3.00f, 0.05f), 0, stepsRange(0.50f, 3.00f, 0.05f))
                    .setTextGetter(s -> Text.literal(String.format("%.2f×", fromSteps(s, 0.50f, 0.05f))))
                    .setSaveConsumer(s -> { cfg.textScale = clampF(fromSteps(s, 0.50f, 0.05f), 0.50f, 3.00f); cfg.save(); })
                    .build();
            cat.addEntry(eTextScale);
            tracked.add(new ConfigLiveBridge.Tracked<>(eTextScale, s -> cfg.textScale = clampF(fromSteps(s, 0.50f, 0.05f), 0.50f, 3.00f)));

            var eHeightScale = eb.startIntSlider(Text.literal("Pill Height Scale"),
                            toSteps(cfg.pillHeightScale, 0.50f, 2.50f, 0.05f), 0, stepsRange(0.50f, 2.50f, 0.05f))
                    .setTextGetter(s -> Text.literal(String.format("%.2f×", fromSteps(s, 0.50f, 0.05f))))
                    .setSaveConsumer(s -> { cfg.pillHeightScale = clampF(fromSteps(s, 0.50f, 0.05f), 0.50f, 2.50f); cfg.save(); })
                    .build();
            cat.addEntry(eHeightScale);
            tracked.add(new ConfigLiveBridge.Tracked<>(eHeightScale, s -> cfg.pillHeightScale = clampF(fromSteps(s, 0.50f, 0.05f), 0.50f, 2.50f)));

            var eCornerR = eb.startIntSlider(Text.literal("Corner Radius (px)"), cfg.cornerRadius, 0, 32)
                    .setSaveConsumer(v -> { cfg.cornerRadius = clamp(v, 0, 32); cfg.save(); })
                    .build();
            cat.addEntry(eCornerR);
            tracked.add(new ConfigLiveBridge.Tracked<>(eCornerR, v -> cfg.cornerRadius = clamp(v, 0, 32)));

            var eSpacing = eb.startIntSlider(Text.literal("Spacing Between Pills (px)"), cfg.spacing, 0, 48)
                    .setSaveConsumer(v -> { cfg.spacing = clamp(v, 0, 48); cfg.save(); })
                    .build();
            cat.addEntry(eSpacing);
            tracked.add(new ConfigLiveBridge.Tracked<>(eSpacing, v -> cfg.spacing = clamp(v, 0, 48)));

            var eExtra = eb.startIntSlider(Text.literal("Extra Width (single pill only)"), cfg.pillExtraWidth, 0, 64)
                    .setSaveConsumer(v -> { cfg.pillExtraWidth = clamp(v, 0, 64); cfg.save(); })
                    .build();
            cat.addEntry(eExtra);
            tracked.add(new ConfigLiveBridge.Tracked<>(eExtra, v -> cfg.pillExtraWidth = clamp(v, 0, 64)));

            var eShadow = eb.startBooleanToggle(Text.literal("Text Shadow"), cfg.shadow)
                    .setSaveConsumer(v -> { cfg.shadow = v; cfg.save(); })
                    .build();
            cat.addEntry(eShadow);
            tracked.add(new ConfigLiveBridge.Tracked<>(eShadow, v -> cfg.shadow = v));

            var eNudge = eb.startIntSlider(Text.literal("Vertical Text Nudge (px)"), cfg.verticalNudge, -16, 16)
                    .setSaveConsumer(v -> { cfg.verticalNudge = clamp(v, -16, 16); cfg.save(); })
                    .build();
            cat.addEntry(eNudge);
            tracked.add(new ConfigLiveBridge.Tracked<>(eNudge, v -> cfg.verticalNudge = clamp(v, -16, 16)));

            var eStyle = eb.startEnumSelector(Text.literal("Corner Style"), LTConfig.CornerStyle.class, cfg.cornerStyle)
                    .setSaveConsumer(v -> { cfg.cornerStyle = v; cfg.save(); })
                    .build();
            cat.addEntry(eStyle);
            tracked.add(new ConfigLiveBridge.Tracked<>(eStyle, v -> cfg.cornerStyle = v));

            var eExponent = eb.startIntSlider(Text.literal("Corner Exponent (squircle)"),
                            toSteps(cfg.cornerExponent, 1.5f, 12.0f, 0.1f), 0, stepsRange(1.5f, 12.0f, 0.1f))
                    .setTextGetter(s -> Text.literal(String.format("%.1f", fromSteps(s, 1.5f, 0.1f))))
                    .setSaveConsumer(s -> { cfg.cornerExponent = clampF(fromSteps(s, 1.5f, 0.1f), 1.5f, 12.0f); cfg.save(); })
                    .build();
            cat.addEntry(eExponent);
            tracked.add(new ConfigLiveBridge.Tracked<>(eExponent, s -> cfg.cornerExponent = clampF(fromSteps(s, 1.5f, 0.1f), 1.5f, 12.0f)));

            var eBorder = eb.startIntSlider(Text.literal("Border Width (px)"), cfg.borderWidth, 0, 6)
                    .setSaveConsumer(v -> { cfg.borderWidth = clamp(v, 0, 6); cfg.save(); })
                    .build();
            cat.addEntry(eBorder);
            tracked.add(new ConfigLiveBridge.Tracked<>(eBorder, v -> cfg.borderWidth = clamp(v, 0, 6)));
        }

        // ---- Position ----
        {
            var cat = builder.getOrCreateCategory(Text.literal("Position"));

            var ePos = eb.startEnumSelector(Text.literal("Anchor Position"), LTConfig.Position.class, cfg.position)
                    .setSaveConsumer(v -> { cfg.position = v; cfg.save(); })
                    .build();
            cat.addEntry(ePos);
            tracked.add(new ConfigLiveBridge.Tracked<>(ePos, v -> cfg.position = v));

            var eX = eb.startIntField(Text.literal("X Offset (px)"), cfg.xOffset)
                    .setSaveConsumer(v -> { cfg.xOffset = v; cfg.save(); })
                    .build();
            cat.addEntry(eX);
            tracked.add(new ConfigLiveBridge.Tracked<>(eX, v -> cfg.xOffset = v));

            var eY = eb.startIntField(Text.literal("Y Offset (px)"), cfg.yOffset)
                    .setSaveConsumer(v -> { cfg.yOffset = v; cfg.save(); })
                    .build();
            cat.addEntry(eY);
            tracked.add(new ConfigLiveBridge.Tracked<>(eY, v -> cfg.yOffset = v));
        }

        // ---- Optional Textures ----
        {
            var cat = builder.getOrCreateCategory(Text.literal("Textures (Optional 9-slice)"));

            var eUse = eb.startBooleanToggle(Text.literal("Use Textured Pills"), cfg.useTexturedPills)
                    .setSaveConsumer(v -> { cfg.useTexturedPills = v; cfg.save(); })
                    .build();
            cat.addEntry(eUse);
            tracked.add(new ConfigLiveBridge.Tracked<>(eUse, v -> cfg.useTexturedPills = v));

            var eTexW = eb.startIntField(Text.literal("Texture Width (texW)"), cfg.texW)
                    .setSaveConsumer(v -> { cfg.texW = Math.max(1, v); cfg.save(); })
                    .build();
            cat.addEntry(eTexW);
            tracked.add(new ConfigLiveBridge.Tracked<>(eTexW, v -> cfg.texW = Math.max(1, v)));

            var eTexH = eb.startIntField(Text.literal("Texture Height (texH)"), cfg.texH)
                    .setSaveConsumer(v -> { cfg.texH = Math.max(1, v); cfg.save(); })
                    .build();
            cat.addEntry(eTexH);
            tracked.add(new ConfigLiveBridge.Tracked<>(eTexH, v -> cfg.texH = Math.max(1, v)));

            var eSL = eb.startIntField(Text.literal("Slice Left"), cfg.sliceLeft)
                    .setSaveConsumer(v -> { cfg.sliceLeft = Math.max(0, v); cfg.save(); })
                    .build();
            cat.addEntry(eSL);
            tracked.add(new ConfigLiveBridge.Tracked<>(eSL, v -> cfg.sliceLeft = Math.max(0, v)));

            var eSR = eb.startIntField(Text.literal("Slice Right"), cfg.sliceRight)
                    .setSaveConsumer(v -> { cfg.sliceRight = Math.max(0, v); cfg.save(); })
                    .build();
            cat.addEntry(eSR);
            tracked.add(new ConfigLiveBridge.Tracked<>(eSR, v -> cfg.sliceRight = Math.max(0, v)));

            var eST = eb.startIntField(Text.literal("Slice Top"), cfg.sliceTop)
                    .setSaveConsumer(v -> { cfg.sliceTop = Math.max(0, v); cfg.save(); })
                    .build();
            cat.addEntry(eST);
            tracked.add(new ConfigLiveBridge.Tracked<>(eST, v -> cfg.sliceTop = Math.max(0, v)));

            var eSB = eb.startIntField(Text.literal("Slice Bottom"), cfg.sliceBottom)
                    .setSaveConsumer(v -> { cfg.sliceBottom = Math.max(0, v); cfg.save(); })
                    .build();
            cat.addEntry(eSB);
            tracked.add(new ConfigLiveBridge.Tracked<>(eSB, v -> cfg.sliceBottom = Math.max(0, v)));
        }

        // Build the screen and start the live session
        Screen screen = builder.build();
        ConfigLiveBridge.beginSession(screen, tracked);
        return screen;
    }

    /* ---------------- helpers: read entry values safely ---------------- */
    @SuppressWarnings("unchecked")
    private static boolean getBool(AbstractConfigListEntry<?> e, boolean def) {
        Object v = e.getValue();
        return (v instanceof Boolean b) ? b : def;
    }
    @SuppressWarnings("unchecked")
    private static int getInt(AbstractConfigListEntry<?> e, int def) {
        Object v = e.getValue();
        return (v instanceof Integer i) ? i : def;
    }
    @SuppressWarnings("unchecked")
    private static String getStr(AbstractConfigListEntry<?> e, String def) {
        Object v = e.getValue();
        return (v instanceof String s) ? s : def;
    }
    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E getEnum(AbstractConfigListEntry<?> e, E def) {
        Object v = e.getValue();
        return (v != null && v.getClass().isEnum()) ? (E) v : def;
    }
    private static String nonEmpty(String s, String fallback) {
        return (s == null || s.isEmpty()) ? fallback : s;
    }

    /* ---------------- numeric helpers ---------------- */

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static float clampF(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }

    // 0..1 <-> 0..100%
    private static int toPct(float f) { return (int)Math.round(clampF(f, 0f, 1f) * 100f); }
    private static float fromPct(int pct) { return clampF(pct / 100f, 0f, 1f); }
    private static String toPctLabel(int pct) { return pct + "%"; }

    // Float slider via steps: value = base + steps * step
    private static int stepsRange(float base, float max, float step) {
        return Math.round((max - base) / step);
    }
    private static int toSteps(float value, float base, float max, float step) {
        value = clampF(value, base, max);
        return Math.round((value - base) / step);
    }
    private static float fromSteps(int steps, float base, float step) {
        return base + steps * step;
    }
}
