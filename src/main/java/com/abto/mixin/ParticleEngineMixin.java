package com.abto.mixin;

import com.abto.render.RenderToggles;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When disableAllParticles is on, no particle is ever registered for display.
 * add(Particle) is the universal chokepoint: every particle reaches the screen
 * through it, whether it came from createParticle or was built directly (e.g.
 * block-break/destroy particles). Cancelling add here drops them all; the created
 * Particle object is simply discarded.
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipParticle(Particle particle, CallbackInfo ci) {
        if (RenderToggles.disableAllParticles()) {
            ci.cancel();
        }
    }
}
