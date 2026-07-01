package com.abto.gui.options;

import com.abto.render.AnimationCatalog;
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
 * A scrollable list of every animated texture the game has ticked, each with an
 * Animated/Frozen toggle. The catalog is populated at runtime as sprites tick, so
 * the list fills out once a world is loaded; if opened from the main menu before
 * any animation has ticked it shows a hint instead. Extends OptionsSubScreen for
 * the native scrolling list. Reached from a "Per-type animations" button on the
 * Animations category of either settings screen.
 */
public final class AbtoAnimationTypesScreen extends OptionsSubScreen {

    public AbtoAnimationTypesScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.literal("Animation Types"));
    }

    @Override
    protected void addOptions() {
        List<String> ids = AnimationCatalog.knownIds();
        if (ids.isEmpty()) {
            Button hint = Button.builder(
                    Component.literal("Enter a world to list animated textures"), b -> {})
                .bounds(0, 0, 310, 20).build();
            hint.active = false;
            this.list.addSmall(List.<AbstractWidget>of(hint));
            return;
        }
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
        boolean disabled = AnimationCatalog.isDisabled(configPath(), id);
        return Button.builder(label(id, disabled), b -> {
                boolean nowDisabled = AnimationCatalog.flip(configPath(), id);
                b.setMessage(label(id, nowDisabled));
            })
            .bounds(0, 0, 150, 20)
            .build();
    }

    private static Component label(String id, boolean disabled) {
        return Component.literal(shortName(id) + ": " + (disabled ? "Frozen" : "Animated"));
    }

    private static String shortName(String id) {
        return id.startsWith("minecraft:") ? id.substring("minecraft:".length()) : id;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("abto.json");
    }
}
