package com.fugginbeenus.locationtooltip.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

/**
 * Shared base for the create + edit region cards: a sleek centered panel with a name field,
 * a tri-state flag grid, and confirm/cancel buttons. The card is clamped to the window height
 * and the flag grid scrolls inside it, so it never spills off-screen at high GUI scales.
 */
public abstract class RegionConfigScreen extends Screen {
    protected static final int PAD = 14;

    protected FlagEditor flags;
    protected EditBox field;

    private int panelX, panelY, panelW, panelH, innerW, colW;
    private int nameY, labelY, gridTop, gridViewH, btnY;
    private int gridContentH, gridScroll;

    protected RegionConfigScreen(String title) {
        super(Component.literal(title));
    }

    protected abstract String headerTitle();
    protected abstract String confirmLabel();
    protected abstract String initialName();
    protected abstract Map<String, Boolean> initialFlags();
    protected abstract void onConfirm(String name, Map<String, Boolean> flags);

    @Override
    protected void init() {
        panelW = 340;
        innerW = panelW - PAD * 2;
        colW = (innerW - 4) / 2;

        // preserve flag edits + typed name across re-init (e.g. window resize)
        flags = (flags == null) ? new FlagEditor(initialFlags()) : new FlagEditor(flags.overrides());
        gridContentH = flags.height();

        final int headerH = 30, nameH = 18, gapName = 8, labelH = 10, gapLabel = 4, gapGrid = 10, btnH = 20, bottomPad = 12;
        int fixed = headerH + nameH + gapName + labelH + gapLabel + gapGrid + btnH + bottomPad;
        int availH = this.height - 12;
        int gridViewMax = Math.max(40, availH - fixed);
        gridViewH = Math.min(gridContentH, gridViewMax);

        panelH = fixed + gridViewH;
        panelX = (this.width - panelW) / 2;
        panelY = Math.max(6, (this.height - panelH) / 2);

        nameY = panelY + headerH;
        labelY = nameY + nameH + gapName;
        gridTop = labelY + labelH + gapLabel;
        btnY = gridTop + gridViewH + gapGrid;
        gridScroll = Math.min(gridScroll, maxScroll());

        String existing = (field != null) ? field.getValue() : initialName();
        field = new EditBox(this.font, panelX + PAD + 4, nameY + 5, innerW - 8, 12, Component.literal("Name"));
        field.setMaxLength(48);
        field.setBordered(false);
        field.setValue(existing);
        addRenderableWidget(field);
        setInitialFocus(field);
    }

    private int maxScroll() {
        return Math.max(0, gridContentH - gridViewH);
    }

    private void confirm() {
        String name = field.getValue().trim();
        if (name.isEmpty()) return;
        onConfirm(name, flags.overrides());
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
        if (maxScroll() > 0 && LTGui.hovered(mx, my, panelX + PAD, gridTop, innerW, gridViewH)) {
            gridScroll -= (int) Math.signum(amount) * 16;
            gridScroll = Math.max(0, Math.min(maxScroll(), gridScroll));
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
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        return ltKey(event.key()) || super.keyPressed(event);
    }
    *///?} else {
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        return ltClick(mx, my, button) || super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return ltKey(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }
    //?}

    private boolean ltClick(double mx, double my, int button) {
        if (button != 0) return false;
        if (LTGui.hovered(mx, my, panelX + PAD, btnY, colW, 20)) { confirm(); return true; }
        if (LTGui.hovered(mx, my, panelX + PAD + colW + 4, btnY, colW, 20)) { onClose(); return true; }
        if (LTGui.hovered(mx, my, panelX + PAD, gridTop, innerW, gridViewH)) {
            flags.layout(panelX + PAD, gridTop - gridScroll, colW, 18, 4);
            if (flags.mouseClicked(mx, my)) return true;
        }
        return false;
    }

    private boolean ltKey(int keyCode) {
        if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) return false;
        if (field == null || !field.isFocused()) return false;
        confirm();
        return true;
    }

    @Override public boolean shouldCloseOnEsc() { return true; }

    // Skip 1.21's default screen blur; render() draws our own dim.
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
        super.render(ctx, mouseX, mouseY, delta);   // widgets (the text field)
    }
    //?}

    private void ltDraw(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, LTGui.DIM);
        LTGui.panel(ctx, panelX, panelY, panelW, panelH);

        ctx.drawString(this.font, Component.literal(headerTitle()), panelX + PAD, panelY + 10, LTGui.TEXT, false);
        ctx.fill(panelX + PAD, panelY + 24, panelX + panelW - PAD, panelY + 25, LTGui.ACCENT);

        LTGui.field(ctx, panelX + PAD, nameY, innerW, 18, field != null && field.isFocused());
        if (field != null && field.getValue().isEmpty()) {
            ctx.drawString(this.font, Component.literal("Region name…"), panelX + PAD + 5, nameY + 5, LTGui.FAINT, false);
        }

        ctx.drawString(this.font, Component.literal("Protection — click to cycle Inherit / Allow / Deny"),
                panelX + PAD, labelY, LTGui.SUBTEXT, false);

        // flag grid (scrolled + clipped)
        ctx.enableScissor(panelX + PAD, gridTop, panelX + panelW - PAD, gridTop + gridViewH);
        flags.layout(panelX + PAD, gridTop - gridScroll, colW, 18, 4);
        flags.render(ctx, this.font, mouseX, mouseY);
        ctx.disableScissor();

        if (maxScroll() > 0) {
            int tx = panelX + panelW - PAD - 3;
            LTGui.roundRect(ctx, tx, gridTop, 3, gridViewH, 1, 0x33000000);
            int thumbH = Math.max(16, (int) ((long) gridViewH * gridViewH / gridContentH));
            int thumbY = gridTop + (int) ((long) (gridViewH - thumbH) * gridScroll / maxScroll());
            LTGui.roundRect(ctx, tx, thumbY, 3, thumbH, 1, LTGui.ACCENT_DIM);
        }

        LTGui.button(ctx, this.font, panelX + PAD, btnY, colW, 20, confirmLabel(),
                LTGui.hovered(mouseX, mouseY, panelX + PAD, btnY, colW, 20), LTGui.OK, LTGui.OK_HOVER);
        LTGui.button(ctx, this.font, panelX + PAD + colW + 4, btnY, colW, 20, "Cancel",
                LTGui.hovered(mouseX, mouseY, panelX + PAD + colW + 4, btnY, colW, 20));

    }
}
