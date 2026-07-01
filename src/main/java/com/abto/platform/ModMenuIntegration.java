package com.abto.platform;

import com.abto.gui.AbtoConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Registers the ABTO config screen as the Mod Menu entry for this mod. */
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new AbtoConfigScreen(parent);
    }
}
