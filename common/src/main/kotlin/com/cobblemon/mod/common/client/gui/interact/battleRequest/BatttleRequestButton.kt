/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.battleRequest

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

class BattleRequestButton(
        pX: Int, pY: Int,
        var text: MutableComponent,
        onPress: OnPress
): Button(pX, pY, (WIDTH * SCALE).toInt(), (HEIGHT * SCALE).toInt(), text, onPress, DEFAULT_NARRATION) {

    companion object {
        private const val WIDTH = 69F
        private const val HEIGHT = 19F
        private const val SCALE = 1F
        private val buttonResource = cobblemonResource("textures/gui/interact/request/button_request_battle.png")
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val hovered = (isHovered(pMouseX.toDouble(), pMouseY.toDouble()))
        blitk(
                matrixStack = context.pose(),
                x = x / SCALE,
                y = y / SCALE,
                texture = buttonResource,
                width = WIDTH,
                height = HEIGHT,
                vOffset = if (hovered) HEIGHT else 0,
                textureHeight = HEIGHT * 2,
                scale = SCALE
        )

        drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = text.bold(),
                x = x + 35,
                y = y + 5,
                centered = true,
                shadow = true
        )
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F))
    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (x.toFloat()..(x.toFloat() + (WIDTH * SCALE))) && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + (HEIGHT * SCALE)))
}