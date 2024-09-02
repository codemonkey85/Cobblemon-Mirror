/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

@Suppress("UNUSED_PARAMETER")
class HealingMachineRenderer<T: BlockEntity>(ctx: BlockEntityRendererProvider.Context): BlockEntityRenderer<T> {
    companion object {
        private val offsets = listOf(
            0.2 to 0.385,
            -0.2 to 0.385,
            0.2 to 0.0,
            -0.2 to 0.0,
            0.2 to -0.385,
            -0.2 to -0.385
        )
    }

    override fun render(blockEntity: T, tickDelta: Float, poseStack: PoseStack, multiBufferSource: MultiBufferSource, light: Int, overlay: Int) {
        if (blockEntity !is HealingMachineBlockEntity) return

        poseStack.pushPose()

        val blockState = if (blockEntity.level != null) blockEntity.blockState
            else (CobblemonBlocks.HEALING_MACHINE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH) as BlockState)
        val yRot = blockState.getValue(HorizontalDirectionalBlock.FACING).toYRot()

        // Position Poké Balls
        poseStack.translate(0.5, 0.5, 0.5)

        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot))
        poseStack.scale(0.65F, 0.65F, 0.65F)

        blockEntity.pokeBalls().forEach { (index, pokeBall) ->
            poseStack.pushPose()
            val offset = offsets[index]
            poseStack.translate(offset.first, 0.4, offset.second)
            Minecraft.getInstance().itemRenderer.renderStatic(pokeBall.stack(), ItemDisplayContext.GROUND, light, overlay, poseStack, multiBufferSource, blockEntity.level, 0)
            poseStack.popPose()
        }
        poseStack.popPose()
    }
}