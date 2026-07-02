package com.abto.gui.options;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * The full set of vanilla video options (render distance, graphics, brightness,
 * and the rest) on their native scrolling list. The Minecraft-style ABTO screen
 * shows these inline; the Sodium-style screen reaches them through this sub-screen
 * so both UIs offer complete parity with the vanilla Video Settings the mod
 * replaces. Reuses Minecraft's own controls via VanillaVideoRows.
 */
public final class AbtoVideoScreen extends OptionsSubScreen {

    public AbtoVideoScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.literal("Video Settings"));
    }

    @Override
    protected void addOptions() {
        List<OptionInstance<?>> vanilla = VanillaVideoRows.collect(this.options);
        this.list.addSmall(vanilla.toArray(new OptionInstance<?>[0]));
    }
}
