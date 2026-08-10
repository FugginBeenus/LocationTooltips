package com.fugginbeenus.locationtooltip;

import com.fugginbeenus.locationtooltip.client.AdminClientCache;
import com.fugginbeenus.locationtooltip.client.AdminPanelScreen;
import com.fugginbeenus.locationtooltip.config.LTConfig;
import com.fugginbeenus.locationtooltip.hud.LocationHudOverlay;
import com.fugginbeenus.locationtooltip.net.client.LTPacketsClient;
import com.fugginbeenus.locationtooltip.registry.LTItems;
import com.fugginbeenus.locationtooltip.util.LTId;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
*///?} else {
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

// com/fugginbeenus/locationtooltip/LocationTooltipClient.java
@Environment(EnvType.CLIENT)
public final class LocationTooltipClient implements ClientModInitializer {

    private static KeyMapping openAdminKey;

    @Override
    public void onInitializeClient() {
        // Packets (client receivers)

        LTItems.init();
        LTPacketsClient.initClient();

        // Admin Compass needle (points at the nearest known region)
        com.fugginbeenus.locationtooltip.client.AdminCompassModel.register();


        // Initialize live bridge once (idempotent now)
        com.fugginbeenus.locationtooltip.config.ui.ConfigLiveBridge.init();

        // Config load + save on shutdown
        LTConfig.get();
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> LTConfig.get().save());

        // HUD overlay (register once)
        //? if >=26.1 {
        /*HudElementRegistry.addLast(LTId.of("locationtooltip", "pill"), new LocationHudOverlay());
        *///?} else {
        HudRenderCallback.EVENT.register(new LocationHudOverlay());
        //?}
        LOG.info("[LT] onInitializeClient() start");

        openAdminKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.locationtooltip.open_admin",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_O,
                        //? if >=1.21.11 {
                        /*net.minecraft.client.KeyMapping.Category.MISC
                        *///?} else {
                        "key.categories.locationtooltip"
                        //?}
                )
        );

        // Client tick: keypress + admin compass visuals
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) return;

            while (openAdminKey.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null) {
                    mc.setScreen(new AdminPanelScreen());
                    LTPacketsClient.requestAllAdminList();
                }
            }

            if (client.player == null || client.level == null) return;

            // Check if holding admin compass
            boolean holding = false;
            var main = client.player.getMainHandItem();
            if (!main.isEmpty() && main.is(LTItems.ADMIN_COMPASS)) holding = true;
            if (!holding) {
                var off = client.player.getOffhandItem();
                if (!off.isEmpty() && off.is(LTItems.ADMIN_COMPASS)) holding = true;
            }

            if (!holding) {
                // Not holding compass - clear regions
                com.fugginbeenus.locationtooltip.client.AdminRegionRenderer.clearAll();
                return;
            }

            // Holding compass - refresh the in-world region boxes (nearby only).
            // Skip while the panel is open; it does its own (all-regions) refresh. [GambaPVP]
            //? if >=26.1 {
            /*if ((client.level.getGameTime() % 20L) == 0L) {
            *///?} else {
            if (!(client.screen instanceof AdminPanelScreen) && (client.level.getGameTime() % 20L) == 0L) {
            //?}
                LTPacketsClient.requestAdminList(256);
            }

            // Update renderer with current regions
            var world = client.level;
            var hereDim = world.dimension().location();
            var rows = AdminClientCache.current();
            if (rows != null && rows.length > 0) {
                com.fugginbeenus.locationtooltip.client.AdminRegionRenderer.updateRegions(rows, hereDim);
            }
        });
    }
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("locationtooltip");
}