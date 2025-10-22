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
import org.lwjgl.glfw.GLFW;

public class NameRegionScreen extends Screen {
    private static final Identifier TEX = new Identifier("locationtooltip", "textures/gui/name_panel.png");

    // Panel draws at native size for crisp edges
    private static final int PANEL_W = 256;
    private static final int PANEL_H = 256;
    private static final int PANEL_NUDGE_X = 0;
    private static final int PANEL_NUDGE_Y = 25;

    // Input bar geometry (relative to texture)
    private static final int INPUT_X = 20;
    private static final int INPUT_Y = 75;
    private static final int INPUT_W = 216; // give a tad more room
    private static final int INPUT_H = 18;

    // Buttons lane (relative to texture)
    private static final int BTN_W = 85;
    private static final int BTN_H = 20;
    private static final int BTN_Y = 130;

    // Horizontal placement controls
    private static final boolean CENTER_BUTTONS = false;
    private static final int CENTER_GAP = 20;
    private static final int BTN_LEFT_X = 20;
    private static final int BTN_RIGHT_X = PANEL_W - 20 - BTN_W;
    private static final int BTN_LEFT_NUDGE_X = -5;
    private static final int BTN_RIGHT_NUDGE_X = -3;

    private final BlockPos a;
    private final BlockPos b;

    private TextFieldWidget nameField;
    private ButtonWidget createBtn, cancelBtn;

    public NameRegionScreen(BlockPos a, BlockPos b) {
        super(Text.literal("Region Name"));
        this.a = a;
        this.b = b;
    }

    @Override
    protected void init() {
        int panelX = (this.width  - PANEL_W) / 2 + PANEL_NUDGE_X;
        int panelY = (this.height - PANEL_H) / 2 + PANEL_NUDGE_Y;

        // --- Text field ---
        int fx = panelX + INPUT_X;
        int fy = panelY + INPUT_Y;
        int fw = INPUT_W;
        int fh = INPUT_H;

        nameField = new TextFieldWidget(this.textRenderer, fx, fy, fw, fh, Text.literal("Region Name"));
        nameField.setMaxLength(48);
        nameField.setDrawsBackground(false);   // let the texture show through
        nameField.setEditableColor(0xFFFFFF);
        nameField.setUneditableColor(0xFFFFFF);
        nameField.setText("");
        this.addDrawableChild(nameField);
        setInitialFocus(nameField);

        // --- Buttons ---
        int btnY = panelY + BTN_Y;
        int leftX, rightX;
        if (CENTER_BUTTONS) {
            int total = BTN_W * 2 + CENTER_GAP;
            leftX  = panelX + (PANEL_W - total) / 2 + BTN_LEFT_NUDGE_X;
            rightX = leftX + BTN_W + CENTER_GAP + BTN_RIGHT_NUDGE_X;
        } else {
            leftX  = panelX + BTN_LEFT_X  + BTN_LEFT_NUDGE_X;
            rightX = panelX + BTN_RIGHT_X + BTN_RIGHT_NUDGE_X;
        }

        createBtn = ButtonWidget.builder(Text.literal("Create"), b -> onCreate())
                .dimensions(leftX, btnY, BTN_W, BTN_H).build();
        cancelBtn = ButtonWidget.builder(Text.literal("Cancel"), b -> close())
                .dimensions(rightX, btnY, BTN_W, BTN_H).build();

        this.addDrawableChild(createBtn);
        this.addDrawableChild(cancelBtn);
    }

    private void onCreate() {
        if (nameField == null) return;
        String name = nameField.getText().trim();
        if (!name.isEmpty()) {
            LTPacketsClient.sendCreate(name, a, b);
            close();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            onCreate();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xB0000000);
        int px = (this.width - PANEL_W) / 2 + PANEL_NUDGE_X;
        int py = (this.height - PANEL_H) / 2 + PANEL_NUDGE_Y;
        ctx.drawTexture(TEX, px, py, 0, 0, PANEL_W, PANEL_H, 256, 256);

        super.render(ctx, mouseX, mouseY, delta); // draws children (nameField, buttons)
    }

    @Override
    public void resize(MinecraftClient mc, int w, int h) {
        String txt = (nameField != null) ? nameField.getText() : "";
        this.init(mc, w, h);
        if (nameField != null) {
            nameField.setText(txt);
            setInitialFocus(nameField);
        }
    }
}