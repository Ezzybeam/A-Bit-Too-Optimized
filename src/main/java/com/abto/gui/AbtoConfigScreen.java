package com.abto.gui;

import com.abto.config.AbtoConfig;
import com.abto.config.ConfigStore;
import com.abto.platform.MinecraftOptionsTarget;
import com.abto.preset.Preset;
import com.abto.preset.PresetEngine;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * The main ABTO config screen. Lets the player pick a performance preset,
 * toggle shader awareness and mod-compatibility options, and re-run the first-
 * run setup wizard. All changes are applied and saved immediately.
 *
 * Widget API confirmed via javap against minecraft-merged-deobf-26.1.2.jar
 * (Mojang official mappings):
 *   - Screen constructor: Screen(Component title)
 *   - Screen fields: this.width, this.height, this.minecraft
 *   - Screen methods: addRenderableWidget(T), protected void init(), onClose()
 *   - Button: Button.builder(Component, OnPress).bounds(x, y, w, h).build()
 *   - Component: Component.literal(String)
 *   - Close to parent: this.minecraft.setScreenAndShow(parent)
 */
public final class AbtoConfigScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;

    private final Screen parent;

    public AbtoConfigScreen(Screen parent) {
        super(Component.literal("A Bit Too Optimized"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        AbtoConfig config = ConfigStore.load(configPath());

        List<PresetButtonList.Entry> entries = PresetButtonList.entries();

        // Total height for all buttons: preset buttons + 3 extra (shaders, other mods,
        // run setup) + 1 done button, with BUTTON_SPACING between each.
        int totalButtons = entries.size() + 3 + 1;
        int totalH = totalButtons * BUTTON_HEIGHT + (totalButtons - 1) * (BUTTON_SPACING - BUTTON_HEIGHT);
        int startY = (this.height - totalH) / 2;
        int centerX = (this.width - BUTTON_WIDTH) / 2;
        int y = startY;

        // One button per preset entry.
        for (PresetButtonList.Entry entry : entries) {
            final Preset preset = entry.preset();
            final String label = entry.label();
            final String desc = entry.description();
            addRenderableWidget(
                Button.builder(
                    Component.literal(label + " - " + desc),
                    btn -> applyPreset(preset)
                ).bounds(centerX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build()
            );
            y += BUTTON_SPACING;
        }

        // Shader toggle.
        addRenderableWidget(
            Button.builder(
                shadersLabel(config),
                btn -> {
                    AbtoConfig cfg = ConfigStore.load(configPath());
                    cfg.usesShaders = !cfg.usesShaders;
                    ConfigStore.save(configPath(), cfg);
                    btn.setMessage(shadersLabel(cfg));
                }
            ).bounds(centerX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build()
        );
        y += BUTTON_SPACING;

        // Apply to other mods toggle.
        addRenderableWidget(
            Button.builder(
                applyModsLabel(config),
                btn -> {
                    AbtoConfig cfg = ConfigStore.load(configPath());
                    cfg.applyToOtherMods = !cfg.applyToOtherMods;
                    ConfigStore.save(configPath(), cfg);
                    btn.setMessage(applyModsLabel(cfg));
                }
            ).bounds(centerX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build()
        );
        y += BUTTON_SPACING;

        // Run setup again.
        addRenderableWidget(
            Button.builder(
                Component.literal("Run setup again"),
                btn -> this.minecraft.setScreenAndShow(new AbtoWizardScreen(parent))
            ).bounds(centerX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build()
        );
        y += BUTTON_SPACING;

        // Done button.
        addRenderableWidget(
            Button.builder(
                Component.literal("Done"),
                btn -> onClose()
            ).bounds(centerX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build()
        );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }

    // --- helpers ---

    private void applyPreset(Preset preset) {
        AbtoConfig config = ConfigStore.load(configPath());
        config.selectedPreset = preset;
        if (preset != Preset.CUSTOM) {
            PresetEngine.apply(preset, config.usesShaders, new MinecraftOptionsTarget(this.minecraft));
        }
        ConfigStore.save(configPath(), config);
        // Rebuild widgets so any state-dependent labels refresh.
        rebuildWidgets();
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("abto.json");
    }

    private static Component shadersLabel(AbtoConfig config) {
        return Component.literal("Use shaders: " + (config.usesShaders ? "ON" : "OFF"));
    }

    private static Component applyModsLabel(AbtoConfig config) {
        return Component.literal("Apply to other mods: " + (config.applyToOtherMods ? "ON" : "OFF"));
    }
}
