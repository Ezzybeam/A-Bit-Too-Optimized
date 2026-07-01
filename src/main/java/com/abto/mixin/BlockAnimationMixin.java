package com.abto.mixin;

import com.abto.render.RenderToggles;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Freezes animated textures when disableBlockAnimations is on. TextureAtlas.tick()
 * cycles every animated sprite's frames and uploads them to the GPU each client
 * tick; cancelling it at HEAD holds all animated textures on their current frame and
 * skips that per-tick upload work, which is a real FPS win in texture-heavy scenes.
 * (Affects all animated atlas textures - water, lava, fire, portal, and similar.)
 */
@Mixin(TextureAtlas.class)
public class BlockAnimationMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void abto$maybeSkipAnimations(CallbackInfo ci) {
        if (RenderToggles.disableBlockAnimations()) {
            ci.cancel();
        }
    }
}
