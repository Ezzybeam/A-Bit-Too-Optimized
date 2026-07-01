package com.abto.render;

import com.abto.config.FeatureToggles;

/**
 * A static, in-memory snapshot of the active render toggles. Render mixins read
 * these getters every frame, so this must be a cheap field read - never a config
 * file load. Updated via apply() when the config loads or the user changes a
 * toggle. Client render is single threaded, so plain static fields are fine.
 * No Minecraft imports (keeps it unit testable).
 */
public final class RenderToggles {

    private static volatile boolean hideClouds;
    private static volatile boolean hideStars;
    private static volatile boolean hideSunMoon;
    private static volatile boolean hideSky;
    private static volatile boolean disableFog;
    private static volatile boolean disableBlockAnimations;
    private static volatile boolean disableWeatherRendering;
    private static volatile boolean disableWeatherParticles;
    private static volatile boolean disableAllParticles;
    private static volatile boolean dynamicFps = true;

    private RenderToggles() {
    }

    public static void apply(FeatureToggles t) {
        hideClouds = t.hideClouds;
        hideStars = t.hideStars;
        hideSunMoon = t.hideSunMoon;
        hideSky = t.hideSky;
        disableFog = t.disableFog;
        disableBlockAnimations = t.disableBlockAnimations;
        disableWeatherRendering = t.disableWeatherRendering;
        disableWeatherParticles = t.disableWeatherParticles;
        disableAllParticles = t.disableAllParticles;
        dynamicFps = t.dynamicFps;
    }

    public static boolean hideClouds() { return hideClouds; }
    public static boolean hideStars() { return hideStars; }
    public static boolean hideSunMoon() { return hideSunMoon; }
    public static boolean hideSky() { return hideSky; }
    public static boolean disableFog() { return disableFog; }
    public static boolean disableBlockAnimations() { return disableBlockAnimations; }
    public static boolean disableWeatherRendering() { return disableWeatherRendering; }
    public static boolean disableWeatherParticles() { return disableWeatherParticles; }
    public static boolean disableAllParticles() { return disableAllParticles; }
    public static boolean dynamicFps() { return dynamicFps; }
}
