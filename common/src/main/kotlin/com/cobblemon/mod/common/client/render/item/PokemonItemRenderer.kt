/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.item

import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import com.mojang.blaze3d.platform.Lighting
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.RenderType
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.LightTexture
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import org.joml.Quaternionf
import org.joml.Vector3f

class PokemonItemRenderer : CobblemonBuiltinItemRenderer {
    val context = RenderContext().also {
        it.put(RenderContext.RENDER_STATE, RenderContext.RenderState.PROFILE)
        it.put(RenderContext.DO_QUIRKS, false)
    }

    override fun render(stack: ItemStack, mode: ItemDisplayContext, matrices: PoseStack, vertexConsumers: MultiBufferSource, light: Int, overlay: Int) {
        val pokemonItem = stack.item as? PokemonItem ?: return
        val (species, aspects) = pokemonItem.getSpeciesAndAspects(stack) ?: return
        val state = FloatingState()
        state.currentAspects = aspects
        matrices.pushPose()
        val model = PokemonModelRepository.getPoser(species.resourceIdentifier, state)
        model.context = context
        context.put(RenderContext.RENDER_STATE, RenderContext.RenderState.PROFILE)
        context.put(RenderContext.SPECIES, species.resourceIdentifier)
        context.put(RenderContext.ASPECTS, aspects)
        context.put(RenderContext.POSABLE_STATE, state)
        state.currentModel = model

        val renderLayer = RenderType.entityCutout(PokemonModelRepository.getTexture(species.resourceIdentifier, state))

        val transformations = positions[mode]!!

        Lighting.setupForFlatItems()
        matrices.scale(transformations.scale.x, transformations.scale.y, transformations.scale.z)
        matrices.translate(transformations.translation.x, transformations.translation.y, transformations.translation.z)
        state.setPoseToFirstSuitable(PoseType.PORTRAIT)
        model.applyAnimations(null, state, 0F, 0F, 0F, 0F, 0F)

        matrices.translate(model.profileTranslation.x, model.profileTranslation.y,  model.profileTranslation.z - 4.0)
        matrices.scale(model.profileScale, model.profileScale, 0.15F)

        val rotation = Quaternionf().fromEulerXYZDegrees(Vector3f(transformations.rotation.x, transformations.rotation.y, transformations.rotation.z))
        matrices.mulPose(rotation)
        rotation.conjugate()
        val vertexConsumer: VertexConsumer = vertexConsumers.getBuffer(renderLayer)
        matrices.pushPose()
        val packedLight = if (mode == ItemDisplayContext.GUI) {
            LightTexture.pack(13, 13)
        } else {
            light
        }

        // x = red, y = green, z = blue, w = alpha
        val tint = pokemonItem.tint(stack)
        model.withLayerContext(vertexConsumers, state, PokemonModelRepository.getLayers(species.resourceIdentifier, state)) {
            val tintRed = (tint.x * 255).toInt()
            val tintGreen = (tint.y * 255).toInt()
            val tintBlue = (tint.z * 255).toInt()
            val tintAlpha = (tint.w * 255).toInt()
            val color = (tintAlpha shl 24) or (tintRed shl 16) or (tintGreen shl 8) or tintBlue
            model.render(context, matrices, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color)
        }

        model.setDefault()
        matrices.popPose()
        matrices.popPose()
        Lighting.setupFor3DItems()
    }

    companion object {
        val positions: MutableMap<ItemDisplayContext, Transformations> = mutableMapOf()

        init {
            positions[ItemDisplayContext.GUI] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(1.0, -1.9, -0.5),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 35F, 0F)
            )
            positions[ItemDisplayContext.FIXED] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(1.0, -2.0, 3.0),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 35F - 180F, 0F)
            )
            positions[ItemDisplayContext.FIRST_PERSON_RIGHT_HAND] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(2.75, -1.2, 5.0),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 35F, 0F)
            )
            positions[ItemDisplayContext.FIRST_PERSON_LEFT_HAND] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(-0.75, -1.2, 5.0),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, -35F, 0F)
            )
            positions[ItemDisplayContext.THIRD_PERSON_RIGHT_HAND] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(1.0, -2.6, 2.75),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 35F, 0F)
            )
            positions[ItemDisplayContext.THIRD_PERSON_LEFT_HAND] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(1.0, -2.6, 2.75),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, -35F, 0F)
            )
            positions[ItemDisplayContext.GROUND] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(1.0, -2.6, 3.0),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 35F, 0F)
            )
            positions[ItemDisplayContext.HEAD] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(1.0, -3.5, 3.0),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 215F, 0F)
            )
            positions[ItemDisplayContext.NONE] = PokemonItemRenderer().Transformations(
                PokemonItemRenderer().Transformation(0.0, 0.0, 0.0),
                PokemonItemRenderer().Transformation(0.5F, -0.5F, -0.5F),
                PokemonItemRenderer().Transformation(0F, 0F, 0F)
            )
        }
    }

    inner class Transformations(val translation: Transformation<Double>, val scale: Transformation<Float>, val rotation: Transformation<Float>)
    inner class Transformation<T>(val x: T, val y: T, val z: T)
}