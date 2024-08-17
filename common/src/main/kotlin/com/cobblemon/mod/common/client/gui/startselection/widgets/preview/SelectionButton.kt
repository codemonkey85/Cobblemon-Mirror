/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.startselection.widgets.preview

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.ColourLibrary
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component

class SelectionButton(
    pX: Int, pY: Int,
    pWidth: Int, pHeight: Int,
    onPress: OnPress
): Button(pX, pY, pWidth, pHeight, Component.literal("SelectionButton"), onPress, DEFAULT_NARRATION) {

    companion object {
        private val buttonTexture = cobblemonResource("textures/gui/starterselection/starterselection_button.png")
        const val BUTTON_WIDTH = 56
        const val BUTTON_HEIGHT = 12
        private const val SCALE = 0.7f
    }

    override fun playDownSound(soundManager: SoundManager) {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val matrices = context.pose()
        if (isHovered)
            blitk(
                matrixStack = matrices,
                texture = buttonTexture,
                x = x + 0.6, y = y + 0.6,
                width = BUTTON_WIDTH - 0.25, height = BUTTON_HEIGHT - 0.25,
                red = 0.75f, green = 0.75f, blue = 0.75f
            )
        else
            blitk(
                matrixStack = matrices,
                texture = buttonTexture,
                x = x + 0.6, y = y + 0.6,
                width = BUTTON_WIDTH - 0.25, height = BUTTON_HEIGHT - 0.25
            )
        drawScaledText(
            context = context,
            text = lang("ui.starter.choosebutton"),
            x = x + BUTTON_WIDTH / 2, y = y + BUTTON_HEIGHT / 2 - 2.4,
            colour = ColourLibrary.WHITE,
            centered = true,
            maxCharacterWidth = 68,
            scale = SCALE,
            shadow = true
        )
    }
}