package com.abto.gui;

import com.abto.preset.Preset;
import com.abto.preset.Presets;
import java.util.ArrayList;
import java.util.List;

/**
 * The preset choices shown as buttons, each with a short plain-ASCII description.
 * Pure data so the labels and descriptions are unit tested and the screen stays
 * a thin drawing layer.
 */
public final class PresetButtonList {

    public record Entry(Preset preset, String label, String description) {
    }

    private PresetButtonList() {
    }

    public static List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        for (Preset p : Presets.ORDER) {
            entries.add(new Entry(p, label(p), description(p)));
        }
        entries.add(new Entry(Preset.CUSTOM, "Custom", "Control every setting yourself. No preset is applied."));
        return List.copyOf(entries);
    }

    private static String label(Preset p) {
        return switch (p) {
            case ULTRA -> "Ultra";
            case HIGH -> "High";
            case NORMAL -> "Normal";
            case LOW -> "Low";
            case VERY_LOW -> "Very Low";
            case POTATO -> "Potato";
            case CUSTOM -> "Custom";
        };
    }

    private static String description(Preset p) {
        return switch (p) {
            case ULTRA -> "High-end PCs. Max view distance, all effects on.";
            case HIGH -> "Good gaming PCs. High distance, most effects on.";
            case NORMAL -> "Average PCs. Balanced default.";
            case LOW -> "Older or weaker PCs. Reduced effects and distance.";
            case VERY_LOW -> "Weak PCs. Aggressive cuts.";
            case POTATO -> "Old laptops. 30 to 60 FPS target at 8 to 12 render distance.";
            case CUSTOM -> "Control every setting yourself. No preset is applied.";
        };
    }
}
