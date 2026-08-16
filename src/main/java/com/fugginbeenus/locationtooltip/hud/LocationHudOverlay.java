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

    private record Pill(ResourceLocation leftIcon, String label, ResourceLocation rightIcon,
                        LTConfig.Position pos, int xOff, int yOff) {}

    private static void render(GuiGraphics ctx, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        //? if <26.1 {
        if (!preview && mc.options.hideGui) return;
        //?}

        LTConfig cfg = LTConfig.get();

        String region = cfg.showRegionName ? currentTitle : null;
        String time   = (cfg.showClock && mc.level != null) ? formatTime(mc.level.getDayTime(), cfg.time24h) : null;

        final boolean hasRegion = region != null && !region.isEmpty();
        final boolean hasTime   = time   != null && !time.isEmpty();

        java.util.List<Pill> pills = new java.util.ArrayList<>();
        if (!cfg.splitElements && hasRegion && hasTime) {
            pills.add(new Pill(ICON_REGION, region + cfg.separator + time, ICON_CLOCK,
                    cfg.position, cfg.xOffset, cfg.yOffset));
        } else {
            if (hasRegion) pills.add(new Pill(ICON_REGION, region, null, cfg.position, cfg.xOffset, cfg.yOffset));
            if (hasTime)   pills.add(new Pill(ICON_CLOCK, time, null, cfg.position, cfg.xOffset, cfg.yOffset));
        }
        if (cfg.showCoords) {
            String coords = coordsText(mc);
            if (coords != null && !coords.isEmpty()) {
                pills.add(new Pill(ICON_COORDS, coords, null,
                        cfg.coordsPosition, cfg.coordsXOffset, cfg.coordsYOffset));
            }
        }
        if (cfg.showBiome) {
            String biome = biomeText(mc);
            if (biome != null && !biome.isEmpty()) {
                pills.add(new Pill(ICON_BIOME, biome, null,
                        cfg.biomePosition, cfg.biomeXOffset, cfg.biomeYOffset));
            }
        }
        if (pills.isEmpty()) return;

        final int icon  = Math.max(8, cfg.iconSize);
        final int pad   = Math.max(0, cfg.pillPadding);
        final float s   = Math.max(0.5f, cfg.textScale);
        final int textH = (int) (mc.font.lineHeight * s);
        final int contentH = Math.max(textH, icon);
        final int totalH   = Math.round((contentH + pad * 2) * Math.max(0.5f, cfg.pillHeightScale));

        final int alpha = (int) (Math.max(0f, Math.min(1f, cfg.backgroundOpacity)) * 255) & 0xFF;
        final int bg = (alpha << 24);

        for (LTConfig.Position pos : LTConfig.Position.values()) {
            java.util.List<Pill> group = new java.util.ArrayList<>();
            for (Pill pill : pills) {
                if (pill.pos() == pos) group.add(pill);
            }
            if (group.isEmpty()) continue;
            if (!preview && pos == LTConfig.Position.TOP_CENTER && bossBarVisible(mc)) continue;
            drawGroup(ctx, cfg, mc, group, pos, icon, pad, s, textH, contentH, totalH, bg);
        }
    }

    private static void drawGroup(GuiGraphics ctx, LTConfig cfg, Minecraft mc,
                                  java.util.List<Pill> group, LTConfig.Position pos,
                                  int icon, int pad, float s, int textH,
                                  int contentH, int totalH, int bg) {
        int[] widths = new int[group.size()];
        int rowW = 0;
        for (int i = 0; i < group.size(); i++) {
            widths[i] = pillWidth(mc, group.get(i), icon, pad, s);
            rowW += widths[i];
        }
        rowW += cfg.spacing * (group.size() - 1) + Math.max(0, cfg.pillExtraWidth);

        Pill first = group.get(0);
        int[] xy = anchor(pos, mc.getWindow(), rowW, totalH, first.xOff(), first.yOff());
        int x = xy[0];

        for (int i = 0; i < group.size(); i++) {
            Pill pill = group.get(i);
            drawPill(ctx, cfg, x, xy[1], widths[i], totalH, bg);

            int cx = x + pad;
            int cy = xy[1] + pad + cfg.verticalNudge;

            if (pill.leftIcon() != null) {
                ltIcon(ctx, pill.leftIcon(), cx, cy + ((contentH - icon) / 2), icon);
                cx += icon + 4;
            }

            ltPush(ctx, cx, cy + (contentH - textH) / 2f, s);
            ctx.drawString(mc.font, Component.literal(pill.label()), 0, 0, 0xFFFFFFFF, cfg.shadow);
            ltPop(ctx);

            if (pill.rightIcon() != null) {
                cx += (int) (mc.font.width(pill.label()) * s) + 4;
                ltIcon(ctx, pill.rightIcon(), cx, cy + ((contentH - icon) / 2), icon);
            }

            x += widths[i] + cfg.spacing;
        }
    }

    private static int pillWidth(Minecraft mc, Pill pill, int icon, int pad, float s) {
        int width = pad + (int) (mc.font.width(pill.label()) * s) + pad;
        if (pill.leftIcon() != null)  width += icon + 4;
        if (pill.rightIcon() != null) width += icon + 4;
        return width;
    }

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
