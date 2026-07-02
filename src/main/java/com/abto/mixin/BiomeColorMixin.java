package com.abto.mixin;

import com.abto.render.RenderToggles;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.BiomeColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Flattens biome-based tint colors when disableBiomeColors is on. Vanilla averages
 * grass, foliage, and water colors over the biome-blend radius for smooth
 * transitions; disabling that returns a single fixed colour per type, skipping the
 * per-block averaging (FO's biome_colors toggle). A visible change - all grass one
 * shade - so it is off by default.
 */
@Mixin(BiomeColors.class)
public class BiomeColorMixin {

    private static final int GRASS = 0xFF7CBD6B;
    private static final int FOLIAGE = 0xFF59AE30;
    private static final int DRY_FOLIAGE = 0xFFA98F55;
    private static final int WATER = 0xFF3F76E4;

    @ModifyReturnValue(method = "getAverageGrassColor", at = @At("RETURN"))
    private static int abto$grass(int original) {
        return RenderToggles.disableBiomeColors() ? GRASS : original;
    }

    @ModifyReturnValue(method = "getAverageFoliageColor", at = @At("RETURN"))
    private static int abto$foliage(int original) {
        return RenderToggles.disableBiomeColors() ? FOLIAGE : original;
    }

    @ModifyReturnValue(method = "getAverageDryFoliageColor", at = @At("RETURN"))
    private static int abto$dryFoliage(int original) {
        return RenderToggles.disableBiomeColors() ? DRY_FOLIAGE : original;
    }

    @ModifyReturnValue(method = "getAverageWaterColor", at = @At("RETURN"))
    private static int abto$water(int original) {
        return RenderToggles.disableBiomeColors() ? WATER : original;
    }
}
