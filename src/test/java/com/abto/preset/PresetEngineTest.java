package com.abto.preset;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PresetEngineTest {

    /** A fake OptionsTarget that records what was written. */
    static final class FakeTarget implements OptionsTarget {
        Integer renderDistance, simulationDistance, biomeBlend, mipmaps;
        GraphicsMode graphics;
        CloudMode clouds;
        ParticleMode particles;
        Boolean entityShadows, smoothLighting;
        Double entityDistanceScaling;
        int saves = 0;

        public void setRenderDistance(int v) { renderDistance = v; }
        public void setSimulationDistance(int v) { simulationDistance = v; }
        public void setGraphics(GraphicsMode v) { graphics = v; }
        public void setClouds(CloudMode v) { clouds = v; }
        public void setParticles(ParticleMode v) { particles = v; }
        public void setEntityShadows(boolean v) { entityShadows = v; }
        public void setSmoothLighting(boolean v) { smoothLighting = v; }
        public void setBiomeBlendRadius(int v) { biomeBlend = v; }
        public void setMipmapLevels(int v) { mipmaps = v; }
        public void setEntityDistanceScaling(double v) { entityDistanceScaling = v; }
        public void save() { saves++; }
    }

    @Test
    void applyingPresetWritesEveryAxisAndSaves() {
        FakeTarget t = new FakeTarget();
        boolean applied = PresetEngine.apply(Preset.LOW, false, t);
        assertTrue(applied);
        PresetSettings expected = Presets.settingsFor(Preset.LOW);
        assertEquals(expected.renderDistance(), t.renderDistance);
        assertEquals(expected.simulationDistance(), t.simulationDistance);
        assertEquals(expected.graphics(), t.graphics);
        assertEquals(expected.clouds(), t.clouds);
        assertEquals(expected.particles(), t.particles);
        assertEquals(expected.entityShadows(), t.entityShadows);
        assertEquals(expected.smoothLighting(), t.smoothLighting);
        assertEquals(expected.biomeBlendRadius(), t.biomeBlend);
        assertEquals(expected.mipmapLevels(), t.mipmaps);
        assertEquals(expected.entityDistanceScaling(), t.entityDistanceScaling);
        assertEquals(1, t.saves);
    }

    @Test
    void customAppliesNothing() {
        FakeTarget t = new FakeTarget();
        boolean applied = PresetEngine.apply(Preset.CUSTOM, false, t);
        assertFalse(applied);
        assertNull(t.graphics);
        assertEquals(0, t.saves);
    }

    @Test
    void shaderAwareKeepsGraphicsAtLeastFancy() {
        FakeTarget t = new FakeTarget();
        // POTATO normally uses FAST graphics; with shaders on we keep at least FANCY.
        PresetEngine.apply(Preset.POTATO, true, t);
        assertEquals(GraphicsMode.FANCY, t.graphics);
    }

    @Test
    void withoutShadersPotatoStaysFast() {
        FakeTarget t = new FakeTarget();
        PresetEngine.apply(Preset.POTATO, false, t);
        assertEquals(GraphicsMode.FAST, t.graphics);
    }
}
