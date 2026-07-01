package com.abto.gui;

import com.abto.config.AbtoConfig;
import com.abto.config.ConfigStore;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import java.nio.file.Path;

/**
 * Shows the first-run wizard once, the first time the title screen appears,
 * when the wizard has not been completed yet. Uses a session guard so it never
 * reopens within one game launch.
 *
 * ScreenEvents.AFTER_INIT lambda signature confirmed via javap against
 * fabric-screen-api-v1-5.0.4+ffbe97199e.jar:
 *   AfterInit.afterInit(Minecraft, Screen, int, int)
 * i.e. (client, screen, scaledWidth, scaledHeight)
 *
 * TitleScreen package confirmed via javap against
 * minecraft-merged-deobf-26.1.2.jar (Mojang mappings):
 *   net.minecraft.client.gui.screens.TitleScreen
 *
 * Screen open method matches the other ABTO screens (AbtoWizardScreen,
 * AbtoOptionsScreen): client.setScreenAndShow(...)
 * Same API present in 26.2; no Stonecutter version guards required.
 */
public final class WizardLauncher {

    private static boolean shownThisSession = false;

    private WizardLauncher() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (shownThisSession || !(screen instanceof TitleScreen)) {
                return;
            }
            Path path = FabricLoader.getInstance().getConfigDir().resolve("abto.json");
            AbtoConfig config = ConfigStore.load(path);
            if (!config.wizardCompleted) {
                shownThisSession = true;
                client.setScreenAndShow(new AbtoWizardScreen(screen));
            }
        });
    }
}
