package com.fugginbeenus.locationtooltip.config.ui;

import com.fugginbeenus.locationtooltip.config.LTConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Simple ClothConfig screen that edits LTConfig and saves immediately as values change.
 * We prefer fields for floats (ClothConfig doesn't provide a stable float slider),
 * and use sliders for integer ranges where supported.
 */
public final class LTClothConfig {
    private LTClothConfig() {}

    public static Screen create(Screen parent) {
        final LTConfig cfg = LTConfig.get();

        final ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Location Tooltip (Advanced)"))
                .setTransparentBackground(true);

        // Also save when the player clicks "Done"
        builder.setSavingRunnable(cfg::save);

        final ConfigEntryBuilder eb = builder.entryBuilder();

        // ---------------- General ----------------
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

        general.addEntry(eb.startBooleanToggle(Text.literal("Show Region Name"), cfg.showRegionName)
                .setSaveConsumer(v -> { cfg.showRegionName = v; cfg.save(); })
                .build());

        general.addEntry(eb.startBooleanToggle(Text.literal("Show Clock"), cfg.showClock)
                .setSaveConsumer(v -> { cfg.showClock = v; cfg.save(); })
                .build());

        general.addEntry(eb.startBooleanToggle(Text.literal("24h Time"), cfg.time24h)
                .setSaveConsumer(v -> { cfg.time24h = v; cfg.save(); })
                .build());

        general.addEntry(eb.startStrField(Text.literal("Single Pill Separator"), cfg.separator)
                .setSaveConsumer(v -> { cfg.separator = (v == null || v.isEmpty()) ? " • " : v; cfg.save(); })
                .build());

        general.addEntry(eb.startBooleanToggle(Text.literal("Split Elements (Two Pills)"), cfg.splitElements)
                .setSaveConsumer(v -> { cfg.splitElements = v; cfg.save(); })
                .build());

        // ---------------- Appearance ----------------
        ConfigCategory appearance = builder.getOrCreateCategory(Text.literal("Appearance"));

        appearance.addEntry(eb.startFloatField(Text.literal("Background Opacity (solid pills)"), cfg.backgroundOpacity)
                .setMin(0f).setMax(1f)
                .setSaveConsumer(v -> { cfg.backgroundOpacity = clamp(v, 0f, 1f); cfg.save(); })
                .build());

        // integers: use sliders when possible
        appearance.addEntry(eb.startIntSlider(Text.literal("Icon Size (px)"), cfg.iconSize, 8, 64)
                .setSaveConsumer(v -> { cfg.iconSize = clamp(v, 8, 64); cfg.save(); })
                .build());

        appearance.addEntry(eb.startIntSlider(Text.literal("Pill Padding (px)"), cfg.pillPadding, 0, 24)
                .setSaveConsumer(v -> { cfg.pillPadding = clamp(v, 0, 24); cfg.save(); })
                .build());

        appearance.addEntry(eb.startFloatField(Text.literal("Text Scale"), cfg.textScale)
                .setMin(0.5f).setMax(3.0f)
                .setSaveConsumer(v -> { cfg.textScale = clamp(v, 0.5f, 3.0f); cfg.save(); })
                .build());

        appearance.addEntry(eb.startFloatField(Text.literal("Pill Height Scale"), cfg.pillHeightScale)
                .setMin(0.5f).setMax(2.5f)
                .setSaveConsumer(v -> { cfg.pillHeightScale = clamp(v, 0.5f, 2.5f); cfg.save(); })
                .build());

        appearance.addEntry(eb.startIntSlider(Text.literal("Corner Radius (px)"), cfg.cornerRadius, 0, 32)
                .setSaveConsumer(v -> { cfg.cornerRadius = clamp(v, 0, 32); cfg.save(); })
                .build());

        appearance.addEntry(eb.startIntSlider(Text.literal("Spacing Between Pills (px)"), cfg.spacing, 0, 48)
                .setSaveConsumer(v -> { cfg.spacing = clamp(v, 0, 48); cfg.save(); })
                .build());

        appearance.addEntry(eb.startIntSlider(Text.literal("Extra Width (single pill only)"), cfg.pillExtraWidth, 0, 64)
                .setSaveConsumer(v -> { cfg.pillExtraWidth = clamp(v, 0, 64); cfg.save(); })
                .build());

        appearance.addEntry(eb.startBooleanToggle(Text.literal("Text Shadow"), cfg.shadow)
                .setSaveConsumer(v -> { cfg.shadow = v; cfg.save(); })
                .build());

        appearance.addEntry(eb.startIntSlider(Text.literal("Vertical Text Nudge (px)"), cfg.verticalNudge, -16, 16)
                .setSaveConsumer(v -> { cfg.verticalNudge = clamp(v, -16, 16); cfg.save(); })
                .build());

        // Corner styling (for vector pills)
        appearance.addEntry(eb.startEnumSelector(Text.literal("Corner Style"), LTConfig.CornerStyle.class, cfg.cornerStyle)
                .setSaveConsumer(v -> { cfg.cornerStyle = v; cfg.save(); })
                .build());
        appearance.addEntry(eb.startFloatField(Text.literal("Corner Exponent (squircle)"), cfg.cornerExponent)
                .setMin(1.5f).setMax(12f)
                .setSaveConsumer(v -> { cfg.cornerExponent = clamp(v, 1.5f, 12f); cfg.save(); })
                .build());
        appearance.addEntry(eb.startIntSlider(Text.literal("Border Width (px)"), cfg.borderWidth, 0, 6)
                .setSaveConsumer(v -> { cfg.borderWidth = clamp(v, 0, 6); cfg.save(); })
                .build());

        // ---------------- Position ----------------
        ConfigCategory positioning = builder.getOrCreateCategory(Text.literal("Position"));

        positioning.addEntry(eb.startEnumSelector(Text.literal("Anchor Position"), LTConfig.Position.class, cfg.position)
                .setSaveConsumer(v -> { cfg.position = v; cfg.save(); })
                .build());

        positioning.addEntry(eb.startIntField(Text.literal("X Offset (px)"), cfg.xOffset)
                .setSaveConsumer(v -> { cfg.xOffset = v; cfg.save(); })
                .build());

        positioning.addEntry(eb.startIntField(Text.literal("Y Offset (px)"), cfg.yOffset)
                .setSaveConsumer(v -> { cfg.yOffset = v; cfg.save(); })
                .build());

        // ---------------- Textures (if you switch to 9-slice) ----------------
        ConfigCategory textured = builder.getOrCreateCategory(Text.literal("Textures (Optional 9-slice)"));

        textured.addEntry(eb.startBooleanToggle(Text.literal("Use Textured Pills"), cfg.useTexturedPills)
                .setSaveConsumer(v -> { cfg.useTexturedPills = v; cfg.save(); })
                .build());

        textured.addEntry(eb.startIntField(Text.literal("Texture Width (texW)"), cfg.texW)
                .setSaveConsumer(v -> { cfg.texW = Math.max(1, v); cfg.save(); })
                .build());

        textured.addEntry(eb.startIntField(Text.literal("Texture Height (texH)"), cfg.texH)
                .setSaveConsumer(v -> { cfg.texH = Math.max(1, v); cfg.save(); })
                .build());

        textured.addEntry(eb.startIntField(Text.literal("Slice Left"), cfg.sliceLeft)
                .setSaveConsumer(v -> { cfg.sliceLeft = Math.max(0, v); cfg.save(); })
                .build());
        textured.addEntry(eb.startIntField(Text.literal("Slice Right"), cfg.sliceRight)
                .setSaveConsumer(v -> { cfg.sliceRight = Math.max(0, v); cfg.save(); })
                .build());
        textured.addEntry(eb.startIntField(Text.literal("Slice Top"), cfg.sliceTop)
                .setSaveConsumer(v -> { cfg.sliceTop = Math.max(0, v); cfg.save(); })
                .build());
        textured.addEntry(eb.startIntField(Text.literal("Slice Bottom"), cfg.sliceBottom)
                .setSaveConsumer(v -> { cfg.sliceBottom = Math.max(0, v); cfg.save(); })
                .build());

        return builder.build();
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static float clamp(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }
}
