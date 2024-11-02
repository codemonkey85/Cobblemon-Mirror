/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.block.SaccharineLeafBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(targets = "net.minecraft.world.entity.animal.Bee$BeeGrowCropGoal")
public abstract class BeeEntityMixin {

    @Unique
    private BlockState cobblemon$result = null;

    @Inject(method = "tick()V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/BonemealableBlock;performBonemeal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            shift = At.Shift.BY,
            by = 2
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void injectCustomGrowth(CallbackInfo ci, int i, BlockPos blockPos, BlockState blockState, Block block) {
        if (block instanceof SaccharineLeafBlock) {

            int age = blockState.getValue(SaccharineLeafBlock.Companion.getAGE());

            if (age < 2) {
                this.cobblemon$result = blockState.setValue(SaccharineLeafBlock.Companion.getAGE(), age + 1);
            }
        }
    }

    @ModifyVariable(method = "tick", at = @At(value = "LOAD", ordinal = 0), index = 5)
    private BlockState applyCustomBlockState(BlockState before) {
        if(this.cobblemon$result != null && before == null) {
            BlockState result = this.cobblemon$result;
            this.cobblemon$result = null;
            return result;
        }

        return before;
    }
}
