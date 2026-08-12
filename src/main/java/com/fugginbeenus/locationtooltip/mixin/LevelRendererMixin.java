package com.fugginbeenus.locationtooltip.mixin;

/**
 * Draws the region boxes on 26.x.
 *
 * Fabric has no world-render event for the renderer Minecraft introduced in 26.x, so we hook
 * the point where vanilla submits the block outline: it hands over a pose that is already at
 * the camera and a collector that takes custom geometry, which is exactly what the boxes need.
 *
 * Client-only, and only present on 26.x — the older versions use Fabric's event instead.
 */
//? if >=26.1 {
/*@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public abstract class LevelRendererMixin {

    @org.spongepowered.asm.mixin.injection.Inject(method = "submitBlockOutline", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private void locationtooltip$submitRegionBoxes(
            com.mojang.blaze3d.vertex.PoseStack matrices,
            net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.LevelRenderState state,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        var camera = state.cameraRenderState;
        if (camera == null || camera.pos == null) return;
        com.fugginbeenus.locationtooltip.client.AdminRegionRenderer.submitBoxes(matrices, collector, camera.pos);
        com.fugginbeenus.locationtooltip.client.SelectionRenderer.submitBoxes(matrices, collector, camera.pos);
    }
}
*///?} else {
public interface LevelRendererMixin {
}
//?}
