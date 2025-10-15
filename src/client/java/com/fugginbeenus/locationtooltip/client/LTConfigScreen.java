package com.fugginbeenus.locationtooltip.client;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class LTConfigScreen {
    private LTConfigScreen() {}

    private enum LayoutMode { AUTO, SPLIT, TOGETHER }

    public static Screen build(Screen parent) {
        LTConfig cfg = LTConfig.get();

        ConfigBuilder b = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Location Tooltip"))
                .setTransparentBackground(true)
                .setSavingRunnable(LTConfig::save);

        ConfigEntryBuilder eb = b.entryBuilder();
        List<Runnable> live = new ArrayList<>();

        // ----- Simple Layout -----
        ConfigCategory layout = b.getOrCreateCategory(Text.literal("Layout"));

        var eMode = eb.startEnumSelector(
                        Text.literal("Mode"), LayoutMode.class, switch (safe(cfg.layoutMode)) {
                            case "split" -> LayoutMode.SPLIT;
                            case "together" -> LayoutMode.TOGETHER;
                            default -> LayoutMode.AUTO;
                        })
                .setTooltip(Text.literal("AUTO = split when Jade is installed, otherwise together"))
                .build();
        layout.addEntry(eMode);
        live.add(() -> cfg.layoutMode = eMode.getValue().name().toLowerCase());

        var eShowClock = eb.startBooleanToggle(Text.literal("Show Clock"), cfg.showClock)
                .setDefaultValue(true).build();
        layout.addEntry(eShowClock);
        live.add(() -> cfg.showClock = eShowClock.getValue());

        var eOffsetX = eb.startIntSlider(Text.literal("Horizontal Offset (px)"), cfg.offsetX, -600, 600)
                .setDefaultValue(0).build();
        layout.addEntry(eOffsetX);
        live.add(() -> cfg.offsetX = eOffsetX.getValue());

        var eOffsetY = eb.startIntSlider(Text.literal("Vertical Offset (px from top)"), cfg.offsetY, 0, 400)
                .setDefaultValue(18).build();
        layout.addEntry(eOffsetY);
        live.add(() -> cfg.offsetY = eOffsetY.getValue());

        var eScale = eb.startFloatField(Text.literal("Scale (0.50 – 1.50)"), cfg.scale)
                .setDefaultValue(0.90f).build();
        layout.addEntry(eScale);
        live.add(() -> cfg.scale = clamp(eScale.getValue(), 0.50f, 1.50f));

        // small tweaks
        var eTogetherGap = eb.startIntSlider(Text.literal("Together Gap (px)"), cfg.togetherGap, 0, 64)
                .setDefaultValue(8).build();
        layout.addEntry(eTogetherGap);
        live.add(() -> cfg.togetherGap = eTogetherGap.getValue());

        var eCenterGap = eb.startIntSlider(Text.literal("Split Center Gap (px)"), cfg.centerGap, 120, 600)
                .setDefaultValue(220).build();
        layout.addEntry(eCenterGap);
        live.add(() -> cfg.centerGap = eCenterGap.getValue());

        // ----- Visual -----
        ConfigCategory visual = b.getOrCreateCategory(Text.literal("Visual"));

        var eNine = eb.startBooleanToggle(Text.literal("Use 9-Slice Bar"), cfg.useNineSlice)
                .setDefaultValue(true).build();
        visual.addEntry(eNine);
        live.add(() -> cfg.useNineSlice = eNine.getValue());

        var eAlpha = eb.startFloatField(Text.literal("Panel Alpha (0.10 – 1.00)"), cfg.panelAlpha)
                .setDefaultValue(0.35f).build();
        visual.addEntry(eAlpha);
        live.add(() -> cfg.panelAlpha = clamp(eAlpha.getValue(), 0.10f, 1.00f));

        var eFontScale = eb.startFloatField(Text.literal("Font Scale (0.75 – 1.50)"), cfg.fontScale)
                .setDefaultValue(1.00f).build();
        visual.addEntry(eFontScale);
        live.add(() -> cfg.fontScale = clamp(eFontScale.getValue(), 0.75f, 1.50f));

        var eMatch = eb.startBooleanToggle(Text.literal("Match Jade Metrics"), cfg.matchJadeMetrics)
                .setDefaultValue(true).build();
        visual.addEntry(eMatch);
        live.add(() -> cfg.matchJadeMetrics = eMatch.getValue());

        var eShadow = eb.startBooleanToggle(Text.literal("Text Shadow"), cfg.textShadow)
                .setDefaultValue(false).build();
        visual.addEntry(eShadow);
        live.add(() -> cfg.textShadow = eShadow.getValue());

        // Build + live apply while the screen is open
        Screen screen = b.build();
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.currentScreen != screen) return;
            // push live values into LTConfig so HUD updates immediately
            for (Runnable r : live) r.run();
        });

        return screen;
    }

    private static String safe(String s) { return s == null ? "" : s.toLowerCase(); }
    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}
