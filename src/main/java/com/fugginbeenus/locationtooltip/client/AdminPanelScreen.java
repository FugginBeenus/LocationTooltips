package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.net.client.LTPacketsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Sleek admin region browser (opened by the Admin Compass / keybind). Custom-rendered via
 * {@link LTGui}: a scrollable card with search, per-region rows (name, dimension, owner, flag
 * icons) and scroll-aware Edit/Delete actions, plus a draggable scrollbar.
 */
public class AdminPanelScreen extends Screen {

    // ===== data model (kept stable — referenced by LTPacketsClient) =====
    public static class RegionRow {
        public final String id;
        public String name;
        public final Identifier dim;
        public final BlockPos a, b;
        public java.util.Map<String, Boolean> flags; // id -> allow/deny; absent = inherit
        public String ownerName;  // Player name or "Server" for admin regions
        public String source;     // PLAYER / SERVER / STRUCTURE

        public RegionRow(String id, String name, Identifier dim, BlockPos a, BlockPos b,
                         java.util.Map<String, Boolean> flags, String ownerName, String source) {
            this.id = id; this.name = name; this.dim = dim; this.a = a; this.b = b;
            this.flags = flags; this.ownerName = ownerName; this.source = source;
        }
        public boolean isStructure() { return "STRUCTURE".equals(source); }
    }

    private static AdminPanelScreen instance;
    private final List<RegionRow> regions = new ArrayList<>();

    private static final int ROW_H = 42;

    private TextFieldWidget searchField;
    private int panelX, panelY, panelW, panelH;
    private int listX, listY, listW, listH;
    private int scroll;
    private boolean draggingScroll;
    private int ticks;

    public AdminPanelScreen() {
        super(Text.literal("Regions"));
        instance = this;
    }

    /** Called from LTPacketsClient when new data arrives. */
    public static void receiveList(RegionRow[] rows) {
        if (instance == null) return;
        instance.regions.clear();
        for (RegionRow r : rows) instance.regions.add(r);
        instance.clampScroll();
    }

    @Override
    protected void init() {
        panelW = 420;
        panelH = Math.max(200, this.height - 48);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        listX = panelX + 10;
        listW = panelW - 20;
        listY = panelY + 36;
        listH = (panelY + panelH - 26) - listY;

        int searchW = 150, searchH = 16;
        int searchX = panelX + panelW - 12 - 16 - 6 - searchW;
        searchField = new TextFieldWidget(this.textRenderer, searchX + 5, panelY + 11, searchW - 10, 12, Text.literal("Search"));
        searchField.setDrawsBackground(false);
        searchField.setMaxLength(48);
        searchField.setChangedListener(s -> clampScroll());
        addDrawableChild(searchField);

        LTPacketsClient.requestAllAdminList();
    }

    @Override
    public void tick() {
        super.tick();
        if (++ticks % 100 == 0) LTPacketsClient.requestAllAdminList(); // refresh ~5s
    }

    // ===== filtered view =====
    private List<RegionRow> visible() {
        String q = (searchField == null) ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return regions;
        List<RegionRow> out = new ArrayList<>();
        for (RegionRow r : regions) if (r.name.toLowerCase(Locale.ROOT).contains(q)) out.add(r);
        return out;
    }

    private void clampScroll() {
        int max = Math.max(0, visible().size() * ROW_H - listH);
        if (scroll < 0) scroll = 0;
        if (scroll > max) scroll = max;
    }

    // ===== input =====
    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        if (LTGui.hovered(mx, my, listX, listY, listW, listH)) {
            scroll -= (int) Math.signum(amount) * (ROW_H / 2);
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mx, my, amount);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            // close X
            if (LTGui.hovered(mx, my, panelX + panelW - 12 - 16, panelY + 8, 16, 16)) { close(); return true; }

            // scrollbar
            int max = Math.max(0, visible().size() * ROW_H - listH);
            if (max > 0 && LTGui.hovered(mx, my, listX + listW - 4, listY, 4, listH)) {
                draggingScroll = true;
                dragTo(my);
                return true;
            }

            // row action buttons (scroll-aware)
            List<RegionRow> vis = visible();
            for (int i = 0; i < vis.size(); i++) {
                int rowY = listY - scroll + i * ROW_H;
                if (rowY + ROW_H <= listY || rowY >= listY + listH) continue; // off-screen
                int[] edit = editRect(rowY), del = deleteRect(rowY);
                if (LTGui.hovered(mx, my, edit[0], edit[1], edit[2], edit[3])) {
                    MinecraftClient.getInstance().setScreen(new EditRegionScreen(vis.get(i), this));
                    return true;
                }
                if (LTGui.hovered(mx, my, del[0], del[1], del[2], del[3])) {
                    RegionRow r = vis.get(i);
                    regions.remove(r);                 // optimistic local removal
                    LTPacketsClient.sendAdminDelete(r.id);
                    clampScroll();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingScroll) { dragTo(my); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingScroll = false;
        return super.mouseReleased(mx, my, button);
    }

    private void dragTo(double my) {
        int max = Math.max(0, visible().size() * ROW_H - listH);
        double frac = (my - listY) / Math.max(1, listH);
        scroll = (int) Math.round(frac * max);
        clampScroll();
    }

    private int[] editRect(int rowY) {
        int w = 46, h = 18;
        int x = listX + listW - 12 - 54 - 6 - w;
        return new int[]{x, rowY + (ROW_H - h) / 2, w, h};
    }

    private int[] deleteRect(int rowY) {
        int w = 54, h = 18;
        int x = listX + listW - 12 - w;
        return new int[]{x, rowY + (ROW_H - h) / 2, w, h};
    }

    // ===== render =====
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, LTGui.DIM);
        LTGui.panel(ctx, panelX, panelY, panelW, panelH);

        // header
        LTGui.roundRect(ctx, panelX, panelY, panelW, 30, 6, LTGui.PANEL_HEAD);
        ctx.drawText(this.textRenderer, Text.literal("§lRegions"), panelX + 12, panelY + 11, LTGui.TEXT, false);
        ctx.fill(panelX + 10, panelY + 29, panelX + panelW - 10, panelY + 30, LTGui.ACCENT);

        // search field
        int searchW = 150;
        int searchX = panelX + panelW - 12 - 16 - 6 - searchW;
        LTGui.field(ctx, searchX, panelY + 8, searchW, 16, searchField != null && searchField.isFocused());
        if (searchField != null && searchField.getText().isEmpty()) {
            ctx.drawText(this.textRenderer, Text.literal("Search…"), searchX + 5, panelY + 11, LTGui.FAINT, false);
        }

        // close X
        boolean closeHover = LTGui.hovered(mouseX, mouseY, panelX + panelW - 12 - 16, panelY + 8, 16, 16);
        LTGui.roundRect(ctx, panelX + panelW - 12 - 16, panelY + 8, 16, 16, 4, closeHover ? LTGui.DANGER_HOVER : LTGui.BTN);
        ctx.drawText(this.textRenderer, Text.literal("✕"), panelX + panelW - 12 - 16 + 5, panelY + 12, LTGui.TEXT, false);

        // list
        List<RegionRow> vis = visible();
        ctx.enableScissor(listX, listY, listX + listW, listY + listH);
        if (vis.isEmpty()) {
            String msg = regions.isEmpty() ? "No regions nearby" : "No matches";
            ctx.drawText(this.textRenderer, msg, listX + 8, listY + 8, LTGui.SUBTEXT, false);
        }
        for (int i = 0; i < vis.size(); i++) {
            int rowY = listY - scroll + i * ROW_H;
            if (rowY + ROW_H <= listY || rowY >= listY + listH) continue;
            renderRow(ctx, vis.get(i), rowY, mouseX, mouseY);
        }
        ctx.disableScissor();

        // scrollbar
        int max = Math.max(0, vis.size() * ROW_H - listH);
        if (max > 0) {
            int trackX = listX + listW - 4;
            LTGui.roundRect(ctx, trackX, listY, 4, listH, 2, 0x33000000);
            int thumbH = Math.max(20, (int) ((long) listH * listH / (vis.size() * ROW_H)));
            int thumbY = listY + (int) ((long) (listH - thumbH) * scroll / max);
            LTGui.roundRect(ctx, trackX, thumbY, 4, thumbH, 2, LTGui.ACCENT_DIM);
        }

        // footer
        ctx.drawText(this.textRenderer,
                Text.literal("§7" + vis.size() + (vis.size() == 1 ? " region" : " regions")),
                panelX + 12, panelY + panelH - 16, LTGui.SUBTEXT, false);

        super.render(ctx, mouseX, mouseY, delta); // search field text
    }

    private void renderRow(DrawContext ctx, RegionRow r, int rowY, int mouseX, int mouseY) {
        boolean hover = LTGui.hovered(mouseX, mouseY, listX, rowY, listW, ROW_H)
                && mouseY >= listY && mouseY < listY + listH;
        LTGui.roundRect(ctx, listX, rowY + 1, listW, ROW_H - 2, 4, hover ? LTGui.ROW_HOVER : LTGui.ROW_ALT);

        // source accent dot (cyan = structure, orange = normal)
        int dotColor = r.isStructure() ? 0xFF40C4D4 : 0xFFE0A53C;
        ctx.fill(listX + 4, rowY + 6, listX + 6, rowY + ROW_H - 6, dotColor);

        // name
        ctx.drawText(this.textRenderer, r.name, listX + 12, rowY + 6, LTGui.TEXT, false);

        // dim + owner
        String sub = "§7" + r.dim.getPath();
        if (r.ownerName != null && !r.ownerName.isEmpty()) sub += " §8• §7" + r.ownerName;
        ctx.drawText(this.textRenderer, sub, listX + 12, rowY + 18, LTGui.SUBTEXT, false);

        // flag icons
        int fx = listX + 12;
        int fyMax = listX + listW - 12 - 54 - 6 - 46 - 8; // keep clear of buttons
        if (r.flags != null && !r.flags.isEmpty()) {
            for (java.util.Map.Entry<String, Boolean> e : r.flags.entrySet()) {
                if (fx > fyMax) { ctx.drawText(this.textRenderer, "…", fx, rowY + 30, LTGui.FAINT, false); break; }
                boolean allow = e.getValue();
                if (FlagIcons.draw(ctx, e.getKey(), fx, rowY + 28, 10)) {
                    ctx.fill(fx, rowY + 38, fx + 10, rowY + 39, allow ? 0xFF55FF55 : 0xFFFF5555);
                    fx += 13;
                } else {
                    ctx.drawText(this.textRenderer, (allow ? "§a" : "§c") + e.getKey(), fx, rowY + 30, LTGui.TEXT, false);
                    fx += this.textRenderer.getWidth(e.getKey()) + 8;
                }
            }
        } else {
            ctx.drawText(this.textRenderer, "§8default", listX + 12, rowY + 30, 0xFF6A7079, false);
        }

        // action buttons
        int[] edit = editRect(rowY), del = deleteRect(rowY);
        LTGui.button(ctx, this.textRenderer, edit[0], edit[1], edit[2], edit[3], "Edit",
                LTGui.hovered(mouseX, mouseY, edit[0], edit[1], edit[2], edit[3]) && mouseY >= listY && mouseY < listY + listH,
                LTGui.BTN, LTGui.BTN_HOVER);
        LTGui.button(ctx, this.textRenderer, del[0], del[1], del[2], del[3], "Delete",
                LTGui.hovered(mouseX, mouseY, del[0], del[1], del[2], del[3]) && mouseY >= listY && mouseY < listY + listH,
                LTGui.DANGER, LTGui.DANGER_HOVER);
    }

    @Override public boolean shouldCloseOnEsc() { return true; }
    @Override public void close() { MinecraftClient.getInstance().setScreen(null); }

    // ===== edit subscreen =====
    public static class EditRegionScreen extends RegionConfigScreen {
        private final RegionRow row;
        private final Screen returnTo;

        public EditRegionScreen(RegionRow row, Screen returnTo) {
            super("Edit Region");
            this.row = row;
            this.returnTo = returnTo;
        }

        @Override protected String headerTitle() { return "Edit Region"; }
        @Override protected String confirmLabel() { return "Save"; }
        @Override protected String initialName() { return row.name; }
        @Override protected java.util.Map<String, Boolean> initialFlags() { return row.flags; }

        @Override
        protected void onConfirm(String name, java.util.Map<String, Boolean> newFlags) {
            row.name = name;
            row.flags = new java.util.LinkedHashMap<>(newFlags);
            LTPacketsClient.sendAdminRename(row.id, name, newFlags);
            close();
        }

        @Override public void close() { MinecraftClient.getInstance().setScreen(returnTo); }
    }
}
