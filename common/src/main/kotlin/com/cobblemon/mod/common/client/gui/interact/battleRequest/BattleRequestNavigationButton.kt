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
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component

class BattleRequestNavigationButton(
    pX: Int, pY: Int,
    private val forward: Boolean,
    onPress: OnPress
): Button(pX, pY, (WIDTH * SCALE).toInt(), (CLICK_HEIGHT * SCALE).toInt(), Component.literal("Navigation"), onPress, DEFAULT_NARRATION) {

    companion object {
        private const val WIDTH = 9F
        private const val HEIGHT = 16F
        private const val CLICK_HEIGHT = HEIGHT * 6
        private const val SCALE = 0.5F
        private val forwardButtonResource = cobblemonResource("textures/gui/interact/request/arrow_right.png")
        private val backwardsButtonResource = cobblemonResource("textures/gui/interact/request/arrow_left.png")
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val hovered = (isHovered(pMouseX.toDouble(), pMouseY.toDouble()))
        blitk(
            matrixStack = context.pose(),
            x = x / SCALE,
            y = (y + (CLICK_HEIGHT - HEIGHT) / 4) / SCALE,
            texture = if (forward) forwardButtonResource else backwardsButtonResource,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (hovered) HEIGHT else 0,
            textureHeight = HEIGHT * 2,
            scale = SCALE
        )
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F))
    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (x.toFloat()..(x.toFloat() + (WIDTH * SCALE))) && mouseY.toFloat() in ((y.toFloat())..(y.toFloat() + (CLICK_HEIGHT * SCALE)))
}