package com.abto.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FeatureTogglesTest {
    @Test
    void defaultsAreSafe() {
        FeatureToggles t = FeatureToggles.defaults();
        // kept from M2
        assertTrue(t.dynamicFps);
        assertTrue(t.entityCulling);
        assertTrue(t.particleCulling);
        // new render toggles default OFF so the mod does not change visuals unasked
        assertFalse(t.hideClouds);
        assertFalse(t.hideStars);
        assertFalse(t.hideSunMoon);
        assertFalse(t.hideSky);
        assertFalse(t.disableFog);
        assertFalse(t.disableBlockAnimations);
    }

    @Test
    void fieldsAreMutable() {
        FeatureToggles t = FeatureToggles.defaults();
        t.hideClouds = true;
        assertTrue(t.hideClouds);
    }
}
