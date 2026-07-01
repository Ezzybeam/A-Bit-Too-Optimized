package com.abto.preset;

/**
 * One coherent bundle of vanilla video settings. Pure data, no Minecraft types,
 * so it stays version independent and unit testable.
 */
public record PresetSettings(
    int renderDistance,
    int simulationDistance,
    GraphicsMode graphics,
    CloudMode clouds,
    ParticleMode particles,
    boolean entityShadows,
    boolean smoothLighting,
    int biomeBlendRadius,
    int mipmapLevels,
    double entityDistanceScaling
) {
}
