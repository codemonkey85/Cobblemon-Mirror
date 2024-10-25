/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.CobblemonBlocks

import com.cobblemon.mod.common.block.entity.LecternBlockEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction
import net.minecraft.world.item.*
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockState

class LecternBlockEntityRenderer(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<LecternBlockEntity> {
    override fun render(blockEntity: LecternBlockEntity, tickDelta: Float, poseStack: PoseStack, multiBufferSource: MultiBufferSource, light: Int, overlay: Int ) {
        if (blockEntity !is LecternBlockEntity) return
        if (!blockEntity.isEmpty()) {
            val blockState = if (blockEntity.level != null) blockEntity.blockState
            else (CobblemonBlocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH) as BlockState)
            val yRot = blockState.getValue(HorizontalDirectionalBlock.FACING).toYRot()

            poseStack.pushPose()
            poseStack.translate(0.5, 1.17, 0.5)
            poseStack.mulPose(Axis.YP.rotationDegrees(-yRot))
            poseStack.mulPose(Axis.XP.rotationDegrees(22.5F))
            poseStack.translate(0.0, 0.0, 0.13)
            Minecraft.getInstance().itemRenderer.renderStatic(blockEntity.getItemStack(), ItemDisplayContext.GROUND, light, overlay, poseStack, multiBufferSource, blockEntity.level, 0)
            poseStack.popPose()
        }
    }
}