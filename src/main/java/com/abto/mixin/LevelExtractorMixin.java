package com.abto.mixin;

import com.abto.render.EntityCuller;
import com.abto.render.RenderToggles;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Entity occlusion culling for 26.2+. isEntityVisible is the per-entity gate the
 * render-state extractor asks before extracting an entity into the frame's render
 * state; when it returns false the entity is not drawn. We only flip a true to
 * false (never force something visible), and only when the entity is fully hidden
 * behind solid blocks.
 *
 * This class only exists on 26.2+ (on 26.1.2 the check is inline in
 * LevelRenderer.extractVisibleEntities), so the target is a soft @Pseudo string
 * reference, the injector uses require = 0, and AbtoMixinPlugin skips this mixin
 * entirely when the LevelExtractor class is absent - keeping 26.1.2 from crashing
 * on a missing target.
 */
@Pseudo
@Mixin(targets = "net.minecraft.client.renderer.extract.LevelExtractor")
public class LevelExtractorMixin {

    @ModifyReturnValue(method = "isEntityVisible", at = @At("RETURN"), require = 0)
    private boolean abto$occlusionCull(boolean original, Entity entity, Frustum frustum,
            double camX, double camY, double camZ) {
        if (original && RenderToggles.entityCulling()
                && EntityCuller.isOccluded(entity, camX, camY, camZ)) {
            return false;
        }
        return original;
    }
}
