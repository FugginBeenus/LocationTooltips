package com.fugginbeenus.locationtooltip.client;

//? if <26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
//?}
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/** Draws every nearby region's box while the admin compass is held. */
public class AdminRegionRenderer {

    private static final List<RegionBox> regions = new ArrayList<>();
    private static long lastUpdateTime = 0;

    private static class RegionBox {
        final BlockPos min, max;
        final boolean structure; // auto-tagged structure region → rendered in a distinct color
        RegionBox(BlockPos a, BlockPos b, boolean structure) {
            int minX = Math.min(a.getX(), b.getX());
            int minY = Math.min(a.getY(), b.getY());
            int minZ = Math.min(a.getZ(), b.getZ());
            int maxX = Math.max(a.getX(), b.getX()) + 1;
            int maxY = Math.max(a.getY(), b.getY()) + 1;
            int maxZ = Math.max(a.getZ(), b.getZ()) + 1;
            this.min = new BlockPos(minX, minY, minZ);
            this.max = new BlockPos(maxX, maxY, maxZ);
            this.structure = structure;
        }
    }

    public static void register() {
        //? if <26.1 {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(AdminRegionRenderer::render);
        //?}
    }

    public static void updateRegions(AdminClientCache.Row[] rows, ResourceLocation currentDim) {
        regions.clear();
        for (var row : rows) {
            if (row.dim.equals(currentDim)) {
                regions.add(new RegionBox(row.a, row.b, row.isStructure()));
            }
        }
        lastUpdateTime = System.currentTimeMillis();
    }

    public static void clearAll() {
        regions.clear();
    }

    /** The 12 edges of every tracked region, as {x1,y1,z1,x2,y2,z2,r,g,b}. */
    public static java.util.List<double[]> edges() {
        java.util.List<double[]> out = new ArrayList<>();
        for (RegionBox box : regions) {
            float r = box.structure ? 0.25f : 1.0f;
            float g = box.structure ? 0.75f : 0.65f;
            float b = box.structure ? 1.0f : 0.15f;
            double x1 = box.min.getX(), y1 = box.min.getY(), z1 = box.min.getZ();
            double x2 = box.max.getX(), y2 = box.max.getY(), z2 = box.max.getZ();
            double[][] corners = {
                    {x1, y1, z1, x2, y1, z1}, {x2, y1, z1, x2, y1, z2},
                    {x2, y1, z2, x1, y1, z2}, {x1, y1, z2, x1, y1, z1},
                    {x1, y2, z1, x2, y2, z1}, {x2, y2, z1, x2, y2, z2},
                    {x2, y2, z2, x1, y2, z2}, {x1, y2, z2, x1, y2, z1},
                    {x1, y1, z1, x1, y2, z1}, {x2, y1, z1, x2, y2, z1},
                    {x2, y1, z2, x2, y2, z2}, {x1, y1, z2, x1, y2, z2},
            };
            for (double[] c : corners) {
                out.add(new double[]{c[0], c[1], c[2], c[3], c[4], c[5], r, g, b});
            }
        }
        return out;
    }

    //? if <26.1 {
    private static void render(WorldRenderContext context) {
        if (regions.isEmpty()) return;

        float pulse = (float) (0.8 + 0.2 * Math.sin((System.currentTimeMillis() - lastUpdateTime) / 1000.0 * 2.0));
        float w = 0.07f;
        float faceAlpha = 0.10f * pulse;
        float sideFaceAlpha = 0.06f * pulse;

        LTBoxRender r = LTBoxRender.begin(context);

        r.startQuads();
        for (RegionBox box : regions) {
            float cr = box.structure ? 0.25f : 1.0f;
            float cg = box.structure ? (0.75f * pulse) : (0.65f * pulse);
            float cb = box.structure ? 1.0f : 0.15f;
            int minX = box.min.getX(), minY = box.min.getY(), minZ = box.min.getZ();
            int maxX = box.max.getX(), maxY = box.max.getY(), maxZ = box.max.getZ();

            r.edgeBox(minX, minY, minZ, maxX, minY, minZ, w, cr, cg, cb, 1.0f);
            r.edgeBox(maxX, minY, minZ, maxX, minY, maxZ, w, cr, cg, cb, 1.0f);
            r.edgeBox(maxX, minY, maxZ, minX, minY, maxZ, w, cr, cg, cb, 1.0f);
            r.edgeBox(minX, minY, maxZ, minX, minY, minZ, w, cr, cg, cb, 1.0f);
            r.edgeBox(minX, maxY, minZ, maxX, maxY, minZ, w, cr, cg, cb, 1.0f);
            r.edgeBox(maxX, maxY, minZ, maxX, maxY, maxZ, w, cr, cg, cb, 1.0f);
            r.edgeBox(maxX, maxY, maxZ, minX, maxY, maxZ, w, cr, cg, cb, 1.0f);
            r.edgeBox(minX, maxY, maxZ, minX, maxY, minZ, w, cr, cg, cb, 1.0f);
            r.edgeBox(minX, minY, minZ, minX, maxY, minZ, w, cr, cg, cb, 1.0f);
            r.edgeBox(maxX, minY, minZ, maxX, maxY, minZ, w, cr, cg, cb, 1.0f);
            r.edgeBox(maxX, minY, maxZ, maxX, maxY, maxZ, w, cr, cg, cb, 1.0f);
            r.edgeBox(minX, minY, maxZ, minX, maxY, maxZ, w, cr, cg, cb, 1.0f);
        }
        r.drawQuads();

        r.startQuads();
        for (RegionBox box : regions) {
            float cr = box.structure ? 0.25f : 1.0f;
            float cg = box.structure ? (0.75f * pulse) : (0.65f * pulse);
            float cb = box.structure ? 1.0f : 0.15f;
            int minX = box.min.getX(), minY = box.min.getY(), minZ = box.min.getZ();
            int maxX = box.max.getX(), maxY = box.max.getY(), maxZ = box.max.getZ();

            r.face(minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, cr, cg, cb, faceAlpha);
            r.face(minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, cr, cg, cb, faceAlpha);
            r.face(minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, cr, cg, cb, sideFaceAlpha);
            r.face(minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, cr, cg, cb, sideFaceAlpha);
            r.face(minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, cr, cg, cb, sideFaceAlpha);
            r.face(maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, cr, cg, cb, sideFaceAlpha);
        }
        r.drawQuads();

        r.end();
    }
    //?}
}
