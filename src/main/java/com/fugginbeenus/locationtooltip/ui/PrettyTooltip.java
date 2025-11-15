package com.fugginbeenus.locationtooltip.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Client-only styled tooltip helper. */
public class PrettyTooltip {

    private PrettyTooltip() {}

    public static void draw(DrawContext ctx, int x, int y, Text text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;

        int pad = 5;
        int w = mc.textRenderer.getWidth(text) + pad * 2;
        int h = mc.textRenderer.fontHeight + pad * 2;

        // semi-transparent dark background
        ctx.fill(x, y, x + w, y + h, 0xC0000000);
        // simple border
        ctx.fill(x, y, x + w, y + 1, 0xFFFFFFFF);
        ctx.fill(x, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        ctx.fill(x, y, x + 1, y + h, 0xFFFFFFFF);
        ctx.fill(x + w - 1, y, x + w, y + h, 0xFFFFFFFF);

        ctx.drawText(mc.textRenderer, text, x + pad, y + pad, 0xFFFFFF, false);
    }
}
