package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.data.Region;
import com.fugginbeenus.locationtooltip.data.RegionManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class LocationTooltipClient implements ClientModInitializer {

    private static final Identifier ICON_LOCATION = new Identifier("locationtooltip","textures/gui/icon_location.png");
    private static final Identifier ICON_CLOCK    = new Identifier("locationtooltip","textures/gui/icon_clock.png");
    private static final Identifier BAR_LEFT  = new Identifier("locationtooltip","textures/gui/location_bar_left.png");
    private static final Identifier BAR_MID   = new Identifier("locationtooltip","textures/gui/location_bar_mid.png");
    private static final Identifier BAR_RIGHT = new Identifier("locationtooltip","textures/gui/location_bar_right.png");

    private static final boolean JADE_LOADED = FabricLoader.getInstance().isModLoaded("jade");
    private static String currentRegionName = "Wilderness";

    @Override
    public void onInitializeClient() {
        LTConfig.load();
        RegionManager.load();
        RegionSelectionClient.init();
        ClientReloadCommand.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            boolean admin = client.isInSingleplayer() || (client.player != null && client.player.hasPermissionLevel(3));
            RegionManager.setAdminOverride(admin);

            BlockPos pos = client.player.getBlockPos();
            Region inside = RegionManager.getRegionAt(client.world, pos);
            String newName = (inside != null && inside.name() != null) ? inside.name() : "Wilderness";
            if (!newName.equals(currentRegionName)) currentRegionName = newName;
        });

        HudRenderCallback.EVENT.register(LocationTooltipClient::renderHUD);
    }

    private static void renderHUD(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        LTConfig cfg = LTConfig.get();

        var win = mc.getWindow();
        int sw = win.getScaledWidth();
        int sh = win.getScaledHeight();

        // Metrics (unscaled)
        final int barH  = cfg.matchJadeMetrics ? 24 : 22;
        final int padX  = cfg.matchJadeMetrics ? 10 : 8;
        final int icon  = cfg.matchJadeMetrics ? 18 : 16;
        final int gap   = cfg.matchJadeMetrics ? 8  : 6;

        float fs    = clamp(cfg.fontScale, 0.75f, 1.50f);
        float scale = clamp(cfg.scale, 0.50f, 1.50f);
        float alpha = clamp(cfg.panelAlpha, 0.10f, 1.00f);

        String clockText = formatMinecraftTime(mc);
        int locTextW = (int)(mc.textRenderer.getWidth(currentRegionName) * fs);
        int clkTextW = (int)(mc.textRenderer.getWidth(clockText) * fs);

        int locW = padX + icon + gap + locTextW + padX;
        int clkW = padX + icon + gap + clkTextW + padX;

        String mode = (cfg.layoutMode == null ? "auto" : cfg.layoutMode).toLowerCase();
        boolean split = switch (mode) {
            case "split" -> true;
            case "together" -> false;
            default -> FabricLoader.getInstance().isModLoaded("jade"); // auto
        };

        int totalW, totalH = barH;

        if (!cfg.showClock) {
            totalW = locW;

            // top-center anchor of the single bar
            int baseX = (int)((sw - totalW * scale) / 2f) + cfg.offsetX;
            int baseY = Math.max(4, Math.min(cfg.offsetY, sh - (int)(totalH * scale) - 4));
            baseX = Math.max(4, Math.min(baseX, sw - (int)(totalW * scale) - 4));

            ctx.getMatrices().push();
            ctx.getMatrices().scale(scale, scale, 1f);
            ctx.getMatrices().translate(baseX / scale, baseY / scale, 0);
            drawPanel(ctx, 0, 0, locW, barH, alpha, cfg);
            drawIconAndText(ctx, 0, 0, currentRegionName, ICON_LOCATION, barH, padX, icon, gap, fs, cfg.textShadow);
            ctx.getMatrices().pop();
            return;
        }

        if (split) {
            // --- NEW: center-anchored split so clock never moves with name width ---
            int centerX = (sw / 2) + cfg.offsetX;   // anchor at screen center
            int baseY   = Math.max(4, Math.min(cfg.offsetY, sh - (int)(barH * scale) - 4));
            int halfGap = Math.max(0, cfg.centerGap) / 2;

            ctx.getMatrices().push();
            ctx.getMatrices().scale(scale, scale, 1f);
            // translate to center in scaled space
            ctx.getMatrices().translate(centerX / scale, baseY / scale, 0);

            // left bar is placed to the LEFT of center by half-gap + its own width
            int leftX  = -(halfGap + locW);
            // right bar is placed to the RIGHT of center by half-gap (independent of locW)
            int rightX =  (halfGap);

            drawPanel(ctx, leftX, 0, locW, barH, alpha, cfg);
            drawIconAndText(ctx, leftX, 0, currentRegionName, ICON_LOCATION, barH, padX, icon, gap, fs, cfg.textShadow);

            drawPanel(ctx, rightX, 0, clkW, barH, alpha, cfg);
            drawIconAndText(ctx, rightX, 0, clockText, ICON_CLOCK, barH, padX, icon, gap, fs, cfg.textShadow);

            ctx.getMatrices().pop();
        } else {
            // together mode unchanged (draw as a single bar)
            totalW = locW + Math.max(0, cfg.togetherGap) + clkW;

            int baseX = (int)((sw - totalW * scale) / 2f) + cfg.offsetX;
            int baseY = Math.max(4, Math.min(cfg.offsetY, sh - (int)(totalH * scale) - 4));
            baseX = Math.max(4, Math.min(baseX, sw - (int)(totalW * scale) - 4));

            ctx.getMatrices().push();
            ctx.getMatrices().scale(scale, scale, 1f);
            ctx.getMatrices().translate(baseX / scale, baseY / scale, 0);

            drawPanel(ctx, 0, 0, totalW, barH, alpha, cfg);
            drawIconAndText(ctx, 0, 0, currentRegionName, ICON_LOCATION, barH, padX, icon, gap, fs, cfg.textShadow);

            int gapW = Math.max(0, cfg.togetherGap);
            if (gapW == 0) {
                int a = (int)(alpha * 255);
                int divider = (a << 24) | 0x303030;
                int sepX = locW;
                ctx.fill(sepX - 1, 4, sepX, barH - 4, divider);
            }

            drawIconAndText(ctx, locW + gapW, 0, clockText, ICON_CLOCK, barH, padX, icon, gap, fs, cfg.textShadow);
            ctx.getMatrices().pop();
        }
    }


    private static void drawPanel(DrawContext ctx, int x, int y, int w, int h, float alpha, LTConfig cfg) {
        int a = (int)(alpha * 255);
        // soft drop shadow
        int shadowA = (int)(a * 0.55f);
        ctx.fill(x + 2, y + 2, x + w + 2, y + h + 2, (shadowA << 24) | 0x000000);

        if (cfg.useNineSlice) {
            final int slice = 8;
            ctx.drawTexture(BAR_LEFT, x, y, 0, 0, slice, h, slice, h);
            int inner = Math.max(0, w - (slice * 2));
            for (int i = 0; i < inner; i += slice) {
                int seg = Math.min(slice, inner - i);
                ctx.drawTexture(BAR_MID, x + slice + i, y, 0, 0, seg, h, slice, h);
            }
            ctx.drawTexture(BAR_RIGHT, x + w - slice, y, 0, 0, slice, h, slice, h);
            return;
        }
        // fallback rect
        int bgTop = (a << 24) | 0x121416;
        int bgBot = (a << 24) | 0x1A1C1E;
        ctx.fillGradient(x, y, x + w, y + h, bgTop, bgBot);
        int outline = (a << 24) | 0x3A3A3A;
        ctx.drawBorder(x, y, w, h, outline);
    }

    private static void drawIconAndText(DrawContext ctx, int x, int y, String text, Identifier iconId,
                                        int h, int padX, int iconSize, int gap, float fontScale, boolean textShadow) {
        var mc = MinecraftClient.getInstance();
        var tr = mc.textRenderer;

        int iconY = y + (h - iconSize) / 2;
        ctx.drawTexture(iconId, x + padX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);

        int tx = x + padX + iconSize + gap;
        int ty = y + (h - 8) / 2;

        ctx.getMatrices().push();
        ctx.getMatrices().translate(tx, ty, 0);
        ctx.getMatrices().scale(fontScale, fontScale, 1f);
        if (textShadow) {
            ctx.drawTextWithShadow(tr, text, 0, 0, 0xFFFFFFFF);
        } else {
            ctx.drawText(tr, text, 0, 0, 0xFFFFFFFF, false);
        }
        ctx.getMatrices().pop();
    }

    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }

    private static String formatMinecraftTime(MinecraftClient mc) {
        long t = mc.world.getTimeOfDay() % 24000L;
        long totalMinutes = (t * 3L) / 50L;
        long minutes = (totalMinutes + 360) % 1440;
        int hour24 = (int)(minutes / 60);
        int min = (int)(minutes % 60);
        String ampm = (hour24 >= 12) ? "PM" : "AM";
        int hour12 = hour24 % 12; if (hour12 == 0) hour12 = 12;
        return String.format("%d:%02d %s", hour12, min, ampm);
    }
}
