package com.abto.render;

/**
 * A small named color palette for the Fun tab's custom grass/foliage/water colors.
 * Index 0 is "Default" (no override, use the vanilla biome color); indices 1+ are
 * fixed colors applied via BiomeColorMixin. Colors include full alpha so they work
 * whether or not the tint path uses the alpha byte.
 */
public final class FunColors {

    private static final String[] NAMES = {
        "Default", "Red", "Orange", "Yellow", "Lime", "Green",
        "Cyan", "Blue", "Purple", "Pink", "White", "Black"
    };

    private static final int[] COLORS = {
        0, 0xFFE53935, 0xFFFB8C00, 0xFFFDD835, 0xFF7CB342, 0xFF43A047,
        0xFF00ACC1, 0xFF1E88E5, 0xFF8E24AA, 0xFFEC407A, 0xFFFFFFFF, 0xFF212121
    };

    private FunColors() {
    }

    public static int count() {
        return NAMES.length;
    }

    public static String name(int index) {
        return NAMES[Math.floorMod(index, NAMES.length)];
    }

    /** The color for an index, or 0 for "Default" (index 0). */
    public static int color(int index) {
        return COLORS[Math.floorMod(index, COLORS.length)];
    }
}
