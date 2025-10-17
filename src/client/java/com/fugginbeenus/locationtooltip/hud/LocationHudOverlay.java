package com.fugginbeenus.locationtooltip.hud;

import com.fugginbeenus.locationtooltip.config.LTConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;

public class LocationHudOverlay implements HudRenderCallback {

    private static String currentTitle = "Wilderness";
    private static final Identifier ICON_REGION = new Identifier("locationtooltip", "textures/gui/region.png");
    private static final Identifier ICON_CLOCK  = new Identifier("locationtooltip", "textures/gui/clock.png");

    public static void setCurrentRegion(String breadcrumb) { currentTitle = breadcrumb; }

    @Override public void onHudRender(DrawContext ctx, float tickDelta) { render(ctx, false); }
    public static void renderPreview(DrawContext ctx) { render(ctx, true); }

    private static void render(DrawContext ctx, boolean preview) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || (!preview && mc.options.hudHidden)) return;

        LTConfig cfg = LTConfig.get();

        String region = cfg.showRegionName ? currentTitle : null;
        String time   = cfg.showClock ? formatTime(mc.world.getTimeOfDay(), cfg.time24h) : null;

        boolean hasRegion = region != null && !region.isEmpty();
        boolean hasTime   = time   != null && !time.isEmpty();
        if (!hasRegion && !hasTime) return;

        // Pre-measure
        int icon = Math.max(8, cfg.iconSize);
        int pad = Math.max(0, cfg.pillPadding);
        float scale = Math.max(0.5f, cfg.textScale);

        int regionW = hasRegion ? (int)(mc.textRenderer.getWidth(region)*scale) : 0;
        int timeW   = hasTime   ? (int)(mc.textRenderer.getWidth(time)*scale)   : 0;
        int textH   = (int)(mc.textRenderer.fontHeight * scale);
        int contentH = Math.max(textH, icon);

        int alpha = (int)(cfg.backgroundOpacity * 255) & 0xFF;
        int bg = (alpha << 24);

        if (!cfg.splitElements) {
            String text = hasRegion && hasTime ? region + cfg.separator + time : (hasRegion ? region : time);
            int textW = (int)(mc.textRenderer.getWidth(text) * scale);
            int iconLeft  = hasRegion ? (icon + 4) : 0;
            int iconRight = hasTime   ? (icon + 4) : 0;
            int totalW = pad + iconLeft + textW + iconRight + pad + Math.max(0, cfg.pillExtraWidth);
            int totalH = Math.round( (contentH + pad*2) * Math.max(0.5f, cfg.pillHeightScale) );


            int[] xy = anchor(cfg.position, mc.getWindow(), totalW, totalH, cfg.xOffset, cfg.yOffset);
            int x = xy[0], y = xy[1];

            fillRoundish(ctx, x, y, x + totalW, y + totalH, cfg.cornerRadius, bg);
            drawSheen(ctx, x, y, totalW, totalH, cfg.gradientSheen, alpha);

            int cx = x + pad;
            int cy = y + pad + cfg.verticalNudge;

            if (hasRegion) { // left icon
                ctx.drawTexture(ICON_REGION, cx, cy + ((contentH - icon) / 2), 0, 0, icon, icon, icon, icon);
                cx += icon + 4;
            }

            ctx.getMatrices().push();
            ctx.getMatrices().translate(cx, cy + (contentH - textH)/2f, 0);
            ctx.getMatrices().scale(scale, scale, 1);
            ctx.drawText(mc.textRenderer, text, 0, 0, 0xFFFFFFFF, cfg.shadow);
            ctx.getMatrices().pop();
            cx += textW + 4;

            if (hasTime) { // right icon
                ctx.drawTexture(ICON_CLOCK, cx, cy + ((contentH - icon) / 2), 0, 0, icon, icon, icon, icon);
            }
        } else {
            // Split pills: compute both boxes and place them with spacing
            int totalH = Math.round( (contentH + pad*2) * Math.max(0.5f, cfg.pillHeightScale) );

            int regIcon = hasRegion ? (icon + 4) : 0;
            int regW = hasRegion ? pad + regIcon + regionW + pad : 0;

            int timeIcon = hasTime ? (icon + 4) : 0;
            int timW = hasTime   ? pad + timeIcon + timeW + pad   : 0;

            int pairW = regW + (hasRegion && hasTime ? cfg.spacing : 0) + timW + Math.max(0, cfg.pillExtraWidth);

            int[] xy = anchor(cfg.position, mc.getWindow(), pairW, totalH, cfg.xOffset, cfg.yOffset);
            int x = xy[0], y = xy[1];

            int rx = x, ry = y;
            int tx = x + regW + (hasRegion && hasTime ? cfg.spacing : 0), ty = y;

            // Region pill
            if (hasRegion) {
                fillRoundish(ctx, rx, ry, rx + regW, ry + totalH, cfg.cornerRadius, bg);
                drawSheen(ctx, rx, ry, regW, totalH, cfg.gradientSheen, alpha);
                int cx = rx + pad;
                int cy = ry + pad + cfg.verticalNudge;

                ctx.drawTexture(ICON_REGION, cx, cy + ((contentH - icon) / 2), 0, 0, icon, icon, icon, icon);
                cx += icon + 4;

                ctx.getMatrices().push();
                ctx.getMatrices().translate(cx, cy + (contentH - textH)/2f, 0);
                ctx.getMatrices().scale(scale, scale, 1);
                ctx.drawText(mc.textRenderer, region, 0, 0, 0xFFFFFFFF, cfg.shadow);
                ctx.getMatrices().pop();
            }

            // Time pill
            if (hasTime) {
                fillRoundish(ctx, tx, ty, tx + timW, ty + totalH, cfg.cornerRadius, bg);
                drawSheen(ctx, tx, ty, timW, totalH, cfg.gradientSheen, alpha);
                int cx = tx + pad;
                int cy = ty + pad + cfg.verticalNudge;

                ctx.drawTexture(ICON_CLOCK, cx, cy + ((contentH - icon) / 2), 0, 0, icon, icon, icon, icon);
                cx += icon + 4;

                ctx.getMatrices().push();
                ctx.getMatrices().translate(cx, cy + (contentH - textH)/2f, 0);
                ctx.getMatrices().scale(scale, scale, 1);
                ctx.drawText(mc.textRenderer, time, 0, 0, 0xFFFFFFFF, cfg.shadow);
                ctx.getMatrices().pop();
            }
        }
    }

    private static int[] anchor(LTConfig.Position pos, Window win, int w, int h, int dx, int dy) {
        int sw = win.getScaledWidth(), sh = win.getScaledHeight();
        int x = switch (pos) {
            case TOP_LEFT -> 0 + dx;
            case TOP_CENTER -> (sw - w)/2 + dx;
            case TOP_RIGHT -> sw - w + dx;
            case BOTTOM_LEFT -> 0 + dx;
            case BOTTOM_CENTER -> (sw - w)/2 + dx;
            case BOTTOM_RIGHT -> sw - w + dx;
        };
        int y = switch (pos) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0 + dy;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> sh - h + dy;
        };
        return new int[]{x,y};
    }

    private static void drawSheen(DrawContext ctx, int x, int y, int w, int h, float strength, int baseAlpha) {
        int sheen = (int)(baseAlpha * Math.max(0f, Math.min(1f, strength)));
        int color = (sheen << 24) | 0xFFFFFF;
        int strip = Math.max(3, (int)(h * 0.45f));
        ctx.fill(x + 2, y + 2, x + w - 2, y + 2 + strip, color);
    }

    private static void fillRoundish(DrawContext ctx, int x1, int y1, int x2, int y2, int r, int color) {
        r = Math.max(0, Math.min(r, (Math.min(x2 - x1, y2 - y1) / 2)));
        if (r == 0) { ctx.fill(x1, y1, x2, y2, color); return; }
        ctx.fill(x1 + r, y1, x2 - r, y2, color); // center
        ctx.fill(x1, y1 + r, x1 + r, y2 - r, color); // sides
        ctx.fill(x2 - r, y1 + r, x2, y2 - r, color);
        // corners (fast approx)
        ctx.fill(x1, y1, x1 + r, y1 + r, color);
        ctx.fill(x2 - r, y1, x2, y1 + r, color);
        ctx.fill(x1, y2 - r, x1 + r, y2, color);
        ctx.fill(x2 - r, y2 - r, x2, y2, color);
    }

    private static String formatTime(long timeOfDay, boolean twentyFour) {
        long ticks = (timeOfDay + 6000) % 24000;
        int h = (int)(ticks / 1000);
        int m = (int)((ticks % 1000) * 60 / 1000);
        if (twentyFour) {
            return String.format("%02d:%02d", h, m);
        } else {
            String ampm = h >= 12 ? "PM" : "AM";
            int hh = h % 12; if (hh == 0) hh = 12;
            return String.format("%d:%02d %s", hh, m, ampm);
        }
    }
}