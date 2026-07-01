package com.abto.mixin;

import com.abto.platform.ShaderCompat;
import com.abto.render.RenderToggles;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips cloud rendering entirely when the hideClouds toggle is on. Clouds are
 * their own render pass in modern Minecraft (CloudRenderer), so cancelling render
 * at HEAD removes them and their per-frame work. Defers to an active shader pack.
 */
@Mixin(CloudRenderer.class)
public class CloudRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipClouds(int color, CloudStatus status, float height, int flags,
            Vec3 camera, long ticks, float partialTick, CallbackInfo ci) {
        if (RenderToggles.hideClouds() && !ShaderCompat.shaderPackActive()) {
            ci.cancel();
        }
    }
}
