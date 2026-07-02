package com.abto.gui.options;

import com.abto.config.FeatureToggles;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * One render feature toggle, described as data so any UI can render it. The
 * getter/setter operate on a FeatureToggles instance; category groups the
 * toggle under a heading (Minecraft-style screen) or a tab (Sodium-style
 * screen). Keeping toggles as data means a new UI is just a new view over the
 * same registry, with no per-toggle code duplicated between screens.
 */
public record ToggleOption(
        String category,
        String name,
        String tooltip,
        Predicate<FeatureToggles> getter,
        BiConsumer<FeatureToggles, Boolean> setter) {
}
