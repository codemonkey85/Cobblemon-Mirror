/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.dialogue.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.dialogue.DialogueScreen
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener

class DialogueTimerWidget(
    val dialogueScreen: DialogueScreen,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) : Renderable, GuiEventListener {
    companion object {
        val timerResource = cobblemonResource("textures/gui/dialogue/dialogue_timer_bar.png")
    }

    override fun isFocused() = false
    override fun setFocused(focused: Boolean) {}

    var ratio = 1F

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (ratio < 0 || ratio > 1 || dialogueScreen.waitingForServerUpdate || !dialogueScreen.dialogueDTO.dialogueInput.showTimer) {
            return
        }

        context.setColor(1F, 1F, 1F, 1F)
        blitk(
            texture = timerResource,
            matrixStack = context.pose(),
            x = x,
            y = y,
            width = width,
            height = height,
            blend = false
        )
        blitk(
            texture = CobblemonResources.WHITE,
            matrixStack = context.pose(),
            x = x.toFloat() + 4,
            y = y.toFloat() + 1,
            width = width * ratio - 8,
            height = height - 2,
            textureWidth = 1,
            textureHeight = 1,
            blend = false,
            red = 255F/255F,
            green = 208F/255F,
            blue = 64F/255F
        )
    }
}