package com.fugginbeenus.locationtooltip;

import com.fugginbeenus.locationtooltip.client.AdminClientCache;
import com.fugginbeenus.locationtooltip.client.AdminPanelScreen;
import com.fugginbeenus.locationtooltip.hud.LocationHudOverlay;
import com.fugginbeenus.locationtooltip.net.client.LTPacketsClient;
import com.fugginbeenus.locationtooltip.registry.LTItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.particle.DustParticleEffect;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class LocationTooltipClient implements ClientModInitializer {

    private static KeyBinding openAdminKey;

    @Override
    public void onInitializeClient() {
        // Client networking receivers
        LTPacketsClient.initClient();

        // HUD overlay (region/title + time pills)
        HudRenderCallback.EVENT.register(new LocationHudOverlay());

        // Keybinds
        openAdminKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.locationtooltip.open_admin",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O, // default: O (change in Controls)
                "key.categories.locationtooltip"
        ));

        // Ticking: key handling + admin compass visuals
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) return;

            // Key: open admin panel
            if (openAdminKey.wasPressed()) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc != null) {
                    mc.setScreen(new AdminPanelScreen());
                    LTPacketsClient.requestAdminList(256);
                }
            }

            if (client.player == null || client.world == null) return;

            // Show borders while holding the admin compass (either hand)
            var player = client.player;
            var world  = client.world;

            boolean holding = false;
            var stack = player.getMainHandStack();
            if (!stack.isEmpty() && stack.isOf(LTItems.ADMIN_COMPASS)) holding = true;
            if (!holding) {
                stack = player.getOffHandStack();
                if (!stack.isEmpty() && stack.isOf(LTItems.ADMIN_COMPASS)) holding = true;
            }
            if (!holding) return;

            // Ask server for region list every second
            if ((world.getTime() % 20L) == 0L) {
                LTPacketsClient.requestAdminList(256);
            }

            // Draw simple aqua border particles for cached regions in this dimension
            var dimHere = world.getRegistryKey().getValue();
            var effect  = new DustParticleEffect(new Vector3f(0.2f, 0.9f, 0.9f), 1.0f);
            int step = 2;

            for (var r : AdminClientCache.last) {
                if (!r.dim.equals(dimHere)) continue;

                int minX = Math.min(r.a.getX(), r.b.getX());
                int minY = Math.min(r.a.getY(), r.b.getY());
                int minZ = Math.min(r.a.getZ(), r.b.getZ());
                int maxX = Math.max(r.a.getX(), r.b.getX());
                int maxY = Math.max(r.a.getY(), r.b.getY());
                int maxZ = Math.max(r.a.getZ(), r.b.getZ());

                // vertical edges
                for (int y = minY; y <= maxY; y += step) {
                    world.addParticle(effect, minX + 0.5, y + 0.1, minZ + 0.5, 0, 0, 0);
                    world.addParticle(effect, minX + 0.5, y + 0.1, maxZ + 0.5, 0, 0, 0);
                    world.addParticle(effect, maxX + 0.5, y + 0.1, minZ + 0.5, 0, 0, 0);
                    world.addParticle(effect, maxX + 0.5, y + 0.1, maxZ + 0.5, 0, 0, 0);
                }
                // bottom/top perimeter (x)
                for (int x = minX; x <= maxX; x += step) {
                    world.addParticle(effect, x + 0.5, minY + 0.1, minZ + 0.5, 0, 0, 0);
                    world.addParticle(effect, x + 0.5, minY + 0.1, maxZ + 0.5, 0, 0, 0);
                    world.addParticle(effect, x + 0.5, maxY + 0.1, minZ + 0.5, 0, 0, 0);
                    world.addParticle(effect, x + 0.5, maxY + 0.1, maxZ + 0.5, 0, 0, 0);
                }
                // bottom/top perimeter (z)
                for (int z = minZ; z <= maxZ; z += step) {
                    world.addParticle(effect, minX + 0.5, minY + 0.1, z + 0.5, 0, 0, 0);
                    world.addParticle(effect, maxX + 0.5, minY + 0.1, z + 0.5, 0, 0, 0);
                    world.addParticle(effect, minX + 0.5, maxY + 0.1, z + 0.5, 0, 0, 0);
                    world.addParticle(effect, maxX + 0.5, maxY + 0.1, z + 0.5, 0, 0, 0);
                }
            }
        });
    }
}
