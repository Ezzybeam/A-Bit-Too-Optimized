package com.abto.preset;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PresetsTest {
    @Test
    void everyTunablePresetHasSettings() {
        for (Preset p : Presets.ORDER) {
            assertNotNull(Presets.settingsFor(p), "missing settings for " + p);
        }
        assertEquals(6, Presets.ORDER.size());
        assertFalse(Presets.ORDER.contains(Preset.CUSTOM));
    }

    @Test
    void customHasNoSettings() {
        assertThrows(IllegalArgumentException.class, () -> Presets.settingsFor(Preset.CUSTOM));
    }

    @Test
    void renderDistanceDecreasesFromUltraToPotato() {
        int prev = Integer.MAX_VALUE;
        for (Preset p : Presets.ORDER) {
            int rd = Presets.settingsFor(p).renderDistance();
            assertTrue(rd <= prev, "render distance should not increase from " + p);
            prev = rd;
        }
    }

    @Test
    void potatoTargetsEightToTwelveRenderDistance() {
        int rd = Presets.settingsFor(Preset.POTATO).renderDistance();
        assertTrue(rd >= 8 && rd <= 12, "potato render distance should be 8 to 12, was " + rd);
    }
}
