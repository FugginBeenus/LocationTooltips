package com.fugginbeenus.locationtooltip.mixin;

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
