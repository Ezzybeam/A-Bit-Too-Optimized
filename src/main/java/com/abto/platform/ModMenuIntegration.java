package com.abto.platform;

import com.abto.gui.options.AbtoOptionsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Registers the ABTO options screen as the Mod Menu entry for this mod. */
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new AbtoOptionsScreen(parent);
    }
}
