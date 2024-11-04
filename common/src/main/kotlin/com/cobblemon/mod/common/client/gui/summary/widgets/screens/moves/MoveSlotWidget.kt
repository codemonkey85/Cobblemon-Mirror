/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.gold
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.MoveCategoryIcon
import com.cobblemon.mod.common.client.gui.TypeIcon
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.math.toRGB
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth

class MoveSlotWidget(
    pX: Int, pY: Int,
    val move: Move?,
    private val movesWidget: MovesWidget,
        private val pokemon: Pokemon,
): SoundlessWidget(pX, pY, MOVE_WIDTH, MOVE_HEIGHT, Component.literal(move?.name ?: "")) {

    companion object {
        private val moveResource = cobblemonResource("textures/gui/summary/summary_move.png")
        private val moveOverlayResource = cobblemonResource("textures/gui/summary/summary_move_overlay.png")
        private val moveSelectedOverlayResource = cobblemonResource("textures/gui/summary/summary_move_selected_overlay.png")

        const val MOVE_WIDTH = 108
        const val MOVE_HEIGHT = 22
    }

    private val moveUpButton = ReorderMoveButton(x, y, true) {
        movesWidget.selectMove(null)
        movesWidget.reorderMove(this, true)
    }.apply {
        addWidget(this)
    }
    private val moveDownButton = ReorderMoveButton(x, y, false) {
        movesWidget.selectMove(null)
        movesWidget.reorderMove(this, false)
    }.apply {
        addWidget(this)
    }

    private val switchMoveButton = SwapMoveButton(x, y, move?.template, movesWidget) {
        movesWidget.selectMove(null)
        if (movesWidget.summary.sideScreenIndex == Summary.MOVE_SWAP) {
            movesWidget.summary.displaySideScreen(Summary.PARTY)
        } else {
            movesWidget.summary.displaySideScreen(Summary.MOVE_SWAP, move)
        }
    }.apply {
        addWidget(this)
    }
    val elementalType:ElementalType = Moves.getByNameOrDummy(move?.name ?: "").getEffectiveElementalType(pokemon)
    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val matrices = context.pose()
        isHovered = pMouseX >= x && pMouseY >= y && pMouseX < x + width && pMouseY < y + height
        if (move != null) {
            val moveTemplate = Moves.getByNameOrDummy(move.name)
            val rgb = elementalType.hue.toRGB()

            if (movesWidget.selectedMove == move) {
                blitk(
                        matrixStack = matrices,
                        texture = moveSelectedOverlayResource,
                        x = x - 1,
                        y = y - 1,
                        width = MOVE_WIDTH + 2,
                        height = MOVE_HEIGHT + 2
                )
            }

            blitk(
                    matrixStack = matrices,
                    texture = moveResource,
                    x = x,
                    y = y,
                    width = MOVE_WIDTH,
                    height = MOVE_HEIGHT,
                    vOffset = if (isHovered) MOVE_HEIGHT else 0,
                    textureHeight = MOVE_HEIGHT * 2,
                    red = rgb.first,
                    green = rgb.second,
                    blue = rgb.third
            )

            blitk(
                    matrixStack = matrices,
                    texture = moveOverlayResource,
                    x = x,
                    y = y,
                    width = MOVE_WIDTH,
                    height = MOVE_HEIGHT
            )

            var movePPText = Component.literal("${move.currentPp}/${move.maxPp}").bold()

            if (move.currentPp <= Mth.floor(move.maxPp / 2F)) {
                movePPText = if (move.currentPp == 0) movePPText.red() else movePPText.gold()
            }

            drawScaledText(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = movePPText,
                    x = x + 93,
                    y = y + 13,
                    centered = true
            )

            // Type Icon
            TypeIcon(
                x = x + 2,
                y = y + 2,
                type = elementalType
            ).render(context)

            // Move Category
            MoveCategoryIcon(
                    x = x + 66,
                    y = y + 13.5,
                    category = move.damageCategory
            ).render(context)

            // Move Name
            drawScaledText(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = move.displayName.bold(),
                    x = x + 28,
                    y = y + 2,
                    shadow = true
            )

            // Reorder Buttons
            moveUpButton.render(context, pMouseX, pMouseY, pPartialTicks)
            moveDownButton.render(context, pMouseX, pMouseY, pPartialTicks)
        }

        // Switch Move Button
        switchMoveButton.render(context, pMouseX, pMouseY, pPartialTicks)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isHovered) {
            movesWidget.selectMove(move)
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }
}