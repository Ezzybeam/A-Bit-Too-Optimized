package com.abto.mixin;

import com.abto.render.RenderToggles;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When disableAllParticles is on, no particles are created at all (a hard off
 * beyond vanilla's Minimal setting). createParticle already returns null in some
 * vanilla cases, so callers tolerate a null return.
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipParticle(ParticleOptions options, double x, double y, double z,
            double dx, double dy, double dz, CallbackInfoReturnable<Particle> cir) {
        if (RenderToggles.disableAllParticles()) {
            cir.setReturnValue(null);
        }
    }
}
