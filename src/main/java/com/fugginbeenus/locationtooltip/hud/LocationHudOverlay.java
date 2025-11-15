package com.fugginbeenus.locationtooltip.hud;

import com.fugginbeenus.locationtooltip.config.LTConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Vector-drawn HUD overlay with 1px "vanilla" rounded corners.
 * Scales cleanly and supports split/combined layouts.
 */
public class LocationHudOverlay implements HudRenderCallback {

    private static String currentTitle = "Wilderness";
    private static long   regionChangedAt = 0L;

    public static void applyLiveConfig(int yOffset, float textScale, boolean showRegion, boolean showClock) {
        com.fugginbeenus.locationtooltip.config.LTConfig cfg = com.fugginbeenus.locationtooltip.config.LTConfig.get();
        cfg.verticalNudge = yOffset;
        cfg.textScale = textScale;
        cfg.showRegionName = showRegion;
        cfg.showClock = showClock;
    }

    /** Preferred API used by client packets. */
    public static void setRegionTitle(String title) {
        currentTitle = (title == null || title.isEmpty()) ? "Wilderness" : title;
        regionChangedAt = System.currentTimeMillis();
    }

    /** Compatibility alias (some code may call setTitle). */
    public static void setTitle(String title) {
        setRegionTitle(title);
    }

    /** Optional external setter if you update the pill elsewhere. */
    public static void setCurrentRegion(String regionName) {
        setRegionTitle(regionName);
    }

    // Icons
    private static final Identifier ICON_REGION = new Identifier("locationtooltip", "textures/gui/region.png");
    private static final Identifier ICON_CLOCK  = new Identifier("locationtooltip", "textures/gui/clock.png");

    @Override
    public void onHudRender(DrawContext ctx, float tickDelta) {
        render(ctx, false);
    }

    public static void renderPreview(DrawContext ctx) {
        render(ctx, true);
    }

    private static void render(DrawContext ctx, boolean preview) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || (!preview && mc.options.hudHidden)) return;

        LTConfig cfg = LTConfig.get();

        // What to show?
        String region = cfg.showRegionName ? currentTitle : null;
        String time   = (cfg.showClock && mc.world != null) ? formatTime(mc.world.getTimeOfDay(), cfg.time24h) : null;

        final boolean hasRegion = region != null && !region.isEmpty();
        final boolean hasTime   = time   != null && !time.isEmpty();
        if (!hasRegion && !hasTime) return;

        // Measurements
        final int icon  = Math.max(8, cfg.iconSize);
        final int pad   = Math.max(0, cfg.pillPadding);
        final float s   = Math.max(0.5f, cfg.textScale);
        final int textH = (int) (mc.textRenderer.fontHeight * s);
        final int contentH = Math.max(textH, icon);
        final int totalH   = Math.round((contentH + pad * 2) * Math.max(0.5f, cfg.pillHeightScale));

        final int regionW = hasRegion ? (int) (mc.textRenderer.getWidth(region) * s) : 0;
        final int timeW   = hasTime   ? (int) (mc.textRenderer.getWidth(time)   * s) : 0;

        final int alpha = (int) (Math.max(0f, Math.min(1f, cfg.backgroundOpacity)) * 255) & 0xFF;
        final int bg = (alpha << 24); // black with alpha

        if (!cfg.splitElements) {
            // Single pill layout
            final String text = hasRegion && hasTime ? region + cfg.separator + time : (hasRegion ? region : time);
            final int textW = (int) (mc.textRenderer.getWidth(text) * s);
            final int iconLeft  = hasRegion ? (icon + 4) : 0;
            final int iconRight = hasTime   ? (icon + 4) : 0;
            final int totalW = pad + iconLeft + textW + iconRight + pad + Math.max(0, cfg.pillExtraWidth);

            int[] xy = anchor(cfg.position, mc.getWindow(), totalW, totalH, cfg.xOffset, cfg.yOffset);
            final int x = xy[0], y = xy[1];

            // Vector pill with crisp corners
            fillRound(ctx, x, y, totalW, totalH, cfg.cornerRadius, bg);

            int cx = x + pad;
            int cy = y + pad + cfg.verticalNudge;

            if (hasRegion) {
                // Icon (region) on left if region is present
                ctx.drawTexture(ICON_REGION, cx, cy + ((contentH - icon) / 2), 0, 0, icon, icon, icon, icon);
                cx += icon + 4;
            }

            // Text
            ctx.getMatrices().push();
            ctx.getMatrices().translate(cx, cy + (contentH - textH) / 2f, 0);
            ctx.getMatrices().scale(s, s, 1);
            ctx.drawText(mc.textRenderer, Text.literal(text), 0, 0, 0xFFFFFFFF, cfg.shadow);
            ctx.getMatrices().pop();
            cx += textW + 4;

            if (hasTime) {
                ctx.drawTexture(ICON_CLOCK, cx, cy + ((contentH - icon) / 2), 0, 0, icon, icon, icon, icon);
            }

        } else {
            // Split pills layout
            final int regIconW = hasRegion ? (icon + 4) : 0;
            final int timeIconW = hasTime ? (icon + 4) : 0;

            final int regW = hasRegion ? pad + regIconW + regionW + pad : 0;
            final int timW = hasTime   ? pad + timeIconW + timeW   + pad : 0;

            final int pairW = regW + (hasRegion && hasTime ? cfg.spacing : 0) + timW + Math.max(0, cfg.pillExtraWidth);

            int[] xy = anchor(cfg.position, mc.getWindow(), pairW, totalH, cfg.xOffset, cfg.yOffset);
            int rx = xy[0], ry = xy[1];
            int tx = rx + regW + (hasRegion && hasTime ? cfg.spacing : 0), ty = ry;

            // Region pill
            if (hasRegion) {
                fillRound(ctx, rx, ry, regW, totalH, cfg.cornerRadius, bg);
                int cx = rx + pad;
                int cy = ry + pad + cfg.verticalNudge;

                ctx.drawTexture(ICON_REGION, cx, cy + ((contentH - icon) / 2), 0, 0, icon, icon, icon, icon);
                cx += icon + 4;

                ctx.getMatrices().push();
                ctx.getMatrices().translate(cx, cy + (contentH - textH) / 2f, 0);
                ctx.getMatrices().scale(s, s, 1);
                ctx.drawText(mc.textRenderer, Text.literal(region), 0, 0, 0xFFFFFFFF, cfg.shadow);
                ctx.getMatrices().pop();
            }

            // Time pill
            if (hasTime) {
                fillRound(ctx, tx, ty, timW, totalH, cfg.cornerRadius, bg);
                int cx = tx + pad;
                int cy = ty + pad + cfg.verticalNudge;

                ctx.drawTexture(ICON_CLOCK, cx, cy + ((contentH - icon) / 2), 0, 0, icon, icon, icon, icon);
                cx += icon + 4;

                ctx.getMatrices().push();
                ctx.getMatrices().translate(cx, cy + (contentH - textH) / 2f, 0);
                ctx.getMatrices().scale(s, s, 1);
                ctx.drawText(mc.textRenderer, Text.literal(time), 0, 0, 0xFFFFFFFF, cfg.shadow);
                ctx.getMatrices().pop();
            }
        }
    }

    /* ------------------------------- helpers ------------------------------- */

    private static int[] anchor(LTConfig.Position pos, Window win, int w, int h, int dx, int dy) {
        int sw = win.getScaledWidth(), sh = win.getScaledHeight();
        int x = switch (pos) {
            case TOP_LEFT      -> 0 + dx;
            case TOP_CENTER    -> (sw - w) / 2 + dx;
            case TOP_RIGHT     -> sw - w + dx;
            case BOTTOM_LEFT   -> 0 + dx;
            case BOTTOM_CENTER -> (sw - w) / 2 + dx;
            case BOTTOM_RIGHT  -> sw - w + dx;
        };
        int y = switch (pos) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0 + dy;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> sh - h + dy;
        };
        return new int[]{x, y};
    }

    private static void fillRound1px(DrawContext ctx, int x, int y, int w, int h, int argb) {
        if (w <= 2 || h <= 2) { // too small to round
            ctx.fill(x, y, x + w, y + h, argb);
            return;
        }
        // middle band (excludes top & bottom rows)
        ctx.fill(x, y + 1, x + w, y + h - 1, argb);
        // top row without the two corner pixels
        ctx.fill(x + 1, y, x + w - 1, y + 1, argb);
        // bottom row without the two corner pixels
        ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, argb);
    }

    /** Vector rounded rect; when r==1 uses the crisp 1px-corner helper. */
    private static void fillRound(DrawContext ctx, int x, int y, int w, int h, int r, int argb) {
        r = (r <= 1) ? 1 : Math.min(r, Math.min(w, h) / 2); // 1px vanilla if ≤ 1
        if (r == 1) { fillRound1px(ctx, x, y, w, h, argb); return; }

        int x2 = x + w, y2 = y + h;
        ctx.fill(x + r, y,     x2 - r, y2,     argb); // center
        ctx.fill(x,     y + r, x + r,  y2 - r, argb); // left
        ctx.fill(x2 - r,y + r, x2,     y2 - r, argb); // right
        // corner blocks (coarse but fine for HUD scale)
        ctx.fill(x,     y,     x + r,  y + r,  argb);
        ctx.fill(x2 - r,y,     x2,     y + r,  argb);
        ctx.fill(x,     y2 - r,x + r,  y2,     argb);
        ctx.fill(x2 - r,y2 - r,x2,     y2,     argb);
    }

    private static String formatTime(long timeOfDay, boolean twentyFour) {
        long ticks = (timeOfDay + 6000) % 24000;
        int h = (int) (ticks / 1000);
        int m = (int) ((ticks % 1000) * 60 / 1000);
        if (twentyFour) return String.format("%02d:%02d", h, m);
        String ampm = h >= 12 ? "PM" : "AM";
        int hh = h % 12; if (hh == 0) hh = 12;
        return String.format("%d:%02d %s", hh, m, ampm);
    }
}
