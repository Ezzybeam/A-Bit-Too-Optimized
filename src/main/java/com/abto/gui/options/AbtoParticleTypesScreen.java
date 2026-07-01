package com.abto.gui.options;

import com.abto.render.ParticleCatalog;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * A scrollable list of every registered particle type, each with a Shown/Hidden
 * toggle that adds or removes the type from the disabled set. Extends the vanilla
 * OptionsSubScreen so it gets the native scrolling list and Done footer for free -
 * no custom scrolling needed for the ~90 entries. Reached from a "Per-type
 * particles" button on the Particles category of either settings screen.
 */
public final class AbtoParticleTypesScreen extends OptionsSubScreen {

    public AbtoParticleTypesScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.literal("Particle Types"));
    }

    @Override
    protected void addOptions() {
        List<String> ids = ParticleCatalog.allTypeIds();
        for (int i = 0; i < ids.size(); i += 2) {
            if (i + 1 < ids.size()) {
                this.list.addSmall(List.<AbstractWidget>of(
                    typeButton(ids.get(i)), typeButton(ids.get(i + 1))));
            } else {
                this.list.addSmall(List.<AbstractWidget>of(typeButton(ids.get(i))));
            }
        }
    }

    private Button typeButton(String id) {
        boolean disabled = ParticleCatalog.isDisabled(configPath(), id);
        return Button.builder(label(id, disabled), b -> {
                boolean nowDisabled = ParticleCatalog.flip(configPath(), id);
                b.setMessage(label(id, nowDisabled));
            })
            .bounds(0, 0, 150, 20)
            .build();
    }

    private static Component label(String id, boolean disabled) {
        return Component.literal(shortName(id) + ": " + (disabled ? "Hidden" : "Shown"));
    }

    /** Drop the "minecraft:" namespace for readability; keep any other namespace. */
    private static String shortName(String id) {
        return id.startsWith("minecraft:") ? id.substring("minecraft:".length()) : id;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("abto.json");
    }
}
