package com.abto.mixin;

import com.abto.render.EntityCuller;
import com.abto.render.RenderToggles;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Entity occlusion culling. shouldRender is the per-entity gate the level renderer
 * asks before drawing an entity; vanilla only frustum- and distance-checks it. When
 * entityCulling is on and vanilla would draw the entity, we additionally test
 * whether it is fully hidden behind solid blocks and, if so, flip the answer to
 * false. We never flip a false to true, so we can only ever hide something vanilla
 * already decided to draw - never force-render something it culled.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @ModifyReturnValue(method = "shouldRender", at = @At("RETURN"))
    private boolean abto$occlusionCull(boolean original, Entity entity, Frustum frustum,
            double camX, double camY, double camZ) {
        if (original && RenderToggles.entityCulling()
                && EntityCuller.isOccluded(entity, camX, camY, camZ)) {
            return false;
        }
        return original;
    }
}
