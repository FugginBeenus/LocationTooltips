package com.fugginbeenus.locationtooltip.client;

//? if <26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
//?}

/**
 * Draws the world-space region boxes. Geometry goes through the vanilla debug-box render type
 * on the frame's shared buffer, so blending and depth come from that type rather than from
 * manual render-state calls, which the 1.21.5 render rewrite removed.
 */
//? if >=26.1 {
/*public final class LTBoxRender {
}
*///?} else {
public final class LTBoxRender {
    private final PoseStack matrices;
    private final MultiBufferSource consumers;
    private VertexConsumer buffer;
    private Matrix4f matrix;

    private LTBoxRender(PoseStack matrices, MultiBufferSource consumers) {
        this.matrices = matrices;
        this.consumers = consumers;
    }

    //? if >=1.21.11 {
    /*private static net.minecraft.client.renderer.rendertype.RenderType boxType() {
        return net.minecraft.client.renderer.rendertype.RenderTypes.debugFilledBox();
    }
    *///?} else {
    private static net.minecraft.client.renderer.RenderType boxType() {
        return net.minecraft.client.renderer.RenderType.debugFilledBox();
    }
    //?}

    /** Translate to camera-relative space and take the frame's buffer. */
    public static LTBoxRender begin(WorldRenderContext ctx) {
        //? if >=1.21.11 {
        /*PoseStack matrices = ctx.matrices();
        Vec3 cam = ctx.worldState().cameraRenderState.pos;
        *///?} else {
        PoseStack matrices = ctx.matrixStack();
        Vec3 cam = ctx.camera().getPosition();
        //?}

        matrices.pushPose();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        return new LTBoxRender(matrices, ctx.consumers());
    }

    public void startQuads() {
        matrix = matrices.last().pose();
        buffer = consumers.getBuffer(boxType());
    }

    public void drawQuads() {
        if (consumers instanceof MultiBufferSource.BufferSource source) {
            source.endBatch(boxType());
        }
    }

    public void end() {
        matrices.popPose();
    }

    private void quad(double x1, double y1, double z1,
                      double x2, double y2, double z2,
                      double x3, double y3, double z3,
                      double x4, double y4, double z4,
                      float r, float g, float b, float a) {
        //? if >=1.21 {
        /*buffer.addVertex(matrix, (float) x1, (float) y1, (float) z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) x2, (float) y2, (float) z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) x3, (float) y3, (float) z3).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) x4, (float) y4, (float) z4).setColor(r, g, b, a);
        *///?} else {
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) x3, (float) y3, (float) z3).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) x4, (float) y4, (float) z4).color(r, g, b, a).endVertex();
        //?}
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
//?}
