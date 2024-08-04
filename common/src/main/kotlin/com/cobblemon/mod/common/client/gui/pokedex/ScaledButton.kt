/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.text
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.util.Identifier

class ScaledButton(
    var buttonX: Float,
    var buttonY: Float,
    val buttonWidth: Number,
    val buttonHeight: Number,
    var resource: ResourceLocation? = null,
    val scale: Float = 0.5F,
    val silent: Boolean = false,
    val clickAction: PressAction
): ButtonWidget(buttonX.toInt(), buttonY.toInt(), buttonWidth.toInt(), buttonHeight.toInt(), "".text(), clickAction, DEFAULT_NARRATION_SUPPLIER) {

    var isActive = false

    override fun mouseDragged(d: Double, e: Double, i: Int, f: Double, g: Double) = false
    override fun appendDefaultNarrations(builder: NarrationMessageBuilder) {
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val matrices = context.pose()

        if (resource != null) {
            blitk(
                matrixStack = matrices,
                texture = resource,
                x = buttonX / scale,
                y = buttonY / scale,
                width = buttonWidth,
                height = buttonHeight,
                vOffset = if (isButtonHovered(pMouseX, pMouseY) || isActive) buttonHeight else 0,
                textureHeight = buttonHeight.toFloat() * 2,
                scale = scale
            )
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active && isButtonHovered(mouseX, mouseY)) {
            super.mouseClicked(mouseX, mouseY, button)
        }
        return false
    }

    override fun playDownSound(soundManager: SoundManager) {
        if (active && !this.silent) {
            soundManager.play(PositionedSoundInstance.master(CobblemonSounds.POKEDEX_CLICK_SHORT, 1.0F))
        }
    }

    private fun isButtonHovered(mouseX: Number, mouseY: Number): Boolean {
        return mouseX.toFloat() in (buttonX..(buttonX + (buttonWidth.toFloat() * scale)))
                && mouseY.toFloat() in (buttonY..(buttonY + (buttonHeight.toFloat() * scale)))
    }
}