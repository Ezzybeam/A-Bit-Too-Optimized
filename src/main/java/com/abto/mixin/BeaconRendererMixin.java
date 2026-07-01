package com.abto.mixin;

import com.abto.render.RenderToggles;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides beacon beams when hideBeaconBeams is on (cancels the beacon submit). */
@Mixin(BeaconRenderer.class)
public class BeaconRendererMixin {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BeaconRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD"), cancellable = true)
    private void abto$maybeHideBeacon(BeaconRenderState state, PoseStack pose,
            SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (RenderToggles.hideBeaconBeams()) {
            ci.cancel();
        }
    }
}
