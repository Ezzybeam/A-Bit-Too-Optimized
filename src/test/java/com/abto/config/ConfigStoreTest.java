package com.abto.config;

import com.abto.preset.Preset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ConfigStoreTest {
    @Test
    void missingFileGivesDefaults(@TempDir Path dir) {
        AbtoConfig config = ConfigStore.load(dir.resolve("abto.json"));
        assertEquals(Preset.NORMAL, config.selectedPreset);
        assertFalse(config.wizardCompleted);
        assertTrue(config.applyToOtherMods);
        assertNotNull(config.hardwareOverrides);
        assertNotNull(config.featureToggles);
    }

    @Test
    void saveThenLoadRoundTrips(@TempDir Path dir) {
        Path file = dir.resolve("abto.json");
        AbtoConfig config = AbtoConfig.defaults();
        config.selectedPreset = Preset.POTATO;
        config.wizardCompleted = true;
        config.usesShaders = true;
        config.hardwareOverrides = new HardwareOverrides(null, 4096L, 4, "Intel UHD");
        ConfigStore.save(file, config);

        AbtoConfig loaded = ConfigStore.load(file);
        assertEquals(Preset.POTATO, loaded.selectedPreset);
        assertTrue(loaded.wizardCompleted);
        assertTrue(loaded.usesShaders);
        assertEquals(4096L, loaded.hardwareOverrides.allocatedRamMb());
        assertEquals("Intel UHD", loaded.hardwareOverrides.gpuName());
    }

    @Test
    void corruptFileFallsBackToDefaultsAndBacksUp(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("abto.json");
        Files.writeString(file, "{ this is not valid json ");
        AbtoConfig config = ConfigStore.load(file);
        assertEquals(Preset.NORMAL, config.selectedPreset);
        assertTrue(Files.exists(dir.resolve("abto.json.bak")), "corrupt file should be backed up");
    }
}
