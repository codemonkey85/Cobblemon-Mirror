/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.battle.subscreen

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class BattleShiftButton(val x: Float, val y: Float) {
    companion object {
        const val WIDTH = 72
        const val HEIGHT = 34
        const val SCALE = 0.5F
        val baseTexture = cobblemonResource("textures/gui/battle/triple_battle_shift.png")

    }

    fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        blitk(
            matrixStack = context.pose(),
            texture = baseTexture,
            x = x * 2,
            y = y * 2,
            height = HEIGHT,
            width = WIDTH,
            vOffset = if (isHovered(mouseX.toDouble(), mouseY.toDouble())) HEIGHT else 0,
            textureHeight = HEIGHT * 2,
            scale = SCALE
        )

    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (x..(x + (WIDTH * SCALE))) && mouseY.toFloat() in (y..(y + (HEIGHT * SCALE)))
}