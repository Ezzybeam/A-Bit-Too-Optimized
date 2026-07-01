package com.abto.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The main ABTO config screen. This is a minimal stub created in Task 3 so
 * ModMenuIntegration compiles. Task 4 replaces this with the full implementation.
 */
public final class AbtoConfigScreen extends Screen {

    private final Screen parent;

    public AbtoConfigScreen(Screen parent) {
        super(Component.literal("A Bit Too Optimized"));
        this.parent = parent;
    }
}
