package com.abto.mixin;

import com.abto.compat.SodiumCompat;
import com.abto.render.RenderToggles;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Leaves culling: a real optimization that removes geometry without a visible
 * change. Vanilla renders every face between two leaf blocks because leaves do not
 * occlude, so a dense tree draws a huge number of interior faces you cannot see.
 * shouldRenderFace is the chunk mesher's per-face gate; when both the block and its
 * neighbour are leaves we flip its yes to no, dropping those hidden interior faces.
 * Only ever hides a face vanilla would have drawn.
 *
 * This affects the vanilla chunk renderer. With Sodium installed, Sodium does its
 * own face culling and this has no effect there - ABTO stacks with Sodium rather
 * than overriding it.
 */
@Mixin(Block.class)
public class LeavesCullingMixin {

    @ModifyReturnValue(method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z",
        at = @At("RETURN"))
    private static boolean abto$cullLeafFaces(boolean original, BlockState state,
            BlockState neighbor, Direction direction) {
        if (original && RenderToggles.cullLeaves() && !SodiumCompat.isSodiumLoaded()
                && state.getBlock() instanceof LeavesBlock
                && neighbor.getBlock() instanceof LeavesBlock) {
            return false;
        }
        return original;
    }
}
