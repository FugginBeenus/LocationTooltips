package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.data.LTConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class HudOverlay {
    private static final Identifier BAR_LEFT  = new Identifier("locationtooltip", "textures/gui/location_bar_left.png");
    private static final Identifier BAR_MID   = new Identifier("locationtooltip", "textures/gui/location_bar_mid.png");
    private static final Identifier BAR_RIGHT = new Identifier("locationtooltip", "textures/gui/location_bar_right.png");
    private static final Identifier ICON_LOC  = new Identifier("locationtooltip", "textures/gui/icon_location.png");
    private static final Identifier ICON_CLK  = new Identifier("locationtooltip", "textures/gui/icon_clock.png");

    private static final int H = 20, CAP = 8, PAD = 8, ICON = 16;
    private static String formatGameTime(MinecraftClient mc) {
        if (mc.world == null) return "??:??";
        // 24000 ticks/day, 0 = 6:00 AM
        long t = mc.world.getTimeOfDay() % 24000L;
        // convert to minutes: 0 ticks -> 6:00 AM, 6000 -> 12:00 PM, 18000 -> 6:00 PM
        int minutes = (int)((t / 20.0) * 60.0 / 60.0); // ticks -> minutes (20 tps)
        // simpler: 1000 ticks ~ 1 hour; 16.666.. ticks = 1 minute
        int totalMinutes = (int)(t / 16.6666667);
        int hour24 = ((6 * 60) + totalMinutes) / 60 % 24;
        int minute = ((6 * 60) + totalMinutes) % 60;
        int hour12 = hour24 % 12; if (hour12 == 0) hour12 = 12;
        String ampm = hour24 < 12 ? "AM" : "PM";
        return String.format("%d:%02d %s", hour12, minute, ampm);
    }

    public static void render(DrawContext ctx, float tickDelta) {
        var mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String area = ClientState.currentText(); // "Wilderness" when null
        String time = formatGameTime(mc);

        var tr = mc.textRenderer;
        int gap = LTConfig.get().gap;
        int areaW = tr.getWidth(area);
        int timeW = tr.getWidth(time);

        boolean jadeLoaded = FabricLoader.getInstance().isModLoaded("jade");
        LTConfig.Layout layout = LTConfig.get().layout;
        if (jadeLoaded && LTConfig.get().preferSplitWhenJade && layout == LTConfig.Layout.CENTER) {
            layout = LTConfig.Layout.SPLIT;
        }

        switch (layout) {
            case SPLIT -> {
                // Left mini bar: [loc-icon] area
                int leftW = PAD + ICON + gap + areaW + PAD;
                drawBar(ctx, LTConfig.get().sideMargin, LTConfig.get().topMargin, leftW);
                int lx = LTConfig.get().sideMargin + PAD;
                int ly = LTConfig.get().topMargin + (H - ICON) / 2;
                ctx.drawTexture(ICON_LOC, lx, ly, 0, 0, ICON, ICON, ICON, ICON);
                ctx.drawTextWithShadow(tr, area, lx + ICON + gap,
                        LTConfig.get().topMargin + (H - 8) / 2 + 1, 0xFFFFFFFF);

                // Right mini bar: [clock-icon] time
                int rightW = PAD + ICON + gap + timeW + PAD;
                int sw = ctx.getScaledWindowWidth();
                int rx = sw - LTConfig.get().sideMargin - rightW;
                drawBar(ctx, rx, LTConfig.get().topMargin, rightW);
                int ix = rx + PAD;
                int iy = LTConfig.get().topMargin + (H - ICON) / 2;
                ctx.drawTexture(ICON_CLK, ix, iy, 0, 0, ICON, ICON, ICON, ICON);
                ctx.drawTextWithShadow(tr, time, ix + ICON + gap,
                        LTConfig.get().topMargin + (H - 8) / 2 + 1, 0xFFFFFFFF);
            }
            case LEFT -> {
                int total = PAD + ICON + gap + areaW + PAD;
                drawBar(ctx, LTConfig.get().sideMargin, LTConfig.get().topMargin, total);
                int x = LTConfig.get().sideMargin + PAD;
                int y = LTConfig.get().topMargin + (H - ICON) / 2;
                ctx.drawTexture(ICON_LOC, x, y, 0, 0, ICON, ICON, ICON, ICON);
                ctx.drawTextWithShadow(tr, area, x + ICON + gap,
                        LTConfig.get().topMargin + (H - 8) / 2 + 1, 0xFFFFFFFF);
            }
            case RIGHT -> {
                int total = PAD + ICON + gap + timeW + PAD;
                int sw = ctx.getScaledWindowWidth();
                int x = sw - LTConfig.get().sideMargin - total;
                drawBar(ctx, x, LTConfig.get().topMargin, total);
                int ix = x + PAD;
                int iy = LTConfig.get().topMargin + (H - ICON) / 2;
                ctx.drawTexture(ICON_CLK, ix, iy, 0, 0, ICON, ICON, ICON, ICON);
                ctx.drawTextWithShadow(tr, time, ix + ICON + gap,
                        LTConfig.get().topMargin + (H - 8) / 2 + 1, 0xFFFFFFFF);
            }
            case CENTER -> {
                // Original single centered bar (if you want it)
                int total = PAD + ICON + gap + areaW + gap + ICON + gap + timeW + PAD;
                int sw = ctx.getScaledWindowWidth();
                int x = (sw - total) / 2;
                drawBar(ctx, x, LTConfig.get().topMargin, total);
                int cx = x + PAD;
                int cy = LTConfig.get().topMargin + (H - ICON) / 2;
                ctx.drawTexture(ICON_LOC, cx, cy, 0, 0, ICON, ICON, ICON, ICON);
                cx += ICON + gap;
                ctx.drawTextWithShadow(tr, area, cx, LTConfig.get().topMargin + (H - 8) / 2 + 1, 0xFFFFFFFF);
                cx += areaW + gap;
                ctx.drawTexture(ICON_CLK, cx, cy, 0, 0, ICON, ICON, ICON, ICON);
                cx += ICON + gap;
                ctx.drawTextWithShadow(tr, time, cx, LTConfig.get().topMargin + (H - 8) / 2 + 1, 0xFFFFFFFF);
            }
        }
    }

    private static void drawBar(DrawContext ctx, int x, int y, int width) {
        // left cap
        ctx.drawTexture(BAR_LEFT, x, y, 0, 0, CAP, H, CAP, H);
        // mid (tile)
        int midX = x + CAP;
        int midW = width - CAP*2;
        for (int i = 0; i < midW; i++) {
            ctx.drawTexture(BAR_MID, midX + i, y, 0, 0, 1, H, 1, H);
        }
        // right cap
        ctx.drawTexture(BAR_RIGHT, x + width - CAP, y, 0, 0, CAP, H, CAP, H);
    }
}
