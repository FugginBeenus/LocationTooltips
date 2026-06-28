package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.region.flag.RegionFlag;
import com.fugginbeenus.locationtooltip.region.flag.RegionFlags;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom-drawn tri-state flag editor: a 2-column grid of flag cells, each showing the flag's
 * icon, name, and current state (Inherit / Allow / Deny) with a colored state accent. Clicking
 * a cell cycles the state. Holds a sparse override map (absent = inherit).
 *
 * Self-rendered (not vanilla widgets) so it matches the redesigned screens and can show icons.
 */
public final class FlagEditor {

    private final Map<String, Boolean> overrides = new LinkedHashMap<>();
    private final List<RegionFlag> flags = new ArrayList<>(RegionFlags.all());

    private int x, y, colW, rowH = 18, gap = 4;

    public FlagEditor(Map<String, Boolean> initial) {
        if (initial != null) overrides.putAll(initial);
    }

    public Map<String, Boolean> overrides() {
        return overrides;
    }

    public void layout(int x, int y, int colW, int rowH, int gap) {
        this.x = x; this.y = y; this.colW = colW; this.rowH = rowH; this.gap = gap;
    }

    public int rows() {
        return (flags.size() + 1) / 2;
    }

    public int height() {
        int r = rows();
        return r * rowH + Math.max(0, r - 1) * gap;
    }

    private int cellX(int i) {
        return x + (i / rows()) * (colW + gap);
    }

    private int cellY(int i) {
        return y + (i % rows()) * (rowH + gap);
    }

    public void render(DrawContext ctx, TextRenderer tr, double mouseX, double mouseY) {
        for (int i = 0; i < flags.size(); i++) {
            RegionFlag f = flags.get(i);
            int bx = cellX(i), by = cellY(i);
            boolean hover = LTGui.hovered(mouseX, mouseY, bx, by, colW, rowH);

            Boolean v = overrides.get(f.id);
            int stateColor = (v == null) ? LTGui.FAINT : (v ? LTGui.OK_HOVER : LTGui.DANGER_HOVER);

            LTGui.roundRect(ctx, bx, by, colW, rowH, 4, hover ? LTGui.BTN_HOVER : LTGui.BTN);
            ctx.fill(bx, by + 2, bx + 2, by + rowH - 2, stateColor); // state accent bar

            int iconY = by + (rowH - 12) / 2;
            boolean hasIcon = FlagIcons.draw(ctx, f.id, bx + 6, iconY, 12);
            int textX = hasIcon ? bx + 22 : bx + 8;

            ctx.drawText(tr, shortName(f), textX, by + (rowH - 8) / 2, LTGui.TEXT, false);

            String state = (v == null) ? "Inherit" : (v ? "Allow" : "Deny");
            int sw = tr.getWidth(state);
            ctx.drawText(tr, state, bx + colW - sw - 6, by + (rowH - 8) / 2, stateColor, false);
        }
    }

    /** Returns true if a cell was clicked (state cycled). */
    public boolean mouseClicked(double mx, double my) {
        for (int i = 0; i < flags.size(); i++) {
            if (LTGui.hovered(mx, my, cellX(i), cellY(i), colW, rowH)) {
                cycle(flags.get(i).id);
                return true;
            }
        }
        return false;
    }

    private void cycle(String id) {
        Boolean cur = overrides.get(id);
        if (cur == null) overrides.put(id, Boolean.TRUE);   // inherit -> allow
        else if (cur) overrides.put(id, Boolean.FALSE);     // allow   -> deny
        else overrides.remove(id);                          // deny    -> inherit
    }

    private static String shortName(RegionFlag f) {
        return f.displayName.startsWith("Allow ") ? f.displayName.substring(6) : f.displayName;
    }
}
