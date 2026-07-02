package com.abto.mixin;

import com.abto.render.SpriteAnimationFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Per-type animation freezing. AnimationState.tick() advances one animated
 * sprite's frames each client tick. Cancelling it at HEAD leaves the sprite on its
 * current frame (frozen). SpriteAnimationFilter decides per sprite, keyed on the
 * sprite name, so water/lava/fire/portal can be frozen independently. Targeted by
 * fully-qualified name because AnimationState is a private inner class.
 */
@Mixin(targets = "net.minecraft.client.renderer.texture.SpriteContents$AnimationState")
public class SpriteAnimationMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void abto$maybeFreeze(CallbackInfo ci) {
        if (SpriteAnimationFilter.shouldFreeze(this)) {
            ci.cancel();
        }
    }
}
