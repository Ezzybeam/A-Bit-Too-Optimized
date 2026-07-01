package com.abto.preset;

/**
 * Applies a preset's vanilla video settings to an OptionsTarget. The only place
 * preset values become real settings. Shader awareness lives here: with shaders
 * on, we do not push the graphics mode below FANCY because some shader packs
 * rely on fancy-level rendering.
 */
public final class PresetEngine {

    private PresetEngine() {
    }

    /**
     * @return true if a preset bundle was applied, false for CUSTOM (which leaves
     *         the user in control of individual settings).
     */
    public static boolean apply(Preset preset, boolean usesShaders, OptionsTarget target) {
        if (preset == Preset.CUSTOM) {
            return false;
        }
        PresetSettings s = Presets.settingsFor(preset);
        target.setRenderDistance(s.renderDistance());
        target.setSimulationDistance(s.simulationDistance());
        target.setGraphics(shaderAwareGraphics(s.graphics(), usesShaders));
        target.setClouds(s.clouds());
        target.setParticles(s.particles());
        target.setEntityShadows(s.entityShadows());
        target.setSmoothLighting(s.smoothLighting());
        target.setBiomeBlendRadius(s.biomeBlendRadius());
        target.setMipmapLevels(s.mipmapLevels());
        target.setEntityDistanceScaling(s.entityDistanceScaling());
        target.save();
        return true;
    }

    private static GraphicsMode shaderAwareGraphics(GraphicsMode mode, boolean usesShaders) {
        if (usesShaders && mode == GraphicsMode.FAST) {
            return GraphicsMode.FANCY;
        }
        return mode;
    }
}
