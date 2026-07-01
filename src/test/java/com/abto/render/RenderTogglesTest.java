package com.abto.render;

import com.abto.config.FeatureToggles;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RenderTogglesTest {
    @Test
    void applyMirrorsFeatureToggles() {
        FeatureToggles t = FeatureToggles.defaults();
        t.hideClouds = true;
        t.disableFog = true;
        RenderToggles.apply(t);
        assertTrue(RenderToggles.hideClouds());
        assertTrue(RenderToggles.disableFog());
        assertFalse(RenderToggles.hideStars());
    }

    @Test
    void applyIsIdempotentAndOverwrites() {
        FeatureToggles on = FeatureToggles.defaults();
        on.hideStars = true;
        RenderToggles.apply(on);
        assertTrue(RenderToggles.hideStars());
        RenderToggles.apply(FeatureToggles.defaults()); // all render toggles off again
        assertFalse(RenderToggles.hideStars());
    }
}
