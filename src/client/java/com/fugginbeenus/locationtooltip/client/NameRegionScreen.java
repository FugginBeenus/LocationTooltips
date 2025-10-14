package com.fugginbeenus.locationtooltip.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class NameRegionScreen extends Screen {
    private static final Identifier PANEL   = new Identifier("locationtooltip", "textures/gui/name_panel.png");
    private static final Identifier ICON_OK = new Identifier("locationtooltip", "textures/gui/icon_save.png");
    private static final Identifier ICON_X  = new Identifier("locationtooltip", "textures/gui/icon_cancel.png");

    private TextFieldWidget nameField;
    private final int defaultPriority;

    // button positions for drawing icons
    private int saveX, saveY, cancelX, cancelY;

    public NameRegionScreen(int defaultPriority) {
        super(Text.literal("Save Region"));
        this.defaultPriority = defaultPriority;
    }

    @Override
    protected void init() {
        int panelW = 260, panelH = 120;
        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;

        int tfW = 220, tfH = 20;
        int cx = this.width / 2;

        nameField = new TextFieldWidget(this.textRenderer, cx - tfW/2, py + 40, tfW, tfH, Text.literal("Region Name"));
        nameField.setMaxLength(64);
        nameField.setDrawsBackground(true);
        nameField.setPlaceholder(Text.literal("Enter region name"));
        addDrawableChild(nameField);

        // Save button
        var save = ButtonWidget.builder(Text.literal("Save"), b -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                RegionSelectionClient.saveRegion(MinecraftClient.getInstance(), name, defaultPriority);
                close();
            }
        }).dimensions(cx - 110, py + 75, 100, 20).build();
        addDrawableChild(save);

        // Cancel button
        var cancel = ButtonWidget.builder(Text.literal("Cancel"), b -> close())
                .dimensions(cx + 10, py + 75, 100, 20).build();
        addDrawableChild(cancel);

        saveX = save.getX(); saveY = save.getY();
        cancelX = cancel.getX(); cancelY = cancel.getY();

        setInitialFocus(nameField);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx);

        int panelW = 260, panelH = 120;
        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;

        // Draw the textured panel
        ctx.drawTexture(PANEL, px, py, 0, 0, panelW, panelH, panelW, panelH);

        // Title
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Save Region", this.width / 2, py + 12, 0xFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);

        // Tiny icons near buttons for style
        ctx.drawTexture(ICON_OK, saveX - 20, saveY + 2, 0, 0, 16, 16, 16, 16);
        ctx.drawTexture(ICON_X,  cancelX - 20, cancelY + 2, 0, 0, 16, 16, 16, 16);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }
}
