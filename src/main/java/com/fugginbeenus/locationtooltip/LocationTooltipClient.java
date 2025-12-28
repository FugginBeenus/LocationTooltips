package com.fugginbeenus.locationtooltip;

import com.fugginbeenus.locationtooltip.client.AdminClientCache;
import com.fugginbeenus.locationtooltip.client.AdminPanelScreen;
import com.fugginbeenus.locationtooltip.config.LTConfig;
import com.fugginbeenus.locationtooltip.hud.LocationHudOverlay;
import com.fugginbeenus.locationtooltip.net.client.LTPacketsClient;
import com.fugginbeenus.locationtooltip.registry.LTItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.particle.DustParticleEffect;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

// com/fugginbeenus/locationtooltip/LocationTooltipClient.java
@Environment(EnvType.CLIENT)
public final class LocationTooltipClient implements ClientModInitializer {

    private static KeyBinding openAdminKey;

    @Override
    public void onInitializeClient() {
        // Packets (client receivers)

        LTItems.init();
        LTPacketsClient.initClient();


        // Initialize live bridge once (idempotent now)
        com.fugginbeenus.locationtooltip.config.ui.ConfigLiveBridge.init();

        // Config load + save on shutdown
        LTConfig.get();
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> LTConfig.get().save());

        // HUD overlay (register once)
        HudRenderCallback.EVENT.register(new LocationHudOverlay());
        LOG.info("[LT] onInitializeClient() start");

        openAdminKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.locationtooltip.open_admin",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_O,
                        "key.categories.locationtooltip"
                )
        );

        // Client tick: keypress + admin compass visuals
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) return;

            while (openAdminKey.wasPressed()) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc != null) {
                    mc.setScreen(new AdminPanelScreen());
                    LTPacketsClient.requestAdminList(256);
                }
            }

            if (client.player == null || client.world == null) return;

            // Check if holding admin compass
            boolean holding = false;
            var main = client.player.getMainHandStack();
            if (!main.isEmpty() && main.isOf(LTItems.ADMIN_COMPASS)) holding = true;
            if (!holding) {
                var off = client.player.getOffHandStack();
                if (!off.isEmpty() && off.isOf(LTItems.ADMIN_COMPASS)) holding = true;
            }

            if (!holding) {
                // Not holding compass - clear regions
                com.fugginbeenus.locationtooltip.client.AdminRegionRenderer.clearAll();
                return;
            }

            // Holding compass - request region list every second
            if (!(client.currentScreen instanceof AdminPanelScreen) && (client.world.getTime() % 20L) == 0L) {
                LTPacketsClient.requestAdminList(256);
            }

            // Update renderer with current regions
            var world = client.world;
            var hereDim = world.getRegistryKey().getValue();
            var rows = AdminClientCache.current();
            if (rows != null && rows.length > 0) {
                com.fugginbeenus.locationtooltip.client.AdminRegionRenderer.updateRegions(rows, hereDim);
            }
        });
    }
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("locationtooltip");
}