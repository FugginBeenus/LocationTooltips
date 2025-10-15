package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.data.Bounds;
import com.fugginbeenus.locationtooltip.data.Region;
import com.fugginbeenus.locationtooltip.data.RegionManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public class RegionAdminScreen extends Screen {

    private TextFieldWidget search;
    private List<Region> all = new ArrayList<>(RegionManager.get());
    private List<Region> filtered = new ArrayList<>(all);
    private int selected = -1;

    public RegionAdminScreen() { super(Text.literal("Region Admin")); }

    @Override
    protected void init() {
        int w = 260, x = (this.width - w) / 2, y = this.height / 2 - 80;

        search = new TextFieldWidget(textRenderer, x, y, w, 20, Text.literal("Search"));
        search.setChangedListener(s -> refilter());
        addDrawableChild(search);

        addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), b -> {
            Region r = getSelected();
            var mc = MinecraftClient.getInstance();
            if (r == null) return;
            boolean ok = RegionManager.deleteRegion(r, mc.player.getUuid());
            mc.player.sendMessage(Text.literal(ok ? "§eDeleted region." : "§cNo permission to delete this region."), false);
            all = new ArrayList<>(RegionManager.get());
            refilter(); // re-run filtering after data changes
        }).dimensions(x, y + 26, 80, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Teleport"), b -> {
            Region r = getSelected();
            var mc = MinecraftClient.getInstance();
            if (mc == null || mc.player == null || mc.world == null || r == null || r.bounds() == null) return;
            Bounds bnd = r.bounds();
            double cx = (bnd.minX + bnd.maxX + 1) / 2.0;
            double cz = (bnd.minZ + bnd.maxZ + 1) / 2.0;
            double y0 = bnd.maxY + 1.0;
            mc.player.requestTeleport(cx + 0.5, y0, cz + 0.5);
            mc.player.sendMessage(Text.literal("§bTeleported to region center."), false);
        }).dimensions(x + 90, y + 26, 80, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(x + 180, y + 26, 80, 20).build());
    }

    private void refilter() {
        String q = search.getText().toLowerCase(Locale.ROOT).trim();
        filtered.clear();
        if (q.isEmpty()) {
            filtered.addAll(all);
        } else {
            for (Region r : all) {
                if ((r.name() != null && r.name().toLowerCase(Locale.ROOT).contains(q))
                        || (r.id()   != null && r.id().toLowerCase(Locale.ROOT).contains(q))
                        || (r.worldKey() != null && r.worldKey().toLowerCase(Locale.ROOT).contains(q))) {
                    filtered.add(r);
                }
            }
        }
        selected = Math.min(selected, filtered.size() - 1);
    }

    private Region getSelected() {
        return (selected < 0 || selected >= filtered.size()) ? null : filtered.get(selected);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = (this.width - 260) / 2, listY = this.height / 2 - 80 + 54, rowH = 12, rows = Math.min(12, filtered.size());
        if (mouseX >= listX && mouseX <= listX + 260 && mouseY >= listY && mouseY <= listY + rows * rowH) {
            int idx = (int)((mouseY - listY) / rowH);
            if (idx >= 0 && idx < filtered.size()) { selected = idx; return true; }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);

        int listX = (this.width - 260) / 2, listY = this.height / 2 - 80 + 54;
        ctx.drawCenteredTextWithShadow(textRenderer, "Regions", this.width / 2, listY - 16, 0xFFFFFF);

        int rowY = listY;
        for (int i = 0; i < Math.min(12, filtered.size()); i++) {
            Region r = filtered.get(i);
            String label = (r.name() != null ? r.name() : r.id());
            String world = (r.worldKey() != null ? r.worldKey() : "?");
            String line = "§f" + label + " §8[" + world + "]";
            int color = (i == selected) ? 0xFFFFAA : 0xFFFFFF;
            ctx.drawTextWithShadow(textRenderer, line, listX, rowY, color);
            rowY += 12;
        }
    }

    @Override public boolean shouldPause() { return false; }
}
