package com.abto.mixin;

import com.abto.render.RenderToggles;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.WaterDropParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Filters particles as they are registered for display. add(Particle) is the
 * universal chokepoint: every particle reaches the screen through it, whether it
 * came from createParticle or was built directly (e.g. block-break/destroy
 * particles). Cancelling add here drops the particle; the created object is
 * simply discarded.
 *
 * The per-type toggles are keyed on the concrete Particle class, which is the
 * only type information available at this point:
 * - TerrainParticle covers block break, mining crumbling, and dust pillars.
 * - WaterDropParticle is the rain-on-ground splash.
 * disableAllParticles is the master switch and takes priority over the rest.
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipParticle(Particle particle, CallbackInfo ci) {
        if (RenderToggles.disableAllParticles()) {
            ci.cancel();
            return;
        }
        if (RenderToggles.disableBlockParticles() && particle instanceof TerrainParticle) {
            ci.cancel();
            return;
        }
        if (RenderToggles.disableRainSplashParticles() && particle instanceof WaterDropParticle) {
            ci.cancel();
        }
    }
}
