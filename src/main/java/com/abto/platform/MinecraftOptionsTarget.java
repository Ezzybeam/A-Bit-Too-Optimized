package com.abto.platform;

import com.abto.preset.CloudMode;
import com.abto.preset.GraphicsMode;
import com.abto.preset.OptionsTarget;
import com.abto.preset.ParticleMode;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.server.level.ParticleStatus;

/**
 * OptionsTarget backed by the real Minecraft Options. This is the only
 * version-coupled class in the core feature set; everything it is handed comes
 * from version-independent code.
 *
 * Accessor names confirmed via javap against minecraft-merged-deobf-26.1.2.jar
 * (Mojang official mappings). The same names are present in 26.2; no
 * Stonecutter guards are required.
 *
 * Real class: net.minecraft.client.Options (not GameOptions).
 * OptionInstance setter: set(T) (not setValue).
 */
public final class MinecraftOptionsTarget implements OptionsTarget {

    private final Options options;

    public MinecraftOptionsTarget(Minecraft client) {
        this.options = client.options;
    }

    @Override
    public void setRenderDistance(int chunks) {
        options.renderDistance().set(chunks);
    }

    @Override
    public void setSimulationDistance(int chunks) {
        options.simulationDistance().set(chunks);
    }

    @Override
    public void setGraphics(GraphicsMode mode) {
        options.graphicsPreset().set(switch (mode) {
            case FAST -> GraphicsPreset.FAST;
            case FANCY -> GraphicsPreset.FANCY;
            case FABULOUS -> GraphicsPreset.FABULOUS;
        });
    }

    @Override
    public void setClouds(CloudMode mode) {
        options.cloudStatus().set(switch (mode) {
            case OFF -> CloudStatus.OFF;
            case FAST -> CloudStatus.FAST;
            case FANCY -> CloudStatus.FANCY;
        });
    }

    @Override
    public void setParticles(ParticleMode mode) {
        options.particles().set(switch (mode) {
            case ALL -> ParticleStatus.ALL;
            case DECREASED -> ParticleStatus.DECREASED;
            case MINIMAL -> ParticleStatus.MINIMAL;
        });
    }

    @Override
    public void setEntityShadows(boolean enabled) {
        options.entityShadows().set(enabled);
    }

    @Override
    public void setSmoothLighting(boolean enabled) {
        options.ambientOcclusion().set(enabled);
    }

    @Override
    public void setBiomeBlendRadius(int radius) {
        options.biomeBlendRadius().set(radius);
    }

    @Override
    public void setMipmapLevels(int levels) {
        options.mipmapLevels().set(levels);
    }

    @Override
    public void setEntityDistanceScaling(double scale) {
        options.entityDistanceScaling().set(scale);
    }

    @Override
    public void save() {
        options.save();
    }
}
