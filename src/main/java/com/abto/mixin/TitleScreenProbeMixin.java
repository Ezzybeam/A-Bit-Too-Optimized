package com.abto.mixin;

import com.abto.Abto;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenProbeMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void abto$onTitleInit(CallbackInfo ci) {
        Abto.LOGGER.info("ABTO mixin active.");
    }
}
