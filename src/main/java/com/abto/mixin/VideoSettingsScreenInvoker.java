package com.abto.mixin;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes the vanilla VideoSettingsScreen's private static option-list builders so
 * the ABTO screen can reuse the exact same set of video options the vanilla screen
 * shows. Using invokers (rather than hardcoding the ~28 Options getters) means the
 * option set auto-adapts to each Minecraft version instead of drifting.
 */
@Mixin(VideoSettingsScreen.class)
public interface VideoSettingsScreenInvoker {

    @Invoker("qualityOptions")
    static OptionInstance<?>[] abto$qualityOptions(Options options) {
        throw new AssertionError();
    }

    @Invoker("displayOptions")
    static OptionInstance<?>[] abto$displayOptions(Options options) {
        throw new AssertionError();
    }

    @Invoker("preferenceOptions")
    static OptionInstance<?>[] abto$preferenceOptions(Options options) {
        throw new AssertionError();
    }
}
