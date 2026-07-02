package com.abto.gui.options;

import com.abto.config.AbtoConfig;
import com.abto.config.ConfigStore;
import com.abto.gui.AbtoWizardScreen;
import com.abto.gui.ButtonColumn;
import com.abto.gui.PresetButtonList;
import com.abto.platform.MinecraftOptionsTarget;
import com.abto.preset.Preset;
import com.abto.preset.PresetEngine;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A Sodium-style settings screen: a left column of category tabs, a right panel
 * of the selected category's options, and a persistent description box along the
 * bottom that shows the hovered option's help text. It reads the same
 * {@link AbtoOptionRegistry} as the Minecraft-style screen, so both UIs always
 * show identical settings. Layout uses {@link ButtonColumn} so the columns
 * compress to fit any GUI scale instead of overflowing (which previously caused
 * a scissor crash at scale 4).
 */
public final class AbtoSodiumScreen extends Screen {

    private static final String GENERAL = "General";

    private static final int LEFT_X = 16;
    private static final int TAB_WIDTH = 116;
    private static final int PANEL_GAP = 12;
    private static final int TOP = 40;
    private static final int DESC_HEIGHT = 44;
    private static final int FOOTER = 30;

    private final Screen parent;
    private final List<String> tabs = new ArrayList<>();
    private String selected;

    // Option buttons in the right panel paired with their help text, for the
    // hovered-description box.
    private final List<DescribedButton> described = new ArrayList<>();

    private record DescribedButton(Button button, String description) {
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

        // Left column: one tab per category. The active tab is drawn inactive so it
        // reads as "you are here".
        List<ButtonColumn.Slot> tabSlots =
            ButtonColumn.layout(this.height - TOP - FOOTER, this.tabs.size(), 20, 24, 0);
        for (int i = 0; i < this.tabs.size(); i++) {
            String tab = this.tabs.get(i);
            ButtonColumn.Slot slot = tabSlots.get(i);
            Button tabButton = Button.builder(Component.literal(tab), b -> selectTab(tab))
                .bounds(LEFT_X, TOP + slot.y(), TAB_WIDTH, slot.height())
                .build();
            tabButton.active = !tab.equals(this.selected);
            this.addRenderableWidget(tabButton);
        }

        // Right panel: the selected category's controls.
        int panelX = LEFT_X + TAB_WIDTH + PANEL_GAP;
        int panelWidth = this.width - panelX - LEFT_X;
        if (this.selected.equals(GENERAL)) {
            buildGeneral(panelX, panelWidth);
        } else {
            buildToggles(panelX, panelWidth);
        }

        // Footer: Done.
        this.addRenderableWidget(
            Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 26, 200, 20)
                .build());
    }

    private void buildToggles(int panelX, int panelWidth) {
        List<ToggleOption> options = AbtoOptionRegistry.byCategory().get(this.selected);
        if (options == null) {
            return;
        }
        // Two columns so a tab with many toggles (Entities has 10) forms half as many
        // rows and never has to compress to unreadable heights. Rows are laid out by
        // ButtonColumn on the row count, so the column still fits any GUI scale.
        int columns = options.size() > 5 ? 2 : 1;
        int colGap = 6;
        int colWidth = columns == 2 ? (panelWidth - colGap) / 2 : panelWidth;
        int rows = (options.size() + columns - 1) / columns;

        int band = this.height - TOP - DESC_HEIGHT - FOOTER;
        List<ButtonColumn.Slot> slots = ButtonColumn.layout(band, rows, 20, 24, 0);
        for (int i = 0; i < options.size(); i++) {
            ToggleOption option = options.get(i);
            int col = i % columns;
            int row = i / columns;
            ButtonColumn.Slot slot = slots.get(row);
            int x = panelX + col * (colWidth + colGap);
            boolean current = AbtoOptionRegistry.current(configPath(), option);
            Button button = Button.builder(label(option.name(), current), b -> {
                    boolean next = AbtoOptionRegistry.flip(configPath(), option);
                    b.setMessage(label(option.name(), next));
                })
                .bounds(x, TOP + slot.y(), colWidth, slot.height())
                .build();
            this.addRenderableWidget(button);
            this.described.add(new DescribedButton(button, option.tooltip()));
        }
    }

    private void buildGeneral(int panelX, int panelWidth) {
        int band = this.height - TOP - DESC_HEIGHT - FOOTER;
        // Five general controls in a single full-width column.
        List<ButtonColumn.Slot> slots = ButtonColumn.layout(band, 5, 20, 24, 0);

        AbtoConfig config = ConfigStore.load(configPath());

        PresetButtonList.Entry entry = entryFor(config.selectedPreset);
        Button preset = Button.builder(
                Component.literal("Preset: " + entry.label()), b -> cyclePreset())
            .bounds(panelX, TOP + slots.get(0).y(), panelWidth, slots.get(0).height())
            .build();
        this.addRenderableWidget(preset);
        this.described.add(new DescribedButton(preset, entry.description()));

        Button uiStyle = Button.builder(
                Component.literal("UI style: " + config.uiStyle.label()), b -> cycleUiStyle())
            .bounds(panelX, TOP + slots.get(1).y(), panelWidth, slots.get(1).height())
            .build();
        this.addRenderableWidget(uiStyle);
        this.described.add(new DescribedButton(uiStyle,
            "Switch between the Sodium-style and Minecraft-style settings screens."));

        Button shaders = Button.builder(
                Component.literal("Use shaders: " + onOff(config.usesShaders)),
                b -> {
                    AbtoConfig c = ConfigStore.load(configPath());
                    c.usesShaders = !c.usesShaders;
                    ConfigStore.save(configPath(), c);
                    b.setMessage(Component.literal("Use shaders: " + onOff(c.usesShaders)));
                })
            .bounds(panelX, TOP + slots.get(2).y(), panelWidth, slots.get(2).height())
            .build();
        this.addRenderableWidget(shaders);
        this.described.add(new DescribedButton(shaders,
            "Keep shader-friendly settings when a preset is applied (for Iris)."));

        Button applyMods = Button.builder(
                Component.literal("Apply to other mods: " + onOff(config.applyToOtherMods)),
                b -> {
                    AbtoConfig c = ConfigStore.load(configPath());
                    c.applyToOtherMods = !c.applyToOtherMods;
                    ConfigStore.save(configPath(), c);
                    b.setMessage(Component.literal(
                        "Apply to other mods: " + onOff(c.applyToOtherMods)));
                })
            .bounds(panelX, TOP + slots.get(3).y(), panelWidth, slots.get(3).height())
            .build();
        this.addRenderableWidget(applyMods);
        this.described.add(new DescribedButton(applyMods,
            "When supported, also tune detected performance mods to match the preset."));

        Button runSetup = Button.builder(
                Component.literal("Run setup again"),
                b -> this.minecraft.setScreenAndShow(new AbtoWizardScreen(this)))
            .bounds(panelX, TOP + slots.get(4).y(), panelWidth, slots.get(4).height())
            .build();
        this.addRenderableWidget(runSetup);
        this.described.add(new DescribedButton(runSetup, "Re-run the first-run setup wizard."));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        // Draws the background and all widgets (tabs, option buttons, Done).
        super.extractRenderState(g, mouseX, mouseY, delta);

        // Title.
        g.centeredText(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);

        int panelX = LEFT_X + TAB_WIDTH + PANEL_GAP;
        int panelRight = this.width - LEFT_X;

        // Divider between the tab column and the option panel. Drawn in the empty gap
        // so it never paints over a widget (draw order here is on top of widgets).
        g.verticalLine(panelX - PANEL_GAP / 2, TOP - 2, this.height - FOOTER, 0x40FFFFFF);

        // Description box: an empty strip between the options and the footer, so filling
        // it after the widgets does not cover any button.
        int descTop = this.height - DESC_HEIGHT - FOOTER + 4;
        g.fill(panelX - 2, descTop, panelRight, descTop + DESC_HEIGHT - 6, 0x70000000);
        String desc = hoveredDescription(mouseX, mouseY);
        if (desc != null) {
            g.textWithWordWrap(this.font, Component.literal(desc),
                panelX + 4, descTop + 5, panelRight - panelX - 8, 0xFFCCCCCC);
        }
    }

    private String hoveredDescription(int mouseX, int mouseY) {
        for (DescribedButton d : this.described) {
            if (d.button().isMouseOver(mouseX, mouseY)) {
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
        // If switched away from Sodium, hand off to the Minecraft-style screen live.
        if (config.uiStyle != com.abto.config.UiStyle.SODIUM) {
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
