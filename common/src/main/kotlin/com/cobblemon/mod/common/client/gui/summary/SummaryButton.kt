/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

class SummaryButton(
    var buttonX: Float,
    var buttonY: Float,
    val buttonWidth: Number,
    val buttonHeight: Number,
    val clickAction: OnPress,
    private val text: MutableComponent = Component.empty(),
    private val resource: ResourceLocation,
    private val activeResource: ResourceLocation? = null,
    private val renderRequirement: ((button: SummaryButton) -> Boolean) = { true },
    private val clickRequirement: ((button: SummaryButton) -> Boolean) = { true },
    private val hoverTexture: Boolean = true,
    private val silent: Boolean = false,
    private val boldText: Boolean = true,
    private val largeText: Boolean = true,
    private val textScale: Float = 1F
): Button(buttonX.toInt(), buttonY.toInt(), buttonWidth.toInt(), buttonHeight.toInt(), text, clickAction, DEFAULT_NARRATION) {

    companion object {
        const val TEXT_HEIGHT = 9
    }

    var buttonActive = false

    override fun mouseDragged(d: Double, e: Double, i: Int, f: Double, g: Double) = false

    override fun defaultButtonNarrationText(builder: NarrationElementOutput) {
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        if (!this.renderRequirement.invoke(this)) {
            return
        }
        val matrices = context.pose()

        // Render Button
        blitk(
            matrixStack = matrices,
            texture = if (buttonActive && activeResource != null) activeResource else resource,
            x = buttonX,
            y = buttonY,
            width = buttonWidth,
            height = buttonHeight,
            vOffset = if (hoverTexture && isHovered) buttonHeight else 0,
            textureHeight = if (hoverTexture) (buttonHeight.toFloat() * 2) else buttonHeight,
        )

        // Render Text
        drawScaledText(
            context = context,
            font = if (largeText) CobblemonResources.DEFAULT_LARGE else null,
            text = if (boldText) text.bold() else text,
            x = buttonX + (buttonWidth.toFloat() / 2),
            y = buttonY + (buttonHeight.toFloat() / 2) - ((TEXT_HEIGHT / 2) * textScale),
            scale = textScale,
            centered = true,
            shadow = true
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.clickRequirement.invoke(this)) {
            super.mouseClicked(mouseX, mouseY, button)
        }
        return false
    }

    override fun playDownSound(soundManager: SoundManager) {
        if (!this.silent) {
            soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
        }
    }

    fun setPosFloat(x: Float, y: Float) {
        this.x = x.toInt()
        this.y = y.toInt()
        this.buttonX = x
        this.buttonY = y
    }
}