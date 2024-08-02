/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.api.storage.pc.link.ProximityPCLink
import com.cobblemon.mod.common.block.PCBlock
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.phys.AABB

class PCBlockEntity(
    blockPos: BlockPos,
    blockState: BlockState
) : BlockEntity(CobblemonBlockEntities.PC, blockPos, blockState) {

    companion object {
        internal val TICKER = BlockEntityTicker<PCBlockEntity> { world, _, _, blockEntity ->
            if (world.isClientSide) return@BlockEntityTicker

            blockEntity.togglePCOn(blockEntity.getInRangeViewerCount(world, blockEntity.blockPos) > 0)
        }
    }

    private fun togglePCOn(on: Boolean) {
        val pcBlock = blockState.block as PCBlock

        val world = level
        if (world != null && !world.isClientSide) {
            val posBottom = pcBlock.getBasePosition(blockState, blockPos)
            val stateBottom = world.getBlockState(posBottom)

            val posTop = pcBlock.getPositionOfOtherPart(stateBottom, posBottom)
            val stateTop = world.getBlockState(posTop)

            try {
                if (stateBottom.getValue(PCBlock.ON) != on) {
                    world.setBlockAndUpdate(posTop, stateTop.setValue(PCBlock.ON, on))
                    world.setBlockAndUpdate(posBottom, stateBottom.setValue(PCBlock.ON, on))
                }
            } catch (exception: IllegalArgumentException) {
                // This is probably a PC from before 1.3. Break it.
                if (world.getBlockState(blockPos.above()).block is PCBlock) {
                    world.setBlockAndUpdate(blockPos.above(), Blocks.AIR.defaultBlockState())
                } else {
                    world.setBlockAndUpdate(blockPos.below(), Blocks.AIR.defaultBlockState())
                }
                world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState())
                world.addFreshEntity(ItemEntity(world, blockPos.x + 0.5, blockPos.y + 1.0, blockPos.z + 0.5,
                    ItemStack(CobblemonBlocks.PC)
                ))
            }
        }
    }

    private fun isPlayerViewing(player: Player): Boolean {
        val pcLink = PCLinkManager.getLink(player.uuid)
        return pcLink != null
                && pcLink is ProximityPCLink
                && pcLink.pos == blockPos
                && pcLink.world!!.dimension() == player.level().dimension()
    }

    private fun getInRangeViewerCount(world: Level, pos: BlockPos, range: Double = 5.0): Int {
        val box = AABB(
            pos.x.toDouble() - range,
            pos.y.toDouble() - range,
            pos.z.toDouble() - range,
            (pos.x + 1).toDouble() + range,
            (pos.y + 1).toDouble() + range,
            (pos.z + 1).toDouble() + range
        )

        return world.getEntities(EntityTypeTest.forClass(Player::class.java), box) { player: Player? -> isPlayerViewing(player!!) }.size
    }
}