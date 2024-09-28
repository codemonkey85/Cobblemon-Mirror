/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.block.entity.GildedChestBlockEntity
import com.cobblemon.mod.common.client.render.models.blockbench.blockentity.BlockEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.level.block.state.properties.BlockStateProperties

class GildedChestBlockRenderer(context: BlockEntityRendererProvider.Context) : BlockEntityRenderer<GildedChestBlockEntity> {
    val context = RenderContext().also {
        it.put(RenderContext.RENDER_STATE, RenderContext.RenderState.BLOCK)
        it.put(RenderContext.DO_QUIRKS, true)
    }
    override fun render(
        entity: GildedChestBlockEntity,
        tickDelta: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        val aspects = emptySet<String>()
        val state = entity.posableState
        state.currentAspects = aspects
        state.updatePartialTicks(tickDelta)

        val poserId = entity.type.poserId

        val model = VaryingModelRepository.getPoser(poserId, state) as BlockEntityModel
        model.context = context
        val texture = VaryingModelRepository.getTexture(poserId, state)
        val vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutout(texture))
        model.bufferProvider = vertexConsumers
        state.currentModel = model
        context.put(RenderContext.ASPECTS, aspects)
        context.put(RenderContext.TEXTURE, texture)
        context.put(RenderContext.SPECIES, poserId)
        context.put(RenderContext.POSABLE_STATE, state)

        matrices.pushPose()
        matrices.mulPose(Axis.ZP.rotationDegrees(180f))
        matrices.translate(-0.5, 0.0, 0.5)
        matrices.mulPose(Axis.YP.rotationDegrees(entity.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()))
        matrices.mulPose(Axis.YP.rotationDegrees(180f))

        model.applyAnimations(
            entity = null,
            state = state,
            headYaw = 0F,
            headPitch = 0F,
            limbSwing = 0F,
            limbSwingAmount = 0F,
            ageInTicks = state.animationSeconds * 20
        )
        model.render(context, matrices, vertexConsumer, light, overlay, -0x1)
        model.withLayerContext(vertexConsumers, state, VaryingModelRepository.getLayers(poserId, state)) {
            model.render(context, matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, -0x1)
        }
        model.setDefault()
        matrices.popPose()

    }
}