/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokemon.breeding.Egg
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.entity.item.ItemEntity

class NestBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(CobblemonBlockEntities.NEST, pos, state) {
    var egg: Egg? = null
    var renderState: BlockEntityRenderState? = null

    fun dropEgg(
        state: BlockState,
        world: Level,
        pos: BlockPos,
    ) {
        //FIXME: Use components
        val blockNbt = CompoundTag()
        val itemEntity = ItemEntity(
            world,
            pos.x.toDouble(),
            pos.y.toDouble(),
            pos.z.toDouble(),
            egg!!.asItemStack(blockNbt)
        )
        world.addFreshEntity(itemEntity)
        egg = null
        this.setChanged()
        world.setBlock(pos, state, Block.UPDATE_CLIENTS)

    }

    fun onUse(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        if (egg != null) {
            return if (egg?.timeToHatch == 0) {
                hatchPokemon(state, world, pos, player)
                InteractionResult.SUCCESS
            } else {
                dropEgg(state, world, pos)
                InteractionResult.SUCCESS
            }
        }
        val playerStack = player.getItemInHand(hand)
        if (playerStack.item == CobblemonItems.POKEMON_EGG) {
            //FIXME: Update to use components
            /*
            val blockNbt = BlockItem.getBlockEntityNbt(playerStack) as CompoundTag
            this.egg = Egg.fromBlockNbt(blockNbt)
            if (!player.isCreative) {
                playerStack.decrement(1)
            }
            this.setChanged()
            world.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS)

             */
            return InteractionResult.CONSUME
        }
        return InteractionResult.FAIL

    }

    fun hatchPokemon(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player
    ) {
        if (!world.isClientSide) {
            val party = Cobblemon.storage.getParty(player.uuid)
            val newPoke = egg!!.hatchedPokemon.generatePokemon()
            party.add(newPoke)
            this.egg = null
            this.setChanged()
            world.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS)
        }


    }

    companion object {
        val TICKER = BlockEntityTicker<NestBlockEntity> { world, pos, state, blockEntity ->
            blockEntity.egg?.let {
                if (it.timeToHatch == 0) {
                    world.setBlockAndUpdate(pos, state)
                    blockEntity.setChanged()
                }
                if (it.timeToHatch > 0) {
                    it.timeToHatch--
                    //Dont write the block to the world every tick, do it every 10 seconds
                    if (it.timeToHatch % 200 == 0) {
                        blockEntity.setChanged()
                    }
                }
            }
        }
    }

}