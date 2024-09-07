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
import net.minecraft.core.Vec3i

class DialogueTimerWidget(
    val dialogueScreen: DialogueScreen,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) : Renderable, GuiEventListener {
    companion object {
        val timerResource = cobblemonResource("textures/gui/dialogue/dialogue_bar.png")
        private val BG_COLOUR = Vec3i(128, 128, 128)
    }

    override fun isFocused() = false
    override fun setFocused(focused: Boolean) {}

    var ratio = 1F

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (ratio < 0 || ratio > 1 || dialogueScreen.waitingForServerUpdate || !dialogueScreen.dialogueDTO.dialogueInput.showTimer) {
            return
        }

        blitk(
            texture = CobblemonResources.WHITE,
            matrixStack = context.pose(),
            x = x + 3,
            y = y + 2,
            width = width - 5,
            height = height - 2,
            blend = false,
            red = BG_COLOUR.x / 255F,
            green = BG_COLOUR.y / 255F,
            blue = BG_COLOUR.z / 255F
        )
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
            x = x.toFloat() + 3,
            y = y.toFloat() + 2,
            width = width * ratio - 4,
            height = height - 5,
            textureWidth = 1,
            textureHeight = 1,
            blend = false,
            red = 193/255F,
            green = 161/255F,
            blue = 32/255F
        )
    }
}