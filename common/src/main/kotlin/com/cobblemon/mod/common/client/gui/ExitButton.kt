/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component

class ExitButton(
    pX: Int, pY: Int,
    onPress: OnPress
): Button(pX, pY, WIDTH.toInt(), HEIGHT.toInt(), Component.literal("Exit"), onPress, DEFAULT_NARRATION) {

    companion object {
        private const val WIDTH = 26F
        private const val HEIGHT = 13F
        private const val SCALE = 0.5F
        private val buttonResource = cobblemonResource("textures/gui/common/back_button.png")
        private val iconResource = cobblemonResource("textures/gui/common/back_button_icon.png")
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        blitk(
            matrixStack = context.pose(),
            texture = buttonResource,
            x = x,
            y = y,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (isHovered(pMouseX.toDouble(), pMouseY.toDouble())) HEIGHT else 0,
            textureHeight = HEIGHT * 2
        )

        blitk(
            matrixStack = context.pose(),
            texture = iconResource,
            x = (x + 7) / SCALE,
            y = (y + 4) / SCALE,
            width = 21,
            height = 11,
            scale = SCALE
        )
    }

    override fun playDownSound(soundManager: SoundManager) {
    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (x.toFloat()..(x.toFloat() + WIDTH)) && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + HEIGHT))
}