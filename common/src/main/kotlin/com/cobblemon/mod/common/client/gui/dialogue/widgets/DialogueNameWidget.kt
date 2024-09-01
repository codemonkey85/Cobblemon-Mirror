/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.dialogue.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.network.chat.MutableComponent

class DialogueNameWidget(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val text: MutableComponent?
) : Renderable, GuiEventListener {
    companion object {
        val nameResource = cobblemonResource("textures/gui/dialogue/dialogue_name.png")
    }

    override fun isFocused() = false
    override fun setFocused(focused: Boolean) {}

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (text == null || text.string.isEmpty()) {
            return
        }

        blitk(
            texture = nameResource,
            matrixStack = context.pose(),
            x = x,
            y = y,
            width = width,
            height = height,
            uOffset = 0,
            vOffset = 0,
            scale = 1F,
            alpha = 1F,
            blend = false
        )

        drawScaledText(
            context = context,
            text = text.visualOrderText,
            x = x + 6,
            y = y + 4,
            shadow = false
        )
    }
}