package com.abto.gui.options;

import com.abto.config.AbtoConfig;
import com.abto.config.ConfigStore;
import com.abto.config.UiStyle;
import com.abto.gui.AbtoWizardScreen;
import com.abto.gui.ButtonColumn;
import com.abto.gui.PresetButtonList;
import com.abto.platform.MinecraftOptionsTarget;
import com.abto.preset.Preset;
import com.abto.preset.PresetEngine;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A Sodium-style settings screen. Left column of flat category tabs, a right panel
 * of the selected category's flat option toggles, and a persistent description box
 * along the bottom that shows the hovered option's help text. Every control is a
 * {@link SodiumWidget} (flat dark panels, green accent when on, hover highlight) so
 * the screen looks like Sodium rather than vanilla, while reading the same
 * {@link AbtoOptionRegistry} as the Minecraft-style screen. Layout uses
 * {@link ButtonColumn} so columns fit any GUI scale instead of overflowing. Click
 * handlers act then rebuild, so labels and on/off accents always reflect config.
 */
public final class AbtoSodiumScreen extends Screen {

    private static final String GENERAL = "General";

    private static final int LEFT_X = 16;
    private static final int TAB_WIDTH = 138;
    private static final int PANEL_GAP = 12;
    private static final int TOP = 40;
    private static final int DESC_HEIGHT = 44;
    private static final int FOOTER = 30;

    private final Screen parent;
    private final List<String> tabs = new ArrayList<>();
    private String selected;

    // Option widgets in the right panel paired with their help text, for the
    // hovered-description box.
    private final List<Described> described = new ArrayList<>();

    private record Described(SodiumWidget widget, String description) {
    }

    public AbtoSodiumScreen(Screen parent) {
        super(Component.literal("A Bit Too Optimized"));
        this.parent = parent;
        this.tabs.add(GENERAL);
        this.tabs.addAll(AbtoOptionRegistry.categories());
        this.selected = GENERAL;
    }

    @Override
    protected void init() {
        this.described.clear();

        // Left column: one flat tab per category, accent bar on the current one.
        List<ButtonColumn.Slot> tabSlots =
            ButtonColumn.layout(this.height - TOP - FOOTER, this.tabs.size(), 20, 24, 0);
        for (int i = 0; i < this.tabs.size(); i++) {
            String tab = this.tabs.get(i);
            ButtonColumn.Slot slot = tabSlots.get(i);
            this.addRenderableWidget(
                new SodiumWidget(LEFT_X, TOP + slot.y(), TAB_WIDTH, slot.height(),
                        Component.literal(tab), () -> selectTab(tab))
                    .role(SodiumWidget.Role.TAB)
                    .selected(tab.equals(this.selected)));
        }

        int panelX = LEFT_X + TAB_WIDTH + PANEL_GAP;
        int panelWidth = this.width - panelX - LEFT_X;
        if (this.selected.equals(GENERAL)) {
            buildGeneral(panelX, panelWidth);
        } else {
            buildToggles(panelX, panelWidth);
        }

        // Footer: Done.
        this.addRenderableWidget(new SodiumWidget(this.width / 2 - 100, this.height - 26, 200, 20,
            Component.literal("Done"), this::onClose));
    }

    private void buildToggles(int panelX, int panelWidth) {
        List<ToggleOption> options = AbtoOptionRegistry.byCategory().get(this.selected);
        if (options == null) {
            return;
        }
        // Single column keeps labels full-width and readable. Only fall back to two
        // columns if a category ever grows past what one column can fit.
        int columns = options.size() > 8 ? 2 : 1;
        int colGap = 6;
        int colWidth = columns == 2 ? (panelWidth - colGap) / 2 : panelWidth;
        int rows = (options.size() + columns - 1) / columns;

        // Add one extra row for the "per-type" button on the Particles/Animations tabs.
        boolean particlesTab = this.selected.equals(AbtoOptionRegistry.CAT_PARTICLES);
        boolean animationsTab = this.selected.equals(AbtoOptionRegistry.CAT_ANIM);
        boolean hasExtra = particlesTab || animationsTab;
        int totalRows = rows + (hasExtra ? 1 : 0);
        int band = this.height - TOP - DESC_HEIGHT - FOOTER;
        List<ButtonColumn.Slot> slots = ButtonColumn.layout(band, totalRows, 20, 24, 0);
        for (int i = 0; i < options.size(); i++) {
            ToggleOption option = options.get(i);
            int col = i % columns;
            int row = i / columns;
            ButtonColumn.Slot slot = slots.get(row);
            int x = panelX + col * (colWidth + colGap);
            boolean current = AbtoOptionRegistry.current(configPath(), option);
            SodiumWidget widget = new SodiumWidget(x, TOP + slot.y(), colWidth, slot.height(),
                    label(option.name(), current),
                    () -> {
                        AbtoOptionRegistry.flip(configPath(), option);
                        this.rebuildWidgets();
                    })
                .role(SodiumWidget.Role.TOGGLE)
                .on(current);
            this.addRenderableWidget(widget);
            this.described.add(new Described(widget, option.tooltip()));
        }
        if (hasExtra) {
            ButtonColumn.Slot slot = slots.get(totalRows - 1);
            String text = particlesTab ? "Per-type particles..." : "Per-type animations...";
            String desc = particlesTab
                ? "Disable any specific particle type individually."
                : "Freeze any specific animated texture individually.";
            Runnable open = particlesTab
                ? () -> this.minecraft.setScreenAndShow(new AbtoParticleTypesScreen(this))
                : () -> this.minecraft.setScreenAndShow(new AbtoAnimationTypesScreen(this));
            SodiumWidget button = new SodiumWidget(panelX, TOP + slot.y(), panelWidth,
                slot.height(), Component.literal(text), open);
            this.addRenderableWidget(button);
            this.described.add(new Described(button, desc));
        }
    }

    private void buildGeneral(int panelX, int panelWidth) {
        int band = this.height - TOP - DESC_HEIGHT - FOOTER;
        List<ButtonColumn.Slot> slots = ButtonColumn.layout(band, 6, 20, 24, 0);
        AbtoConfig config = ConfigStore.load(configPath());

        PresetButtonList.Entry entry = entryFor(config.selectedPreset);
        addGeneral(panelX, panelWidth, slots.get(0), SodiumWidget.Role.PLAIN, false,
            Component.literal("Preset: " + entry.label()), entry.description(), this::cyclePreset);

        addGeneral(panelX, panelWidth, slots.get(1), SodiumWidget.Role.PLAIN, false,
            Component.literal("UI style: " + config.uiStyle.label()),
            "Switch between the Sodium-style and Minecraft-style settings screens.",
            this::cycleUiStyle);

        addGeneral(panelX, panelWidth, slots.get(2), SodiumWidget.Role.TOGGLE, config.usesShaders,
            Component.literal("Use shaders: " + onOff(config.usesShaders)),
            "Keep shader-friendly settings when a preset is applied (for Iris).",
            () -> {
                AbtoConfig c = ConfigStore.load(configPath());
                c.usesShaders = !c.usesShaders;
                ConfigStore.save(configPath(), c);
                this.rebuildWidgets();
            });

        addGeneral(panelX, panelWidth, slots.get(3), SodiumWidget.Role.TOGGLE,
            config.applyToOtherMods,
            Component.literal("Apply to other mods: " + onOff(config.applyToOtherMods)),
            "When supported, also tune detected performance mods to match the preset.",
            () -> {
                AbtoConfig c = ConfigStore.load(configPath());
                c.applyToOtherMods = !c.applyToOtherMods;
                ConfigStore.save(configPath(), c);
                this.rebuildWidgets();
            });

        addGeneral(panelX, panelWidth, slots.get(4), SodiumWidget.Role.PLAIN, false,
            Component.literal("Run setup again"), "Re-run the first-run setup wizard.",
            () -> this.minecraft.setScreenAndShow(new AbtoWizardScreen(this)));

        addGeneral(panelX, panelWidth, slots.get(5), SodiumWidget.Role.PLAIN, false,
            Component.literal("Vanilla video settings..."),
            "Open the full vanilla video options (render distance, graphics, and more).",
            () -> this.minecraft.setScreenAndShow(new AbtoVideoScreen(this)));
    }

    private void addGeneral(int panelX, int panelWidth, ButtonColumn.Slot slot,
            SodiumWidget.Role role, boolean on, Component label, String description,
            Runnable onPress) {
        SodiumWidget widget = new SodiumWidget(panelX, TOP + slot.y(), panelWidth, slot.height(),
                label, onPress)
            .role(role)
            .on(on);
        this.addRenderableWidget(widget);
        this.described.add(new Described(widget, description));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        // Draws the background and all widgets (tabs, option toggles, Done).
        super.extractRenderState(g, mouseX, mouseY, delta);

        g.centeredText(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);

        int panelX = LEFT_X + TAB_WIDTH + PANEL_GAP;
        int panelRight = this.width - LEFT_X;

        // Divider between the tab column and the option panel (in the empty gap).
        g.verticalLine(panelX - PANEL_GAP / 2, TOP - 2, this.height - FOOTER, 0x40FFFFFF);

        // Description box: an empty strip above the footer, so filling it after the
        // widgets never covers a control.
        int descTop = this.height - DESC_HEIGHT - FOOTER + 4;
        g.fill(panelX - 2, descTop, panelRight, descTop + DESC_HEIGHT - 6, 0x70000000);
        g.outline(panelX - 2, descTop, panelRight - (panelX - 2), DESC_HEIGHT - 6, 0x33FFFFFF);
        String desc = hoveredDescription(mouseX, mouseY);
        if (desc != null) {
            g.textWithWordWrap(this.font, Component.literal(desc),
                panelX + 4, descTop + 5, panelRight - panelX - 8, 0xFFCCCCCC);
        }
    }

    private String hoveredDescription(int mouseX, int mouseY) {
        for (Described d : this.described) {
            if (d.widget().isMouseOver(mouseX, mouseY)) {
                return d.description();
            }
        }
        return null;
    }

    private void selectTab(String tab) {
        this.selected = tab;
        this.rebuildWidgets();
    }

    private void cycleUiStyle() {
        AbtoConfig config = ConfigStore.load(configPath());
        config.uiStyle = config.uiStyle.next();
        ConfigStore.save(configPath(), config);
        if (config.uiStyle != UiStyle.SODIUM) {
            this.minecraft.setScreenAndShow(new AbtoOptionsScreen(this.parent));
        } else {
            this.rebuildWidgets();
        }
    }

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
        this.rebuildWidgets();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(this.parent);
    }

    private static Component label(String name, boolean value) {
        return Component.literal(name + ": " + onOff(value));
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
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

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("abto.json");
    }
}
