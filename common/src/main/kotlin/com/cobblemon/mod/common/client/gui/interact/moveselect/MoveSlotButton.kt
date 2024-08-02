/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.moveselect

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.gold
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.MoveCategoryIcon
import com.cobblemon.mod.common.client.gui.TypeIcon
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.math.toRGB
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Button.CreateNarration
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth

class MoveSlotButton(
    x: Int, y: Int,
    val move: MoveTemplate,
    val pp: Int,
    val ppMax: Int,
    val enabled: Boolean = true,
    onPress: OnPress
) : Button(x, y, WIDTH, HEIGHT, Component.literal("Move"), onPress, CreateNarration { "".text() }) {

    companion object {
        private val moveResource = cobblemonResource("textures/gui/summary/summary_move.png")
        private val moveOverlayResource = cobblemonResource("textures/gui/summary/summary_move_overlay.png")

        const val WIDTH = 108
        const val HEIGHT = 22
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        isHovered = pMouseX >= x && pMouseY >= y && pMouseX < x + width && pMouseY < y + height && enabled

        val moveTemplate = Moves.getByNameOrDummy(move.name)
        val rgb = moveTemplate.elementalType.hue.toRGB()

        val alpha = if (enabled) 1.0 else 0.5

        val matrices = context.pose()
        blitk(
            matrixStack = matrices,
            texture = moveResource,
            x = x,
            y = y,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (isHovered) HEIGHT else 0,
            textureHeight = HEIGHT * 2,
            red = rgb.first,
            green = rgb.second,
            blue = rgb.third,
            alpha = alpha
        )

        blitk(
            matrixStack = matrices,
            texture = moveOverlayResource,
            x = x,
            y = y,
            width = WIDTH,
            height = HEIGHT,
            alpha = alpha
        )

        if (pp != -1 && ppMax != -1) {
            var movePPText = Component.literal("$pp/$ppMax").bold()

            if (pp <= Mth.floor(ppMax / 2F)) {
                movePPText = if (pp == 0) movePPText.red() else movePPText.gold()
            }

            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = movePPText,
                x = x + 93,
                y = y + 13,
                centered = true
            )
        }

        // Type Icon
        TypeIcon(
            x = x + 2,
            y = y + 2,
            type = moveTemplate.elementalType
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
    }

    override fun playDownSound(soundManager: SoundManager) {}
}