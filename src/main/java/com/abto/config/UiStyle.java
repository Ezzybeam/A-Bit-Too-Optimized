package com.abto.config;

/**
 * Which settings screen ABTO opens when the Video Settings button is clicked.
 * MINECRAFT is the vanilla-button scrolling list; SODIUM is the tabbed two-pane
 * layout. Selecting a style is the strategy switch: the routing mixin reads this
 * and constructs the matching screen, so both UIs show the same option registry.
 */
public enum UiStyle {
    MINECRAFT("Minecraft style"),
    SODIUM("Sodium style");

    private final String label;

    UiStyle(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** Next style in the cycle, for a single-button toggle. */
    public UiStyle next() {
        return this == MINECRAFT ? SODIUM : MINECRAFT;
    }
}
