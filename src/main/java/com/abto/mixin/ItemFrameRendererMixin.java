package com.abto.mixin;

import com.abto.render.RenderToggles;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides item frames (both the frame and the item inside) when hideItemFrames is on,
 * by cancelling their submit. Useful in item-frame-heavy storage rooms. The full
 * descriptor targets the ItemFrameRenderState overload, not the inherited bridge.
 */
@Mixin(ItemFrameRenderer.class)
public class ItemFrameRendererMixin {

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;"
            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
            + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
            + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD"), cancellable = true)
    private void abto$maybeHideItemFrame(ItemFrameRenderState state, PoseStack pose,
            SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (RenderToggles.hideItemFrames()) {
            ci.cancel();
        }
    }
}
