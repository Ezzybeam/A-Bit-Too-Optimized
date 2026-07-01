package com.abto.mixin;

import com.abto.render.RenderToggles;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Per-type particle disabling. createParticle builds a particle and adds it in one
 * call, so cancelling it at HEAD (returning null) skips both. We look up the
 * particle's registry id and skip it when that id is in the disabled set. The
 * fast-path check avoids the registry lookup when no types are disabled. Particles
 * that bypass createParticle (block-break/destroy) are handled separately in
 * ParticleEngineMixin.
 */
@Mixin(ParticleEngine.class)
public class ParticleTypeMixin {

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipByType(ParticleOptions options, double x, double y, double z,
            double dx, double dy, double dz, CallbackInfoReturnable<Particle> cir) {
        if (!RenderToggles.anyParticleTypeDisabled()) {
            return;
        }
        Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType());
        if (id != null && RenderToggles.isParticleTypeDisabled(id.toString())) {
            cir.setReturnValue(null);
        }
    }
}
