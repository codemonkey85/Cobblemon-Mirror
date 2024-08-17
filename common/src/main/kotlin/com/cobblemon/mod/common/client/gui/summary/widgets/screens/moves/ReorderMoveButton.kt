/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component

class ReorderMoveButton(
    val pX: Int, val pY: Int,
    private val isUp: Boolean,
    onPress: OnPress
): Button((pX - OFFSET_X).toInt(), (pY + (if (isUp) OFFSET_Y_UP else OFFSET_Y_DOWN)), (WIDTH * SCALE).toInt(), (HEIGHT * SCALE).toInt(), Component.empty(), onPress, DEFAULT_NARRATION) {

    companion object {
        private const val WIDTH = 8
        private const val HEIGHT = 6
        private const val OFFSET_X = 11.5F
        private const val OFFSET_Y_UP = 6
        private const val OFFSET_Y_DOWN = 13
        private const val SCALE = 0.5F
        private val moveReorderUpResource = cobblemonResource("textures/gui/summary/summary_move_reorder_up.png")
        private val moveReorderDownResource = cobblemonResource("textures/gui/summary/summary_move_reorder_down.png")
        private var blocked = false
    }

    override fun mouseDragged(d: Double, e: Double, i: Int, f: Double, g: Double): Boolean {
        return false
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val offsetY = if (isUp) OFFSET_Y_UP else OFFSET_Y_DOWN

        blitk(
            matrixStack = context.pose(),
            x = (pX - OFFSET_X) / SCALE,
            y = (pY + offsetY) / SCALE,
            texture = if (isUp) moveReorderUpResource else moveReorderDownResource,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (isHovered(pMouseX.toDouble(), pMouseY.toDouble(), offsetY.toFloat())) HEIGHT else 0,
            textureHeight = HEIGHT * 2,
            scale = SCALE
        )
    }

    override fun onRelease(pMouseX: Double, pMouseY: Double) {
        blocked = false
    }

    override fun onClick(pMouseX: Double, pMouseY: Double) {
        if (!blocked) {
            blocked = true
            onPress.onPress(this)
        }
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
    }

    fun isHovered(mouseX: Double, mouseY: Double, offsetY: Float) = mouseX.toFloat() in ((pX - OFFSET_X)..((pX - OFFSET_X) + (WIDTH * SCALE))) && mouseY.toFloat() in ((pY + offsetY)..((pY + offsetY) + (HEIGHT * SCALE) - 0.5F))
}