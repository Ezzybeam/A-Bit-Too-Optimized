package com.abto.mixin;

import com.abto.render.RenderToggles;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips sign text rendering when hideSignText is on, leaving the sign board
 * itself intact. AbstractSignRenderer is the shared base for standing and
 * hanging signs, and submitSignText is the per-line text entry point, so one
 * cancel here suppresses all sign text. The first parameter is the erased
 * generic type SignRenderState.
 */
@Mixin(AbstractSignRenderer.class)
public class SignTextMixin {
    @Inject(method = "submitSignText(Lnet/minecraft/client/renderer/blockentity/state/SignRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/level/block/entity/SignText;)V",
        at = @At("HEAD"), cancellable = true)
    private void abto$maybeHideSignText(SignRenderState state, PoseStack pose,
            SubmitNodeCollector collector, SignText text, CallbackInfo ci) {
        if (RenderToggles.hideSignText()) {
            ci.cancel();
        }
    }
}
