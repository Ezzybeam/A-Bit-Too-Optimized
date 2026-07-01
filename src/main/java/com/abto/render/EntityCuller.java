package com.abto.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Occlusion culling for entities: an entity fully hidden behind solid blocks does
 * not need to be drawn. This is a real optimization (unlike the hide toggles) - it
 * removes rendering work while everything you can actually see stays identical.
 *
 * The test casts rays from the camera to sample points on the entity's bounding
 * box (its center and eight corners). If any ray reaches its target without hitting
 * a solid block, some part of the entity is visible and it is drawn. Only when
 * every ray is blocked is the entity culled. The common case - a visible entity in
 * front of the camera - returns after the very first (center) ray, so visible
 * entities cost one raycast; only fully hidden ones pay for all nine.
 *
 * Limitations (acceptable for a first version): uses the collision shape as the
 * occluder, so an entity directly behind glass or a similar solid-but-transparent
 * block can be culled. Entities closer than a few blocks are never culled to avoid
 * pop-in when the camera is near a wall.
 */
public final class EntityCuller {

    private static final double MIN_DISTANCE_SQR = 4.0 * 4.0;
    private static final double INSET = 0.1;

    private EntityCuller() {
    }

    /** True when the entity is fully occluded by solid blocks from the camera. */
    public static boolean isOccluded(Entity entity, double camX, double camY, double camZ) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return false;
        }
        Vec3 cam = new Vec3(camX, camY, camZ);
        AABB box = entity.getBoundingBox();
        Vec3 center = box.getCenter();
        if (center.distanceToSqr(cam) < MIN_DISTANCE_SQR) {
            return false;
        }

        double x0 = box.minX + INSET;
        double y0 = box.minY + INSET;
        double z0 = box.minZ + INSET;
        double x1 = box.maxX - INSET;
        double y1 = box.maxY - INSET;
        double z1 = box.maxZ - INSET;

        Vec3[] samples = {
            center,
            new Vec3(x0, y0, z0), new Vec3(x1, y0, z0),
            new Vec3(x0, y1, z0), new Vec3(x1, y1, z0),
            new Vec3(x0, y0, z1), new Vec3(x1, y0, z1),
            new Vec3(x0, y1, z1), new Vec3(x1, y1, z1)
        };
        for (Vec3 target : samples) {
            if (isVisible(level, cam, target, entity)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isVisible(ClientLevel level, Vec3 from, Vec3 to, Entity entity) {
        BlockHitResult hit = level.clip(
            new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        // MISS means the ray reached the target without a solid block in the way.
        return hit.getType() == HitResult.Type.MISS;
    }
}
