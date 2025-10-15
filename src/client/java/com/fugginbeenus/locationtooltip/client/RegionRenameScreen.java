package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.data.Region;
import com.fugginbeenus.locationtooltip.data.RegionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class RegionRenameScreen extends Screen {
    private final Region target;
    private TextFieldWidget field;

    public RegionRenameScreen(Region target) {
        super(Text.literal("Rename Region"));
        this.target = target;
    }

    @Override
    protected void init() {
        int w = 220, x = (this.width - w) / 2, y = this.height / 2 - 30;

        field = new TextFieldWidget(textRenderer, x, y, w, 20, Text.literal("New Name"));
        field.setMaxLength(64);
        field.setText(target.name() != null ? target.name() : "");
        field.setFocused(true);
        addDrawableChild(field);

        addDrawableChild(ButtonWidget.builder(Text.literal("Rename"), b -> {
            String name = field.getText().trim();
            if (name.isEmpty()) name = "Unnamed";
            boolean ok = RegionManager.rename(target, name, MinecraftClient.getInstance().player.getUuid());
            if (!ok && MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§cYou can’t rename this region."), true);
            }
            close();
        }).dimensions(x, y + 28, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> close())
                .dimensions(x + 120, y + 28, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);
        String base = "Renaming: " + (target.name() != null ? target.name() : target.id());
        ctx.drawCenteredTextWithShadow(textRenderer, base, this.width / 2, this.height / 2 - 55, 0xFFFFFF);
    }

    @Override public boolean shouldPause() { return false; }
}
