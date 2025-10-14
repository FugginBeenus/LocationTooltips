package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.data.Region;
import com.fugginbeenus.locationtooltip.data.RegionManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class LocationTooltipClient implements ClientModInitializer {

    private static final Identifier BAR_LEFT  = new Identifier("locationtooltip", "textures/gui/location_bar_left.png");
    private static final Identifier BAR_MID   = new Identifier("locationtooltip", "textures/gui/location_bar_mid.png");
    private static final Identifier BAR_RIGHT = new Identifier("locationtooltip", "textures/gui/location_bar_right.png");
    private static final Identifier ICON_LOCATION = new Identifier("locationtooltip", "textures/gui/icon_location.png");
    private static final Identifier ICON_CLOCK    = new Identifier("locationtooltip", "textures/gui/icon_clock.png");

    private static String currentRegionName = "Wilderness";
    private static int fadeTicks = 0;

    @Override
    public void onInitializeClient() {
        LTConfig.load();
        ClientReloadCommand.register();
        RegionSelectionClient.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            BlockPos pos = client.player.getBlockPos();
            Region inside = RegionManager.getRegionAt(client.world, pos);
            String newName = (inside != null && inside.name() != null) ? inside.name() : "Wilderness";

            if (!newName.equals(currentRegionName)) {
                currentRegionName = newName;
                fadeTicks = LTConfig.get().fadeTicks;
            }
        });

        HudRenderCallback.EVENT.register(LocationTooltipClient::renderHUD);
    }

    private static void renderHUD(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        LTConfig cfg = LTConfig.get();
        Window window = mc.getWindow();
        int sw = window.getScaledWidth();
        int sh = window.getScaledHeight();

        int alpha = (int) (Math.min(fadeTicks, cfg.fadeTicks) / (float) cfg.fadeTicks * 255);
        int textColor = (alpha << 24) | 0xFFFFFF;

        int barHeight = 20;
        int textPadX = 8;
        int iconSize = 16;
        int iconPad = 4;
        int minBarW = 110;
        int maxBarW = 240;

        int locTextW = mc.textRenderer.getWidth(currentRegionName);
        int locBarW = clamp(minBarW, maxBarW, iconSize + iconPad + textPadX + locTextW + textPadX);

        int cx = sw / 2;
        int y = sh / 2 + cfg.gutterY;

        // left-side location bar
        int locX = cx - cfg.centerOffset - locBarW;
        drawBar(ctx, locX, y, locBarW, barHeight);
        drawIconAndText(ctx, locX, y, currentRegionName, ICON_LOCATION, textColor, barHeight, textPadX, iconSize, iconPad);

        // right-side clock bar (optional)
        if (cfg.showClock) {
            String clockText = formatMinecraftTime(mc);
            int clkTextW = mc.textRenderer.getWidth(clockText);
            int clkBarW = clamp(minBarW, maxBarW, iconSize + iconPad + textPadX + clkTextW + textPadX);
            int clkX = cx + cfg.centerOffset;
            drawBar(ctx, clkX, y, clkBarW, barHeight);
            drawIconAndText(ctx, clkX, y, clockText, ICON_CLOCK, textColor, barHeight, textPadX, iconSize, iconPad);
        }

        if (fadeTicks > 0) fadeTicks--;
    }

    private static void drawBar(DrawContext ctx, int x, int y, int width, int height) {
        ctx.drawTexture(BAR_LEFT, x, y, 0, 0, 8, height, 8, height);
        int inner = Math.max(0, width - 16);
        for (int i = 0; i < inner; i += 8) {
            int w = Math.min(8, inner - i);
            ctx.drawTexture(BAR_MID, x + 8 + i, y, 0, 0, w, height, 8, height);
        }
        ctx.drawTexture(BAR_RIGHT, x + width - 8, y, 0, 0, 8, height, 8, height);
    }

    private static void drawIconAndText(DrawContext ctx, int x, int y, String text, Identifier icon, int color, int height, int pad, int size, int gap) {
        var tr = MinecraftClient.getInstance().textRenderer;
        int iconY = y + (height - size) / 2;
        ctx.drawTexture(icon, x + pad, iconY, 0, 0, size, size, size, size);
        int tx = x + pad + size + gap;
        int ty = y + (height - 8) / 2;
        ctx.drawTextWithShadow(tr, text, tx, ty, color);
    }

    private static int clamp(int min, int max, int v) {
        return Math.max(min, Math.min(max, v));
    }

    private static String formatMinecraftTime(MinecraftClient mc) {
        long t = mc.world.getTimeOfDay() % 24000L;
        long totalMinutes = (t * 60L) / 16L;
        long minutes = (totalMinutes + 360) % 1440;
        int hour24 = (int) (minutes / 60);
        int min = (int) (minutes % 60);
        String ampm = (hour24 >= 12) ? "PM" : "AM";
        int hour12 = hour24 % 12;
        if (hour12 == 0) hour12 = 12;
        return String.format("%d:%02d %s", hour12, min, ampm);
    }
}
