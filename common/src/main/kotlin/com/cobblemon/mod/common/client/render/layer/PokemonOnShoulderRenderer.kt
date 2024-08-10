/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.layer

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonFloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.isPokemonEntity
import java.util.UUID
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.Identifier
import net.minecraft.util.Pair
import net.minecraft.util.math.RotationAxis

class PokemonOnShoulderRenderer<T : PlayerEntity>(renderLayerParent: FeatureRendererContext<T, PlayerEntityModel<T>>) : FeatureRenderer<T, PlayerEntityModel<T>>(renderLayerParent) {

    override fun render(
        matrixStack: MatrixStack,
        buffer: VertexConsumerProvider,
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

    private fun render(
        matrixStack: MatrixStack,
        buffer: VertexConsumerProvider,
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
            matrixStack.push()
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
            val scale = shoulderData.form.baseScale * shoulderData.scaleModifier
            val width = shoulderData.form.hitbox.width
            val heightOffset = -1.5 * scale
            val widthOffset = width / 2 - 0.7
            // If they're sneaking, the pokemon needs to rotate a little bit and push forward
            // Shoulders move a bit when sneaking which is why the translation is necessary.
            // Shoulder exact rotation according to testing (miasmus) is 0.4 radians, the -0.15 is eyeballed though.
            if (livingEntity.isInSneakingPose) {
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(0.4F))
                matrixStack.translate(0F, 0F, -0.15F)
            }
            matrixStack.translate(
                if (pLeftShoulder) -widthOffset else widthOffset,
                (if (livingEntity.isInSneakingPose) heightOffset + 0.2 else heightOffset),
                0.0
            )

            matrixStack.scale(scale, scale, scale)

            val model = PokemonModelRepository.getPoser(shoulderData.species.resourceIdentifier, shoulderData.aspects)
            val state = PokemonFloatingState()
            state.updatePartialTicks(ageInTicks + partialTicks)
            val vertexConsumer = buffer.getBuffer(model.getLayer(PokemonModelRepository.getTexture(shoulderData.species.resourceIdentifier, shoulderData.aspects, state.animationSeconds)))
            val i = LivingEntityRenderer.getOverlay(livingEntity, 0.0f)

            val pose = model.poses.values
                .firstOrNull { (if (pLeftShoulder) PoseType.SHOULDER_LEFT else PoseType.SHOULDER_RIGHT) in it.poseTypes  }
                ?: model.poses.values.first()
            state.setPose(pose.poseName)
            state.timeEnteredPose = 0F
            model.setupAnimStateful(
                entity = null,
                state = state,
                headYaw = netHeadYaw,
                headPitch = headPitch,
                limbSwing = limbSwing,
                limbSwingAmount = limbSwingAmount,
                ageInTicks = livingEntity.age.toFloat()
            )
            model.render(matrixStack, vertexConsumer, packedLight, i, 1.0f, 1.0f, 1.0f, 1.0f)
            model.withLayerContext(buffer, state, PokemonModelRepository.getLayers(shoulderData.species.resourceIdentifier, shoulderData.aspects)) {
                model.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F)
            }
            model.setDefault()
            matrixStack.pop()
        }
    }

    private fun extractUuid(shoulderNbt: NbtCompound): UUID {
        if (!shoulderNbt.contains(DataKeys.SHOULDER_UUID)) {
            return shoulderNbt.getCompound(DataKeys.POKEMON).getUuid(DataKeys.POKEMON_UUID)
        }
        return shoulderNbt.getUuid(DataKeys.SHOULDER_UUID)
    }

    private fun extractData(shoulderNbt: NbtCompound, pokemonUUID: UUID): ShoulderData? {
        // To not crash with existing ones, this will still have the aspect issue
        if (!shoulderNbt.contains(DataKeys.SHOULDER_SPECIES)) {
            val pokemon = Pokemon().apply { isClient = true }.loadFromNBT(shoulderNbt.getCompound(DataKeys.POKEMON))
            return ShoulderData(pokemonUUID, pokemon.species, pokemon.form, pokemon.aspects, pokemon.scaleModifier)
        }
        val species = PokemonSpecies.getByIdentifier(Identifier(shoulderNbt.getString(DataKeys.SHOULDER_SPECIES)))
            ?: return null

        val formName = shoulderNbt.getString(DataKeys.SHOULDER_FORM)
        val form = species.forms.firstOrNull { it.name == formName } ?: species.standardForm
        val aspects = shoulderNbt.getList(DataKeys.SHOULDER_ASPECTS, NbtElement.STRING_TYPE.toInt()).map { it.asString() }.toSet()
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
        fun shoulderDataOf(player: PlayerEntity): Pair<ShoulderData?, ShoulderData?> {
            val cache = playerCache[player.uuid] ?: return Pair(null, null)
            return Pair(cache.lastKnownLeft, cache.lastKnownRight)
        }

    }

}