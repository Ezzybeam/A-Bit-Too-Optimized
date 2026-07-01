package com.abto.gui.options;

import com.abto.config.AbtoConfig;
import com.abto.config.ConfigStore;
import com.abto.gui.AbtoWizardScreen;
import com.abto.gui.PresetButtonList;
import com.abto.platform.MinecraftOptionsTarget;
import com.abto.preset.Preset;
import com.abto.preset.PresetEngine;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * The ABTO video settings screen. Extends the vanilla OptionsSubScreen so it gets
 * the native scrolling OptionsList, title, and Done footer for free (the scrolling
 * list clips safely, so there is no manual scissor handling to get wrong). It shows
 * ABTO's own controls (preset selector and toggles) followed by the full set of
 * vanilla video options, reused via their own widgets. The vanilla Video Settings
 * button is redirected here by OptionsScreenMixin.
 */
public final class AbtoOptionsScreen extends OptionsSubScreen {

    private static final int ROW_WIDTH = 310;
    private static final int HALF_WIDTH = 150;

    public AbtoOptionsScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.literal("A Bit Too Optimized"));
    }

    @Override
    protected void addOptions() {
        AbtoConfig config = ConfigStore.load(configPath());

        this.list.addHeader(Component.literal("A Bit Too Optimized"));
        this.list.addSmall(List.<AbstractWidget>of(presetButton(config)));
        this.list.addSmall(List.<AbstractWidget>of(shadersButton(config), applyModsButton(config)));
        this.list.addSmall(List.<AbstractWidget>of(uiStyleButton(config), runSetupButton()));

        // Render feature toggles, grouped by the shared registry's categories.
        for (Map.Entry<String, List<ToggleOption>> group
                : AbtoOptionRegistry.byCategory().entrySet()) {
            this.list.addHeader(Component.literal(group.getKey()));
            List<ToggleOption> toggles = group.getValue();
            for (int i = 0; i < toggles.size(); i += 2) {
                if (i + 1 < toggles.size()) {
                    this.list.addSmall(List.<AbstractWidget>of(
                        renderToggle(toggles.get(i)), renderToggle(toggles.get(i + 1))));
                } else {
                    this.list.addSmall(List.<AbstractWidget>of(renderToggle(toggles.get(i))));
                }
            }
            if (group.getKey().equals(AbtoOptionRegistry.CAT_PARTICLES)) {
                this.list.addSmall(List.<AbstractWidget>of(Button.builder(
                        Component.literal("Per-type particles..."),
                        b -> this.minecraft.setScreenAndShow(new AbtoParticleTypesScreen(this)))
                    .bounds(0, 0, ROW_WIDTH, 20).build()));
            }
            if (group.getKey().equals(AbtoOptionRegistry.CAT_ANIM)) {
                this.list.addSmall(List.<AbstractWidget>of(Button.builder(
                        Component.literal("Per-type animations..."),
                        b -> this.minecraft.setScreenAndShow(new AbtoAnimationTypesScreen(this)))
                    .bounds(0, 0, ROW_WIDTH, 20).build()));
            }
        }

        this.list.addHeader(Component.literal("Video"));
        List<OptionInstance<?>> vanilla = VanillaVideoRows.collect(this.options);
        this.list.addSmall(vanilla.toArray(new OptionInstance<?>[0]));
    }

    /**
     * Builds an on/off button for one registry toggle. Clicking it flips the value
     * via the registry (load, invert, save, refresh RenderToggles) so the change
     * takes effect live. Definition, label, and tooltip all come from the shared
     * ToggleOption so this screen and the Sodium screen stay in lockstep.
     */
    private Button renderToggle(ToggleOption option) {
        boolean current = AbtoOptionRegistry.current(configPath(), option);
        return Button.builder(
                Component.literal(option.name() + ": " + onOff(current)),
                b -> {
                    boolean next = AbtoOptionRegistry.flip(configPath(), option);
                    b.setMessage(Component.literal(option.name() + ": " + onOff(next)));
                })
            .bounds(0, 0, 150, 20)
            .tooltip(Tooltip.create(Component.literal(option.tooltip())))
            .build();
    }

    // --- ABTO controls (plain buttons; each persists immediately) ---

    private Button presetButton(AbtoConfig config) {
        PresetButtonList.Entry entry = entryFor(config.selectedPreset);
        return Button.builder(
                Component.literal("Preset: " + entry.label()),
                b -> cyclePreset())
            .bounds(0, 0, ROW_WIDTH, 20)
            .tooltip(Tooltip.create(Component.literal(entry.description())))
            .build();
    }

    private Button shadersButton(AbtoConfig config) {
        return Button.builder(
                Component.literal("Use shaders: " + onOff(config.usesShaders)),
                b -> {
                    AbtoConfig c = ConfigStore.load(configPath());
                    c.usesShaders = !c.usesShaders;
                    ConfigStore.save(configPath(), c);
                    b.setMessage(Component.literal("Use shaders: " + onOff(c.usesShaders)));
                })
            .bounds(0, 0, HALF_WIDTH, 20)
            .tooltip(Tooltip.create(Component.literal(
                "Keep shader-friendly settings when a preset is applied (for Iris).")))
            .build();
    }

    private Button applyModsButton(AbtoConfig config) {
        return Button.builder(
                Component.literal("Apply to other mods: " + onOff(config.applyToOtherMods)),
                b -> {
                    AbtoConfig c = ConfigStore.load(configPath());
                    c.applyToOtherMods = !c.applyToOtherMods;
                    ConfigStore.save(configPath(), c);
                    b.setMessage(Component.literal("Apply to other mods: " + onOff(c.applyToOtherMods)));
                })
            .bounds(0, 0, HALF_WIDTH, 20)
            .tooltip(Tooltip.create(Component.literal(
                "When supported, also tune detected performance mods (Sodium and friends) to match the preset.")))
            .build();
    }

    private Button uiStyleButton(AbtoConfig config) {
        return Button.builder(
                Component.literal("UI style: " + config.uiStyle.label()),
                b -> {
                    AbtoConfig c = ConfigStore.load(configPath());
                    c.uiStyle = c.uiStyle.next();
                    ConfigStore.save(configPath(), c);
                    if (c.uiStyle == com.abto.config.UiStyle.SODIUM) {
                        this.minecraft.setScreenAndShow(new AbtoSodiumScreen(this.lastParent()));
                    } else {
                        b.setMessage(Component.literal("UI style: " + c.uiStyle.label()));
                    }
                })
            .bounds(0, 0, HALF_WIDTH, 20)
            .tooltip(Tooltip.create(Component.literal(
                "Switch between the Sodium-style and Minecraft-style settings screens.")))
            .build();
    }

    /** The screen we return to on Done (the vanilla Options screen). */
    private Screen lastParent() {
        return this.lastScreen;
    }

    private Button runSetupButton() {
        return Button.builder(
                Component.literal("Run setup again"),
                b -> this.minecraft.setScreenAndShow(new AbtoWizardScreen(this)))
            .bounds(0, 0, HALF_WIDTH, 20)
            .build();
    }

    // --- behavior ---

    /** Advance to the next preset in the list, apply it, persist, and refresh. */
    private void cyclePreset() {
        AbtoConfig config = ConfigStore.load(configPath());
        List<PresetButtonList.Entry> entries = PresetButtonList.entries();
        int current = indexOf(entries, config.selectedPreset);
        Preset next = entries.get((current + 1) % entries.size()).preset();
        config.selectedPreset = next;
        if (next != Preset.CUSTOM) {
            PresetEngine.apply(next, config.usesShaders, new MinecraftOptionsTarget(this.minecraft));
        }
        ConfigStore.save(configPath(), config);
        // Rebuild so the preset button label and the vanilla option controls reflect
        // the newly applied values.
        this.rebuildWidgets();
    }

    private static int indexOf(List<PresetButtonList.Entry> entries, Preset preset) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).preset() == preset) {
                return i;
            }
        }
        return 0;
    }

    private static PresetButtonList.Entry entryFor(Preset preset) {
        for (PresetButtonList.Entry e : PresetButtonList.entries()) {
            if (e.preset() == preset) {
                return e;
            }
        }
        return PresetButtonList.entries().get(0);
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("abto.json");
    }
}
