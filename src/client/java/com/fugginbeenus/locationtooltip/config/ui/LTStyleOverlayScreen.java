package com.fugginbeenus.locationtooltip.config.ui;

import com.fugginbeenus.locationtooltip.config.LTConfig;
import com.fugginbeenus.locationtooltip.hud.LocationHudOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class LTStyleOverlayScreen extends Screen {
    private final Screen parent;
    private final LTConfig cfg;

    private int tab = 0; // 0=Appearance,1=Layout,2=Advanced

    public LTStyleOverlayScreen(Screen parent, LTConfig cfg) {
        super(Text.literal("Location Tooltip — Config"));
        this.parent = parent;
        this.cfg = cfg;
    }

    @Override
    protected void init() {
        int x = 16, y = 16, w = 110, h = 20, gap = 6;

        // Tabs
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Appearance"), b -> { tab = 0; rebuild(); })
                .dimensions(x, y, w, h).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Layout"), b -> { tab = 1; rebuild(); })
                .dimensions(x + (w + 6), y, w, h).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Advanced"), b -> { tab = 2; rebuild(); })
                .dimensions(x + 2*(w + 6), y, w, h).build());

        // Close
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(this.width - 16 - 70, 16, 70, h).build());

        buildTabContent();
    }

    private void rebuild() {
        this.clearChildren();
        this.init();
    }

    private void buildTabContent() {
        int left = 24, top = 52, fullW = 260, rowH = 22, y = top;

        if (tab == 0) { // Appearance
            // Background opacity
            this.addDrawableChild(makeFloatSlider(left, y, fullW, "Background Opacity", cfg.backgroundOpacity, 0f, 1f,
                    v -> cfg.backgroundOpacity = v)); y += rowH;
            // Text Scale
            this.addDrawableChild(makeFloatSlider(left, y, fullW, "Text Scale", cfg.textScale, 0.5f, 2.0f,
                    v -> cfg.textScale = v)); y += rowH;
            // Shadow toggle
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Shadow: " + (cfg.shadow ? "ON" : "OFF")), b -> {
                cfg.shadow = !cfg.shadow; rebuild();
            }).dimensions(left, y, fullW, 20).build());
            // Sheen
            this.addDrawableChild(makeFloatSlider(left, y += rowH, fullW, "Sheen Strength", cfg.gradientSheen, 0f, 1f,
                    v -> cfg.gradientSheen = v));
            this.addDrawableChild(makeFloatSlider(left, y += rowH, fullW, "Pill Height ×", cfg.pillHeightScale, 0.5f, 2.0f,
                    v -> cfg.pillHeightScale = v));
            this.addDrawableChild(makeIntSlider(left, y += rowH, fullW, "Extra Width +", cfg.pillExtraWidth, 0, 300,
                    v -> cfg.pillExtraWidth = v));


        } else if (tab == 1) { // Layout
            // Padding
            this.addDrawableChild(makeIntSlider(left, y, fullW, "Padding", cfg.pillPadding, 0, 20,
                    v -> cfg.pillPadding = v)); y += rowH;
            // Corner radius
            this.addDrawableChild(makeIntSlider(left, y, fullW, "Corner Radius", cfg.cornerRadius, 0, 20,
                    v -> cfg.cornerRadius = v)); y += rowH;

            // Position cycler
            this.addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
                    Text.literal("Position: " + cfg.position.name().replace('_',' ')),
                    b -> {
                        var vals = LTConfig.Position.values();
                        int next = (cfg.position.ordinal() + 1) % vals.length;
                        cfg.position = vals[next];
                        rebuild();
                    }).dimensions(left, y, fullW, 20).build()); y += rowH;

// Offsets
            this.addDrawableChild(makeIntSlider(left, y, fullW, "X Offset", cfg.xOffset, -200, 200, v -> cfg.xOffset = v)); y += rowH;
            this.addDrawableChild(makeIntSlider(left, y, fullW, "Y Offset", cfg.yOffset, -200, 200, v -> cfg.yOffset = v)); y += rowH;

// Icon size, Spacing, Vertical text nudge
            this.addDrawableChild(makeIntSlider(left, y, fullW, "Icon Size", cfg.iconSize, 8, 24, v -> cfg.iconSize = v)); y += rowH;
            this.addDrawableChild(makeIntSlider(left, y, fullW, "Pill Spacing", cfg.spacing, 0, 32, v -> cfg.spacing = v)); y += rowH;
            this.addDrawableChild(makeIntSlider(left, y, fullW, "Vertical Nudge", cfg.verticalNudge, -6, 6, v -> cfg.verticalNudge = v)); y += rowH;


        } else { // Advanced
            // Show toggles
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Show Region: " + (cfg.showRegionName ? "ON" : "OFF")), b -> {
                cfg.showRegionName = !cfg.showRegionName; rebuild();
            }).dimensions(left, y, fullW, 20).build()); y += rowH;

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Show Clock: " + (cfg.showClock ? "ON" : "OFF")), b -> {
                cfg.showClock = !cfg.showClock; rebuild();
            }).dimensions(left, y, fullW, 20).build()); y += rowH;

            // Split elements toggle
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Split Elements: " + (cfg.splitElements ? "ON" : "OFF")), b -> {
                cfg.splitElements = !cfg.splitElements; rebuild();
            }).dimensions(left, y, fullW, 20).build()); y += rowH;

// 24h time toggle
            this.addDrawableChild(ButtonWidget.builder(Text.literal("24h Time: " + (cfg.time24h ? "ON" : "OFF")), b -> {
                cfg.time24h = !cfg.time24h; rebuild();
            }).dimensions(left, y, fullW, 20).build()); y += rowH;

// Separator quick cycle (•, |, -)
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Separator: '" + cfg.separator + "'"), b -> {
                String[] opts = {" • ", " | ", " - "};
                int idx = java.util.Arrays.asList(opts).indexOf(cfg.separator);
                cfg.separator = opts[(idx + 1 + opts.length) % opts.length];
                rebuild();
            }).dimensions(left, y, fullW, 20).build()); y += rowH;
        }
    }

    private SliderWidget makeFloatSlider(int x, int y, int w, String label, float value, float min, float max, FloatSetter set) {
        return new SliderWidget(x, y, w, 20, Text.literal(label + ": " + pretty(value)), (value - min) / (max - min)) {
            private final float lo = min, hi = max;
            @Override protected void updateMessage() {
                float v = (float) (lo + this.value * (hi - lo));
                this.setMessage(Text.literal(label + ": " + pretty(v)));
            }
            @Override protected void applyValue() {
                float v = (float) (lo + this.value * (hi - lo));
                set.set(v);
            }
        };
    }

    private SliderWidget makeIntSlider(int x, int y, int w, String label, int value, int min, int max, IntSetter set) {
        return new SliderWidget(x, y, w, 20, Text.literal(label + ": " + value), (value - (double)min) / (max - (double)min)) {
            private final int lo = min, hi = max;
            @Override protected void updateMessage() {
                int v = (int) Math.round(lo + this.value * (hi - lo));
                this.setMessage(Text.literal(label + ": " + v));
            }
            @Override protected void applyValue() {
                int v = (int) Math.round(lo + this.value * (hi - lo));
                set.set(v);
            }
        };
    }

    private String pretty(float f) { return String.format("%.2f", f); }

    @Override
    public void close() { MinecraftClient.getInstance().setScreen(parent); }


    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Transparent bg
        ctx.fill(0, 0, this.width, this.height, 0x80000000);
        super.render(ctx, mouseX, mouseY, delta);

        // Live preview overlay: draw HUD at current config in the world
        LocationHudOverlay.renderPreview(ctx);
    }

    @FunctionalInterface interface FloatSetter { void set(float v); }
    @FunctionalInterface interface IntSetter   { void set(int v); }
}
