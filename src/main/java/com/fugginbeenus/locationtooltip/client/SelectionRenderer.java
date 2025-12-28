package com.fugginbeenus.locationtooltip.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;

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

        MatrixStack matrices = context.matrixStack();
        Camera camera = context.camera();

        matrices.push();

        double camX = camera.getPos().x;
        double camY = camera.getPos().y;
        double camZ = camera.getPos().z;
        matrices.translate(-camX, -camY, -camZ);

        int minX = Math.min(corner1.getX(), corner2.getX());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());
        int maxX = Math.max(corner1.getX(), corner2.getX()) + 1;
        int maxZ = Math.max(corner1.getZ(), corner2.getZ()) + 1;

        int minY = Math.min(corner1.getY(), corner2.getY()) - 1;
        int maxY = Math.max(corner1.getY(), corner2.getY()) + 5;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float time = (System.currentTimeMillis() - lastUpdateTime) / 1000.0f;
        float pulse = (float) (0.8 + 0.2 * Math.sin(time * 2.0));

        float r, g, b;
        if (isAdminCompass) {
            r = 1.0f;
            g = 0.65f * pulse;
            b = 0.1f;
        } else {
            r = 0.2f;
            g = 1.0f * pulse;
            b = 1.0f * pulse;
        }

        // Draw thick edges as rectangular beams
        float w = 0.08f; // Width of edge beams
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // Bottom 4 edges
        drawEdgeBox(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, w, r, g, b, 1.0f);
        drawEdgeBox(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, w, r, g, b, 1.0f);
        drawEdgeBox(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, w, r, g, b, 1.0f);
        drawEdgeBox(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, w, r, g, b, 1.0f);

        // Top 4 edges
        drawEdgeBox(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, w, r, g, b, 1.0f);
        drawEdgeBox(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, w, r, g, b, 1.0f);
        drawEdgeBox(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, w, r, g, b, 1.0f);
        drawEdgeBox(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, w, r, g, b, 1.0f);

        // Vertical 4 edges
        drawEdgeBox(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, w, r, g, b, 1.0f);
        drawEdgeBox(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, w, r, g, b, 1.0f);
        drawEdgeBox(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, w, r, g, b, 1.0f);
        drawEdgeBox(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, w, r, g, b, 1.0f);

        tessellator.draw();

        // Draw semi-transparent faces
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float faceAlpha = 0.15f * pulse;
        float sideFaceAlpha = 0.08f * pulse;

        drawQuad(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, faceAlpha);
        drawQuad(buffer, matrix, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, r, g, b, faceAlpha);
        drawQuad(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, r, g, b, sideFaceAlpha);
        drawQuad(buffer, matrix, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, sideFaceAlpha);
        drawQuad(buffer, matrix, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, sideFaceAlpha);
        drawQuad(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, sideFaceAlpha);

        tessellator.draw();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        matrices.pop();
    }

    // Draw a rectangular beam for an edge - draws all 4 sides
    private static void drawEdgeBox(BufferBuilder buffer, Matrix4f matrix,
                                    double x1, double y1, double z1,
                                    double x2, double y2, double z2,
                                    float width, float r, float g, float b, float a) {
        // Calculate direction
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        double w = width / 2.0;

        // Determine which axis the line is along
        if (Math.abs(dx) > 0.1) {
            // X-axis line
            double y1a = y1 - w, y1b = y1 + w;
            double z1a = z1 - w, z1b = z1 + w;
            double y2a = y2 - w, y2b = y2 + w;
            double z2a = z2 - w, z2b = z2 + w;

            // 4 sides
            drawQuad(buffer, matrix, x1, y1a, z1a, x2, y2a, z2a, x2, y2a, z2b, x1, y1a, z1b, r, g, b, a);
            drawQuad(buffer, matrix, x1, y1b, z1a, x1, y1b, z1b, x2, y2b, z2b, x2, y2b, z2a, r, g, b, a);
            drawQuad(buffer, matrix, x1, y1a, z1a, x1, y1b, z1a, x2, y2b, z2a, x2, y2a, z2a, r, g, b, a);
            drawQuad(buffer, matrix, x1, y1a, z1b, x2, y2a, z2b, x2, y2b, z2b, x1, y1b, z1b, r, g, b, a);
        } else if (Math.abs(dy) > 0.1) {
            // Y-axis line
            double x1a = x1 - w, x1b = x1 + w;
            double z1a = z1 - w, z1b = z1 + w;
            double x2a = x2 - w, x2b = x2 + w;
            double z2a = z2 - w, z2b = z2 + w;

            // 4 sides
            drawQuad(buffer, matrix, x1a, y1, z1a, x2a, y2, z2a, x2b, y2, z2a, x1b, y1, z1a, r, g, b, a);
            drawQuad(buffer, matrix, x1a, y1, z1b, x1b, y1, z1b, x2b, y2, z2b, x2a, y2, z2b, r, g, b, a);
            drawQuad(buffer, matrix, x1a, y1, z1a, x1a, y1, z1b, x2a, y2, z2b, x2a, y2, z2a, r, g, b, a);
            drawQuad(buffer, matrix, x1b, y1, z1a, x2b, y2, z2a, x2b, y2, z2b, x1b, y1, z1b, r, g, b, a);
        } else {
            // Z-axis line
            double x1a = x1 - w, x1b = x1 + w;
            double y1a = y1 - w, y1b = y1 + w;
            double x2a = x2 - w, x2b = x2 + w;
            double y2a = y2 - w, y2b = y2 + w;

            // 4 sides
            drawQuad(buffer, matrix, x1a, y1a, z1, x2a, y2a, z2, x2b, y2a, z2, x1b, y1a, z1, r, g, b, a);
            drawQuad(buffer, matrix, x1a, y1b, z1, x1b, y1b, z1, x2b, y2b, z2, x2a, y2b, z2, r, g, b, a);
            drawQuad(buffer, matrix, x1a, y1a, z1, x1a, y1b, z1, x2a, y2b, z2, x2a, y2a, z2, r, g, b, a);
            drawQuad(buffer, matrix, x1b, y1a, z1, x2b, y2a, z2, x2b, y2b, z2, x1b, y1b, z1, r, g, b, a);
        }
    }

    private static void drawQuad(BufferBuilder buffer, Matrix4f matrix,
                                 double x1, double y1, double z1,
                                 double x2, double y2, double z2,
                                 double x3, double y3, double z3,
                                 double x4, double y4, double z4,
                                 float r, float g, float b, float a) {
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a).next();
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a).next();
        buffer.vertex(matrix, (float) x3, (float) y3, (float) z3).color(r, g, b, a).next();
        buffer.vertex(matrix, (float) x4, (float) y4, (float) z4).color(r, g, b, a).next();
    }
}