/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.api.gui.renderSprite
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.client.render.SpriteType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.toHex
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import org.joml.Quaternionf
import org.joml.Vector3f

fun drawProfilePokemon(
    renderablePokemon: RenderablePokemon,
    matrixStack: PoseStack,
    rotation: Quaternionf,
    poseType: PoseType = PoseType.PROFILE,
    state: PosableState,
    partialTicks: Float,
    scale: Float = 20F,
    applyProfileTransform: Boolean = true,
    applyBaseScale: Boolean = false,
    r: Float = 1F,
    g: Float = 1F,
    b: Float = 1F,
    a: Float = 1F
) = drawProfilePokemon(
    species = renderablePokemon.species.resourceIdentifier,
    matrixStack = matrixStack,
    rotation = rotation,
    poseType = poseType,
    state = state.also { it.currentAspects = renderablePokemon.aspects },
    partialTicks = partialTicks,
    scale = scale,
    applyProfileTransform = applyProfileTransform,
    applyBaseScale = applyBaseScale,
    r = r,
    g = g,
    b = b,
    a = a,
)

fun drawProfilePokemon(
    species: ResourceLocation,
    matrixStack: PoseStack,
    rotation: Quaternionf,
    poseType: PoseType = PoseType.PROFILE,
    state: PosableState,
    partialTicks: Float,
    scale: Float = 20F,
    applyProfileTransform: Boolean = true,
    applyBaseScale: Boolean = false,
    r: Float = 1F,
    g: Float = 1F,
    b: Float = 1F,
    a: Float = 1F
) {
    RenderSystem.applyModelViewMatrix()
    matrixStack.scale(scale, scale, -scale)

    val sprite = PokemonModelRepository.getSprite(species, state, SpriteType.PROFILE)

    if (sprite == null) {

        val model = PokemonModelRepository.getPoser(species, state)
        val texture = PokemonModelRepository.getTexture(species, state)

        val context = RenderContext()
        model.context = context
        PokemonModelRepository.getTextureNoSubstitute(species, state).let { context.put(RenderContext.TEXTURE, it) }
        val baseScale = PokemonSpecies.getByIdentifier(species)!!.getForm(state.currentAspects).baseScale
        context.put(RenderContext.SCALE, baseScale)
        context.put(RenderContext.SPECIES, species)
        context.put(RenderContext.ASPECTS, state.currentAspects)
        context.put(RenderContext.RENDER_STATE, RenderContext.RenderState.PROFILE)
        context.put(RenderContext.POSABLE_STATE, state)
        context.put(RenderContext.DO_QUIRKS, false)

        state.currentModel = model

        val renderType = RenderType.entityCutout(texture)

        state.setPoseToFirstSuitable(poseType)
        state.updatePartialTicks(partialTicks)
        model.applyAnimations(null, state, 0F, 0F, 0F, 0F, 0F)
        if (applyProfileTransform) {
            matrixStack.translate(model.profileTranslation.x, model.profileTranslation.y, model.profileTranslation.z - 4.0)
            matrixStack.scale(model.profileScale, model.profileScale, 1 / model.profileScale)} else {
            matrixStack.translate(0F, 0F, -4.0F)
            if (applyBaseScale) {
                matrixStack.scale(baseScale, baseScale, 1 / baseScale)
            }
        }
        matrixStack.mulPose(rotation)
        Lighting.setupForEntityInInventory()
        val entityRenderDispatcher = Minecraft.getInstance().entityRenderDispatcher
        rotation.conjugate()
        entityRenderDispatcher.overrideCameraOrientation(rotation)
        entityRenderDispatcher.setRenderShadow(true)

        val bufferSource = Minecraft.getInstance().renderBuffers().bufferSource()
        val buffer = bufferSource.getBuffer(renderType)
        val light1 = Vector3f(-1F, 1F, 1.0F)
        val light2 = Vector3f(1.3F, -1F, 1.0F)
        RenderSystem.setShaderLights(light1, light2)
        val packedLight = LightTexture.pack(11, 7)

        val colour = toHex(r, g, b, a)
        model.withLayerContext(bufferSource, state, PokemonModelRepository.getLayers(species, state)) {
            model.render(context, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, colour)
            bufferSource.endBatch()
        }
        model.setDefault()
        entityRenderDispatcher.setRenderShadow(true)
        Lighting.setupFor3DItems()
    } else {
        renderSprite(matrixStack, sprite)
    }
}

