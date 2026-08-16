package com.fugginbeenus.locationtooltip.hud;

import com.fugginbeenus.locationtooltip.config.LTConfig;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
*///?} else {
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
//?}
import com.fugginbeenus.locationtooltip.util.LTId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

//? if >=26.1 {
/*public class LocationHudOverlay implements HudElement {
*///?} else {
public class LocationHudOverlay implements HudRenderCallback {
//?}

    private static String currentTitle = "Wilderness";
    private static long   regionChangedAt = 0L;

    public static void applyLiveConfig(int yOffset, float textScale, boolean showRegion, boolean showClock) {
        com.fugginbeenus.locationtooltip.config.LTConfig cfg = com.fugginbeenus.locationtooltip.config.LTConfig.get();
        cfg.verticalNudge = yOffset;
        cfg.textScale = textScale;
        cfg.showRegionName = showRegion;
        cfg.showClock = showClock;
    }

    public static void setRegionTitle(String title) {
        currentTitle = (title == null || title.isEmpty()) ? "Wilderness" : title;
        regionChangedAt = System.currentTimeMillis();
    }

    public static void setTitle(String title) {
        setRegionTitle(title);
    }

    public static void setCurrentRegion(String regionName) {
        setRegionTitle(regionName);
    }

    private static final ResourceLocation ICON_REGION = LTId.of("locationtooltip", "textures/gui/region.png");
    private static final ResourceLocation ICON_CLOCK  = LTId.of("locationtooltip", "textures/gui/clock.png");
    private static final ResourceLocation ICON_COORDS = LTId.of("locationtooltip", "textures/gui/coordinates.png");
    private static final ResourceLocation ICON_BIOME  = LTId.of("locationtooltip", "textures/gui/biome.png");

    @Override
    //? if >=26.1 {
    /*public void extractRenderState(GuiGraphics ctx, net.minecraft.client.DeltaTracker tickCounter) {
    *///?} elif >=1.21 {
    /*public void onHudRender(GuiGraphics ctx, net.minecraft.client.DeltaTracker tickCounter) {
    *///?} else {
    public void onHudRender(GuiGraphics ctx, float tickDelta) {
    //?}
        render(ctx, false);
    }

    public static void renderPreview(GuiGraphics ctx) {
        render(ctx, true);
    }

    private static void render(GuiGraphics ctx, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        //? if <26.1 {
        if (!preview && mc.options.hideGui) return;
        //?}

        LTConfig cfg = LTConfig.get();

        if (!preview && cfg.position == LTConfig.Position.TOP_CENTER && bossBarVisible(mc)) return;

        String region = cfg.showRegionName ? currentTitle : null;
        String time   = (cfg.showClock && mc.level != null) ? formatTime(mc.level.getDayTime(), cfg.time24h) : null;

        final boolean hasRegion = region != null && !region.isEmpty();
        final boolean hasTime   = time   != null && !time.isEmpty();

        final int icon  = Math.max(8, cfg.iconSize);
        final int pad   = Math.max(0, cfg.pillPadding);
        final float s   = Math.max(0.5f, cfg.textScale);
        final int textH = (int) (mc.font.lineHeight * s);
        final int contentH = Math.max(textH, icon);
        final int totalH   = Math.round((contentH + pad * 2) * Math.max(0.5f, cfg.pillHeightScale));

        final int regionW = hasRegion ? (int) (mc.font.width(region) * s) : 0;
        final int timeW   = hasTime   ? (int) (mc.font.width(time)   * s) : 0;

        final int alpha = (int) (Math.max(0f, Math.min(1f, cfg.backgroundOpacity)) * 255) & 0xFF;
        final int bg = (alpha << 24);

        extraPills(ctx, cfg, mc, pad, s, contentH, totalH, bg);

        if (!hasRegion && !hasTime) return;

        if (!cfg.splitElements) {
            final String text = hasRegion && hasTime ? region + cfg.separator + time : (hasRegion ? region : time);
            final int textW = (int) (mc.font.width(text) * s);
            final int iconLeft  = hasRegion ? (icon + 4) : 0;
            final int iconRight = hasTime   ? (icon + 4) : 0;
            final int totalW = pad + iconLeft + textW + iconRight + pad + Math.max(0, cfg.pillExtraWidth);

            int[] xy = anchor(cfg.position, mc.getWindow(), totalW, totalH, cfg.xOffset, cfg.yOffset);
            final int x = xy[0], y = xy[1];

            drawPill(ctx, cfg, x, y, totalW, totalH, bg);

            int cx = x + pad;
            int cy = y + pad + cfg.verticalNudge;

            if (hasRegion) {
                ltIcon(ctx, ICON_REGION, cx, cy + ((contentH - icon) / 2), icon);
                cx += icon + 4;
            }

            ltPush(ctx, cx, cy + (contentH - textH) / 2f, s);
            ctx.drawString(mc.font, Component.literal(text), 0, 0, 0xFFFFFFFF, cfg.shadow);
            ltPop(ctx);
            cx += textW + 4;

            if (hasTime) {
                ltIcon(ctx, ICON_CLOCK, cx, cy + ((contentH - icon) / 2), icon);
            }
        } else {
            final int regIconW = hasRegion ? (icon + 4) : 0;
            final int timeIconW = hasTime ? (icon + 4) : 0;

            final int regW = hasRegion ? pad + regIconW + regionW + pad : 0;
            final int timW = hasTime   ? pad + timeIconW + timeW   + pad : 0;

            final int pairW = regW + (hasRegion && hasTime ? cfg.spacing : 0) + timW + Math.max(0, cfg.pillExtraWidth);

            int[] xy = anchor(cfg.position, mc.getWindow(), pairW, totalH, cfg.xOffset, cfg.yOffset);
            int rx = xy[0], ry = xy[1];
            int tx = rx + regW + (hasRegion && hasTime ? cfg.spacing : 0), ty = ry;

            if (hasRegion) {
                drawPill(ctx, cfg, rx, ry, regW, totalH, bg);
                int cx = rx + pad;
                int cy = ry + pad + cfg.verticalNudge;

                ltIcon(ctx, ICON_REGION, cx, cy + ((contentH - icon) / 2), icon);
                cx += icon + 4;

                ltPush(ctx, cx, cy + (contentH - textH) / 2f, s);
                ctx.drawString(mc.font, Component.literal(region), 0, 0, 0xFFFFFFFF, cfg.shadow);
                ltPop(ctx);
            }

            if (hasTime) {
                drawPill(ctx, cfg, tx, ty, timW, totalH, bg);
                int cx = tx + pad;
                int cy = ty + pad + cfg.verticalNudge;

                ltIcon(ctx, ICON_CLOCK, cx, cy + ((contentH - icon) / 2), icon);
                cx += icon + 4;

                ltPush(ctx, cx, cy + (contentH - textH) / 2f, s);
                ctx.drawString(mc.font, Component.literal(time), 0, 0, 0xFFFFFFFF, cfg.shadow);
                ltPop(ctx);
            }
        }
    }

    /* ------------------------------- helpers ------------------------------- */

    private static boolean bossBarVisible(Minecraft mc) {
        //? if >=26.1 {
        /*return false;
        *///?} else {
        try {
            if (mc.gui == null) return false;
            var bossBarHud = mc.gui.getBossOverlay();
            if (bossBarHud == null) return false;
            return !((com.fugginbeenus.locationtooltip.mixin.BossBarHudAccessor) (Object) bossBarHud)
                    .getBossBars().isEmpty();
        } catch (Throwable ignored) {
            return false;
        }
        //?}
    }

    private static void extraPills(GuiGraphics ctx, LTConfig cfg, Minecraft mc,
                                   int pad, float s, int contentH, int totalH, int bg) {
        if (cfg.showCoords) {
            iconPill(ctx, cfg, mc, ICON_COORDS, coordsText(mc), cfg.coordsPosition,
                    cfg.coordsXOffset, cfg.coordsYOffset, pad, s, contentH, totalH, bg);
        }
        if (cfg.showBiome) {
            iconPill(ctx, cfg, mc, ICON_BIOME, biomeText(mc), cfg.biomePosition,
                    cfg.biomeXOffset, cfg.biomeYOffset, pad, s, contentH, totalH, bg);
        }
    }

    private static void iconPill(GuiGraphics ctx, LTConfig cfg, Minecraft mc,
                                 ResourceLocation iconTexture, String text,
                                 LTConfig.Position pos, int xOff, int yOff,
                                 int pad, float s, int contentH, int totalH, int bg) {
        if (text == null || text.isEmpty()) return;

        final int icon = Math.max(8, cfg.iconSize);
        final int textH = (int) (mc.font.lineHeight * s);
        final int textW = (int) (mc.font.width(text) * s);
        final int totalW = pad + icon + 4 + textW + pad + Math.max(0, cfg.pillExtraWidth);

        int[] xy = anchor(pos, mc.getWindow(), totalW, totalH, xOff, yOff);
        drawPill(ctx, cfg, xy[0], xy[1], totalW, totalH, bg);

        int cx = xy[0] + pad;
        int cy = xy[1] + pad + cfg.verticalNudge;

        ltIcon(ctx, iconTexture, cx, cy + ((contentH - icon) / 2), icon);
        cx += icon + 4;

        ltPush(ctx, cx, cy + (contentH - textH) / 2f, s);
        ctx.drawString(mc.font, Component.literal(text), 0, 0, 0xFFFFFFFF, cfg.shadow);
        ltPop(ctx);
    }

    private static String coordsText(Minecraft mc) {
        if (mc.player == null) return null;
        return Mth.floor(mc.player.getX()) + ", " + Mth.floor(mc.player.getY()) + ", " + Mth.floor(mc.player.getZ());
    }

    private static String biomeText(Minecraft mc) {
        if (mc.player == null || mc.level == null) return null;
        try {
            var key = mc.level.getBiome(mc.player.blockPosition()).unwrapKey().orElse(null);
            if (key == null) return null;
            var id = key.location();
            String translation = "biome." + id.getNamespace() + "." + id.getPath();
            String name = Component.translatable(translation).getString();
            return translation.equals(name) ? prettify(id.getPath()) : name;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String prettify(String path) {
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    private static int[] anchor(LTConfig.Position pos, Window win, int w, int h, int dx, int dy) {
        int sw = win.getGuiScaledWidth(), sh = win.getGuiScaledHeight();
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

    private static int effectiveRadius(LTConfig cfg, int w, int h) {
        int max = Math.min(w, h) / 2;
        return switch (cfg.cornerStyle) {
            case PILL -> max;
            case SQUIRCLE -> Math.min(max, Math.max(cfg.cornerRadius, Math.min(w, h) / 4));
            default -> Math.min(Math.max(0, cfg.cornerRadius), max);
        };
    }

    private static void drawPill(GuiGraphics ctx, LTConfig cfg, int x, int y, int w, int h, int bg) {
        int bw = Math.max(0, cfg.borderWidth);
        if (bw > 0) {
            int a = (bg >>> 24) & 0xFF;
            int borderColor = (a << 24) | 0x00FFFFFF;
            fillRound(ctx, x - bw, y - bw, w + bw * 2, h + bw * 2,
                    effectiveRadius(cfg, w + bw * 2, h + bw * 2), borderColor);
        }
        fillRound(ctx, x, y, w, h, effectiveRadius(cfg, w, h), bg);
    }

    private static void fillRound1px(GuiGraphics ctx, int x, int y, int w, int h, int argb) {
        if (w <= 2 || h <= 2) {
            ctx.fill(x, y, x + w, y + h, argb);
            return;
        }

        ctx.fill(x, y + 1, x + w, y + h - 1, argb);

        ctx.fill(x + 1, y, x + w - 1, y + 1, argb);

        ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, argb);
    }

    private static void fillRound(GuiGraphics ctx, int x, int y, int w, int h, int r, int argb) {
        r = (r <= 1) ? 1 : Math.min(r, Math.min(w, h) / 2);
        if (r == 1) { fillRound1px(ctx, x, y, w, h, argb); return; }

        int x2 = x + w, y2 = y + h;
        ctx.fill(x + r, y,     x2 - r, y2,     argb);
        ctx.fill(x,     y + r, x + r,  y2 - r, argb);
        ctx.fill(x2 - r,y + r, x2,     y2 - r, argb);

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

    private static void ltIcon(GuiGraphics ctx, ResourceLocation tex, int x, int y, int size) {
        //? if >=1.21.11 {
        /*ctx.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, tex, x, y, 0f, 0f, size, size, size, size);
        *///?} else {
        ctx.blit(tex, x, y, 0f, 0f, size, size, size, size);
        //?}
    }

    private static void ltPush(GuiGraphics ctx, float tx, float ty, float s) {
        //? if >=1.21.11 {
        /*ctx.pose().pushMatrix();
        ctx.pose().translate(tx, ty);
        ctx.pose().scale(s, s);
        *///?} else {
        ctx.pose().pushPose();
        ctx.pose().translate(tx, ty, 0);
        ctx.pose().scale(s, s, 1);
        //?}
    }

    private static void ltPop(GuiGraphics ctx) {
        //? if >=1.21.11 {
        /*ctx.pose().popMatrix();
        *///?} else {
        ctx.pose().popPose();
        //?}
    }
}
