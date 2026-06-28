package com.fugginbeenus.locationtooltip.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves and draws per-flag icons from a path convention:
 * {@code assets/locationtooltip/textures/gui/flags/<flagId>.png} (16×16).
 *
 * Existence is checked against the resource manager and cached, so a missing icon never
 * shows the magenta/black "missing texture" — callers fall back to text. Drop in your own
 * 16×16 art at the same path to replace the generated placeholders; mod-added flags work
 * too, just add a matching png.
 */
public final class FlagIcons {
    private FlagIcons() {}

    private static final Map<String, Identifier> PATHS = new HashMap<>();
    private static final Map<String, Boolean> EXISTS = new HashMap<>();

    private static Identifier path(String flagId) {
        return PATHS.computeIfAbsent(flagId,
                id -> new Identifier("locationtooltip", "textures/gui/flags/" + id + ".png"));
    }

    /** True if an icon texture exists for this flag (cached). */
    public static boolean has(String flagId) {
        return EXISTS.computeIfAbsent(flagId, id -> {
            try {
                return MinecraftClient.getInstance().getResourceManager().getResource(path(id)).isPresent();
            } catch (Throwable t) {
                return false;
            }
        });
    }

    /** Draw the flag icon scaled to size×size at (x,y). Returns false (drawing nothing) if absent. */
    public static boolean draw(DrawContext ctx, String flagId, int x, int y, int size) {
        if (!has(flagId)) return false;
        ctx.drawTexture(path(flagId), x, y, size, size, 0f, 0f, 16, 16, 16, 16);
        return true;
    }
}
