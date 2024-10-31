/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.getDepletableRedGreen
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.renderScaledGuiItemIcon
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.joml.Quaternionf
import org.joml.Vector3f

class PartySlotWidget(
    pX: Number,
    pY: Number,
    private val partyWidget: PartyWidget,
    private val summary: Summary,
    private val pokemon: Pokemon?,
    private val index: Int,
    private val isClientPartyMember: Boolean,
) : SoundlessWidget(pX.toInt(), pY.toInt(), WIDTH, HEIGHT, Component.literal("PartyMember")) {
    val state = FloatingState()
    companion object {
        const val WIDTH = 46
        const val HEIGHT = 27
        private const val PORTRAIT_DIAMETER = 25

        private val slotResource = cobblemonResource("textures/gui/summary/summary_party_slot.png")
        private val slotFaintedResource = cobblemonResource("textures/gui/summary/summary_party_slot_fainted.png")
        private val slotEmptyResource = cobblemonResource("textures/gui/summary/summary_party_slot_empty.png")
        val genderIconMale = cobblemonResource("textures/gui/party/party_gender_male.png")
        val genderIconFemale = cobblemonResource("textures/gui/party/party_gender_female.png")
    }

    private fun getSlotTexture(pokemon: Pokemon?): ResourceLocation {
        if (pokemon != null) {
            if (pokemon.isFainted()) return slotFaintedResource
            return slotResource
        }
        return slotEmptyResource
    }

    private fun getSlotVOffset(pokemon: Pokemon?, isHovered: Boolean, isSelected: Boolean): Int {
        if (isHovered || isSelected) {
            if (pokemon == null) {
                if (partyWidget.swapEnabled) return height
                return 0
            }
            return height
        }
        return 0
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
        val matrices = context.pose()
        val isDraggedSlot = partyWidget.swapEnabled && partyWidget.swapSource == index
        val slotPokemon = if (isDraggedSlot) null else pokemon
        val isSelected = this.isClientPartyMember && this.summary.selectedPokemon.uuid == slotPokemon?.uuid

        state.currentAspects = slotPokemon?.aspects ?: emptySet()

        blitk(
            matrixStack = matrices,
            texture = getSlotTexture(slotPokemon),
            x = x,
            y = y,
            width = width,
            height = height,
            vOffset = getSlotVOffset(slotPokemon, isHovered, isSelected),
            textureHeight = height * 2,
        )

        if (slotPokemon != null) {
            val halfScale = 0.5F

            val stateIcon = slotPokemon.state.getIcon(slotPokemon)
            if (stateIcon != null) {
                blitk(
                    matrixStack = matrices,
                    texture = stateIcon,
                    x = (x + 24.5) / halfScale,
                    y = (y + 3) / halfScale,
                    height = 17,
                    width = 24,
                    scale = halfScale
                )
            }

            val ballIcon = cobblemonResource("textures/gui/ball/" + slotPokemon.caughtBall.name.path + ".png")
            val ballHeight = 22
            blitk(
                matrixStack = matrices,
                texture = ballIcon,
                x = (x - 2) / halfScale,
                y = (y - 3) / halfScale,
                height = ballHeight,
                width = 18,
                textureHeight = ballHeight * 2,
                scale = halfScale
            )

            val status = slotPokemon.status?.status
            if (!slotPokemon.isFainted() && status != null) {
                val statusName = status.showdownName
                    blitk(
                    matrixStack = matrices,
                    texture = cobblemonResource("textures/gui/party/status_$statusName.png"),
                    x = x + 42,
                    y = y + 5,
                    height = 14,
                    width = 4
                )
            }

            val hpRatio = slotPokemon.currentHealth / slotPokemon.maxHealth.toFloat()
            val barWidthMax = 37
            val barWidth = hpRatio * barWidthMax
            val (red, green) = getDepletableRedGreen(hpRatio)

            blitk(
                matrixStack = matrices,
                texture = CobblemonResources.WHITE,
                x = x + 4,
                y = y + 25,
                width = barWidth,
                height = 1,
                textureWidth = barWidth / hpRatio,
                uOffset = barWidthMax - barWidth,
                red = red * 0.8F,
                green = green * 0.8F,
                blue = 0.27F
            )

            // Render Pokémon
            matrices.pushPose()
            matrices.translate(x + (PORTRAIT_DIAMETER / 2.0), y - 3.0, 0.0)
            matrices.scale(2.5F, 2.5F, 1F)
            drawProfilePokemon(
                species = slotPokemon.species.resourceIdentifier,
                matrixStack = matrices,
                rotation = Quaternionf().fromEulerXYZDegrees(Vector3f(13F, 35F, 0F)),
                state = state,
                scale = 4.5F,
                partialTicks = delta
            )
            matrices.popPose()

            drawScaledText(
                context = context,
                text = slotPokemon.getDisplayName(),
                x = x + 4,
                y = y + 20,
                scale = halfScale
            )

            if (slotPokemon.gender != Gender.GENDERLESS) {
                blitk(
                    matrixStack = matrices,
                    texture = if (slotPokemon.gender == Gender.MALE) genderIconMale else genderIconFemale,
                    x = (x + 40) / halfScale,
                    y = (y + 20) / halfScale,
                    height = 7,
                    width = 5,
                    scale = halfScale
                )
            }

            drawScaledText(
                context = context,
                text = lang("ui.lv.number", slotPokemon.level),
                x = x + 31,
                y = y + 13,
                centered = true,
                shadow = true,
                scale = halfScale
            )

            // Held Item
            val heldItem = slotPokemon.heldItemNoCopy()
            if (!heldItem.isEmpty) {
                renderScaledGuiItemIcon(
                    itemStack = heldItem,
                    x = x + 14.0,
                    y = y + 9.5,
                    scale = 0.5,
                    matrixStack = matrices
                )
            }
        }
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (isValidClick(pMouseX, pMouseY, pButton)) {
            if (partyWidget.swapEnabled) {
                toggleDrag(true)
            } else {
                if ((index > -1) && (summary.selectedPokemon.uuid != pokemon?.uuid)) {
                    summary.switchSelection(index)
                    if (pokemon != null) partyWidget.playSound(CobblemonSounds.GUI_CLICK)
                    return true
                }
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }

    override fun mouseReleased(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (partyWidget.swapEnabled) toggleDrag(false)
        return partyWidget.mouseReleased(pMouseX, pMouseY, pButton)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, f: Double, g: Double): Boolean {
        if (partyWidget.swapEnabled && partyWidget.isWithinScreen(mouseX, mouseY) && index < 0) {
            repositionSlot(mouseX, mouseY)
        } else {
            if (partyWidget.swapSource != null) partyWidget.playSound(CobblemonSounds.PC_DROP)
            toggleDrag(false)
            partyWidget.swapSource = null
            partyWidget.draggedSlot = null
        }
        return super.mouseDragged(mouseX, mouseY, button, f, g)
    }

    private fun toggleDrag(boolean: Boolean) {
        val focusedElement = if (boolean) partyWidget.draggedSlot else null
        summary.focused = focusedElement
        summary.isDragging = boolean
    }

    private fun repositionSlot(mouseX: Double, mouseY: Double) {
        x = (mouseX - (WIDTH / 2)).toInt()
        y = (mouseY - (HEIGHT / 2)).toInt()
    }

    private fun isValidClick(mouseX: Double, mouseY: Double, button: Int): Boolean = button == 0
        && mouseX.toInt() in x..(x + width)
        && mouseY.toInt() in y..(y + height)
}