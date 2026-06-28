package com.fugginbeenus.locationtooltip.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Tiny shared UI toolkit for the mod's custom screens: a dark "high-end" theme + rounded-rect
 * drawing, hover testing, fields and buttons. Built on vanilla {@link DrawContext} (no GUI
 * library dependency).
 */
public final class LTGui {
    private LTGui() {}

    // ---- theme ----
    public static final int DIM         = 0xC00A0A0F; // full-screen scrim
    public static final int PANEL       = 0xF014141C; // panel body
    public static final int PANEL_HEAD  = 0xFF1B1B26; // header strip
    public static final int BORDER      = 0xFF30303F; // panel/field border
    public static final int ACCENT      = 0xFF40C4D4; // teal accent
    public static final int ACCENT_DIM  = 0x5540C4D4;
    public static final int ROW_ALT     = 0x0EFFFFFF;
    public static final int ROW_HOVER   = 0x22FFFFFF;
    public static final int TEXT        = 0xFFF2F3F5;
    public static final int SUBTEXT     = 0xFF99A0AC;
    public static final int FAINT       = 0xFF6A7079;
    public static final int FIELD       = 0xFF0D0D13;
    public static final int BTN         = 0xFF2A2A35;
    public static final int BTN_HOVER   = 0xFF3A3A48;
    public static final int DANGER      = 0xFF7C2F26;
    public static final int DANGER_HOVER= 0xFFB94130;
    public static final int OK          = 0xFF2E7D52;
    public static final int OK_HOVER    = 0xFF3CA268;

    public static boolean hovered(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** Solid rounded rectangle (anti-aliased-ish via per-row corner insets). */
    public static void roundRect(DrawContext ctx, int x, int y, int w, int h, int r, int argb) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        int x2 = x + w, y2 = y + h;
        if (r <= 0) { ctx.fill(x, y, x2, y2, argb); return; }
        ctx.fill(x + r, y, x2 - r, y2, argb);       // center band (full height)
        ctx.fill(x, y + r, x + r, y2 - r, argb);    // left band (middle)
        ctx.fill(x2 - r, y + r, x2, y2 - r, argb);  // right band (middle)
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int dx = (int) Math.round(Math.sqrt(Math.max(0, (double) r * r - dy * dy)));
            ctx.fill(x + r - dx, y + i, x2 - (r - dx), y + i + 1, argb);
            ctx.fill(x + r - dx, y2 - 1 - i, x2 - (r - dx), y2 - i, argb);
        }
    }

    /** 1px rounded border (edges only). */
    public static void roundBorder(DrawContext ctx, int x, int y, int w, int h, int r, int argb) {
        if (w <= 0 || h <= 0) return;
        ctx.fill(x + r, y, x + w - r, y + 1, argb);
        ctx.fill(x + r, y + h - 1, x + w - r, y + h, argb);
        ctx.fill(x, y + r, x + 1, y + h - r, argb);
        ctx.fill(x + w - 1, y + r, x + w, y + h - r, argb);
    }

    /** Panel with body + subtle border. */
    public static void panel(DrawContext ctx, int x, int y, int w, int h) {
        roundRect(ctx, x, y, w, h, 6, PANEL);
        roundBorder(ctx, x, y, w, h, 6, BORDER);
    }

    /** A field background (use with a TextFieldWidget that has drawsBackground=false). */
    public static void field(DrawContext ctx, int x, int y, int w, int h, boolean focused) {
        roundRect(ctx, x, y, w, h, 4, FIELD);
        roundBorder(ctx, x, y, w, h, 4, focused ? ACCENT : BORDER);
    }

    /** Pill button; caller does the click via hovered(). */
    public static void button(DrawContext ctx, TextRenderer tr, int x, int y, int w, int h,
                              String label, boolean hover, int base, int baseHover) {
        roundRect(ctx, x, y, w, h, 4, hover ? baseHover : base);
        int tw = tr.getWidth(label);
        ctx.drawText(tr, label, x + (w - tw) / 2, y + (h - 8) / 2, TEXT, false);
    }

    public static void button(DrawContext ctx, TextRenderer tr, int x, int y, int w, int h, String label, boolean hover) {
        button(ctx, tr, x, y, w, h, label, hover, BTN, BTN_HOVER);
    }
}
