package com.abto.preset;

/**
 * The surface the preset engine writes settings through. A Minecraft-backed
 * implementation lives in com.abto.platform; tests use a fake. This keeps the
 * engine free of Minecraft types and unit testable.
 */
public interface OptionsTarget {
    void setRenderDistance(int chunks);
    void setSimulationDistance(int chunks);
    void setGraphics(GraphicsMode mode);
    void setClouds(CloudMode mode);
    void setParticles(ParticleMode mode);
    void setEntityShadows(boolean enabled);
    void setSmoothLighting(boolean enabled);
    void setBiomeBlendRadius(int radius);
    void setMipmapLevels(int levels);
    void setEntityDistanceScaling(double scale);
    void save();
}
