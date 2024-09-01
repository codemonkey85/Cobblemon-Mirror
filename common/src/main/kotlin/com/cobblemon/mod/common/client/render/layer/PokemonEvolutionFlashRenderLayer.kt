/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.layer

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.sin

class PokemonEvolutionFlashRenderLayer<T : PokemonEntity>(parent: FeatureRendererContext<T, EntityModel<T>>): FeatureRenderer<T, EntityModel<T>>(parent) {
    override fun render(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        entity: T,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        val model = contextModel
        val texture = getTexture(entity)
        val age: Float = entity.age.toFloat()
        contextModel.copyStateTo(model)
        model.animateModel(entity, limbAngle, limbDistance, tickDelta)
        model.setAngles(entity, limbAngle, limbDistance, age, headYaw, headPitch)
        val vertexConsumer = vertexConsumers!!.getBuffer(RenderLayer.getEntityCutoutNoCull(texture))
        val glowTime = (entity.delegate as PokemonClientDelegate).glowTime
        if(glowTime > 0) {
            matrices.scale(1.01f, 1.01f, 1.01f)
            model.render(
                matrices,
                vertexConsumer,
                light,
                LivingEntityRenderer.getOverlay(entity, sin(glowTime + tickDelta * 0.5f)),
                1.0f,
                1.0f,
                1.0f,
                1.0f
            )
        }
    }
}