package com.fugginbeenus.locationtooltip.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class RegionNameScreen extends Screen {
    private TextFieldWidget field;

    public RegionNameScreen() { super(Text.literal("Name Region")); }

    @Override
    protected void init() {
        int w = 220, x = (this.width - w) / 2, y = this.height / 2 - 30;

        field = new TextFieldWidget(textRenderer, x, y, w, 20, Text.literal("Region Name"));
        field.setMaxLength(64);
        field.setText("");
        field.setFocused(true);
        addDrawableChild(field);

        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> {
            String name = field.getText().trim();
            if (name.isEmpty()) name = "Unnamed";
            RegionSelectionClient.saveRegion(MinecraftClient.getInstance(), name);
            close();
        }).dimensions(x, y + 28, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> close())
                .dimensions(x + 120, y + 28, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "Enter region name", this.width / 2, this.height / 2 - 55, 0xFFFFFF);
    }

    @Override public boolean shouldPause() { return false; }
}
