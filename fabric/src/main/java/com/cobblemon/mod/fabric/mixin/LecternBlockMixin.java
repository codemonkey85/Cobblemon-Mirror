/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.mixin;

import com.cobblemon.mod.common.block.entity.LecternBlockEntity;
import com.cobblemon.mod.common.CobblemonBlocks;
import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.item.PokedexItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.LecternBlock.HAS_BOOK;

@Mixin(LecternBlock.class)
public abstract class LecternBlockMixin {
    @Inject(method = "useItemOn", at = @At(value = "HEAD"), cancellable = true)
    private void cobblemon$useItemOn(ItemStack itemStack, BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (!world.isClientSide && itemStack.getItem() instanceof PokedexItem) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (!blockState.getValue(HAS_BOOK)
                && blockEntity instanceof net.minecraft.world.level.block.entity.LecternBlockEntity
                && blockState.getBlock() instanceof LecternBlock
            ) {
                ItemStack pokedexItemStack = itemStack.copy();
                Direction facing = blockState.getValue(HorizontalDirectionalBlock.FACING);
                itemStack.consumeAndReturn(1, player);
                blockEntity.setRemoved();
                BlockState newBlockState = CobblemonBlocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
                world.setBlockAndUpdate(blockPos, newBlockState);
                BlockEntity lecternEntity = world.getBlockEntity(blockPos);

                if (lecternEntity instanceof LecternBlockEntity) {
                    ((LecternBlockEntity) lecternEntity).setItemStack((pokedexItemStack));
                }

                world.playSound(null, blockPos, CobblemonSounds.POKEDEX_CLOSE, SoundSource.BLOCKS, 0.5F, 1.25F);
                cir.setReturnValue(itemStack.isEmpty() && interactionHand == InteractionHand.MAIN_HAND ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            }
        }
    }
}
