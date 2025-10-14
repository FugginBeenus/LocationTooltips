package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.data.Region;
import com.fugginbeenus.locationtooltip.data.RegionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.text.Text;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RegionAdminScreen extends Screen {

    private TextFieldWidget searchField;
    private final List<Region> all = new ArrayList<>(RegionManager.allMutable());
    private List<Region> filtered = new ArrayList<>(all);
    private int scroll = 0;

    private static final int ROW_H = 22;
    private static final int LIST_W = 340;

    public RegionAdminScreen() { super(Text.literal("Location Tooltip – Regions")); }

    @Override
    protected void init() {
        int cx = this.width / 2;

        searchField = new TextFieldWidget(this.textRenderer, cx - LIST_W / 2, 30, LIST_W, 20, Text.literal("Search"));
        searchField.setChangedListener(s -> applyFilter());
        addDrawableChild(searchField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(cx + LIST_W / 2 - 80, this.height - 30, 70, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("▲"), b -> { scroll = Math.max(0, scroll - 1); })
                .dimensions(cx + LIST_W / 2 + 8, 60, 20, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("▼"), b -> {
            int visible = (this.height - 110) / ROW_H;
            scroll = Math.max(0, Math.min(scroll + 1, Math.max(0, filtered.size() - visible)));
        }).dimensions(cx + LIST_W / 2 + 8, 82, 20, 20).build());
    }

    private void applyFilter() {
        String q = searchField.getText().toLowerCase(Locale.ROOT).trim();
        filtered.clear();
        for (Region r : all) {
            if (q.isEmpty()
                    || (r.name() != null && r.name().toLowerCase(Locale.ROOT).contains(q))
                    || (r.world() != null && r.world().toLowerCase(Locale.ROOT).contains(q))
                    || (r.id() != null && r.id().toLowerCase(Locale.ROOT).contains(q))) {
                filtered.add(r);
            }
        }
        scroll = 0;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx);
        int cx = this.width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer, "Regions", cx, 12, 0xFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);

        int x = cx - LIST_W / 2;
        int y = 60;
        int h = this.height - 120;
        ctx.fill(x - 4, y - 4, x + LIST_W + 4, y + h + 4, 0x88000000);

        int visible = h / ROW_H;
        for (int i = 0; i < visible && (i + scroll) < filtered.size(); i++) {
            Region r = filtered.get(i + scroll);
            int ry = y + i * ROW_H;

            ctx.fill(x, ry, x + LIST_W, ry + ROW_H - 1, (i % 2 == 0) ? 0x22000000 : 0x33000000);

            String line = (r.name() != null ? r.name() : "(unnamed)")
                    + " §7[p=" + r.priority() + "] §8" + (r.world() != null ? r.world() : "?");
            ctx.drawTextWithShadow(textRenderer, line, x + 6, ry + 7, 0xFFFFFF);

            drawButtons(ctx, r, ry);
        }
    }

    private void drawButtons(DrawContext ctx, Region r, int ry) {
        int cx = this.width / 2;
        int x = cx - LIST_W / 2;
        int bx = x + LIST_W - 200;

        boolean canAdmin = RegionManager.canAdmin();

        ButtonWidget highlight = ButtonWidget.builder(Text.literal("Highlight"), b -> highlight(r))
                .dimensions(bx, ry + 3, 70, 16).build();
        bx += 74;

        ButtonWidget del = ButtonWidget.builder(Text.literal("Delete"), b -> delete(r))
                .dimensions(bx, ry + 3, 60, 16).build();
        bx += 64;

        ButtonWidget tp = ButtonWidget.builder(Text.literal("TP"), b -> teleport(r))
                .dimensions(bx, ry + 3, 40, 16).build();

        drawMiniButton(ctx, highlight, 0xFF222244);
        drawMiniButton(ctx, canAdmin ? del : disabled(del), canAdmin ? 0xFF442222 : 0x55222222);
        drawMiniButton(ctx, canAdmin ? tp  : disabled(tp),  canAdmin ? 0xFF224422 : 0x55222222);
    }

    private ButtonWidget disabled(ButtonWidget b) { return b; }

    private void drawMiniButton(DrawContext ctx, ButtonWidget b, int color) {
        ctx.fill(b.getX(), b.getY(), b.getX() + b.getWidth(), b.getY() + b.getHeight(), color);
        ctx.drawCenteredTextWithShadow(textRenderer, b.getMessage(), b.getX() + b.getWidth() / 2, b.getY() + 4, 0xFFFFFF);
    }

    /** Public so /lt highlight can call it. */
    public static void highlight(Region r) {
        var mc = MinecraftClient.getInstance();
        if (mc.world == null || r == null || r.bounds() == null || !r.bounds().valid()) return;

        new Thread(() -> {
            var color = new Vector3f(0.7f, 0.85f, 1.0f);
            var fx = new DustParticleEffect(color, 1.25f);
            double minX = r.bounds().minX(), minY = r.bounds().minY(), minZ = r.bounds().minZ();
            double maxX = r.bounds().maxX(), maxY = r.bounds().maxY(), maxZ = r.bounds().maxZ();
            long end = System.currentTimeMillis() + 3000;

            while (System.currentTimeMillis() < end) {
                if (mc.world == null) break;
                for (double x = minX; x <= maxX; x += 0.5) {
                    mc.world.addParticle(fx, x, minY, minZ, 0, 0, 0);
                    mc.world.addParticle(fx, x, minY, maxZ, 0, 0, 0);
                    mc.world.addParticle(fx, x, maxY, minZ, 0, 0, 0);
                    mc.world.addParticle(fx, x, maxY, maxZ, 0, 0, 0);
                }
                for (double z = minZ; z <= maxZ; z += 0.5) {
                    mc.world.addParticle(fx, minX, minY, z, 0, 0, 0);
                    mc.world.addParticle(fx, maxX, minY, z, 0, 0, 0);
                    mc.world.addParticle(fx, minX, maxY, z, 0, 0, 0);
                    mc.world.addParticle(fx, maxX, maxY, z, 0, 0, 0);
                }
                for (double y = minY; y <= maxY; y += 0.5) {
                    mc.world.addParticle(fx, minX, y, minZ, 0, 0, 0);
                    mc.world.addParticle(fx, maxX, y, minZ, 0, 0, 0);
                    mc.world.addParticle(fx, minX, y, maxZ, 0, 0, 0);
                    mc.world.addParticle(fx, maxX, y, maxZ, 0, 0, 0);
                }
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void delete(Region r) {
        if (!RegionManager.canAdmin()) {
            RegionManager.notifyPlayer("§cYou don't have permission to delete regions.");
            return;
        }
        if (r == null) return;
        boolean ok = RegionManager.deleteById(r.id());
        if (ok) {
            RegionManager.notifyPlayer("§eDeleted region: §f" + (r.name() != null ? r.name() : r.id()));
            refresh();
        }
    }

    private void teleport(Region r) {
        var mc = MinecraftClient.getInstance();
        if (!RegionManager.canAdmin()) {
            RegionManager.notifyPlayer("§cYou don't have permission to teleport.");
            return;
        }
        if (mc.player == null || mc.world == null) return;

        double cx = (r.bounds().minX() + r.bounds().maxX()) / 2.0;
        double cz = (r.bounds().minZ() + r.bounds().maxZ()) / 2.0;
        double y  = r.bounds().maxY() + 1.0;
        mc.player.updatePosition(cx, y, cz);
        RegionManager.notifyPlayer("§bTeleported to region: §f" + (r.name() != null ? r.name() : r.id()));
    }

    private void refresh() {
        all.clear();
        all.addAll(RegionManager.allMutable());
        applyFilter();
    }
}
