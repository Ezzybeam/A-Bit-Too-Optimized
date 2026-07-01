package com.abto.gui.options;

import com.abto.mixin.VideoSettingsScreenInvoker;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects the vanilla video options (the exact set the vanilla VideoSettingsScreen
 * shows, via VideoSettingsScreenInvoker) so the ABTO screen can add them to its
 * OptionsList. This reuses Minecraft's controls rather than reimplementing them, so
 * they stay correct and adapt across versions.
 */
public final class VanillaVideoRows {

    private VanillaVideoRows() {
    }

    /** All vanilla video options, in the vanilla order (quality, then display, then preference). */
    public static List<OptionInstance<?>> collect(Options options) {
        List<OptionInstance<?>> rows = new ArrayList<>();
        rows.addAll(List.of(VideoSettingsScreenInvoker.abto$qualityOptions(options)));
        rows.addAll(List.of(VideoSettingsScreenInvoker.abto$displayOptions(options)));
        rows.addAll(List.of(VideoSettingsScreenInvoker.abto$preferenceOptions(options)));
        return rows;
    }
}
