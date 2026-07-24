package com.fugginbeenus.locationtooltip.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

/**
 * Version-isolated immediate-mode drawing for the world-space region boxes (wand selection +
 * admin-compass regions). Everything that changed in the 1.21 render rewrite —
 * {@code Tessellator}/{@code BufferBuilder} lifecycle, the {@code .vertex().color().next()}
 * chain, {@code RenderSystem} setup — lives here, so both renderers stay version-agnostic and
 * the two identical copies of the beam/quad helpers are now one.
 */
public final class LTBoxRender {
    private final MatrixStack matrices;
    private final Matrix4f matrix;
    private final Tessellator tessellator;
    private final BufferBuilder buffer;

    private LTBoxRender(MatrixStack matrices, Matrix4f matrix, Tessellator tessellator, BufferBuilder buffer) {
        this.matrices = matrices;
        this.matrix = matrix;
        this.tessellator = tessellator;
        this.buffer = buffer;
    }

    /** Set up blend/depth/shader, translate to camera-relative space, and begin a batch. */
    public static LTBoxRender begin(WorldRenderContext ctx) {
        MatrixStack matrices = ctx.matrixStack();
        Camera camera = ctx.camera();

        matrices.push();
        matrices.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        return new LTBoxRender(matrices, matrices.peek().getPositionMatrix(), tessellator, tessellator.getBuffer());
    }

    public void startQuads() {
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
    }

    public void drawQuads() {
        tessellator.draw();
    }

    /** Restore render state and pop the matrix. */
    public void end() {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private void quad(double x1, double y1, double z1,
                      double x2, double y2, double z2,
                      double x3, double y3, double z3,
                      double x4, double y4, double z4,
                      float r, float g, float b, float a) {
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a).next();
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a).next();
        buffer.vertex(matrix, (float) x3, (float) y3, (float) z3).color(r, g, b, a).next();
        buffer.vertex(matrix, (float) x4, (float) y4, (float) z4).color(r, g, b, a).next();
    }

    /** One flat quad in the current batch. */
    public void face(double x1, double y1, double z1,
                     double x2, double y2, double z2,
                     double x3, double y3, double z3,
                     double x4, double y4, double z4,
                     float r, float g, float b, float a) {
        quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, r, g, b, a);
    }

    /** A rectangular beam along one axis between two points, drawn as its 4 sides. */
    public void edgeBox(double x1, double y1, double z1,
                        double x2, double y2, double z2,
                        float width, float r, float g, float b, float a) {
        double dx = x2 - x1, dy = y2 - y1;
        double w = width / 2.0;

        if (Math.abs(dx) > 0.1) {
            double y1a = y1 - w, y1b = y1 + w, z1a = z1 - w, z1b = z1 + w;
            double y2a = y2 - w, y2b = y2 + w, z2a = z2 - w, z2b = z2 + w;
            quad(x1, y1a, z1a, x2, y2a, z2a, x2, y2a, z2b, x1, y1a, z1b, r, g, b, a);
            quad(x1, y1b, z1a, x1, y1b, z1b, x2, y2b, z2b, x2, y2b, z2a, r, g, b, a);
            quad(x1, y1a, z1a, x1, y1b, z1a, x2, y2b, z2a, x2, y2a, z2a, r, g, b, a);
            quad(x1, y1a, z1b, x2, y2a, z2b, x2, y2b, z2b, x1, y1b, z1b, r, g, b, a);
        } else if (Math.abs(dy) > 0.1) {
            double x1a = x1 - w, x1b = x1 + w, z1a = z1 - w, z1b = z1 + w;
            double x2a = x2 - w, x2b = x2 + w, z2a = z2 - w, z2b = z2 + w;
            quad(x1a, y1, z1a, x2a, y2, z2a, x2b, y2, z2a, x1b, y1, z1a, r, g, b, a);
            quad(x1a, y1, z1b, x1b, y1, z1b, x2b, y2, z2b, x2a, y2, z2b, r, g, b, a);
            quad(x1a, y1, z1a, x1a, y1, z1b, x2a, y2, z2b, x2a, y2, z2a, r, g, b, a);
            quad(x1b, y1, z1a, x2b, y2, z2a, x2b, y2, z2b, x1b, y1, z1b, r, g, b, a);
        } else {
            double x1a = x1 - w, x1b = x1 + w, y1a = y1 - w, y1b = y1 + w;
            double x2a = x2 - w, x2b = x2 + w, y2a = y2 - w, y2b = y2 + w;
            quad(x1a, y1a, z1, x2a, y2a, z2, x2b, y2a, z2, x1b, y1a, z1, r, g, b, a);
            quad(x1a, y1b, z1, x1b, y1b, z1, x2b, y2b, z2, x2a, y2b, z2, r, g, b, a);
            quad(x1a, y1a, z1, x1a, y1b, z1, x2a, y2b, z2, x2a, y2a, z2, r, g, b, a);
            quad(x1b, y1a, z1, x2b, y2a, z2, x2b, y2b, z2, x1b, y1b, z1, r, g, b, a);
        }
    }
}
