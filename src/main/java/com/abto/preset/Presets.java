package com.abto.preset;

import java.util.List;
import java.util.Map;

/**
 * The preset table. Each non-custom preset is a coherent strategy, not just a
 * slider position. Values live here so they are easy to read and tune.
 * Order: strongest visuals first (ULTRA) to lightest (POTATO).
 */
public final class Presets {

    public static final List<Preset> ORDER = List.of(
        Preset.ULTRA, Preset.HIGH, Preset.NORMAL, Preset.LOW, Preset.VERY_LOW, Preset.POTATO);

    private static final Map<Preset, PresetSettings> TABLE = Map.of(
        Preset.ULTRA, new PresetSettings(
            32, 12, GraphicsMode.FABULOUS, CloudMode.FANCY, ParticleMode.ALL,
            true, true, 7, 4, 1.0),
        Preset.HIGH, new PresetSettings(
            24, 12, GraphicsMode.FANCY, CloudMode.FAST, ParticleMode.ALL,
            true, true, 5, 4, 1.0),
        Preset.NORMAL, new PresetSettings(
            16, 10, GraphicsMode.FANCY, CloudMode.FAST, ParticleMode.DECREASED,
            true, true, 3, 4, 0.75),
        Preset.LOW, new PresetSettings(
            12, 8, GraphicsMode.FAST, CloudMode.OFF, ParticleMode.DECREASED,
            false, true, 1, 2, 0.5),
        Preset.VERY_LOW, new PresetSettings(
            10, 6, GraphicsMode.FAST, CloudMode.OFF, ParticleMode.MINIMAL,
            false, false, 0, 1, 0.5),
        Preset.POTATO, new PresetSettings(
            8, 5, GraphicsMode.FAST, CloudMode.OFF, ParticleMode.MINIMAL,
            false, false, 0, 0, 0.5)
    );

    private Presets() {
    }

    public static PresetSettings settingsFor(Preset preset) {
        PresetSettings settings = TABLE.get(preset);
        if (settings == null) {
            throw new IllegalArgumentException("No settings table entry for preset: " + preset);
        }
        return settings;
    }
}
