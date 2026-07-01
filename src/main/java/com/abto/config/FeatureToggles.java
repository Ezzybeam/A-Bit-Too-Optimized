package com.abto.config;

public record FeatureToggles(
    boolean dynamicFps,
    boolean entityCulling,
    boolean particleCulling
) {
    public static FeatureToggles defaults() {
        return new FeatureToggles(true, true, true);
    }
}
