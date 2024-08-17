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
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.client.gui.summary.SummaryButton
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import java.security.InvalidParameterException

class PartyWidget(
    pX: Int,
    pY: Int,
    val isParty: Boolean,
    val summary: Summary,
    private val partyList: List<Pokemon?>
) : SoundlessWidget(pX, pY, WIDTH, HEIGHT, Component.literal("PartyOverlay")) {

    companion object {
        const val WIDTH = 114
        const val HEIGHT = 113
        private const val SCALE = 0.5F

        private val backgroundResource = cobblemonResource("textures/gui/summary/summary_party_background.png")
        private val swapButtonResource = cobblemonResource("textures/gui/summary/summary_party_swap.png")
        private val swapButtonActiveResource = cobblemonResource("textures/gui/summary/summary_party_swap_active.png")
        private val swapButtonIconResource = cobblemonResource("textures/gui/summary/summary_party_swap_icon.png")
    }

    var swapEnabled: Boolean = false
    var swapSource: Int? = null
    var draggedSlot: PartySlotWidget? = null

    private val partySize = partyList.size
    private val partySlots = arrayListOf<PartySlotWidget>()
    private val swapButton: SummaryButton = SummaryButton(
        buttonX = x + 80F,
        buttonY = y - 9F,
        buttonWidth = 26,
        buttonHeight = 14,
        resource = swapButtonResource,
        activeResource = swapButtonActiveResource,
        clickAction = {
            swapEnabled = !swapEnabled
            if (!swapEnabled) {
                swapSource = null
                draggedSlot = null
            }
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
        }
    )

    init {
        if (partySize > 6 || partySize < 1)
            throw InvalidParameterException("Invalid party size")

        this.partyList.forEachIndexed { index, pokemon ->
            var x = x + 6
            var y = y + 7

            if (index > 0) {
                val isEven = index % 2 == 0
                val offsetIndex = (index - (if (isEven) 0 else 1)) / 2
                val offsetX = if (isEven) 0 else 51
                val offsetY = if (isEven) 0 else 8

                x += offsetX
                y += (32 * offsetIndex) + offsetY
            }

            PartySlotWidget(
                pX = x,
                pY = y,
                partyWidget = this,
                summary = summary,
                pokemon = pokemon,
                index = index,
                isClientPartyMember = isParty
            ).also { widget ->
                this.addWidget(widget)
                partySlots.add(widget)
            }
        }
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val matrices = context.pose()
        blitk(
            matrixStack = matrices,
            texture = backgroundResource,
            x = x,
            y = y,
            width = width,
            height = height
        )

        // Label
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.party").bold(),
            x = x + 32.5,
            y = y - 14.5,
            centered = true,
            shadow = true
        )

        swapButton.render(context, pMouseX, pMouseY, pPartialTicks)

        blitk(
            matrixStack = matrices,
            texture = swapButtonIconResource,
            x = (x + 90) / SCALE,
            y = (y - 6) / SCALE,
            width = 12,
            height = 17,
            scale = SCALE
        )

        partySlots.forEach { it.render(context, pMouseX, pMouseY, pPartialTicks) }

        if (draggedSlot != null) {
            matrices.pushPose()
            matrices.translate(0.0, 0.0, 500.0)
            draggedSlot!!.render(context, pMouseX, pMouseY, pPartialTicks)
            matrices.popPose()
        }
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (swapButton.isHovered) {
            swapButton.onPress()
            swapButton.buttonActive = swapEnabled
        }

        if (swapEnabled) {
            val index = getIndexFromPos(pMouseX, pMouseY)
            if (index > -1) {
                val sourcePokemon = partyList[index]
                if (sourcePokemon != null) {
                    swapSource = index
                    draggedSlot = PartySlotWidget(
                        pX = pMouseX - (PartySlotWidget.WIDTH / 2),
                        pY = pMouseY - (PartySlotWidget.HEIGHT / 2),
                        partyWidget = this,
                        summary = summary,
                        pokemon = sourcePokemon,
                        index = -1,
                        isClientPartyMember = isParty
                    )
                    playSound(CobblemonSounds.PC_GRAB)
                }
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }

    override fun mouseReleased(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (swapEnabled) {
            if (swapSource != null) {
                val index = getIndexFromPos(pMouseX, pMouseY)
                if (index > -1 && index != swapSource) {
                    summary.swapPartySlot(swapSource!!, index)
                }
                swapSource = null
                draggedSlot = null
                playSound(CobblemonSounds.PC_DROP)
            }
        }
        return true
    }

    fun enableSwap(boolean: Boolean = true) {
        swapEnabled = boolean
        swapButton.buttonActive = boolean
    }

    private fun getIndexFromPos(mouseX: Double, mouseY: Double): Int {
        for (index in 0..5) {
            var posX = x + 6
            var posY = y + 7
            if (index > 0) {
                val isEven = index % 2 == 0
                val offsetIndex = (index - (if (isEven) 0 else 1)) / 2
                val offsetX = if (isEven) 0 else 51
                val offsetY = if (isEven) 0 else 8

                posX += offsetX
                posY += (32 * offsetIndex) + offsetY
            }
            if (mouseX.toInt() in posX..(posX + PartySlotWidget.WIDTH)
                && mouseY.toInt() in posY..(posY + PartySlotWidget.HEIGHT)) {
                return index
            }
        }
        return -1
    }

    fun playSound(soundEvent: SoundEvent) {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(soundEvent, 1.0F))
    }

    fun isWithinScreen(mouseX: Double, mouseY: Double): Boolean = mouseX.toInt() in x..(x + WIDTH)
            && mouseY.toInt() in y..(y + HEIGHT)
}