package com.abto.mixin;

import com.abto.render.RenderToggles;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Disables fog when the disableFog toggle is on, by pushing every fog distance in
 * the computed FogData far beyond view so no fog gradient is ever visible. Off by
 * default and opt-in: this removes ALL fog, including Nether, water, lava,
 * blindness, and darkness fog, which is a visual choice the player makes for extra
 * clarity and a small FPS gain.
 */
@Mixin(FogRenderer.class)
public class FogMixin {

    // Both far past the render distance so fog never touches visible geometry, and
    // start is kept distinctly below end so the fog band is well-formed (never a
    // zero-width band the fog math might handle oddly).
    private static final float ABTO_FOG_START = 1_000_000.0f;
    private static final float ABTO_FOG_END = 2_000_000.0f;

    @ModifyReturnValue(method = "setupFog", at = @At("RETURN"))
    private FogData abto$maybeDisableFog(FogData data) {
        if (RenderToggles.disableFog() && data != null) {
            data.environmentalStart = ABTO_FOG_START;
            data.environmentalEnd = ABTO_FOG_END;
            data.renderDistanceStart = ABTO_FOG_START;
            data.renderDistanceEnd = ABTO_FOG_END;
            data.skyEnd = ABTO_FOG_END;
            data.cloudEnd = ABTO_FOG_END;
        }
        return data;
    }
}
