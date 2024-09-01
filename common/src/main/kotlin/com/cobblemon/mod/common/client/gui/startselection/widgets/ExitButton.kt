/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.startselection.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.sounds.SoundManager

class ExitButton(
    pX: Int, pY: Int,
    pWidth: Int, pHeight: Int,
    pXTexStart: Int, pYTexStart: Int, pYDiffText: Int,
    onPress: OnPress
): Button(pX, pY, EXIT_BUTTON_WIDTH.toInt(), EXIT_BUTTON_HEIGHT.toInt(), "cobblemon.ui.starter.narrator.backbutton".asTranslated(), onPress, DEFAULT_NARRATION) {

    companion object {
        private const val EXIT_BUTTON_WIDTH = 15.95F
        private const val EXIT_BUTTON_HEIGHT = 11.95F
        private val exitButtonResource = cobblemonResource("textures/gui/starterselection/starterselection_exit.png")
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        isHovered = pMouseX >= x && pMouseY >= y && pMouseX < x + width && pMouseY < y + height
        if (isHovered) {
            blitk(
                matrixStack = context.pose(),
                x = x + 0.075f, y = y + 1.05f,
                texture = exitButtonResource,
                width = EXIT_BUTTON_WIDTH, height = EXIT_BUTTON_HEIGHT
            )
        }
    }

    override fun playDownSound(soundManager: SoundManager) { }

}