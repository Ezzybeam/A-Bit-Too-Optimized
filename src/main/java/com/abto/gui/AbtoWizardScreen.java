package com.abto.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Minimal stub for the setup wizard screen. Task 5 replaces this with the full
 * implementation. Created here so AbtoConfigScreen can wire the "Run setup
 * again" button without a compile error.
 */
public final class AbtoWizardScreen extends Screen {

    private final Screen parent;

    public AbtoWizardScreen(Screen parent) {
        super(Component.literal("A Bit Too Optimized - Setup"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }
}
