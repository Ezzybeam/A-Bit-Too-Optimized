package com.abto.gui;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure layout helper: computes button positions for a vertical column that
 * always fits within the screen height. No Minecraft or Fabric imports so
 * this class is unit-testable without a Minecraft environment.
 *
 * The normal case uses the preferred height and spacing. When the column
 * would overflow the available band (screenHeight - 2*margin) the heights and
 * spacing are compressed proportionally so the whole group fits.
 */
public final class ButtonColumn {

    /** A single button position and size produced by {@link #layout}. */
    public record Slot(int y, int height) {}

    private ButtonColumn() {}

    /**
     * Compute {@code count} button slots centered vertically.
     *
     * @param screenHeight    total pixel height of the screen
     * @param count           number of buttons
     * @param preferredHeight preferred button height in pixels
     * @param preferredSpacing top-to-top distance between buttons at preferred size
     * @param margin          minimum pixel gap at the top and bottom of the band
     * @return list of Slots ordered top to bottom; every slot satisfies
     *         {@code s.y() >= 0} and {@code s.y() + s.height() <= screenHeight}
     */
    public static List<Slot> layout(
            int screenHeight,
            int count,
            int preferredHeight,
            int preferredSpacing,
            int margin) {

        if (count <= 0) {
            return List.of();
        }

        int available = Math.max(screenHeight - 2 * margin, preferredHeight);

        int height;
        int spacing;

        // Total height using preferred sizes.
        int preferredTotal = (count - 1) * preferredSpacing + preferredHeight;

        if (preferredTotal <= available) {
            // Column fits at preferred dimensions.
            height  = preferredHeight;
            spacing = preferredSpacing;
        } else {
            // Compress: derive stride from available band.
            int stride = available / count;
            height  = Math.min(preferredHeight, Math.max(8, stride - 2));
            spacing = Math.max(height, Math.min(preferredSpacing, stride));
        }

        int total  = (count - 1) * spacing + height;
        int startY = margin + Math.max(0, (available - total) / 2);

        // Clamp: if integer rounding pushes the last button below the allowed
        // bottom, reduce spacing by 1 until it fits.
        int bottomLimit = screenHeight - margin;
        while (count > 1 && startY + (count - 1) * spacing + height > bottomLimit && spacing > height) {
            spacing -= 1;
        }
        // Final clamp of startY so first button is never above the margin.
        if (startY < margin) {
            startY = margin;
        }

        List<Slot> slots = new ArrayList<>(count);
        int y = startY;
        for (int i = 0; i < count; i++) {
            slots.add(new Slot(y, height));
            y += spacing;
        }
        return slots;
    }
}
