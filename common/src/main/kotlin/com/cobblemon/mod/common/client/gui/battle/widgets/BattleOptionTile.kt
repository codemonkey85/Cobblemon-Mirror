/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.battle.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.render.drawScaledText
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

class BattleOptionTile(
    val battleGUI: BattleGUI,
    val x: Int,
    val y: Int,
    val resource: ResourceLocation,
    val text: MutableComponent,
    val onClick: () -> Unit
) : Renderable, GuiEventListener, NarratableEntry {
    companion object {
        const val  OPTION_WIDTH = 90
        const val OPTION_HEIGHT = 26
    }

    private var focused = false

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val opacity = CobblemonClient.battleOverlay.opacityRatio
        if (opacity < 0.1) {
            return
        }
        blitk(
            matrixStack = context.pose(),
            texture = resource,
            x = x,
            y = y,
            alpha = opacity,
            width = OPTION_WIDTH,
            height = OPTION_HEIGHT,
            vOffset = if (isHovered(mouseX.toDouble(), mouseY.toDouble())) OPTION_HEIGHT else 0,
            textureHeight = OPTION_HEIGHT * 2
        )

        val scale = 1F
        drawScaledText(
            context = context,
            text = text,
            x = x + 6,
            y = y + 8,
            opacity = opacity,
            scale = scale,
            shadow = true
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (mouseX < x || mouseY < y || mouseX > x + OPTION_WIDTH || mouseY > y + OPTION_HEIGHT) {
            return false
        }
        onClick()
        return true
    }

    override fun setFocused(focused: Boolean) {
        this.focused = focused
    }

    override fun isFocused() = focused

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX > x && mouseY > y && mouseX < x + OPTION_WIDTH && mouseY < y + OPTION_HEIGHT

    override fun updateNarration(builder: NarrationElementOutput) {
        builder.add(NarratedElementType.TITLE, text)
    }

    override fun narrationPriority() = NarratableEntry.NarrationPriority.HOVERED


}