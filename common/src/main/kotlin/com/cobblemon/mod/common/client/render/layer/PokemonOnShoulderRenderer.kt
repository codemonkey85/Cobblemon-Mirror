/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.layer

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.isPokemonEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import java.util.UUID
import net.minecraft.client.model.PlayerModel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

class PokemonOnShoulderRenderer<T : Player>(renderLayerParent: RenderLayerParent<T, PlayerModel<T>>) : RenderLayer<T, PlayerModel<T>>(renderLayerParent) {

    val context = RenderContext().also {
        it.put(RenderContext.RENDER_STATE, RenderContext.RenderState.WORLD)
    }

    var leftState = FloatingState()
    var lastRenderedLeft: ShoulderData? = null
    var rightState = FloatingState()
    var lastRenderedRight: ShoulderData? = null

    override fun render(
        matrixStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        livingEntity: T,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTicks: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        this.render(matrixStack, buffer, packedLight, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, true)
        this.render(matrixStack, buffer, packedLight, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, false)
    }

    fun makeState(model: PosableModel, aspects: Set<String>, leftShoulder: Boolean): FloatingState {
        val state = FloatingState()
        state.currentModel = model
        state.currentAspects = aspects
        state.setPoseToFirstSuitable(if (leftShoulder) PoseType.SHOULDER_LEFT else PoseType.SHOULDER_RIGHT)
        return state
    }

    private fun render(
        matrixStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        livingEntity: T,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTicks: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float,
        pLeftShoulder: Boolean
    ) {
        val compoundTag = if (pLeftShoulder) livingEntity.shoulderEntityLeft else livingEntity.shoulderEntityRight
        if (compoundTag.isPokemonEntity()) {
            matrixStack.pushPose()
            val uuid = this.extractUuid(compoundTag)
            val cache = playerCache.getOrPut(livingEntity.uuid) { ShoulderCache() }
            var shoulderData: ShoulderData? = null
            if (pLeftShoulder && cache.lastKnownLeft?.uuid != uuid) {
                shoulderData = this.extractData(compoundTag, uuid)
                cache.lastKnownLeft = shoulderData
            }
            else if (!pLeftShoulder && cache.lastKnownRight?.uuid != uuid) {
                shoulderData = this.extractData(compoundTag, uuid)
                cache.lastKnownRight = shoulderData
            }

            if (shoulderData == null){
                // Could be null
                shoulderData = (if (pLeftShoulder) cache.lastKnownLeft else cache.lastKnownRight) ?: return
            }

            val model = PokemonModelRepository.getPoser(shoulderData.species.resourceIdentifier, shoulderData.aspects)
            context.put(RenderContext.SPECIES, shoulderData.species.resourceIdentifier)
            context.put(RenderContext.ASPECTS, shoulderData.aspects)
            val scale = shoulderData.form.baseScale * shoulderData.scaleModifier
            val width = shoulderData.form.hitbox.width
            val heightOffset = -1.5 * scale
            val widthOffset = width / 2 - 0.7
            // If they're sneaking, the pokemon needs to rotate a little bit and push forward
            // Shoulders move a bit when sneaking which is why the translation is necessary.
            // Shoulder exact rotation according to testing (miasmus) is 0.4 radians, the -0.15 is eyeballed though.
            if (livingEntity.isCrouching) {
                matrixStack.mulPose(Axis.XP.rotation(0.4F))
                matrixStack.translate(0F, 0F, -0.15F)
            }
            matrixStack.translate(
                if (pLeftShoulder) -widthOffset else widthOffset,
                (if (livingEntity.isCrouching) heightOffset + 0.2 else heightOffset),
                0.0
            )

            matrixStack.scale(scale, scale, scale)

            val state = if (pLeftShoulder && shoulderData != lastRenderedLeft) {
                leftState = makeState(model, shoulderData.aspects, true)
                lastRenderedLeft = shoulderData
                leftState
            } else if (!pLeftShoulder && shoulderData != lastRenderedRight) {
                rightState = makeState(model, shoulderData.aspects, false)
                lastRenderedRight = shoulderData
                rightState
            } else {
                if (pLeftShoulder) leftState else rightState
            }
            state.updatePartialTicks(partialTicks)
            context.put(RenderContext.POSABLE_STATE, state)
            state.currentModel = model
            val vertexConsumer = buffer.getBuffer(RenderType.entityCutout(PokemonModelRepository.getTexture(shoulderData.species.resourceIdentifier, shoulderData.aspects, state.animationSeconds)))
            val i = LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0f)

            model.applyAnimations(
                entity = null,
                state = state,
                headYaw = netHeadYaw,
                headPitch = headPitch,
                limbSwing = limbSwing,
                limbSwingAmount = limbSwingAmount,
                ageInTicks = livingEntity.tickCount.toFloat()
            )
            model.render(context, matrixStack, vertexConsumer, packedLight, i, -0x1)
            model.withLayerContext(buffer, state, PokemonModelRepository.getLayers(shoulderData.species.resourceIdentifier, shoulderData.aspects)) {
                model.render(context, matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -0x1)
            }
            model.setDefault()
            matrixStack.popPose()
        }
    }

    private fun extractUuid(shoulderNbt: CompoundTag): UUID {
        if (!shoulderNbt.contains(DataKeys.SHOULDER_UUID)) {
            return shoulderNbt.getCompound(DataKeys.POKEMON).getUUID(DataKeys.POKEMON_UUID)
        }
        return shoulderNbt.getUUID(DataKeys.SHOULDER_UUID)
    }

    private fun extractData(shoulderNbt: CompoundTag, pokemonUUID: UUID): ShoulderData? {
        // To not crash with existing ones, this will still have the aspect issue
        if (!shoulderNbt.contains(DataKeys.SHOULDER_SPECIES)) {
            return Pokemon.CLIENT_CODEC.decode(NbtOps.INSTANCE, shoulderNbt.getCompound(DataKeys.POKEMON))
                .map { it.first }
                .mapOrElse({ ShoulderData(pokemonUUID, it.species, it.form, it.aspects, it.scaleModifier) }, { null })
        }
        val species = PokemonSpecies.getByIdentifier(ResourceLocation.parse(shoulderNbt.getString(DataKeys.SHOULDER_SPECIES)))
            ?: return null
        val formName = shoulderNbt.getString(DataKeys.SHOULDER_FORM)
        val form = species.forms.firstOrNull { it.name == formName } ?: species.standardForm
        val aspects = shoulderNbt.getList(DataKeys.SHOULDER_ASPECTS, Tag.TAG_STRING.toInt()).map { it.asString }.toSet()
        val scaleModifier = shoulderNbt.getFloat(DataKeys.SHOULDER_SCALE_MODIFIER)
        return ShoulderData(pokemonUUID, species, form, aspects, scaleModifier)
    }

    private data class ShoulderCache(
        var lastKnownLeft: ShoulderData? = null,
        var lastKnownRight: ShoulderData? = null
    )

    data class ShoulderData(
        val uuid: UUID,
        val species: Species,
        val form: FormData,
        val aspects: Set<String>,
        val scaleModifier: Float
    )

    companion object {

        private val playerCache = hashMapOf<UUID, ShoulderCache>()

        /**
         * Checks if a player has shoulder data cached.
         *
         * @param player The player being checked.
         * @return A [Pair] with [Pair.left] and [Pair.right] being the respective shoulder.
         */
        @JvmStatic
        fun shoulderDataOf(player: Player): Pair<ShoulderData?, ShoulderData?> {
            val cache = playerCache[player.uuid] ?: return Pair(null, null)
            return Pair(cache.lastKnownLeft, cache.lastKnownRight)
        }

    }

}