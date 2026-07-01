package com.abto.mixin;

import com.abto.render.RenderToggles;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.client.renderer.blockentity.state.PistonHeadRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Skips the moving piston render when hideMovingPistons is on. */
@Mixin(PistonHeadRenderer.class)
public class PistonHeadRendererMixin {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/PistonHeadRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD"), cancellable = true)
    private void abto$maybeHidePiston(PistonHeadRenderState state, PoseStack pose,
            SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (RenderToggles.hideMovingPistons()) {
            ci.cancel();
        }
    }
}
