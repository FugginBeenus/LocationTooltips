package com.fugginbeenus.locationtooltip.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class LTBoxRender {
    private final List<float[]> verts = new ArrayList<>();
    private final PoseStack matrices;

    //? if >=26.1 {
    /*private final net.minecraft.client.renderer.SubmitNodeCollector collector;

    private LTBoxRender(PoseStack matrices, net.minecraft.client.renderer.SubmitNodeCollector collector) {
        this.matrices = matrices;
        this.collector = collector;
    }

    public static LTBoxRender begin(PoseStack matrices,
                                    net.minecraft.client.renderer.SubmitNodeCollector collector,
                                    Vec3 cam) {
        matrices.pushPose();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        return new LTBoxRender(matrices, collector);
    }

    public void drawQuads() {
        if (verts.isEmpty()) return;
        List<float[]> batch = new ArrayList<>(verts);
        collector.submitCustomGeometry(matrices,
                net.minecraft.client.renderer.rendertype.RenderTypes.debugFilledBox(),
                (pose, consumer) -> {
                    for (float[] v : batch) {
                        consumer.addVertex(pose.pose(), v[0], v[1], v[2])
                                .setColor(v[3], v[4], v[5], v[6]);
                    }
                });
        verts.clear();
    }
    *///?} else {
    private final net.minecraft.client.renderer.MultiBufferSource consumers;

    private LTBoxRender(PoseStack matrices, net.minecraft.client.renderer.MultiBufferSource consumers) {
        this.matrices = matrices;
        this.consumers = consumers;
    }

    public static LTBoxRender begin(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx) {
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

    public void drawQuads() {
        if (verts.isEmpty()) return;
        var type = boxType();
        var buffer = consumers.getBuffer(type);
        var matrix = matrices.last().pose();
        for (float[] v : verts) {
            //? if >=1.21 {
            /*buffer.addVertex(matrix, v[0], v[1], v[2]).setColor(v[3], v[4], v[5], v[6]);
            *///?} else {
            buffer.vertex(matrix, v[0], v[1], v[2]).color(v[3], v[4], v[5], v[6]).endVertex();
            //?}
        }
        if (consumers instanceof net.minecraft.client.renderer.MultiBufferSource.BufferSource source) {
            source.endBatch(type);
        }
        verts.clear();
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
    //?}

    public void startQuads() {
        verts.clear();
    }

    public void end() {
        matrices.popPose();
    }

    private void emit(double x, double y, double z, float r, float g, float b, float a) {
        verts.add(new float[]{(float) x, (float) y, (float) z, r, g, b, a});
    }

    private void quad(double x1, double y1, double z1,
                      double x2, double y2, double z2,
                      double x3, double y3, double z3,
                      double x4, double y4, double z4,
                      float r, float g, float b, float a) {
        emit(x1, y1, z1, r, g, b, a);
        emit(x2, y2, z2, r, g, b, a);
        emit(x3, y3, z3, r, g, b, a);
        emit(x4, y4, z4, r, g, b, a);
    }

    public void face(double x1, double y1, double z1,
                     double x2, double y2, double z2,
                     double x3, double y3, double z3,
                     double x4, double y4, double z4,
                     float r, float g, float b, float a) {
        quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, r, g, b, a);
    }

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
