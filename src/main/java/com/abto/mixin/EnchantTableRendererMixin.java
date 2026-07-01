package com.abto.mixin;

import com.abto.render.RenderToggles;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Skips the floating enchanting table book render when hideEnchantTableBook is on. */
@Mixin(EnchantTableRenderer.class)
public class EnchantTableRendererMixin {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/EnchantTableRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD"), cancellable = true)
    private void abto$maybeHideBook(EnchantTableRenderState state, PoseStack pose,
            SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (RenderToggles.hideEnchantTableBook()) {
            ci.cancel();
        }
    }
}
