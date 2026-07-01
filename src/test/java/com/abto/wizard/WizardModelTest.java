package com.abto.wizard;

import com.abto.config.AbtoConfig;
import com.abto.config.HardwareOverrides;
import com.abto.preset.Preset;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WizardModelTest {
    @Test
    void activeStepsDependOnContext() {
        WizardModel min = new WizardModel(Preset.NORMAL, false, false, false);
        assertEquals(List.of(WizardStep.HARDWARE, WizardStep.PRESET, WizardStep.FINISH), min.activeSteps());

        WizardModel full = new WizardModel(Preset.HIGH, true, true, true);
        assertEquals(List.of(WizardStep.HARDWARE, WizardStep.PRESET, WizardStep.SHADERS,
            WizardStep.APPLY_TO_MODS, WizardStep.RAM_ADVICE, WizardStep.FINISH), full.activeSteps());
    }

    @Test
    void startsOnFirstStepWithRecommendedPreset() {
        WizardModel m = new WizardModel(Preset.LOW, false, false, false);
        assertEquals(WizardStep.HARDWARE, m.currentStep());
        assertEquals(Preset.LOW, m.selectedPreset());
        assertFalse(m.hasPrevious());
        assertTrue(m.hasNext());
    }

    @Test
    void navigationStaysInBounds() {
        WizardModel m = new WizardModel(Preset.NORMAL, false, false, false);
        m.next(); // PRESET
        m.next(); // FINISH
        assertEquals(WizardStep.FINISH, m.currentStep());
        assertFalse(m.hasNext());
        m.next(); // no-op at the end
        assertEquals(WizardStep.FINISH, m.currentStep());
        m.previous();
        assertEquals(WizardStep.PRESET, m.currentStep());
    }

    @Test
    void toConfigAppliesChoicesAndMarksWizardDone() {
        WizardModel m = new WizardModel(Preset.NORMAL, true, true, false);
        m.setSelectedPreset(Preset.POTATO);
        m.setUsesShaders(true);
        m.setApplyToOtherMods(false);
        m.setHardwareOverrides(new HardwareOverrides(null, 4096L, 4, "Intel UHD"));

        AbtoConfig out = m.toConfig(AbtoConfig.defaults());
        assertEquals(Preset.POTATO, out.selectedPreset);
        assertTrue(out.usesShaders);
        assertFalse(out.applyToOtherMods);
        assertEquals(4096L, out.hardwareOverrides.allocatedRamMb());
        assertTrue(out.wizardCompleted);
    }
}
