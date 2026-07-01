package com.abto.wizard;

import com.abto.config.AbtoConfig;
import com.abto.config.HardwareOverrides;
import com.abto.preset.Preset;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the wizard's state and logic with no Minecraft dependency, so it is unit
 * testable. The screen reads currentStep() to decide what to draw and calls
 * next()/previous() and the choice setters. toConfig() turns the choices into a
 * persisted AbtoConfig.
 */
public final class WizardModel {

    private final List<WizardStep> activeSteps;
    private int index = 0;

    private Preset selectedPreset;
    private boolean usesShaders = false;
    private boolean applyToOtherMods = true;
    private HardwareOverrides hardwareOverrides = HardwareOverrides.none();

    public WizardModel(Preset recommended, boolean irisPresent, boolean anyPerfModPresent, boolean ramLooksLow) {
        this.selectedPreset = recommended;
        List<WizardStep> steps = new ArrayList<>();
        steps.add(WizardStep.HARDWARE);
        steps.add(WizardStep.PRESET);
        if (irisPresent) {
            steps.add(WizardStep.SHADERS);
        }
        if (anyPerfModPresent) {
            steps.add(WizardStep.APPLY_TO_MODS);
        }
        if (ramLooksLow) {
            steps.add(WizardStep.RAM_ADVICE);
        }
        steps.add(WizardStep.FINISH);
        this.activeSteps = List.copyOf(steps);
    }

    public List<WizardStep> activeSteps() {
        return activeSteps;
    }

    public WizardStep currentStep() {
        return activeSteps.get(index);
    }

    public boolean hasNext() {
        return index < activeSteps.size() - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void next() {
        if (hasNext()) {
            index++;
        }
    }

    public void previous() {
        if (hasPrevious()) {
            index--;
        }
    }

    public Preset selectedPreset() {
        return selectedPreset;
    }

    public void setSelectedPreset(Preset preset) {
        this.selectedPreset = preset;
    }

    public boolean usesShaders() {
        return usesShaders;
    }

    public void setUsesShaders(boolean usesShaders) {
        this.usesShaders = usesShaders;
    }

    public boolean applyToOtherMods() {
        return applyToOtherMods;
    }

    public void setApplyToOtherMods(boolean applyToOtherMods) {
        this.applyToOtherMods = applyToOtherMods;
    }

    public HardwareOverrides hardwareOverrides() {
        return hardwareOverrides;
    }

    public void setHardwareOverrides(HardwareOverrides hardwareOverrides) {
        this.hardwareOverrides = hardwareOverrides;
    }

    public AbtoConfig toConfig(AbtoConfig base) {
        base.selectedPreset = selectedPreset;
        base.usesShaders = usesShaders;
        base.applyToOtherMods = applyToOtherMods;
        base.hardwareOverrides = hardwareOverrides;
        base.wizardCompleted = true;
        return base;
    }
}
