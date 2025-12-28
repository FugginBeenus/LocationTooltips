package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.net.client.LTPacketsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Vanilla-looking admin panel:
 * - scrolling list
 * - instant local row removal on delete
 * - periodic refresh while open
 */
public class AdminPanelScreen extends Screen {

    // ===== data model =====
    public static class RegionRow {
        public final String id;
        public String name;
        public final Identifier dim;
        public final BlockPos a, b;
        public boolean allowPvP;
        public boolean allowMobSpawning;

        public RegionRow(String id, String name, Identifier dim, BlockPos a, BlockPos b, boolean allowPvP, boolean allowMobSpawning) {
            this.id = id;
            this.name = name;
            this.dim = dim;
            this.a = a;
            this.b = b;
            this.allowPvP = allowPvP;
            this.allowMobSpawning = allowMobSpawning;
        }
    }

    private static AdminPanelScreen instance;
    private final List<RegionRow> regions = new ArrayList<>();

    // ===== UI chrome =====
    private TextFieldWidget radiusField;
    private ButtonWidget refreshBtn, closeBtn;

    // scrolling
    private static final int ROW_H = 24;
    private int listX, listY, listW, listH;
    private int scroll;                // pixel scroll offset
    private final List<ButtonWidget> rowButtons = new ArrayList<>();
    private int lastFirst = -1, lastCount = -1; // avoid rebuilding buttons every frame

    // periodic refresh
    private int ticks; // 20 ticks ~ 1s

    public AdminPanelScreen() {
        super(Text.literal("Admin Control Panel"));
        instance = this;
    }

    /** Called from LTPacketsClient when new data arrives. */
    public static void receiveList(RegionRow[] rows) {
        if (instance != null) {
            instance.regions.clear();
            for (RegionRow r : rows) instance.regions.add(r);
            instance.rebuildRowButtonsIfNeeded(true);
        }
    }

    @Override
    protected void init() {
        final int panelW = 360;
        final int x = (this.width - panelW) / 2;
        final int y = 40;

        // header controls
        radiusField = new TextFieldWidget(this.textRenderer, x + 20, y - 25, 80, 20, Text.literal("Radius"));
        radiusField.setText("256");
        addDrawableChild(radiusField);

        refreshBtn = ButtonWidget.builder(Text.literal("Refresh"), b -> {
            int r = 256;
            try { r = Integer.parseInt(radiusField.getText().trim()); } catch (Exception ignored) {}
            LTPacketsClient.requestAdminList(r);
        }).dimensions(x + 110, y - 25, 80, 20).build();
        addDrawableChild(refreshBtn);

        closeBtn = ButtonWidget.builder(Text.literal("Close"), b -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) mc.setScreen(null);
        }).dimensions(x + 200, y - 25, 80, 20).build();
        addDrawableChild(closeBtn);

        // list viewport
        listX = x + 8;
        listY = y;
        listW = panelW - 16;
        listH = this.height - y - 30;

        scroll = 0;
        clearRowButtons();
        rebuildRowButtonsIfNeeded(true);
    }

    @Override
    public void tick() {
        super.tick();
        // OPTIMIZATION: Reduced from 20 (1s) to 100 (5s) to reduce network traffic
        if (++ticks % 100 == 0) { // every ~5s
            int r = 256;
            try { r = Integer.parseInt(radiusField.getText().trim()); } catch (Exception ignored) {}
            LTPacketsClient.requestAdminList(r);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + listH) {
            int maxContentH = regions.size() * ROW_H;
            int maxScroll = Math.max(0, maxContentH - listH);
            scroll -= (int) Math.signum(amount) * 24; // one row per wheel step
            if (scroll < 0) scroll = 0;
            if (scroll > maxScroll) scroll = maxScroll;
            rebuildRowButtonsIfNeeded(false);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // dim backdrop
        ctx.fill(0, 0, this.width, this.height, 0xA0000000);

        // list frame
        ctx.fill(listX - 2, listY - 2, listX + listW + 2, listY + listH + 2, 0x60000000);

        var matrices = ctx.getMatrices();
        matrices.push();
        ctx.enableScissor(listX, listY, listX + listW, listY + listH);

        int first = Math.max(0, scroll / ROW_H);
        int y0 = listY - (scroll % ROW_H);

        for (int i = first, row = 0; i < regions.size(); i++, row++) {
            int entryY = y0 + row * ROW_H;
            if (entryY > listY + listH) break;

            RegionRow rr = regions.get(i);
            String label = rr.name + " §7(" + rr.dim.getPath() + ")";
            ctx.drawText(this.textRenderer, label, listX + 4, entryY + 6, 0xFFFFFF, false);
        }

        ctx.disableScissor();
        matrices.pop();

        super.render(ctx, mouseX, mouseY, delta);
    }

    // ===== row button lifecycle =====
    private void clearRowButtons() {
        for (var b : rowButtons) remove(b);
        rowButtons.clear();
        lastFirst = -1; lastCount = -1;
    }

    private void rebuildRowButtonsIfNeeded(boolean force) {
        int first = Math.max(0, scroll / ROW_H);
        int maxRows = Math.max(1, listH / ROW_H);
        int count = Math.min(maxRows + 1, Math.max(0, regions.size() - first)); // +1 buffer

        if (!force && first == lastFirst && count == lastCount) return;

        clearRowButtons();

        for (int idx = 0; idx < count; idx++) {
            int modelIndex = first + idx;
            if (modelIndex >= regions.size()) break;

            int y = listY - (scroll % ROW_H) + idx * ROW_H;
            int renameX = listX + listW - 160;
            int deleteX = listX + listW - 80;

            RegionRow rr = regions.get(modelIndex);

            ButtonWidget renameBtn = ButtonWidget.builder(Text.literal("Rename"), b -> {
                try {
                    MinecraftClient.getInstance().setScreen(new RenameRegionScreen(rr, this));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }).dimensions(renameX, y + 2, 72, 20).build();

            ButtonWidget deleteBtn = ButtonWidget.builder(Text.literal("Delete"), b -> {
                int removeAt = regions.indexOf(rr);
                if (removeAt >= 0) {
                    regions.remove(removeAt);
                    int maxScroll = Math.max(0, regions.size() * ROW_H - listH);
                    if (scroll > maxScroll) scroll = maxScroll;
                    rebuildRowButtonsIfNeeded(true);
                }
                LTPacketsClient.sendAdminDelete(rr.id);
            }).dimensions(deleteX, y + 2, 72, 20).build();

            rowButtons.add(renameBtn);
            rowButtons.add(deleteBtn);
            addDrawableChild(renameBtn);
            addDrawableChild(deleteBtn);
        }

        lastFirst = first;
        lastCount = count;
    }

    // ===== rename subscreen =====
    public static class RenameRegionScreen extends Screen {
        private final RegionRow row;
        private final Screen returnTo;
        private TextFieldWidget field;
        private ButtonWidget saveBtn, cancelBtn;
        private CheckboxWidget pvpCheckbox, mobSpawnCheckbox;

        // Store checkbox positions for custom text rendering
        private int pvpCheckboxX, pvpCheckboxY;
        private int mobCheckboxX, mobCheckboxY;

        public RenameRegionScreen(RegionRow row, Screen returnTo) {
            super(Text.literal("Rename Region"));
            this.row = row;
            this.returnTo = returnTo;
        }

        @Override
        protected void init() {
            int w = 240;
            int x = (this.width - w) / 2;
            int y = this.height / 2 - 50;  // Move up to make room for checkboxes

            field = new TextFieldWidget(this.textRenderer, x, y, w, 20, Text.literal("Name"));
            field.setText(row.name);
            addDrawableChild(field);

            // Add checkboxes below name field
            int checkboxY = y + 35;
            pvpCheckboxX = x;
            pvpCheckboxY = checkboxY;
            mobCheckboxX = x;
            mobCheckboxY = checkboxY + 24;

            pvpCheckbox = new CheckboxWidget(
                    x, checkboxY,
                    200, 20,
                    Text.literal(""),  // Empty - we'll draw custom white text
                    row.allowPvP
            );
            addDrawableChild(pvpCheckbox);

            mobSpawnCheckbox = new CheckboxWidget(
                    x, checkboxY + 24,
                    200, 20,
                    Text.literal(""),  // Empty - we'll draw custom white text
                    row.allowMobSpawning
            );
            addDrawableChild(mobSpawnCheckbox);

            saveBtn = ButtonWidget.builder(Text.literal("Save"), b -> {
                String newName = field.getText().trim();
                if (!newName.isEmpty()) {
                    row.name = newName; // reflect locally
                    row.allowPvP = pvpCheckbox.isChecked();
                    row.allowMobSpawning = mobSpawnCheckbox.isChecked();
                    LTPacketsClient.sendAdminRename(row.id, newName, row.allowPvP, row.allowMobSpawning);
                    MinecraftClient.getInstance().setScreen(returnTo);
                }
            }).dimensions(x, y + 90, 110, 20).build();  // Move buttons down

            cancelBtn = ButtonWidget.builder(Text.literal("Cancel"), b -> {
                MinecraftClient.getInstance().setScreen(returnTo);
            }).dimensions(x + 130, y + 90, 110, 20).build();

            addDrawableChild(saveBtn);
            addDrawableChild(cancelBtn);
        }

        @Override public boolean shouldCloseOnEsc() { return true; }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
            ctx.fill(0, 0, this.width, this.height, 0xB0000000);
            super.render(ctx, mouseX, mouseY, delta);

            String title = "Edit Region";
            int tw = this.textRenderer.getWidth(title);
            ctx.drawText(this.textRenderer, title, (this.width - tw) / 2, this.height / 2 - 75, 0xFFFFFF, false);

            // Draw "Settings:" label
            ctx.drawText(this.textRenderer, Text.literal("Settings:"),
                    (this.width - 240) / 2, this.height / 2 - 23, 0xA0A0A0, false);

            field.render(ctx, mouseX, mouseY, delta);

            // Draw white checkbox text
            if (pvpCheckbox != null) {
                ctx.drawTextWithShadow(this.textRenderer, Text.literal("Allow PvP"),
                        pvpCheckboxX + 24, pvpCheckboxY + 6, 0xFFFFFF);
            }
            if (mobSpawnCheckbox != null) {
                ctx.drawTextWithShadow(this.textRenderer, Text.literal("Allow Mob Spawning"),
                        mobCheckboxX + 24, mobCheckboxY + 6, 0xFFFFFF);
            }
        }
    }
}