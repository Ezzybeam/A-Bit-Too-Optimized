package com.abto.config;

import com.abto.preset.Preset;
import com.abto.preset.PresetSettings;

/**
 * The whole on-disk config. A mutable POJO so Gson can populate it and callers
 * can update single fields. configVersion lets future versions migrate.
 */
public final class AbtoConfig {

    public static final int CURRENT_VERSION = 1;

    public int configVersion = CURRENT_VERSION;
    public Preset selectedPreset = Preset.NORMAL;
    public boolean wizardCompleted = false;
    public boolean usesShaders = false;
    public boolean applyToOtherMods = true;
    public PresetSettings customSettings = null;
    public HardwareOverrides hardwareOverrides = HardwareOverrides.none();
    public FeatureToggles featureToggles = FeatureToggles.defaults();

    public static AbtoConfig defaults() {
        return new AbtoConfig();
    }

    /** Replaces any null fields (e.g. from a partial JSON file) with defaults. */
    public void fillMissingWithDefaults() {
        if (selectedPreset == null) selectedPreset = Preset.NORMAL;
        if (hardwareOverrides == null) hardwareOverrides = HardwareOverrides.none();
        if (featureToggles == null) featureToggles = FeatureToggles.defaults();
        if (configVersion <= 0) configVersion = CURRENT_VERSION;
    }
}
