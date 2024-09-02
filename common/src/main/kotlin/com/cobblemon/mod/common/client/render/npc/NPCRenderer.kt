/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.npc

import com.cobblemon.mod.common.client.entity.NPCClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.PosableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.npc.PosableNPCModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.NPCModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.npc.NPCEntity
import kotlin.math.min
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.resources.ResourceLocation

class NPCRenderer(context: Context) : LivingEntityRenderer<NPCEntity, PosableEntityModel<NPCEntity>>(context, PosableNPCModel(), 0.5f) {
    override fun getTextureLocation(entity: NPCEntity): ResourceLocation {
        return NPCModelRepository.getTexture(entity.npc.resourceIdentifier, entity.aspects, (entity.delegate as NPCClientDelegate).animationSeconds)
    }

    override fun render(
        entity: NPCEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseMatrix: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        val aspects = entity.aspects
        shadowRadius = min((entity.boundingBox.maxX - entity.boundingBox.minX), (entity.boundingBox.maxZ) - (entity.boundingBox.minZ)).toFloat() / 1.5F
        val model = NPCModelRepository.getPoser(entity.npc.resourceIdentifier, aspects)
        this.model.posableModel = model
        model.context = this.model.context
        this.model.setupEntityTypeContext(entity)
        this.model.context.put(RenderContext.TEXTURE, getTextureLocation(entity))
        val clientDelegate = entity.delegate as NPCClientDelegate
        clientDelegate.updatePartialTicks(partialTicks)

        model.setLayerContext(buffer, clientDelegate, NPCModelRepository.getLayers(entity.npc.resourceIdentifier, aspects))

        super.render(entity, entityYaw, partialTicks, poseMatrix, buffer, packedLight)

        model.red = 1F
        model.green = 1F
        model.blue = 1F
        model.resetLayerContext()

//        if (this.shouldRenderLabel(entity)) {
//            this.renderLabelIfPresent(entity, entity.displayName, poseMatrix, buffer, packedLight)
//        }
    }
}