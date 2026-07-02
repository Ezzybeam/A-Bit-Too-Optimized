package com.abto.mixin;

import com.abto.render.RenderToggles;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides floating entity name tags. EntityRenderer.submitNameDisplay is the shared
 * entry point every entity's name label flows through, so cancelling it at HEAD
 * suppresses the name. Three levels of control: hideNameTags hides all of them;
 * hidePlayerNames hides only players (AvatarRenderState); hideMobNames hides every
 * non-player name. The first parameter is the erased generic type EntityRenderState.
 */
@Mixin(EntityRenderer.class)
public class EntityNameTagMixin {
    @Inject(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD"), cancellable = true)
    private void abto$maybeHideNameTag(EntityRenderState state, PoseStack pose,
            SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        boolean isPlayer = state instanceof AvatarRenderState;
        if (RenderToggles.hideNameTags()
                || (RenderToggles.hidePlayerNames() && isPlayer)
                || (RenderToggles.hideMobNames() && !isPlayer)) {
            ci.cancel();
        }
    }
}
