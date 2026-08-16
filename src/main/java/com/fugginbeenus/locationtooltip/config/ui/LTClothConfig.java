package com.fugginbeenus.locationtooltip.config.ui;

import com.fugginbeenus.locationtooltip.config.LTConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class LTClothConfig {
    private LTClothConfig() {}

    public static Screen create(Screen parent) {
        final LTConfig cfg = LTConfig.get();
        final List<ConfigLiveBridge.Tracked<?>> tracked = new ArrayList<>();

        final var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Location Tooltip (Advanced)"))
                .setTransparentBackground(true);

        builder.setSavingRunnable(cfg::save);
        final var eb = builder.entryBuilder();

        {
            var cat = builder.getOrCreateCategory(Component.literal("General"));

            var eShowRegion = eb.startBooleanToggle(Component.literal("Show Region Name"), cfg.showRegionName)
                    .setSaveConsumer(v -> { cfg.showRegionName = v; cfg.save(); })
                    .build();
            cat.addEntry(eShowRegion);
            tracked.add(new ConfigLiveBridge.Tracked<>(eShowRegion, v -> cfg.showRegionName = v));

            var eShowClock = eb.startBooleanToggle(Component.literal("Show Clock"), cfg.showClock)
                    .setSaveConsumer(v -> { cfg.showClock = v; cfg.save(); })
                    .build();
            cat.addEntry(eShowClock);
            tracked.add(new ConfigLiveBridge.Tracked<>(eShowClock, v -> cfg.showClock = v));

            var e24h = eb.startBooleanToggle(Component.literal("24h Time"), cfg.time24h)
                    .setSaveConsumer(v -> { cfg.time24h = v; cfg.save(); })
                    .build();
            cat.addEntry(e24h);
            tracked.add(new ConfigLiveBridge.Tracked<>(e24h, v -> cfg.time24h = v));

            var eSep = eb.startStrField(Component.literal("Single Pill Separator"), cfg.separator)
                    .setSaveConsumer(v -> { cfg.separator = (v == null || v.isEmpty()) ? " • " : v; cfg.save(); })
                    .build();
            cat.addEntry(eSep);
            tracked.add(new ConfigLiveBridge.Tracked<>(eSep, v -> cfg.separator = (v == null || v.isEmpty()) ? " • " : v));

            var eSplit = eb.startBooleanToggle(Component.literal("Split Elements (Two Pills)"), cfg.splitElements)
                    .setSaveConsumer(v -> { cfg.splitElements = v; cfg.save(); })
                    .build();
            cat.addEntry(eSplit);
            tracked.add(new ConfigLiveBridge.Tracked<>(eSplit, v -> cfg.splitElements = v));
        }

        {
            var cat = builder.getOrCreateCategory(Component.literal("Appearance"));

            var eOpacity = eb.startIntSlider(Component.literal("Background Opacity"), toPct(cfg.backgroundOpacity), 0, 100)
                    .setTooltip(Component.literal("Opacity of pill background (0–100%)"))
                    .setTextGetter(pct -> Component.literal(toPctLabel(pct)))
                    .setSaveConsumer(pct -> { cfg.backgroundOpacity = clampF(fromPct(pct), 0f, 1f); cfg.save(); })
                    .build();
            cat.addEntry(eOpacity);
            tracked.add(new ConfigLiveBridge.Tracked<>(eOpacity, pct -> cfg.backgroundOpacity = clampF(fromPct(pct), 0f, 1f)));

            var eIcon = eb.startIntSlider(Component.literal("Icon Size (px)"), cfg.iconSize, 8, 64)
                    .setSaveConsumer(v -> { cfg.iconSize = clamp(v, 8, 64); cfg.save(); })
                    .build();
            cat.addEntry(eIcon);
            tracked.add(new ConfigLiveBridge.Tracked<>(eIcon, v -> cfg.iconSize = clamp(v, 8, 64)));

            var ePad = eb.startIntSlider(Component.literal("Pill Padding (px)"), cfg.pillPadding, 0, 24)
                    .setSaveConsumer(v -> { cfg.pillPadding = clamp(v, 0, 24); cfg.save(); })
                    .build();
            cat.addEntry(ePad);
            tracked.add(new ConfigLiveBridge.Tracked<>(ePad, v -> cfg.pillPadding = clamp(v, 0, 24)));

            var eTextScale = eb.startIntSlider(Component.literal("Text Scale"),
                            toSteps(cfg.textScale, 0.50f, 3.00f, 0.05f), 0, stepsRange(0.50f, 3.00f, 0.05f))
                    .setTextGetter(s -> Component.literal(String.format("%.2f×", fromSteps(s, 0.50f, 0.05f))))
                    .setSaveConsumer(s -> { cfg.textScale = clampF(fromSteps(s, 0.50f, 0.05f), 0.50f, 3.00f); cfg.save(); })
                    .build();
            cat.addEntry(eTextScale);
            tracked.add(new ConfigLiveBridge.Tracked<>(eTextScale, s -> cfg.textScale = clampF(fromSteps(s, 0.50f, 0.05f), 0.50f, 3.00f)));

            var eHeightScale = eb.startIntSlider(Component.literal("Pill Height Scale"),
                            toSteps(cfg.pillHeightScale, 0.50f, 2.50f, 0.05f), 0, stepsRange(0.50f, 2.50f, 0.05f))
                    .setTextGetter(s -> Component.literal(String.format("%.2f×", fromSteps(s, 0.50f, 0.05f))))
                    .setSaveConsumer(s -> { cfg.pillHeightScale = clampF(fromSteps(s, 0.50f, 0.05f), 0.50f, 2.50f); cfg.save(); })
                    .build();
            cat.addEntry(eHeightScale);
            tracked.add(new ConfigLiveBridge.Tracked<>(eHeightScale, s -> cfg.pillHeightScale = clampF(fromSteps(s, 0.50f, 0.05f), 0.50f, 2.50f)));

            var eCornerR = eb.startIntSlider(Component.literal("Corner Radius (px)"), cfg.cornerRadius, 0, 32)
                    .setSaveConsumer(v -> { cfg.cornerRadius = clamp(v, 0, 32); cfg.save(); })
                    .build();
            cat.addEntry(eCornerR);
            tracked.add(new ConfigLiveBridge.Tracked<>(eCornerR, v -> cfg.cornerRadius = clamp(v, 0, 32)));

            var eSpacing = eb.startIntSlider(Component.literal("Spacing Between Pills (px)"), cfg.spacing, 0, 48)
                    .setSaveConsumer(v -> { cfg.spacing = clamp(v, 0, 48); cfg.save(); })
                    .build();
            cat.addEntry(eSpacing);
            tracked.add(new ConfigLiveBridge.Tracked<>(eSpacing, v -> cfg.spacing = clamp(v, 0, 48)));

            var eExtra = eb.startIntSlider(Component.literal("Extra Width (single pill only)"), cfg.pillExtraWidth, 0, 64)
                    .setSaveConsumer(v -> { cfg.pillExtraWidth = clamp(v, 0, 64); cfg.save(); })
                    .build();
            cat.addEntry(eExtra);
            tracked.add(new ConfigLiveBridge.Tracked<>(eExtra, v -> cfg.pillExtraWidth = clamp(v, 0, 64)));

            var eShadow = eb.startBooleanToggle(Component.literal("Text Shadow"), cfg.shadow)
                    .setSaveConsumer(v -> { cfg.shadow = v; cfg.save(); })
                    .build();
            cat.addEntry(eShadow);
            tracked.add(new ConfigLiveBridge.Tracked<>(eShadow, v -> cfg.shadow = v));

            var eNudge = eb.startIntSlider(Component.literal("Vertical Text Nudge (px)"), cfg.verticalNudge, -16, 16)
                    .setSaveConsumer(v -> { cfg.verticalNudge = clamp(v, -16, 16); cfg.save(); })
                    .build();
            cat.addEntry(eNudge);
            tracked.add(new ConfigLiveBridge.Tracked<>(eNudge, v -> cfg.verticalNudge = clamp(v, -16, 16)));

            var eStyle = eb.startEnumSelector(Component.literal("Corner Style"), LTConfig.CornerStyle.class, cfg.cornerStyle)
                    .setTooltip(Component.literal("ROUND = corner radius below; PILL = fully rounded ends; SQUIRCLE = soft square"))
                    .setSaveConsumer(v -> { cfg.cornerStyle = v; cfg.save(); })
                    .build();
            cat.addEntry(eStyle);
            tracked.add(new ConfigLiveBridge.Tracked<>(eStyle, v -> cfg.cornerStyle = v));

            var eBorder = eb.startIntSlider(Component.literal("Border Width (px)"), cfg.borderWidth, 0, 6)
                    .setSaveConsumer(v -> { cfg.borderWidth = clamp(v, 0, 6); cfg.save(); })
                    .build();
            cat.addEntry(eBorder);
            tracked.add(new ConfigLiveBridge.Tracked<>(eBorder, v -> cfg.borderWidth = clamp(v, 0, 6)));
        }

        {
            var cat = builder.getOrCreateCategory(Component.literal("Position"));

            var ePos = eb.startEnumSelector(Component.literal("Anchor Position"), LTConfig.Position.class, cfg.position)
                    .setSaveConsumer(v -> { cfg.position = v; cfg.save(); })
                    .build();
            cat.addEntry(ePos);
            tracked.add(new ConfigLiveBridge.Tracked<>(ePos, v -> cfg.position = v));

            var eX = eb.startIntField(Component.literal("X Offset (px)"), cfg.xOffset)
                    .setSaveConsumer(v -> { cfg.xOffset = v; cfg.save(); })
                    .build();
            cat.addEntry(eX);
            tracked.add(new ConfigLiveBridge.Tracked<>(eX, v -> cfg.xOffset = v));

            var eY = eb.startIntField(Component.literal("Y Offset (px)"), cfg.yOffset)
                    .setSaveConsumer(v -> { cfg.yOffset = v; cfg.save(); })
                    .build();
            cat.addEntry(eY);
            tracked.add(new ConfigLiveBridge.Tracked<>(eY, v -> cfg.yOffset = v));
        }

        {
            var cat = builder.getOrCreateCategory(Component.literal("Coordinates"));

            var eOn = eb.startBooleanToggle(Component.literal("Show Coordinates"), cfg.showCoords)
                    .setSaveConsumer(v -> { cfg.showCoords = v; cfg.save(); })
                    .build();
            cat.addEntry(eOn);
            tracked.add(new ConfigLiveBridge.Tracked<>(eOn, v -> cfg.showCoords = v));

            var ePos = eb.startEnumSelector(Component.literal("Anchor Position"), LTConfig.Position.class, cfg.coordsPosition)
                    .setSaveConsumer(v -> { cfg.coordsPosition = v; cfg.save(); })
                    .build();
            cat.addEntry(ePos);
            tracked.add(new ConfigLiveBridge.Tracked<>(ePos, v -> cfg.coordsPosition = v));

            var eX = eb.startIntField(Component.literal("X Offset (px)"), cfg.coordsXOffset)
                    .setSaveConsumer(v -> { cfg.coordsXOffset = v; cfg.save(); })
                    .build();
            cat.addEntry(eX);
            tracked.add(new ConfigLiveBridge.Tracked<>(eX, v -> cfg.coordsXOffset = v));

            var eY2 = eb.startIntField(Component.literal("Y Offset (px)"), cfg.coordsYOffset)
                    .setSaveConsumer(v -> { cfg.coordsYOffset = v; cfg.save(); })
                    .build();
            cat.addEntry(eY2);
            tracked.add(new ConfigLiveBridge.Tracked<>(eY2, v -> cfg.coordsYOffset = v));
        }

        {
            var cat = builder.getOrCreateCategory(Component.literal("Biome"));

            var eOn = eb.startBooleanToggle(Component.literal("Show Biome"), cfg.showBiome)
                    .setSaveConsumer(v -> { cfg.showBiome = v; cfg.save(); })
                    .build();
            cat.addEntry(eOn);
            tracked.add(new ConfigLiveBridge.Tracked<>(eOn, v -> cfg.showBiome = v));

            var ePos = eb.startEnumSelector(Component.literal("Anchor Position"), LTConfig.Position.class, cfg.biomePosition)
                    .setSaveConsumer(v -> { cfg.biomePosition = v; cfg.save(); })
                    .build();
            cat.addEntry(ePos);
            tracked.add(new ConfigLiveBridge.Tracked<>(ePos, v -> cfg.biomePosition = v));

            var eX = eb.startIntField(Component.literal("X Offset (px)"), cfg.biomeXOffset)
                    .setSaveConsumer(v -> { cfg.biomeXOffset = v; cfg.save(); })
                    .build();
            cat.addEntry(eX);
            tracked.add(new ConfigLiveBridge.Tracked<>(eX, v -> cfg.biomeXOffset = v));

            var eY3 = eb.startIntField(Component.literal("Y Offset (px)"), cfg.biomeYOffset)
                    .setSaveConsumer(v -> { cfg.biomeYOffset = v; cfg.save(); })
                    .build();
            cat.addEntry(eY3);
            tracked.add(new ConfigLiveBridge.Tracked<>(eY3, v -> cfg.biomeYOffset = v));
        }

        Screen screen = builder.build();
        ConfigLiveBridge.beginSession(screen, tracked);
        return screen;
    }

    /* ---------------- numeric helpers ---------------- */

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static float clampF(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }

    private static int toPct(float f) { return (int)Math.round(clampF(f, 0f, 1f) * 100f); }
    private static float fromPct(int pct) { return clampF(pct / 100f, 0f, 1f); }
    private static String toPctLabel(int pct) { return pct + "%"; }

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
