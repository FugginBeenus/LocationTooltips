package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.net.client.LTPacketsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin panel listing all nearby regions, allowing rename or delete.
 */
public class AdminPanelScreen extends Screen {

    private static AdminPanelScreen instance;
    private final List<RegionRow> regions = new ArrayList<>();
    private TextFieldWidget radiusField;
    private ButtonWidget refreshBtn, closeBtn;

    public AdminPanelScreen() {
        super(Text.literal("Admin Control Panel"));
        instance = this;
    }

    public static class RegionRow {
        public final String id;
        public String name;
        public final Identifier dim;
        public final BlockPos a;
        public final BlockPos b;

        public RegionRow(String id, String name, Identifier dim, BlockPos a, BlockPos b) {
            this.id = id;
            this.name = name;
            this.dim = dim;
            this.a = a;
            this.b = b;
        }
    }

    // Called from LTPacketsClient when new data arrives
    public static void receiveList(RegionRow[] rows) {
        if (instance != null) {
            instance.regions.clear();
            for (RegionRow r : rows) instance.regions.add(r);
        }
    }

    @Override
    protected void init() {
        int panelW = 340;
        int x = (this.width - panelW) / 2;
        int y = 40;

        radiusField = new TextFieldWidget(this.textRenderer, x + 20, y - 25, 80, 20, Text.literal("Radius"));
        radiusField.setText("256");
        this.addDrawableChild(radiusField);

        refreshBtn = ButtonWidget.builder(Text.literal("Refresh"), b -> {
            int r = 256;
            try { r = Integer.parseInt(radiusField.getText().trim()); } catch (Exception ignored) {}
            LTPacketsClient.requestAdminList(r);
        }).dimensions(x + 110, y - 25, 80, 20).build();
        this.addDrawableChild(refreshBtn);

        closeBtn = ButtonWidget.builder(Text.literal("Close"), b -> closeScreen())
                .dimensions(x + 200, y - 25, 80, 20).build();
        this.addDrawableChild(closeBtn);
    }

    private void closeScreen() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) mc.setScreen(null);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // translucent background
        ctx.fill(0, 0, this.width, this.height, 0xA0000000);

        // header
        String title = "Nearby Regions";
        int tw = this.textRenderer.getWidth(title);
        ctx.drawText(this.textRenderer, title, (this.width - tw) / 2, 10, 0xFFFFFF, false);

        // region list
        int x = 40;
        int y = 50;
        int i = 0;

        for (RegionRow row : regions) {
            int entryY = y + (i * 25);
            String label = row.name + " §7(" + row.dim.getPath() + ")";
            ctx.drawText(this.textRenderer, label, x, entryY, 0xFFFFFF, false);

            ButtonWidget renameBtn = ButtonWidget.builder(Text.literal("Rename"), b -> {
                MinecraftClient.getInstance().setScreen(new RenameRegionScreen(row));
            }).dimensions(x + 180, entryY - 4, 60, 20).build();

            ButtonWidget deleteBtn = ButtonWidget.builder(Text.literal("Delete"), b -> {
                LTPacketsClient.sendAdminDelete(row.id);
            }).dimensions(x + 245, entryY - 4, 60, 20).build();

            this.addDrawableChild(renameBtn);
            this.addDrawableChild(deleteBtn);
            i++;
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    /** Subscreen for renaming a region */
    public static class RenameRegionScreen extends Screen {
        private final RegionRow row;
        private TextFieldWidget field;
        private ButtonWidget saveBtn, cancelBtn;

        protected RenameRegionScreen(RegionRow row) {
            super(Text.literal("Rename Region"));
            this.row = row;
        }

        @Override
        protected void init() {
            int w = 240;
            int x = (this.width - w) / 2;
            int y = this.height / 2 - 20;

            field = new TextFieldWidget(this.textRenderer, x, y, w, 20, Text.literal("Name"));
            field.setText(row.name);
            this.addDrawableChild(field);

            saveBtn = ButtonWidget.builder(Text.literal("Save"), b -> {
                String newName = field.getText().trim();
                if (!newName.isEmpty()) {
                    LTPacketsClient.sendAdminRename(row.id, newName);
                    MinecraftClient.getInstance().setScreen(new AdminPanelScreen());
                }
            }).dimensions(x, y + 30, 110, 20).build();

            cancelBtn = ButtonWidget.builder(Text.literal("Cancel"), b -> {
                MinecraftClient.getInstance().setScreen(new AdminPanelScreen());
            }).dimensions(x + 130, y + 30, 110, 20).build();

            this.addDrawableChild(saveBtn);
            this.addDrawableChild(cancelBtn);
        }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
            ctx.fill(0, 0, this.width, this.height, 0xB0000000);
            super.render(ctx, mouseX, mouseY, delta);
            String title = "Rename Region";
            int tw = this.textRenderer.getWidth(title);
            ctx.drawText(this.textRenderer, title, (this.width - tw) / 2, this.height / 2 - 45, 0xFFFFFF, false);
            field.render(ctx, mouseX, mouseY, delta);
        }
    }
}
