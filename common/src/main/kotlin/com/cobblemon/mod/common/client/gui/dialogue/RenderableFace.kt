/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.dialogue

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.gui.drawPosablePortrait
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.client.entity.NPCClientDelegate
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.NPCModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.npc.NPCEntity
import java.util.UUID
import kotlin.math.atan
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.util.Identifier
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * Some time of face that can be rendered in a dialogue.
 *
 * @author Hiroku
 * @since January 1st, 2024
 */
sealed interface RenderableFace {
    fun render(drawContext: DrawContext, partialTicks: Float)
}

class PlayerRenderableFace(val playerId: UUID) : RenderableFace {
    override fun render(drawContext: DrawContext, partialTicks: Float) {
        val entity = MinecraftClient.getInstance().world?.getPlayerByUuid(playerId) ?: return
        // All of the maths below is shamelessly stolen from InventoryScreen.drawEntity.
        // the -20 and 5 divided by 40 are for configuring the yaw and pitch tilt of the body and head respectively.
        // For more information, pray for divine inspiration or something idk.
        val f = atan((-40 / 40.0f).toDouble()).toFloat()
        val g = atan((-10 / 40.0f).toDouble()).toFloat()
        val quaternionf = Quaternionf().rotateZ(Math.PI.toFloat())
        val quaternionf2 = Quaternionf().rotateX(g * 20.0f * (Math.PI.toFloat() / 180))
        quaternionf.mul(quaternionf2)
        val oldBodyYaw = entity.bodyYaw
        val oldEntityYaw = entity.yaw
        val oldPitch = entity.pitch
        val oldPrevHeadYaw = entity.prevHeadYaw
        val oldHeadYaw = entity.headYaw
        // Modifies the entity for rendering based on our f and g values
        entity.bodyYaw = 180.0F + f * 20.0F
        entity.setYaw(180.0F + f * 40.0F)
        entity.setPitch(0F)
        entity.headYaw = entity.yaw
        entity.prevHeadYaw = entity.yaw
        val size = 37F
        val xOffset = 0
        val yOffset = 72
        InventoryScreen.drawEntity(drawContext, xOffset.toFloat(), yOffset.toFloat(), size, Vector3f(), quaternionf, quaternionf2, entity)
        // Resets the entity
        entity.bodyYaw = oldBodyYaw
        entity.setYaw(oldEntityYaw)
        entity.setPitch(oldPitch)
        entity.prevHeadYaw = oldPrevHeadYaw
        entity.headYaw = oldHeadYaw
    }
}

class ReferenceRenderableFace(val entity: PosableEntity): RenderableFace {
    val state = entity.delegate as PosableState
    override fun render(drawContext: DrawContext, partialTicks: Float) {
        val state = this.state
        if (state is PokemonClientDelegate) {
            state.currentAspects = state.currentEntity.pokemon.aspects
            drawPosablePortrait(
                identifier = state.currentEntity.pokemon.species.resourceIdentifier,
                aspects = state.currentEntity.pokemon.aspects,
                repository = PokemonModelRepository,
                contextScale = state.currentEntity.pokemon.form.baseScale,
                matrixStack = drawContext.matrices,
                state = state,
                partialTicks = 0F // It's already being rendered potentially so we don't need to tick the state.
            )
        } else if (state is NPCClientDelegate) {
            entity as NPCEntity
            state.currentAspects = entity.aspects
            val limbSwing = entity.limbAnimator.getPos(partialTicks)
            val limbSwingAmount = entity.limbAnimator.getSpeed(partialTicks)
            drawPosablePortrait(
                identifier = state.npcEntity.npc.resourceIdentifier,
                aspects = state.npcEntity.aspects,
                repository = NPCModelRepository,
                matrixStack = drawContext.matrices,
                state = state,
                partialTicks = 0F, // It's already being rendered potentially so we don't need to tick the state.
                limbSwing = limbSwing,
                limbSwingAmount = limbSwingAmount,
                ageInTicks = entity.age.toFloat(),
            )
        }
    }
}

class ArtificialRenderableFace(
    val modelType: String,
    val identifier: Identifier,
    val aspects: Set<String>
): RenderableFace {
    val state = FloatingState()

    override fun render(drawContext: DrawContext, partialTicks: Float) {
        val state = this.state
        state.currentAspects = aspects
        if (modelType == "pokemon") {
            val species = PokemonSpecies.getByIdentifier(identifier) ?: run {
                Cobblemon.LOGGER.error("Unable to find species for $identifier for a dialogue face. Defaulting to first species.")
                PokemonSpecies.species.first()
            }
            drawPosablePortrait(
                identifier = species.resourceIdentifier,
                aspects = aspects,
                matrixStack = drawContext.matrices,
                contextScale = species.getForm(aspects).baseScale,
                state = state,
                repository = PokemonModelRepository,
                partialTicks = partialTicks
            )
        } else if (modelType == "npc") {
            drawPosablePortrait(
                identifier = identifier,
                aspects = aspects,
                matrixStack = drawContext.matrices,
                state = state,
                repository = NPCModelRepository,
                partialTicks = partialTicks
            )
        }
    }
}