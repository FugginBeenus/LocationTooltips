package com.fugginbeenus.locationtooltip.client;

import com.fugginbeenus.locationtooltip.net.client.LTPacketsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

/**
 * Region creation card. Name + protection flags, then sends the new region to the server.
 * Layout/scroll handled by {@link RegionConfigScreen}.
 */
public class NameRegionScreen extends RegionConfigScreen {

    private final BlockPos a, b;

    public NameRegionScreen(BlockPos a, BlockPos b) {
        super("Create Region");
        this.a = a;
        this.b = b;
    }

    @Override protected String headerTitle() { return "Create Region"; }
    @Override protected String confirmLabel() { return "Create"; }
    @Override protected String initialName() { return ""; }
    @Override protected Map<String, Boolean> initialFlags() { return null; }

    @Override
    protected void onConfirm(String name, Map<String, Boolean> flags) {
        LTPacketsClient.sendCreate(name, a, b, flags);
        close();
    }

    @Override public void close() { MinecraftClient.getInstance().setScreen(null); }
}
