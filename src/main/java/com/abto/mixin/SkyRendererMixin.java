package com.abto.mixin;

import com.abto.render.RenderToggles;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips individual sky elements when their toggle is on: the sky disc/gradient,
 * the stars, and the sun and moon. Each is a HEAD cancel so the element (and its
 * per-frame draw) is simply not rendered.
 */
@Mixin(SkyRenderer.class)
public class SkyRendererMixin {

    @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipSky(int color, CallbackInfo ci) {
        if (RenderToggles.hideSky()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStars", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipStars(float alpha, PoseStack pose, CallbackInfo ci) {
        if (RenderToggles.hideStars()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipSun(float alpha, PoseStack pose, CallbackInfo ci) {
        if (RenderToggles.hideSunMoon()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipMoon(MoonPhase phase, float alpha, PoseStack pose, CallbackInfo ci) {
        if (RenderToggles.hideSunMoon()) {
            ci.cancel();
        }
    }
}
