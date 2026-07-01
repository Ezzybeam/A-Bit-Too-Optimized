package com.abto.preset;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PresetSettingsTest {
    @Test
    void holdsAllTenAxes() {
        PresetSettings s = new PresetSettings(
            16, 10, GraphicsMode.FANCY, CloudMode.FAST, ParticleMode.DECREASED,
            true, true, 3, 4, 0.75);
        assertEquals(16, s.renderDistance());
        assertEquals(10, s.simulationDistance());
        assertEquals(GraphicsMode.FANCY, s.graphics());
        assertEquals(CloudMode.FAST, s.clouds());
        assertEquals(ParticleMode.DECREASED, s.particles());
        assertTrue(s.entityShadows());
        assertTrue(s.smoothLighting());
        assertEquals(3, s.biomeBlendRadius());
        assertEquals(4, s.mipmapLevels());
        assertEquals(0.75, s.entityDistanceScaling());
    }
}
