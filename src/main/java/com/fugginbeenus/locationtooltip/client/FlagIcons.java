package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.util.LTId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class FlagIcons {
    private FlagIcons() {}

    private static final Map<String, ResourceLocation> PATHS = new HashMap<>();
    private static final Map<String, Boolean> EXISTS = new HashMap<>();

    private static ResourceLocation path(String flagId) {
        return PATHS.computeIfAbsent(flagId,
                id -> LTId.of("locationtooltip", "textures/gui/flags/" + id + ".png"));
    }

    public static boolean has(String flagId) {
        return EXISTS.computeIfAbsent(flagId, id -> {
            try {
                return Minecraft.getInstance().getResourceManager().getResource(path(id)).isPresent();
            } catch (Throwable t) {
                return false;
            }
        });
    }

    public static boolean draw(GuiGraphics ctx, String flagId, int x, int y, int size) {
        if (!has(flagId)) return false;
        //? if >=1.21.11 {
        /*ctx.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, path(flagId), x, y, 0f, 0f, size, size, 16, 16);
        *///?} else {
        ctx.blit(path(flagId), x, y, size, size, 0f, 0f, 16, 16, 16, 16);
        //?}
        return true;
    }
}
