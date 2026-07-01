package com.abto.mixin;

import com.abto.platform.ShaderCompat;
import com.abto.render.RenderToggles;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips weather rendering (rain and snow) when disableWeatherRendering is on, and
 * skips the rain splash particles when disableWeatherParticles is on. Both defer to
 * an active shader pack (which renders weather via its own path).
 */
@Mixin(WeatherEffectRenderer.class)
public class WeatherRenderMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipWeather(Vec3 camera, WeatherRenderState state, CallbackInfo ci) {
        if (RenderToggles.disableWeatherRendering() && !ShaderCompat.shaderPackActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "tickRainParticles", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipRainParticles(ClientLevel level, Camera camera, int ticks,
            ParticleStatus status, int count, CallbackInfo ci) {
        if (RenderToggles.disableWeatherParticles()) {
            ci.cancel();
        }
    }
}
