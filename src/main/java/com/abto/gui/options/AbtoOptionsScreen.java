package com.abto.gui.options;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The ABTO Sodium-style video settings screen. This is a minimal stub: it opens
 * and closes back to its parent. Later tasks add the tabbed pages, option rows,
 * and description panel. It replaces the vanilla Video Settings screen, which the
 * OptionsScreenMixin redirects to here.
 */
public final class AbtoOptionsScreen extends Screen {

    private final Screen parent;

    public AbtoOptionsScreen(Screen parent) {
        super(Component.literal("A Bit Too Optimized - Video"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Filled in by later Milestone 4 tasks.
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }
}
