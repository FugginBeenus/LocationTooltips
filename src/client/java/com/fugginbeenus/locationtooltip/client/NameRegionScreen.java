package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.net.client.LTPacketsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class NameRegionScreen extends Screen {

    private final BlockPos a;
    private final BlockPos b;

    private TextFieldWidget nameField;
    private ButtonWidget createBtn;
    private ButtonWidget cancelBtn;

    public NameRegionScreen(BlockPos a, BlockPos b) {
        super(Text.literal("Name Region"));
        this.a = a;
        this.b = b;
    }

    @Override
    protected void init() {
        int w = 260;
        int x = (this.width - w) / 2;
        int y = this.height / 2 - 30;

        nameField = new TextFieldWidget(this.textRenderer, x, y, w, 20, Text.literal("Region Name"));
        nameField.setMaxLength(128);
        nameField.setText("");
        nameField.setFocusUnlocked(true);
        nameField.setEditable(true);
        nameField.setChangedListener(s -> updateButtons());
        this.addDrawableChild(nameField);

        y += 28;

        createBtn = ButtonWidget.builder(Text.literal("Create"), btn -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                LTPacketsClient.sendCreate(name, this.a, this.b);
                closeScreen();
            }
        }).dimensions(x, y, 125, 20).build();

        this.addDrawableChild(createBtn);

        cancelBtn = ButtonWidget.builder(Text.literal("Cancel"), b2 -> closeScreen())
                .dimensions(x + 135, y, 125, 20).build();
        this.addDrawableChild(cancelBtn);

        setInitialFocus(nameField);
        updateButtons();
    }

    private void updateButtons() {
        boolean ok = nameField != null && !nameField.getText().trim().isEmpty();
        if (createBtn != null) createBtn.active = ok;
    }

    private void closeScreen() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) mc.setScreen(null);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // translucent backdrop so you can still see the selection
        ctx.fill(0, 0, this.width, this.height, 0x60000000);

        // panel
        int panelW = 300, panelH = 110;
        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;
        ctx.fill(px, py, px + panelW, py + panelH, 0xB0000000);

        super.render(ctx, mouseX, mouseY, delta);

        // title + hint
        String title = "Name Your Region";
        int tw = this.textRenderer.getWidth(title);
        ctx.drawText(this.textRenderer, title, px + (panelW - tw) / 2, py + 8, 0xFFFFFFFF, false);

        String hint = "Type a name and press Create";
        ctx.drawText(this.textRenderer, hint, px + 20, py + 30, 0xFFAAAAAA, false);

        if (nameField != null) nameField.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Enter to confirm
        if (this.nameField.isFocused() && (keyCode == 257 || keyCode == 335)) { // Enter / Numpad Enter
            if (createBtn.active) createBtn.onPress();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
