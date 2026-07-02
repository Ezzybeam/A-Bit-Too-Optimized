package com.abto.mixin;

import com.abto.config.AbtoConfig;
import com.abto.config.ConfigStore;
import com.abto.config.UiStyle;
import com.abto.gui.options.AbtoOptionsScreen;
import com.abto.gui.options.AbtoSodiumScreen;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Redirects the vanilla "Video Settings" button so it opens ABTO's own settings
 * screen instead of the vanilla VideoSettingsScreen. OptionsScreen.init() builds
 * each settings button via openScreenButton(label, supplier); we wrap that call
 * and, only for the Video button (translatable key "options.video"), substitute a
 * supplier that opens whichever ABTO screen matches the configured UI style. All
 * other buttons are left untouched.
 */
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {

    @WrapOperation(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/options/OptionsScreen;"
                + "openScreenButton(Lnet/minecraft/network/chat/Component;"
                + "Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;"))
    private Button abto$redirectVideoButton(
            OptionsScreen self,
            Component label,
            Supplier<Screen> supplier,
            Operation<Button> original) {
        if (isVideoLabel(label)) {
            Supplier<Screen> abtoSupplier = () -> abto$openSettings(self);
            return original.call(self, label, abtoSupplier);
        }
        return original.call(self, label, supplier);
    }

    private static Screen abto$openSettings(Screen parent) {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("abto.json");
        AbtoConfig config = ConfigStore.load(configPath);
        return config.uiStyle == UiStyle.MINECRAFT
            ? new AbtoOptionsScreen(parent)
            : new AbtoSodiumScreen(parent);
    }

    private static boolean isVideoLabel(Component label) {
        return label.getContents() instanceof TranslatableContents tc
            && "options.video".equals(tc.getKey());
    }
}
