package com.abto.gui;

import com.abto.compat.KnownMods;
import com.abto.compat.ModPresenceDetector;
import com.abto.config.AbtoConfig;
import com.abto.config.ConfigStore;
import com.abto.config.HardwareOverrides;
import com.abto.hardware.EffectiveHardware;
import com.abto.hardware.HardwareInfo;
import com.abto.hardware.HardwareProbe;
import com.abto.platform.FabricModPresence;
import com.abto.platform.MinecraftOptionsTarget;
import com.abto.platform.RuntimeHardware;
import com.abto.preset.Preset;
import com.abto.preset.PresetEngine;
import com.abto.recommend.RecommendationEngine;
import com.abto.wizard.OverrideInput;
import com.abto.wizard.WizardModel;
import com.abto.wizard.WizardStep;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * First-run setup wizard. Walks the player through hardware confirmation,
 * preset selection, and optional steps (shaders, mod compat, RAM advice).
 * Saves config and applies the chosen preset on Finish.
 *
 * Widget API confirmed via javap against minecraft-merged-deobf-26.1.2.jar
 * (Mojang official mappings):
 *   - Screen.extractRenderState(GuiGraphicsExtractor, int, int, float)
 *   - GuiGraphicsExtractor.text(Font, Component, int, int, int)
 *   - GuiGraphicsExtractor.centeredText(Font, Component, int, int, int)
 *   - EditBox(Font, x, y, width, height, Component)
 *   - EditBox.setValue(String), getValue(), setMaxLength(int)
 *   - AbstractWidget.getX(), getY() (via LayoutElement)
 *   - Button.builder(Component, OnPress).bounds(x,y,w,h).build()
 *   - Screen.addRenderableWidget(T), rebuildWidgets(), this.font, this.width, this.height
 *   - Minecraft.setScreenAndShow(Screen)
 * Same API present in 26.2; no Stonecutter version guards required.
 */
public final class AbtoWizardScreen extends Screen {

    // Layout constants
    private static final int BTN_W    = 200;
    private static final int BTN_H    = 20;
    private static final int BOX_W    = 200;
    private static final int BOX_H    = 20;
    // Gap added to BOX_H between EditBox rows (label-height + padding)
    private static final int ROW_STEP = BOX_H + 23;
    // Y distance from top of EditBox to its label
    private static final int LABEL_ABOVE = 13;

    // Colors (ARGB: top byte is alpha; 0xFF = fully opaque)
    private static final int COL_WHITE = 0xFFFFFFFF;
    private static final int COL_GRAY  = 0xFFAAAAAA;

    private final Screen parent;
    private final WizardModel model;
    private final HardwareInfo detected;
    private final Preset recommended;
    private final Set<String> presentMods;
    private final Path configPath;

    // HARDWARE step EditBox fields; null on all other steps
    private EditBox allocatedRamBox;
    private EditBox totalRamBox;
    private EditBox cpuCoresBox;
    private EditBox gpuNameBox;

    public AbtoWizardScreen(Screen parent) {
        super(Component.literal("A Bit Too Optimized - Setup"));
        this.parent     = parent;
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve("abto.json");

        this.detected = HardwareProbe.detect(
            RuntimeHardware::maxMemoryBytes,
            RuntimeHardware::cpuCores,
            RuntimeHardware::totalRamMb,
            RuntimeHardware::gpuName
        );

        AbtoConfig cfg = ConfigStore.load(this.configPath);
        HardwareOverrides existingOverrides = cfg.hardwareOverrides != null
            ? cfg.hardwareOverrides : HardwareOverrides.none();

        EffectiveHardware effective = EffectiveHardware.resolve(detected, existingOverrides);
        this.recommended  = RecommendationEngine.recommend(effective);
        this.presentMods  = ModPresenceDetector.detect(FabricModPresence.isLoaded());

        boolean irisPresent      = presentMods.contains("iris");
        boolean anyPerfModPresent = presentMods.stream().anyMatch(KnownMods.PERFORMANCE::contains);
        boolean ramLooksLow       = effective.allocatedRamMb() < 4096;

        this.model = new WizardModel(recommended, irisPresent, anyPerfModPresent, ramLooksLow);
    }

    // -------------------------------------------------------------------------
    // Widget construction
    // -------------------------------------------------------------------------

    @Override
    protected void init() {
        allocatedRamBox = null;
        totalRamBox     = null;
        cpuCoresBox     = null;
        gpuNameBox      = null;

        switch (model.currentStep()) {
            case HARDWARE      -> initHardware();
            case PRESET        -> initPreset();
            case SHADERS       -> initShaders();
            case APPLY_TO_MODS -> initApplyToMods();
            case RAM_ADVICE    -> { /* static text only; nav buttons below */ }
            case FINISH        -> { /* nav buttons handle finish */ }
        }

        initNav();
    }

    private void initHardware() {
        int boxX = this.width / 2 - BOX_W / 2;
        int y    = 55;

        allocatedRamBox = editBox(boxX, y, "Allocated RAM MB");
        allocatedRamBox.setMaxLength(10);
        allocatedRamBox.setValue(String.valueOf(detected.allocatedRamMb()));
        addRenderableWidget(allocatedRamBox);
        y += ROW_STEP;

        totalRamBox = editBox(boxX, y, "Total System RAM MB");
        totalRamBox.setMaxLength(10);
        detected.totalRamMb().ifPresent(v -> totalRamBox.setValue(String.valueOf(v)));
        addRenderableWidget(totalRamBox);
        y += ROW_STEP;

        cpuCoresBox = editBox(boxX, y, "CPU Cores");
        cpuCoresBox.setMaxLength(4);
        cpuCoresBox.setValue(String.valueOf(detected.cpuCores()));
        addRenderableWidget(cpuCoresBox);
        y += ROW_STEP;

        gpuNameBox = editBox(boxX, y, "GPU Name");
        gpuNameBox.setMaxLength(100);
        detected.gpuName().ifPresent(gpuNameBox::setValue);
        addRenderableWidget(gpuNameBox);
    }

    private void initPreset() {
        int cx = this.width / 2;
        int y  = 44;
        for (PresetButtonList.Entry e : PresetButtonList.entries()) {
            Preset p = e.preset();
            String suffix = (p == recommended) ? " (recommended)" : "";
            String label  = e.label() + suffix;
            addRenderableWidget(
                Button.builder(Component.literal(label), btn -> model.setSelectedPreset(p))
                    .bounds(cx - BTN_W / 2, y, BTN_W, BTN_H).build()
            );
            y += BTN_H + 4;
        }
    }

    private void initShaders() {
        int cx       = this.width / 2;
        int y        = this.height / 2;
        boolean uses = model.usesShaders();
        addRenderableWidget(
            Button.builder(Component.literal(uses ? "Yes (selected)" : "Yes"),
                    btn -> model.setUsesShaders(true))
                .bounds(cx - BTN_W / 2, y, BTN_W / 2 - 2, BTN_H).build()
        );
        addRenderableWidget(
            Button.builder(Component.literal(!uses ? "No (selected)" : "No"),
                    btn -> model.setUsesShaders(false))
                .bounds(cx + 2, y, BTN_W / 2 - 2, BTN_H).build()
        );
    }

    private void initApplyToMods() {
        int cx      = this.width / 2;
        int y       = this.height - 55;
        boolean app = model.applyToOtherMods();
        addRenderableWidget(
            Button.builder(Component.literal(app ? "Yes (selected)" : "Yes"),
                    btn -> model.setApplyToOtherMods(true))
                .bounds(cx - BTN_W / 2, y, BTN_W / 2 - 2, BTN_H).build()
        );
        addRenderableWidget(
            Button.builder(Component.literal(!app ? "No (selected)" : "No"),
                    btn -> model.setApplyToOtherMods(false))
                .bounds(cx + 2, y, BTN_W / 2 - 2, BTN_H).build()
        );
    }

    private void initNav() {
        int navY = this.height - 28;
        int cx   = this.width / 2;

        if (model.hasPrevious()) {
            addRenderableWidget(
                Button.builder(Component.literal("Back"), btn -> {
                    model.previous();
                    rebuildWidgets();
                }).bounds(cx - BTN_W / 2, navY, BTN_W / 2 - 2, BTN_H).build()
            );
        }

        int nextX = model.hasPrevious() ? cx + 2 : cx - BTN_W / 2;
        int nextW = model.hasPrevious() ? BTN_W / 2 - 2 : BTN_W;

        if (model.hasNext()) {
            addRenderableWidget(
                Button.builder(Component.literal("Next"), btn -> {
                    if (model.currentStep() == WizardStep.HARDWARE) {
                        readHardwareOverrides();
                    }
                    model.next();
                    rebuildWidgets();
                }).bounds(nextX, navY, nextW, BTN_H).build()
            );
        } else {
            addRenderableWidget(
                Button.builder(Component.literal("Finish"), btn -> finish())
                    .bounds(nextX, navY, nextW, BTN_H).build()
            );
        }
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);

        int cx     = this.width / 2;
        int stepNo = model.activeSteps().indexOf(model.currentStep()) + 1;
        int total  = model.activeSteps().size();

        g.centeredText(this.font,
            Component.literal("A Bit Too Optimized - Setup (" + stepNo + "/" + total + ")"),
            cx, 10, COL_WHITE);

        switch (model.currentStep()) {
            case HARDWARE      -> drawHardware(g, cx);
            case PRESET        -> drawPreset(g, cx);
            case SHADERS       -> drawShaders(g, cx);
            case APPLY_TO_MODS -> drawApplyToMods(g, cx);
            case RAM_ADVICE    -> drawRamAdvice(g, cx);
            case FINISH        -> drawFinish(g, cx);
        }
    }

    private void drawHardware(GuiGraphicsExtractor g, int cx) {
        g.centeredText(this.font,
            Component.literal("Review detected hardware. Correct any wrong values."),
            cx, 26, COL_GRAY);

        if (allocatedRamBox != null) {
            g.text(this.font,
                Component.literal("Allocated RAM (MB, detected: " + detected.allocatedRamMb() + ")"),
                allocatedRamBox.getX(), allocatedRamBox.getY() - LABEL_ABOVE, COL_GRAY);
        }
        if (totalRamBox != null) {
            String val = detected.totalRamMb().isPresent()
                ? String.valueOf(detected.totalRamMb().getAsLong()) : "unknown";
            g.text(this.font,
                Component.literal("Total System RAM (MB, detected: " + val + ")"),
                totalRamBox.getX(), totalRamBox.getY() - LABEL_ABOVE, COL_GRAY);
        }
        if (cpuCoresBox != null) {
            g.text(this.font,
                Component.literal("CPU Cores (detected: " + detected.cpuCores() + ")"),
                cpuCoresBox.getX(), cpuCoresBox.getY() - LABEL_ABOVE, COL_GRAY);
        }
        if (gpuNameBox != null) {
            String val = detected.gpuName().orElse("unknown");
            g.text(this.font,
                Component.literal("GPU Name (detected: " + val + ")"),
                gpuNameBox.getX(), gpuNameBox.getY() - LABEL_ABOVE, COL_GRAY);
        }
    }

    private void drawPreset(GuiGraphicsExtractor g, int cx) {
        g.centeredText(this.font,
            Component.literal("Choose a performance preset:"),
            cx, 30, COL_GRAY);
    }

    private void drawShaders(GuiGraphicsExtractor g, int cx) {
        g.centeredText(this.font,
            Component.literal("Do you use shaders (Iris/OptiFine)?"),
            cx, 30, COL_WHITE);
        g.centeredText(this.font,
            Component.literal("ABTO boosts settings automatically when shaders are off."),
            cx, 46, COL_GRAY);
    }

    private void drawApplyToMods(GuiGraphicsExtractor g, int cx) {
        g.centeredText(this.font,
            Component.literal("Apply ABTO preset to compatible performance mods?"),
            cx, 26, COL_WHITE);
        g.centeredText(this.font,
            Component.literal("Detected performance mods:"),
            cx, 42, COL_GRAY);

        List<String> perf = presentMods.stream()
            .filter(KnownMods.PERFORMANCE::contains)
            .toList();

        int y = 56;
        if (perf.isEmpty()) {
            g.centeredText(this.font, Component.literal("(none detected)"), cx, y, COL_GRAY);
        } else {
            for (String id : perf) {
                g.centeredText(this.font, Component.literal("- " + id), cx, y, COL_GRAY);
                y += 12;
            }
        }
    }

    private void drawRamAdvice(GuiGraphicsExtractor g, int cx) {
        int y = 30;
        g.centeredText(this.font, Component.literal("RAM Advice"), cx, y, COL_WHITE);
        y += 16;
        g.centeredText(this.font,
            Component.literal("Current allocated RAM: " + detected.allocatedRamMb() + " MB"),
            cx, y, COL_GRAY);
        y += 14;
        g.centeredText(this.font,
            Component.literal("Recommended: at least 4096 MB for a smooth experience."),
            cx, y, COL_GRAY);
        y += 14;
        g.centeredText(this.font,
            Component.literal("Increase it in your launcher's JVM arguments."),
            cx, y, COL_GRAY);
    }

    private void drawFinish(GuiGraphicsExtractor g, int cx) {
        int y = 30;
        g.centeredText(this.font, Component.literal("Setup complete!"), cx, y, COL_WHITE);
        y += 18;
        g.centeredText(this.font,
            Component.literal("Selected preset: " + model.selectedPreset().name()),
            cx, y, COL_GRAY);
        y += 14;
        g.centeredText(this.font,
            Component.literal("Shaders: " + (model.usesShaders() ? "Yes" : "No")),
            cx, y, COL_GRAY);
        y += 14;
        g.centeredText(this.font,
            Component.literal("Apply to other mods: " + (model.applyToOtherMods() ? "Yes" : "No")),
            cx, y, COL_GRAY);
        y += 18;
        g.centeredText(this.font,
            Component.literal("Click Finish to save and apply your settings."),
            cx, y, COL_GRAY);
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    private void readHardwareOverrides() {
        Long    allocMb = null;
        Long    totalMb = null;
        Integer cores   = null;
        String  gpuName = null;

        if (allocatedRamBox != null) {
            var ol = OverrideInput.parsePositiveLong(allocatedRamBox.getValue());
            if (ol.isPresent()) allocMb = ol.getAsLong();
        }
        if (totalRamBox != null) {
            var ol = OverrideInput.parsePositiveLong(totalRamBox.getValue());
            if (ol.isPresent()) totalMb = ol.getAsLong();
        }
        if (cpuCoresBox != null) {
            var oi = OverrideInput.parsePositiveInt(cpuCoresBox.getValue());
            if (oi.isPresent()) cores = oi.getAsInt();
        }
        if (gpuNameBox != null) {
            var os = OverrideInput.cleanGpuName(gpuNameBox.getValue());
            gpuName = os.orElse(null);
        }

        model.setHardwareOverrides(new HardwareOverrides(totalMb, allocMb, cores, gpuName));
    }

    private void finish() {
        AbtoConfig config = ConfigStore.load(this.configPath);
        model.toConfig(config);
        ConfigStore.save(this.configPath, config);
        if (config.selectedPreset != Preset.CUSTOM) {
            PresetEngine.apply(config.selectedPreset, config.usesShaders,
                new MinecraftOptionsTarget(this.minecraft));
        }
        this.minecraft.setScreenAndShow(parent);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private EditBox editBox(int x, int y, String narration) {
        return new EditBox(this.font, x, y, BOX_W, BOX_H, Component.literal(narration));
    }
}
