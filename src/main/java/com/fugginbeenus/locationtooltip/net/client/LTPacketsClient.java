package com.fugginbeenus.locationtooltip.net.client;

import com.fugginbeenus.locationtooltip.client.AdminClientCache;
import com.fugginbeenus.locationtooltip.client.AdminPanelScreen;
import com.fugginbeenus.locationtooltip.client.AdminRegionRenderer;
import com.fugginbeenus.locationtooltip.client.NameRegionScreen;
import com.fugginbeenus.locationtooltip.client.SelectionRenderer;
import com.fugginbeenus.locationtooltip.hud.LocationHudOverlay;
import com.fugginbeenus.locationtooltip.net.LTPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;

public final class LTPacketsClient {
    private LTPacketsClient() {}

    public static void initClient() {
        LTNetClient.init();
        SelectionRenderer.register();
        AdminRegionRenderer.register();

        LTNetClient.registerReceiver(LTPayloads.SELECTION_UPDATE, (client, p) ->
                SelectionRenderer.setCorners(p.a(), p.b()));

        LTNetClient.registerReceiver(LTPayloads.SELECTION_CLEAR, (client, p) ->
                SelectionRenderer.clear());

        LTNetClient.registerReceiver(LTPayloads.OPEN_ADMIN_PANEL, (client, p) -> {
            if (client == null) return;
            client.setScreen(new AdminPanelScreen());
            requestAllAdminList();
        });

        LTNetClient.registerReceiver(LTPayloads.OPEN_NAME, (client, p) -> {
            if (client != null) client.setScreen(new NameRegionScreen(p.a(), p.b()));
        });

        LTNetClient.registerReceiver(LTPayloads.ADMIN_LIST, (client, p) -> {
            AdminPanelScreen.RegionRow[] rows = toPanelRows(p.entries());
            AdminPanelScreen.receiveList(rows);
            AdminClientCache.set(toCacheRows(rows));
        });

        LTNetClient.registerReceiver(LTPayloads.REGION_UPDATE, (client, p) ->
                LocationHudOverlay.setRegionTitle(p.name()));

        LTNetClient.registerReceiver(LTPayloads.REGION_CREATED, (client, p) -> celebrate());
    }

    // -------- client → server --------
    public static void requestAdminList(int radius) {
        LTNetClient.send(LTPayloads.REQUEST_ADMIN_LIST, new LTPayloads.RequestAdminList(radius));
    }

    /** Request every region (all dimensions), not just nearby ones. Contributed by GambaPVP. */
    public static void requestAllAdminList() {
        LTNetClient.send(LTPayloads.REQUEST_ADMIN_LIST, new LTPayloads.RequestAdminList(-1));
    }

    public static void sendAdminRename(String id, String newName, Map<String, Boolean> flags) {
        LTNetClient.send(LTPayloads.ADMIN_RENAME, new LTPayloads.AdminRename(id, newName, flags));
    }

    public static void sendAdminDelete(String id) {
        LTNetClient.send(LTPayloads.ADMIN_DELETE, new LTPayloads.AdminDelete(id));
    }

    public static void sendCreate(String name, BlockPos a, BlockPos b, Map<String, Boolean> flags) {
        LTNetClient.send(LTPayloads.CREATE_REGION, new LTPayloads.CreateRegion(name, a, b, flags));
    }

    // -------- helpers --------
    private static void celebrate() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) return;

        var w = mc.level;
        BlockPos p = mc.player.blockPosition();
        for (int i = 0; i < 60; i++) {
            w.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    p.getX() + 0.5 + (w.getRandom().nextDouble() - 0.5) * 2.0,
                    p.getY() + 1.2 + w.getRandom().nextDouble(),
                    p.getZ() + 0.5 + (w.getRandom().nextDouble() - 0.5) * 2.0,
                    0, 0.02, 0);
        }
        mc.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 1.0f);
    }

    private static AdminPanelScreen.RegionRow[] toPanelRows(List<LTPayloads.RegionEntry> entries) {
        var out = new AdminPanelScreen.RegionRow[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            LTPayloads.RegionEntry e = entries.get(i);
            out[i] = new AdminPanelScreen.RegionRow(e.id(), e.name(), e.dim(), e.min(), e.max(),
                    e.flags(), e.ownerName(), e.source());
        }
        return out;
    }

    private static AdminClientCache.Row[] toCacheRows(AdminPanelScreen.RegionRow[] in) {
        var out = new AdminClientCache.Row[in.length];
        for (int i = 0; i < in.length; i++) {
            var r = in[i];
            out[i] = new AdminClientCache.Row(r.id, r.name, r.dim, r.a, r.b, r.flags, r.ownerName, r.source);
        }
        return out;
    }
}
