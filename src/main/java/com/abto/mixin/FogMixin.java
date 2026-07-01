package com.abto.mixin;

import com.abto.render.RenderToggles;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Disables fog when the disableFog toggle is on, by pushing every fog distance in
 * the computed FogData far beyond view so no fog gradient is ever visible. Opt-in
 * and off by default: this removes ALL fog (including Nether and water fog), which
 * is a visual choice the player makes for extra clarity and a small FPS gain.
 */
@Mixin(FogRenderer.class)
public class FogMixin {

    private static final float ABTO_NO_FOG = 1_000_000.0f;

    @ModifyReturnValue(method = "setupFog", at = @At("RETURN"))
    private FogData abto$maybeDisableFog(FogData data) {
        if (RenderToggles.disableFog() && data != null) {
            data.environmentalStart = ABTO_NO_FOG;
            data.environmentalEnd = ABTO_NO_FOG;
            data.renderDistanceStart = ABTO_NO_FOG;
            data.renderDistanceEnd = ABTO_NO_FOG;
            data.skyEnd = ABTO_NO_FOG;
            data.cloudEnd = ABTO_NO_FOG;
        }
        return data;
    }
}
