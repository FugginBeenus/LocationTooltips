package com.fugginbeenus.locationtooltip.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.core.BlockPos;

/** Draws the pulsing box for the current wand corner selection. */
public class SelectionRenderer {

    private static BlockPos corner1 = null;
    private static BlockPos corner2 = null;
    private static long lastUpdateTime = 0;
    private static boolean isAdminCompass = false;

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(SelectionRenderer::render);
    }

    public static void setCorners(BlockPos a, BlockPos b) {
        setCorners(a, b, false);
    }

    public static void setCorners(BlockPos a, BlockPos b, boolean fromAdminCompass) {
        corner1 = a;
        corner2 = b;
        lastUpdateTime = System.currentTimeMillis();
        isAdminCompass = fromAdminCompass;
    }

    public static void clear() {
        corner1 = null;
        corner2 = null;
        isAdminCompass = false;
    }

    private static void render(WorldRenderContext context) {
        if (corner1 == null || corner2 == null) return;

        int minX = Math.min(corner1.getX(), corner2.getX());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());
        int maxX = Math.max(corner1.getX(), corner2.getX()) + 1;
        int maxZ = Math.max(corner1.getZ(), corner2.getZ()) + 1;
        int minY = Math.min(corner1.getY(), corner2.getY()) - 1;
        int maxY = Math.max(corner1.getY(), corner2.getY()) + 5;

        float pulse = (float) (0.8 + 0.2 * Math.sin((System.currentTimeMillis() - lastUpdateTime) / 1000.0 * 2.0));

        float r, g, b;
        if (isAdminCompass) {
            r = 1.0f; g = 0.65f * pulse; b = 0.1f;
        } else {
            r = 0.2f; g = 1.0f * pulse; b = 1.0f * pulse;
        }

        float w = 0.08f;
        float faceAlpha = 0.15f * pulse;
        float sideFaceAlpha = 0.08f * pulse;

        LTBoxRender box = LTBoxRender.begin(context);

        box.startQuads();
        box.edgeBox(minX, minY, minZ, maxX, minY, minZ, w, r, g, b, 1.0f);
        box.edgeBox(maxX, minY, minZ, maxX, minY, maxZ, w, r, g, b, 1.0f);
        box.edgeBox(maxX, minY, maxZ, minX, minY, maxZ, w, r, g, b, 1.0f);
        box.edgeBox(minX, minY, maxZ, minX, minY, minZ, w, r, g, b, 1.0f);
        box.edgeBox(minX, maxY, minZ, maxX, maxY, minZ, w, r, g, b, 1.0f);
        box.edgeBox(maxX, maxY, minZ, maxX, maxY, maxZ, w, r, g, b, 1.0f);
        box.edgeBox(maxX, maxY, maxZ, minX, maxY, maxZ, w, r, g, b, 1.0f);
        box.edgeBox(minX, maxY, maxZ, minX, maxY, minZ, w, r, g, b, 1.0f);
        box.edgeBox(minX, minY, minZ, minX, maxY, minZ, w, r, g, b, 1.0f);
        box.edgeBox(maxX, minY, minZ, maxX, maxY, minZ, w, r, g, b, 1.0f);
        box.edgeBox(maxX, minY, maxZ, maxX, maxY, maxZ, w, r, g, b, 1.0f);
        box.edgeBox(minX, minY, maxZ, minX, maxY, maxZ, w, r, g, b, 1.0f);
        box.drawQuads();

        box.startQuads();
        box.face(minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, faceAlpha);
        box.face(minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, r, g, b, faceAlpha);
        box.face(minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, r, g, b, sideFaceAlpha);
        box.face(minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, sideFaceAlpha);
        box.face(minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, sideFaceAlpha);
        box.face(maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, sideFaceAlpha);
        box.drawQuads();

        box.end();
    }
}
