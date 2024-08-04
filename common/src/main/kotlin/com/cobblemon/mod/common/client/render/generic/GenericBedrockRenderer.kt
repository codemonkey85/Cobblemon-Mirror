/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.generic

import com.cobblemon.mod.common.client.entity.GenericBedrockClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.generic.PosableGenericEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.GenericBedrockEntityModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.entity.generic.GenericBedrockEntity
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis

class GenericBedrockRenderer(context: EntityRendererProvider.Context) : EntityRenderer<GenericBedrockEntity>(context) {
    val model = PosableGenericEntityModel()
    override fun getTextureLocation(entity: GenericBedrockEntity) = GenericBedrockEntityModelRepository.getTexture(entity.category, entity.aspects, (entity.delegate as GenericBedrockClientDelegate).animationSeconds)

    override fun render(entity: GenericBedrockEntity, yaw: Float, partialTicks: Float, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int) {
        if (entity.isInvisible) {
            return
        }

        val model = GenericBedrockEntityModelRepository.getPoser(entity.category, entity.aspects)
        this.model.posableModel = model
        model.context = this.model.context
        this.model.setupEntityTypeContext(entity)
        poseStack.pushPose()
        poseStack.scale(1.0F, -1.0F, 1.0F)
        poseStack.scale(entity.scale, entity.scale, entity.scale)
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw))
        val vertexConsumer = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)))

        val state = entity.delegate as GenericBedrockClientDelegate
        state.updatePartialTicks(partialTicks)
        model.setLayerContext(buffer, state, PokemonModelRepository.getLayers(entity.category, entity.aspects))
        this.model.setupAnim(entity, 0f, 0f, entity.tickCount + partialTicks, 0F, 0F)
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -0x1)

        model.green = 1F
        model.blue = 1F
        model.red = 1F

        model.resetLayerContext()

        poseStack.popPose()
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight)
    }
}