/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.gui.pasture.PasturePCGUIConfiguration
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.renderScaledGuiItemIcon
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import org.joml.Quaternionf
import org.joml.Vector3f

open class StorageSlot(
    x: Int, y: Int,
    private val parent: StorageWidget,
    onPress: OnPress
) : Button(x, y, SIZE, SIZE, Component.literal("StorageSlot"), onPress, DEFAULT_NARRATION) {
    val state = FloatingState()

    companion object {
        const val SIZE = 25

        private val genderIconMale = cobblemonResource("textures/gui/pc/gender_icon_male.png")
        private val genderIconFemale = cobblemonResource("textures/gui/pc/gender_icon_female.png")
        private val selectPointerResource = cobblemonResource("textures/gui/pc/pc_pointer.png")
        private val slotOverlayResource = cobblemonResource("textures/gui/pc/pc_slot_overlay.png")
        private val slotOverlayPastureIconResource = cobblemonResource("textures/gui/pasture/pc_slot_icon_pasture.png")
        private val slotOverlayMoveIconResource = cobblemonResource("textures/gui/pasture/pc_slot_icon_move.png")
    }

    override fun playDownSound(soundManager: SoundManager) {
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (shouldRender()) {
            renderSlot(context, x, y, delta)
        }
    }

    fun renderSlot(context: GuiGraphics, posX: Int, posY: Int, partialTicks: Float) {
        val pokemon = getPokemon() ?: return
        val matrices = context.pose()
        context.enableScissor(
            posX - 2,
            posY + 2,
            posX + SIZE + 4,
            posY + SIZE + 4
        )

        // Render Pokémon
        matrices.pushPose()
        matrices.translate(posX + (SIZE / 2.0), posY + 1.0, 0.0)
        matrices.scale(2.5F, 2.5F, 1F)
        drawProfilePokemon(
            renderablePokemon = pokemon.asRenderablePokemon(),
            matrixStack = matrices,
            rotation = Quaternionf().fromEulerXYZDegrees(Vector3f(13F, 35F, 0F)),
            state = state,
            partialTicks = partialTicks,
            scale = 4.5F
        )
        matrices.popPose()

        context.disableScissor()

        // Ensure elements are not hidden behind Pokémon render
        matrices.pushPose()
        matrices.translate(0.0, 0.0, 100.0)
        // Level
        drawScaledText(
            context = context,
            text = lang("ui.lv.number", pokemon.level),
            x = posX + 1,
            y = posY + 1,
            shadow = true,
            scale = PCGUI.SCALE
        )

        if (pokemon.gender != Gender.GENDERLESS) {
            blitk(
                matrixStack = matrices,
                texture = if (pokemon.gender == Gender.MALE) genderIconMale else genderIconFemale,
                x = (posX + 21) / PCGUI.SCALE,
                y = (posY + 1) / PCGUI.SCALE,
                width = 6,
                height = 8,
                scale = PCGUI.SCALE
            )
        }

        // Held Item
        val heldItem = pokemon.heldItemNoCopy()
        if (!heldItem.isEmpty) {
            renderScaledGuiItemIcon(
                itemStack = heldItem,
                x = posX + 16.0,
                y = posY + 16.0,
                scale = 0.5,
                matrixStack = matrices
            )
        }
        matrices.popPose()

        // Ensure overlay elements are on top
        matrices.pushPose()
        matrices.translate(0.0, 0.0, 500.0)

        val config = parent.pcGui.configuration
        if (pokemon.tetheringId != null) {
            if (isStationary()) {
                blitk(
                    matrixStack = matrices,
                    x = posX,
                    y = posY,
                    width = SIZE,
                    height = SIZE,
                    texture = slotOverlayResource
                )
            }

            val opacity = if (config is PasturePCGUIConfiguration && config.pasturedPokemon.get().none { it.pokemonId == pokemon.uuid }) 0.5F else 1F

            blitk(
                matrixStack = matrices,
                x = (posX + 7.5) / PCGUI.SCALE,
                y = (posY + 7.5) / PCGUI.SCALE,
                width = 20,
                height = 20,
                texture = slotOverlayPastureIconResource,
                scale = PCGUI.SCALE,
                alpha = opacity
            )
        }

        if (isHoveredOrFocused) {
            // If pasture UI and slot is not in pasture
            if (config is PasturePCGUIConfiguration
                && pokemon.tetheringId == null
                && isStationary()
                && config.permissions.canPasture
                && config.canSelect(pokemon)
                && config.pasturedPokemon.get().size < config.limit
                && config.pasturedPokemon.get().count { it.playerId == Minecraft.getInstance().player!!.uuid } < config.permissions.maxPokemon
            ) {
                blitk(
                    matrixStack = matrices,
                    x = posX,
                    y = posY,
                    width = SIZE,
                    height = SIZE,
                    texture = slotOverlayResource
                )

                blitk(
                    matrixStack = matrices,
                    x = (posX + 7.5) / PCGUI.SCALE,
                    y = (posY + 7.5) / PCGUI.SCALE,
                    width = 20,
                    height = 20,
                    texture = slotOverlayMoveIconResource,
                    scale = PCGUI.SCALE
                )
            }

            // Arrow pointer
            blitk(
                matrixStack = matrices,
                texture = selectPointerResource,
                x = (posX + 10) / PCGUI.SCALE,
                y = ((posY - 3) / PCGUI.SCALE) - parent.pcGui.selectPointerOffsetY,
                width = 11,
                height = 8,
                scale = PCGUI.SCALE
            )
        }
        matrices.popPose()
    }

    open fun isStationary(): Boolean {
        return true
    }

    open fun getPokemon(): Pokemon? {
        return null
    }

    override fun isHoveredOrFocused(): Boolean {
        return getPokemon() == parent.pcGui.previewPokemon
    }

    open fun shouldRender(): Boolean {
        return true
    }

    fun isHovered(mouseX: Int, mouseY: Int) = mouseX.toFloat() in (x.toFloat()..(x.toFloat() + SIZE)) && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + SIZE))
}