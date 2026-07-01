package com.abto;

import com.abto.compat.ModPresenceDetector;
import com.abto.config.AbtoConfig;
import com.abto.config.ConfigStore;
import com.abto.hardware.EffectiveHardware;
import com.abto.hardware.HardwareInfo;
import com.abto.hardware.HardwareProbe;
import com.abto.platform.FabricModPresence;
import com.abto.platform.MinecraftOptionsTarget;
import com.abto.platform.RuntimeHardware;
import com.abto.preset.Preset;
import com.abto.preset.PresetEngine;
import com.abto.recommend.RecommendationEngine;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import java.nio.file.Path;
import java.util.Set;

public final class AbtoClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve("abto.json");
        AbtoConfig config = ConfigStore.load(configFile);

        // Make the saved render toggles live so the render mixins read the right state.
        com.abto.render.RenderToggles.apply(config.featureToggles);

        HardwareInfo detected = HardwareProbe.detect(
            RuntimeHardware::maxMemoryBytes,
            RuntimeHardware::cpuCores,
            RuntimeHardware::totalRamMb,
            RuntimeHardware::gpuName);
        EffectiveHardware effective = EffectiveHardware.resolve(detected, config.hardwareOverrides);
        Preset recommended = RecommendationEngine.recommend(effective);

        Set<String> presentMods = ModPresenceDetector.detect(FabricModPresence.isLoaded());

        Abto.LOGGER.info("A Bit Too Optimized loaded. Selected preset: {}. Recommended for this machine: {}. Known perf mods present: {}.",
            config.selectedPreset, recommended, presentMods);

        if (config.selectedPreset != Preset.CUSTOM) {
            Minecraft client = Minecraft.getInstance();
            if (client != null && client.options != null) {
                boolean applied = PresetEngine.apply(
                    config.selectedPreset, config.usesShaders, new MinecraftOptionsTarget(client));
                Abto.LOGGER.info("Applied preset {} to vanilla options: {}.", config.selectedPreset, applied);
            }
        }

        com.abto.gui.WizardLauncher.register();
    }
}
