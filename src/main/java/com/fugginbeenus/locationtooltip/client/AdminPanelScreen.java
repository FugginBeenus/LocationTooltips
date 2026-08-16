package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.net.client.LTPacketsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminPanelScreen extends Screen {
    public static class RegionRow {
        public final String id;
        public String name;
        public final ResourceLocation dim;
        public final BlockPos a, b;
        public java.util.Map<String, Boolean> flags;
        public String ownerName;
        public String source;
        public final boolean nameable;

        public RegionRow(String id, String name, ResourceLocation dim, BlockPos a, BlockPos b,
                         java.util.Map<String, Boolean> flags, String ownerName, String source,
                         boolean nameable) {
            this.id = id; this.name = name; this.dim = dim; this.a = a; this.b = b;
            this.flags = flags; this.ownerName = ownerName; this.source = source;
            this.nameable = nameable;
        }
        public boolean isStructure() { return "STRUCTURE".equals(source); }
    }

    private static AdminPanelScreen instance;
    private final List<RegionRow> regions = new ArrayList<>();

    private static final int ROW_H = 42;

    private EditBox searchField;
    private int panelX, panelY, panelW, panelH;
    private int searchW;
    private int listX, listY, listW, listH;
    private int scroll;
    private boolean draggingScroll;
    private int ticks;

    public AdminPanelScreen() {
        super(Component.literal("Regions"));
        instance = this;
    }

    public static void receiveList(RegionRow[] rows) {
        if (instance == null) return;
        instance.regions.clear();
        for (RegionRow r : rows) instance.regions.add(r);
        instance.clampScroll();
    }

    @Override
    protected void init() {
        panelW = Math.min(420, this.width - 24);
        panelH = Math.min(Math.max(160, this.height - 48), this.height - 12);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        listX = panelX + 10;
        listW = panelW - 20;
        listY = panelY + 36;
        listH = (panelY + panelH - 26) - listY;

        searchW = Math.max(60, Math.min(150, panelW - 190));
        int searchX = panelX + panelW - 12 - 16 - 6 - searchW;
        searchField = new EditBox(this.font, searchX + 5, panelY + 11, searchW - 10, 12, Component.literal("Search"));
        searchField.setBordered(false);
        searchField.setMaxLength(48);
        searchField.setResponder(s -> clampScroll());
        addRenderableWidget(searchField);

        LTPacketsClient.requestAllAdminList();
    }

    @Override
    public void tick() {
        super.tick();
        if (++ticks % 100 == 0) LTPacketsClient.requestAllAdminList();
    }

    private List<RegionRow> visible() {
        String q = (searchField == null) ? "" : searchField.getValue().trim().toLowerCase(Locale.ROOT);
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

    //? if >=1.21 {
    /*@Override
    public boolean mouseScrolled(double mx, double my, double horizontalAmount, double amount) {
        return ltScroll(mx, my, amount) || super.mouseScrolled(mx, my, horizontalAmount, amount);
    }
    *///?} else {
    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        return ltScroll(mx, my, amount) || super.mouseScrolled(mx, my, amount);
    }
    //?}

    private boolean ltScroll(double mx, double my, double amount) {
        if (LTGui.hovered(mx, my, listX, listY, listW, listH)) {
            scroll -= (int) Math.signum(amount) * (ROW_H / 2);
            clampScroll();
            return true;
        }
        return false;
    }

    //? if >=1.21.11 {
    /*@Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        return ltClick(event.x(), event.y(), event.buttonInfo().button())
                || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy) {
        if (draggingScroll) { dragTo(event.y()); return true; }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        draggingScroll = false;
        return super.mouseReleased(event);
    }
    *///?} else {
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        return ltClick(mx, my, button) || super.mouseClicked(mx, my, button);
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
    //?}

    private boolean ltClick(double mx, double my, int button) {
        if (button != 0) return false;

        if (LTGui.hovered(mx, my, panelX + panelW - 12 - 16, panelY + 8, 16, 16)) { onClose(); return true; }

        int max = Math.max(0, visible().size() * ROW_H - listH);
        if (max > 0 && LTGui.hovered(mx, my, listX + listW - 4, listY, 4, listH)) {
            draggingScroll = true;
            dragTo(my);
            return true;
        }

        List<RegionRow> vis = visible();
        for (int i = 0; i < vis.size(); i++) {
            int rowY = listY - scroll + i * ROW_H;
            if (rowY + ROW_H <= listY || rowY >= listY + listH) continue;
            RegionRow row = vis.get(i);
            if (row.nameable) {
                int[] name = nameRect(rowY);
                if (LTGui.hovered(mx, my, name[0], name[1], name[2], name[3])) {
                    Minecraft.getInstance().setScreen(new NameVillageScreen(row, this));
                    return true;
                }
                continue;
            }
            int[] edit = editRect(rowY), del = deleteRect(rowY);
            if (LTGui.hovered(mx, my, edit[0], edit[1], edit[2], edit[3])) {
                Minecraft.getInstance().setScreen(new EditRegionScreen(vis.get(i), this));
                return true;
            }
            if (LTGui.hovered(mx, my, del[0], del[1], del[2], del[3])) {
                RegionRow r = vis.get(i);
                regions.remove(r);
                LTPacketsClient.sendAdminDelete(r.id);
                clampScroll();
                return true;
            }
        }
        return false;
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

    private int[] nameRect(int rowY) {
        int w = 54, h = 18;
        int x = listX + listW - 12 - w;
        return new int[]{x, rowY + (ROW_H - h) / 2, w, h};
    }

    //? if >=26.1 {
    /*@Override
    public void extractBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }
    *///?} elif >=1.21 {
    /*@Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }
    *///?}

    //? if >=26.1 {
    /*@Override
    public void extractRenderState(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        ltDraw(ctx, mouseX, mouseY, delta);
        super.extractRenderState(ctx, mouseX, mouseY, delta);
    }
    *///?} else {
    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        ltDraw(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
    }
    //?}

    private void ltDraw(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, LTGui.DIM);
        LTGui.panel(ctx, panelX, panelY, panelW, panelH);

        LTGui.roundRect(ctx, panelX, panelY, panelW, 30, 6, LTGui.PANEL_HEAD);
        ctx.drawString(this.font, Component.literal("§lRegions"), panelX + 12, panelY + 11, LTGui.TEXT, false);
        ctx.fill(panelX + 10, panelY + 29, panelX + panelW - 10, panelY + 30, LTGui.ACCENT);

        int searchX = panelX + panelW - 12 - 16 - 6 - searchW;
        LTGui.field(ctx, searchX, panelY + 8, searchW, 16, searchField != null && searchField.isFocused());
        if (searchField != null && searchField.getValue().isEmpty()) {
            ctx.drawString(this.font, Component.literal("Search…"), searchX + 5, panelY + 11, LTGui.FAINT, false);
        }

        boolean closeHover = LTGui.hovered(mouseX, mouseY, panelX + panelW - 12 - 16, panelY + 8, 16, 16);
        LTGui.roundRect(ctx, panelX + panelW - 12 - 16, panelY + 8, 16, 16, 4, closeHover ? LTGui.DANGER_HOVER : LTGui.BTN);
        ctx.drawString(this.font, Component.literal("✕"), panelX + panelW - 12 - 16 + 5, panelY + 12, LTGui.TEXT, false);

        List<RegionRow> vis = visible();
        ctx.enableScissor(listX, listY, listX + listW, listY + listH);
        if (vis.isEmpty()) {
            String msg = regions.isEmpty() ? "No regions nearby" : "No matches";
            ctx.drawString(this.font, msg, listX + 8, listY + 8, LTGui.SUBTEXT, false);
        }
        for (int i = 0; i < vis.size(); i++) {
            int rowY = listY - scroll + i * ROW_H;
            if (rowY + ROW_H <= listY || rowY >= listY + listH) continue;
            renderRow(ctx, vis.get(i), rowY, mouseX, mouseY);
        }
        ctx.disableScissor();

        int max = Math.max(0, vis.size() * ROW_H - listH);
        if (max > 0) {
            int trackX = listX + listW - 4;
            LTGui.roundRect(ctx, trackX, listY, 4, listH, 2, 0x33000000);
            int thumbH = Math.max(20, (int) ((long) listH * listH / (vis.size() * ROW_H)));
            int thumbY = listY + (int) ((long) (listH - thumbH) * scroll / max);
            LTGui.roundRect(ctx, trackX, thumbY, 4, thumbH, 2, LTGui.ACCENT_DIM);
        }

        ctx.drawString(this.font,
                Component.literal("§7" + vis.size() + (vis.size() == 1 ? " region" : " regions")),
                panelX + 12, panelY + panelH - 16, LTGui.SUBTEXT, false);
    }

    private void renderRow(GuiGraphics ctx, RegionRow r, int rowY, int mouseX, int mouseY) {
        boolean hover = LTGui.hovered(mouseX, mouseY, listX, rowY, listW, ROW_H)
                && mouseY >= listY && mouseY < listY + listH;
        LTGui.roundRect(ctx, listX, rowY + 1, listW, ROW_H - 2, 4, hover ? LTGui.ROW_HOVER : LTGui.ROW_ALT);

        int dotColor = r.isStructure() ? 0xFF40C4D4 : 0xFFE0A53C;
        ctx.fill(listX + 4, rowY + 6, listX + 6, rowY + ROW_H - 6, dotColor);

        ctx.drawString(this.font, r.name, listX + 12, rowY + 6, LTGui.TEXT, false);

        String sub = "§7" + r.dim.getPath();
        if (r.ownerName != null && !r.ownerName.isEmpty()) sub += " §8• §7" + r.ownerName;
        ctx.drawString(this.font, sub, listX + 12, rowY + 18, LTGui.SUBTEXT, false);

        if (r.nameable) {
            ctx.drawString(this.font, "§8you are standing here — name it", listX + 12, rowY + 30, 0xFF6A7079, false);
            int[] nameBtn = nameRect(rowY);
            LTGui.button(ctx, this.font, nameBtn[0], nameBtn[1], nameBtn[2], nameBtn[3], "Name",
                    LTGui.hovered(mouseX, mouseY, nameBtn[0], nameBtn[1], nameBtn[2], nameBtn[3])
                            && mouseY >= listY && mouseY < listY + listH,
                    LTGui.OK, LTGui.OK_HOVER);
            return;
        }

        int fx = listX + 12;
        int fyMax = listX + listW - 12 - 54 - 6 - 46 - 8;
        if (r.flags != null && !r.flags.isEmpty()) {
            for (java.util.Map.Entry<String, Boolean> e : r.flags.entrySet()) {
                if (fx > fyMax) { ctx.drawString(this.font, "…", fx, rowY + 30, LTGui.FAINT, false); break; }
                boolean allow = e.getValue();
                if (FlagIcons.draw(ctx, e.getKey(), fx, rowY + 28, 10)) {
                    ctx.fill(fx, rowY + 38, fx + 10, rowY + 39, allow ? 0xFF55FF55 : 0xFFFF5555);
                    fx += 13;
                } else {
                    ctx.drawString(this.font, (allow ? "§a" : "§c") + e.getKey(), fx, rowY + 30, LTGui.TEXT, false);
                    fx += this.font.width(e.getKey()) + 8;
                }
            }
        } else {
            ctx.drawString(this.font, "§8default", listX + 12, rowY + 30, 0xFF6A7079, false);
        }

        int[] edit = editRect(rowY), del = deleteRect(rowY);
        LTGui.button(ctx, this.font, edit[0], edit[1], edit[2], edit[3], "Edit",
                LTGui.hovered(mouseX, mouseY, edit[0], edit[1], edit[2], edit[3]) && mouseY >= listY && mouseY < listY + listH,
                LTGui.BTN, LTGui.BTN_HOVER);
        LTGui.button(ctx, this.font, del[0], del[1], del[2], del[3], "Delete",
                LTGui.hovered(mouseX, mouseY, del[0], del[1], del[2], del[3]) && mouseY >= listY && mouseY < listY + listH,
                LTGui.DANGER, LTGui.DANGER_HOVER);
    }

    @Override public boolean shouldCloseOnEsc() { return true; }
    @Override public void onClose() { Minecraft.getInstance().setScreen(null); }

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
            onClose();
        }

        @Override public void onClose() { Minecraft.getInstance().setScreen(returnTo); }
    }

    public static class NameVillageScreen extends RegionConfigScreen {
        private final RegionRow row;
        private final Screen returnTo;

        public NameVillageScreen(RegionRow row, Screen returnTo) {
            super("Name Village");
            this.row = row;
            this.returnTo = returnTo;
        }

        @Override protected String headerTitle() { return "Name This Village"; }
        @Override protected String confirmLabel() { return "Name"; }
        @Override protected String initialName() { return row.name; }
        @Override protected java.util.Map<String, Boolean> initialFlags() { return java.util.Map.of(); }
        @Override protected boolean showFlags() { return false; }

        @Override
        protected void onConfirm(String name, java.util.Map<String, Boolean> newFlags) {
            row.name = name;
            LTPacketsClient.sendPlayerName(row.id, name);
            onClose();
        }

        @Override public void onClose() { Minecraft.getInstance().setScreen(returnTo); }
    }
}
