package com.fugginbeenus.locationtooltip.mixin;

/**
 * Draws the region boxes on 26.x.
 *
 * Fabric has no world-render event for the renderer Minecraft introduced in 26.x, so we hook
 * the point where the level renderer collects everything it is about to draw and hand it the
 * box geometry. That runs every frame, unlike the block-outline step, which is skipped unless
 * you happen to be looking at a block.
 *
 * The pose stack is our own rather than vanilla's, so nothing here can unbalance theirs. An
 * identity pose sits at the camera, which is why the boxes translate by the camera position.
 *
 * Client-only, and only present on 26.x — the older versions use Fabric's event instead.
 */
//? if >=26.1 {
/*@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public abstract class LevelRendererMixin {

    @org.spongepowered.asm.mixin.injection.Inject(method = "submitFeatures", at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void locationtooltip$submitRegionBoxes(
            net.minecraft.client.renderer.state.level.LevelRenderState state,
            net.minecraft.client.renderer.SubmitNodeCollector collector,
            boolean sky,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        var camera = state.cameraRenderState;
        if (camera == null || camera.pos == null) return;

        var matrices = new com.mojang.blaze3d.vertex.PoseStack();
        com.fugginbeenus.locationtooltip.client.AdminRegionRenderer.submitBoxes(matrices, collector, camera.pos);
        com.fugginbeenus.locationtooltip.client.SelectionRenderer.submitBoxes(matrices, collector, camera.pos);
    }
}
*///?} else {
public interface LevelRendererMixin {
}
//?}
