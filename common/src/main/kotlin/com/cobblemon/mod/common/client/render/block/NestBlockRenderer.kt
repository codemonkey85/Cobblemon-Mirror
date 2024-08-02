/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.api.pokemon.breeding.Egg
import com.cobblemon.mod.common.api.pokemon.breeding.EggPatterns
import com.cobblemon.mod.common.block.entity.NestBlockEntity
import com.cobblemon.mod.common.client.render.atlas.CobblemonAtlases
import com.cobblemon.mod.common.client.render.layer.CobblemonRenderLayers
import com.cobblemon.mod.common.client.render.models.blockbench.repository.EggModelRepo
import com.cobblemon.mod.common.client.render.models.blockbench.setRotation
import com.cobblemon.mod.common.util.math.geometry.Axis
import com.mojang.authlib.minecraft.client.MinecraftClient
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexBuffer
import net.minecraft.client.renderer.GameRenderer
import com.mojang.blaze3d.vertex.Tesselator
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import com.mojang.blaze3d.vertex.PoseStack
import java.awt.Color

class NestBlockRenderer(private val context: BlockEntityRendererProvider.Context) : BlockEntityRenderer<NestBlockEntity> {
    override fun render(
        entity: NestBlockEntity,
        tickDelta: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        if (entity.renderState == null) {
            entity.renderState = BasicBlockEntityRenderState()
        }
        val renderState = entity.renderState as BasicBlockEntityRenderState
        if (renderState.needsRebuild || renderState.vboLightLevel != light || renderState.vbo.format == null) {
            renderToBuffer(entity, light, overlay, renderState.vbo, entity.egg)
            renderState.vboLightLevel = light
            renderState.needsRebuild = false
        }
        if (entity.egg != null) {
            matrices.pushPose()
            CobblemonRenderLayers.EGG_LAYER.setupRenderState()
            renderState.vbo.bind()
            renderState.vbo.drawWithShader(
                matrices.last().pose().mul(RenderSystem.getModelViewMatrix()),
                RenderSystem.getProjectionMatrix(),
                GameRenderer.getRendertypeCutoutShader()
            )
            VertexBuffer.unbind()
            CobblemonRenderLayers.EGG_LAYER.clearRenderState()
            matrices.popPose()
        }

    }

    fun renderToBuffer(entity: NestBlockEntity, light: Int, overlay: Int, buffer: VertexBuffer, egg: Egg?) {
        if (egg != null) {
            val bufferBuilder = Tesselator.getInstance().begin(
                CobblemonRenderLayers.EGG_LAYER.mode(),
                CobblemonRenderLayers.EGG_LAYER.format()
            )
            val pattern = EggPatterns.patternMap[egg.patternId]!!
            val model = EggModelRepo.eggModels[pattern.model]
            val baseTexture = pattern.baseTexturePath
            val baseAtlasedTexture = CobblemonAtlases.EGG_PATTERN_ATLAS.getSprite(baseTexture)

            val primaryColor = Color.decode("#${egg.baseColor}")

            //Patching uvs so we can use atlases
            val baseModel = model?.createWithUvOverride(
                false,
                baseAtlasedTexture.x,
                baseAtlasedTexture.y,
                CobblemonAtlases.EGG_PATTERN_ATLAS.textureAtlas.width,
                CobblemonAtlases.EGG_PATTERN_ATLAS.textureAtlas.height
            )?.bakeRoot()
            baseModel?.setRotation(Axis.Z_AXIS.ordinal, Math.toRadians(180.0).toFloat())
            val baseTextureModel = model?.createWithUvOverride(
                false,
                baseAtlasedTexture.x,
                baseAtlasedTexture.y,
                CobblemonAtlases.EGG_PATTERN_ATLAS.textureAtlas.width,
                CobblemonAtlases.EGG_PATTERN_ATLAS.textureAtlas.height
            )?.bakeRoot()
            baseTextureModel?.setRotation(Axis.Z_AXIS.ordinal, Math.toRadians(180.0).toFloat())
            val matrixStack = PoseStack()
            matrixStack.setIdentity()
            //matrixStack.translate(0F, 0.3F, 0F)
            baseModel?.render(matrixStack, bufferBuilder, light, overlay)
            baseTextureModel?.render(
                matrixStack,
                bufferBuilder,
                light,
                overlay,
                //FIXME: ARGB or RGBA
                primaryColor.rgb
            )


            pattern.overlayTexturePath?.let {
                val overlayAtlasedTexture = CobblemonAtlases.EGG_PATTERN_ATLAS.getSprite(it)
                val overlayColor = Color.decode("#${egg.overlayColor}")
                if (overlayColor != null) {
                    val overlayTextureModel = model?.createWithUvOverride(
                        false,
                        overlayAtlasedTexture.x,
                        overlayAtlasedTexture.y,
                        CobblemonAtlases.EGG_PATTERN_ATLAS.textureAtlas.width,
                        CobblemonAtlases.EGG_PATTERN_ATLAS.textureAtlas.height
                    )?.bakeRoot()
                    overlayTextureModel?.setRotation(Axis.Z_AXIS.ordinal, Math.toRadians(180.0).toFloat())
                    overlayTextureModel?.render(
                        matrixStack,
                        bufferBuilder,
                        light,
                        overlay,
                        //FIXME: ARGB or RGBA
                        overlayColor.rgb
                    )
                }

            }



            val bufferBuilderFinal = bufferBuilder.build()
            buffer.bind()
            buffer.upload(bufferBuilderFinal)
            VertexBuffer.unbind()
        }
    }
}