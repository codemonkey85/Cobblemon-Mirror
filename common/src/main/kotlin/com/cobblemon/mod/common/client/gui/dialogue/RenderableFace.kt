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
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.npc.NPCEntity
import java.util.UUID
import kotlin.math.atan
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.resources.ResourceLocation
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * Some time of face that can be rendered in a dialogue.
 *
 * @author Hiroku
 * @since January 1st, 2024
 */
sealed interface RenderableFace {
    val isLeftSide: Boolean
    fun render(GuiGraphics: GuiGraphics, partialTicks: Float)
}

class PlayerRenderableFace(val playerId: UUID, override val isLeftSide: Boolean) : RenderableFace {
    override fun render(GuiGraphics: GuiGraphics, partialTicks: Float) {
        val entity = Minecraft.getInstance().level?.getPlayerByUUID(playerId) ?: return
        // All of the maths below is shamelessly stolen from InventoryScreen.drawEntity.
        // the -20 and 5 divided by 40 are for configuring the yaw and pitch tilt of the body and head respectively.
        // For more information, pray for divine inspiration or something idk.
        val f = atan((-40 / 40.0f).toDouble()).toFloat()
        val g = atan((-10 / 40.0f).toDouble()).toFloat()
        val quaternionf = Quaternionf().rotateZ(Math.PI.toFloat())
        val quaternionf2 = Quaternionf().rotateX(g * 20.0f * (Math.PI.toFloat() / 180))
        quaternionf.mul(quaternionf2)
        val oldBodyYaw = entity.yBodyRot
        val oldEntityYaw = entity.yRot
        val oldPitch = entity.xRot
        val oldPrevHeadYaw = entity.yHeadRotO
        val oldHeadYaw = entity.yHeadRot
        // Modifies the entity for rendering based on our f and g values
        entity.yBodyRot = 180.0F + f * 20.0F
        entity.yRot = (180.0F + f * 40.0F) * if (isLeftSide) 1 else -1
        entity.xRot = 0F
        entity.yHeadRot = entity.yRot // TODO (techdaan): is this correct, looks weird.
        entity.yHeadRotO = entity.yRot
        val size = 37F
        val xOffset = 0
        val yOffset = 72
        InventoryScreen.renderEntityInInventory(GuiGraphics, xOffset.toFloat(), yOffset.toFloat(), size, Vector3f(), quaternionf, quaternionf2, entity)
        // Resets the entity
        entity.yBodyRot = oldBodyYaw
        entity.yRot = oldEntityYaw
        entity.xRot = oldPitch
        entity.yHeadRotO = oldPrevHeadYaw
        entity.yHeadRot = oldHeadYaw
    }
}

class ReferenceRenderableFace(val entity: PosableEntity, override val isLeftSide: Boolean): RenderableFace {
    val state = entity.delegate as PosableState
    override fun render(GuiGraphics: GuiGraphics, partialTicks: Float) {
        val state = this.state
        if (state is PokemonClientDelegate) {
            state.currentAspects = state.currentEntity.pokemon.aspects
            drawPosablePortrait(
                identifier = state.currentEntity.pokemon.species.resourceIdentifier,
                contextScale = state.currentEntity.pokemon.form.baseScale,
                matrixStack = GuiGraphics.pose(),
                state = state,
                reversed = !isLeftSide,
                partialTicks = 0F // It's already being rendered potentially so we don't need to tick the state.
            )
        } else if (state is NPCClientDelegate) {
            entity as NPCEntity
            state.currentAspects = entity.aspects
            val limbSwing = entity.walkAnimation.position(partialTicks)
            val limbSwingAmount = entity.walkAnimation.speed(partialTicks)
            drawPosablePortrait(
                identifier = state.npcEntity.npc.resourceIdentifier,
                matrixStack = GuiGraphics.pose(),
                state = state,
                reversed = !isLeftSide,
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
    val identifier: ResourceLocation,
    val aspects: Set<String>,
    override val isLeftSide: Boolean
): RenderableFace {
    val state = FloatingState()

    override fun render(GuiGraphics: GuiGraphics, partialTicks: Float) {
        val state = this.state
        state.currentAspects = aspects
        if (modelType == "pokemon") {
            val species = PokemonSpecies.getByIdentifier(identifier) ?: run {
                Cobblemon.LOGGER.error("Unable to find species for $identifier for a dialogue face. Defaulting to first species.")
                PokemonSpecies.species.first()
            }
            drawPosablePortrait(
                identifier = species.resourceIdentifier,
                matrixStack = GuiGraphics.pose(),
                contextScale = species.getForm(aspects).baseScale,
                state = state,
                reversed = !isLeftSide,
                partialTicks = partialTicks
            )
        } else if (modelType == "npc") {
            drawPosablePortrait(
                identifier = identifier,
                matrixStack = GuiGraphics.pose(),
                state = state,
                reversed = !isLeftSide,
                partialTicks = partialTicks
            )
        }
    }
}