package com.abto.mixin;

import com.abto.render.FunColors;
import com.abto.render.RenderToggles;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.BiomeColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Recolors biome-based grass, foliage, and water tints. Two layers: a Fun-tab
 * custom color (a chosen palette color, index 0 meaning no override) takes priority;
 * otherwise disableBiomeColors flattens to a fixed default; otherwise the vanilla
 * biome-blended color is kept. Colors are baked into the chunk mesh, so a change
 * shows after the affected chunks rebuild.
 */
@Mixin(BiomeColors.class)
public class BiomeColorMixin {

    private static final int GRASS = 0xFF7CBD6B;
    private static final int FOLIAGE = 0xFF59AE30;
    private static final int DRY_FOLIAGE = 0xFFA98F55;
    private static final int WATER = 0xFF3F76E4;

    @ModifyReturnValue(method = "getAverageGrassColor", at = @At("RETURN"))
    private static int abto$grass(int original) {
        int idx = RenderToggles.grassColorIndex();
        if (idx > 0) {
            return FunColors.color(idx);
        }
        return RenderToggles.disableBiomeColors() ? GRASS : original;
    }

    @ModifyReturnValue(method = "getAverageFoliageColor", at = @At("RETURN"))
    private static int abto$foliage(int original) {
        int idx = RenderToggles.foliageColorIndex();
        if (idx > 0) {
            return FunColors.color(idx);
        }
        return RenderToggles.disableBiomeColors() ? FOLIAGE : original;
    }

    @ModifyReturnValue(method = "getAverageDryFoliageColor", at = @At("RETURN"))
    private static int abto$dryFoliage(int original) {
        int idx = RenderToggles.foliageColorIndex();
        if (idx > 0) {
            return FunColors.color(idx);
        }
        return RenderToggles.disableBiomeColors() ? DRY_FOLIAGE : original;
    }

    @ModifyReturnValue(method = "getAverageWaterColor", at = @At("RETURN"))
    private static int abto$water(int original) {
        int idx = RenderToggles.waterColorIndex();
        if (idx > 0) {
            return FunColors.color(idx);
        }
        return RenderToggles.disableBiomeColors() ? WATER : original;
    }
}
