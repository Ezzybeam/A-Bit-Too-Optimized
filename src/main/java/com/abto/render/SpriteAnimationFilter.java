package com.abto.render;

import net.minecraft.client.renderer.texture.SpriteContents;

import java.lang.reflect.Field;

/**
 * Decides whether one sprite's animation should be frozen this tick, based on the
 * per-type animation toggles (water, lava, fire, portal). The sprite name lives
 * two hops away from the animation ticker across private inner classes
 * (AnimationState.animationInfo -> AnimatedTexture.this$0 -> SpriteContents), which
 * Mixin @Shadow cannot cleanly type, so we read those two fields by cached
 * reflection and then use the public SpriteContents.name(). SpriteContents is a
 * public type, so only the two field reads need reflection.
 *
 * This runs from SpriteAnimationMixin at the head of AnimationState.tick(); if it
 * returns true the tick is cancelled and the sprite stays on its current frame.
 * Ticks happen per animated sprite per client tick (~20/s), so the reflective
 * reads are negligible. The master "disable block animations" toggle freezes every
 * animation a layer up, so this only matters when that master is off.
 */
public final class SpriteAnimationFilter {

    private static Field animationInfoField;
    private static Field parentField;
    private static boolean initFailed;

    private SpriteAnimationFilter() {
    }

    public static boolean shouldFreeze(Object animationState) {
        String id = spriteId(animationState);
        if (id == null) {
            return false;
        }
        // Record every sprite that ticks so the per-type animation list can show it.
        AnimationCatalog.record(id);

        if (RenderToggles.disableWaterAnimation() && id.contains("water")) {
            return true;
        }
        if (RenderToggles.disableLavaAnimation() && id.contains("lava")) {
            return true;
        }
        if (RenderToggles.disableFireAnimation() && id.contains("fire")) {
            return true;
        }
        if (RenderToggles.disablePortalAnimation() && id.contains("portal")) {
            return true;
        }
        return RenderToggles.anySpriteDisabled() && RenderToggles.isSpriteDisabled(id);
    }

    /** The full sprite id, e.g. "minecraft:block/water_still", or null if unreadable. */
    private static String spriteId(Object animationState) {
        try {
            ensureInit(animationState);
            if (initFailed) {
                return null;
            }
            Object animatedTexture = animationInfoField.get(animationState);
            if (animatedTexture == null) {
                return null;
            }
            Object parent = parentField.get(animatedTexture);
            if (parent instanceof SpriteContents sc) {
                return sc.name().toString();
            }
        } catch (ReflectiveOperationException e) {
            initFailed = true;
        }
        return null;
    }

    private static void ensureInit(Object animationState) throws ReflectiveOperationException {
        if (animationInfoField != null || initFailed) {
            return;
        }
        Field info = animationState.getClass().getDeclaredField("animationInfo");
        info.setAccessible(true);
        Object animatedTexture = info.get(animationState);
        if (animatedTexture == null) {
            return;
        }
        Field parent = animatedTexture.getClass().getDeclaredField("this$0");
        parent.setAccessible(true);
        animationInfoField = info;
        parentField = parent;
    }
}
