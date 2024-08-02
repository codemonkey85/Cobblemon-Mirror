/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.api.pokemon.breeding.Egg
import com.cobblemon.mod.common.block.entity.EggBlockEntity
import com.cobblemon.mod.common.client.gui.PartyOverlay.Companion.state
import com.mojang.serialization.MapCodec
import dev.lambdaurora.lambdynlights.util.SodiumDynamicLightHandler.pos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.RenderShape

class EggBlock(properties: Properties) : BaseEntityBlock(properties) {
    override fun setPlacedBy(
        level: Level,
        blockPos: BlockPos,
        blockState: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack
    ) {
        val entity = level.getBlockEntity(blockPos) as? EggBlockEntity
        //FIXME: Component
        val itemBlockEntityNbt = null /*BlockItem.getBlockEntityNbt(itemStack)*/ ?: return
        //entity?.let { entity.readNbt(itemBlockEntityNbt) }
        super.setPlacedBy(level, blockPos, blockState, placer, itemStack)
    }

    override fun codec(): MapCodec<out BaseEntityBlock> {
        TODO("Not yet implemented")
    }

    override fun getRenderShape(state: BlockState) = RenderShape.ENTITYBLOCK_ANIMATED
    override fun newBlockEntity(pos: BlockPos, state: BlockState) = EggBlockEntity(pos, state)
}