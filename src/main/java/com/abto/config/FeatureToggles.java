package com.abto.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-feature on/off flags. A mutable POJO (not a record) so the render-toggle
 * catalog can grow and Gson fills any field missing from an older config file with
 * the field initializer below. New visual toggles default OFF so the mod never
 * changes what the player sees unless they ask.
 */
public final class FeatureToggles {

    // Kept from Milestone 2.
    public boolean dynamicFps = true;
    public boolean entityCulling = true;
    public boolean particleCulling = true;

    // Milestone 5 batch 1 (default OFF).
    public boolean hideClouds = false;
    public boolean hideStars = false;
    public boolean hideSunMoon = false;
    public boolean hideSky = false;
    public boolean disableFog = false;
    public boolean disableBlockAnimations = false;

    // Milestone 6 batch 2 (default OFF).
    public boolean disableWeatherRendering = false;
    public boolean disableWeatherParticles = false;
    public boolean disableAllParticles = false;
    public boolean hideItemFrames = false;
    public boolean hideArmorStands = false;
    public boolean hidePaintings = false;
    public boolean hideBeaconBeams = false;
    public boolean hideMovingPistons = false;
    public boolean hideEnchantTableBook = false;
    public boolean hideNameTags = false;
    public boolean hidePlayerNames = false;
    public boolean hideMobNames = false;
    public boolean disableBlockParticles = false;
    public boolean disableRainSplashParticles = false;
    public boolean hideSignText = false;
    public boolean disableWaterAnimation = false;
    public boolean disableLavaAnimation = false;
    public boolean disableFireAnimation = false;
    public boolean disablePortalAnimation = false;
    public List<String> disabledParticleTypes = new ArrayList<>();

    public static FeatureToggles defaults() {
        return new FeatureToggles();
    }
}
