package com.abto.compat;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Detects whether Sodium is installed. ABTO is a soft dependency of nothing and
 * targets vanilla classes, so it loads with or without Sodium. Where an ABTO
 * optimization overlaps with what Sodium already does (terrain face culling,
 * entity culling), ABTO defers to Sodium when it is present - both to avoid doing
 * the same work twice and to stay clear of Sodium's own mixins on those paths.
 * Features Sodium does not touch (sky, particles, per-type toggles, HUD) keep
 * working either way. The result is checked once at load and cached.
 */
public final class SodiumCompat {

    private static final boolean SODIUM_LOADED =
        FabricLoader.getInstance().isModLoaded("sodium");

    private SodiumCompat() {
    }

    public static boolean isSodiumLoaded() {
        return SODIUM_LOADED;
    }
}
