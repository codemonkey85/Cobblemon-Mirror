/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.boat

import com.cobblemon.mod.common.entity.boat.CobblemonBoatEntity
import com.cobblemon.mod.common.entity.boat.CobblemonBoatType
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.model.BoatModel
import net.minecraft.client.model.ChestBoatModel
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.EntityRenderer
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import org.joml.Quaternionf

class CobblemonBoatRenderer(ctx: EntityRendererProvider.Context, private val hasChest: Boolean) : EntityRenderer<CobblemonBoatEntity>(ctx) {

    private val boatModels = hashMapOf<CobblemonBoatType, Pair<ResourceLocation, BoatModel>>()

    init {
        this.shadowRadius = 0.8F
        CobblemonBoatType.values().forEach { type ->
            this.boatModels[type] = generateTextureIdentifier(type, this.hasChest) to generateBoatModel(ctx, type, this.hasChest)
        }
    }

    override fun getTextureLocation(entity: CobblemonBoatEntity): ResourceLocation = this.boatModels[entity.boatType]!!.first

    override fun render(entity: CobblemonBoatEntity, yaw: Float, tickDelta: Float, matrices: PoseStack, vertexConsumers: MultiBufferSource, light: Int) {
        matrices.pushPose()
        matrices.translate(0F, 0.375F, 0F)
        matrices.mulPose(Axis.YP.rotationDegrees(180F - yaw))
        val h = entity.hurtTime - tickDelta
        val j = (entity.damage - tickDelta).coerceAtLeast(0F)
        if (h > 0F) {
            matrices.mulPose(Axis.XP.rotationDegrees(Mth.sin(h) * h * j / 10F * entity.hurtDir))
        }
        val k = entity.getBubbleAngle(tickDelta)
        if (!Mth.equal(k, 0F)) {
            matrices.mulPose(Quaternionf().setAngleAxis(entity.getBubbleAngle(tickDelta) * 0.017453292F, 1F, 0F, 1F))
        }
        val (identifier, entityModel) = this.boatModels[entity.boatType]!!
        matrices.scale(-1F, -1F, 1F)
        matrices.mulPose(Axis.YP.rotationDegrees(90F))
        entityModel.setupAnim(entity, tickDelta, 0F, -0.1F, 0F, 0F)
        val vertexConsumer = vertexConsumers.getBuffer(entityModel.renderType(identifier))
        entityModel.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, -0x1)
        if (!entity.isUnderWater) {
            val vertexConsumer2 = vertexConsumers.getBuffer(RenderType.waterMask())
            entityModel.waterPatch().render(matrices, vertexConsumer2, light, OverlayTexture.NO_OVERLAY)
        }
        matrices.popPose()
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    companion object {

        private fun generateTextureIdentifier(type: CobblemonBoatType, hasChest: Boolean): ResourceLocation {
            val boatSubPath = if (hasChest) "chest_boat" else "boat"
            val path = "textures/entity/$boatSubPath/${type.name.lowercase()}.png"
            return cobblemonResource(path)
        }

        private fun generateBoatModel(ctx: EntityRendererProvider.Context, type: CobblemonBoatType, hasChest: Boolean): BoatModel {
            val modelLayer = this.createBoatModelLayer(type, hasChest)
            val modelPart = ctx.bakeLayer(modelLayer)
            return if (hasChest) ChestBoatModel(modelPart) else BoatModel(modelPart)
        }

        internal fun createBoatModelLayer(type: CobblemonBoatType, hasChest: Boolean): ModelLayerLocation {
            val boatSubPath = if (hasChest) "chest_boat" else "boat"
            val path = "$boatSubPath/${type.name.lowercase()}"
            return ModelLayerLocation(cobblemonResource(path), "main")
        }

    }

}