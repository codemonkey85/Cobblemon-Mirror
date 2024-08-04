/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.dialogue.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.dialogue.DialogueScreen
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener

class DialogueFaceWidget(
    val dialogueScreen: DialogueScreen,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) : Renderable, GuiEventListener {
    companion object {
        val frameResource = cobblemonResource("textures/gui/dialogue/dialogue_face.png")
        val frameBackground = cobblemonResource("textures/gui/dialogue/dialogue_face_background.png")
    }
    override fun setFocused(focused: Boolean) {}
    override fun isFocused() = false
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val face = dialogueScreen.dialogueDTO.currentPageDTO.speaker?.let { dialogueScreen.speakers[it] }?.face ?: return

        blitk(
            texture = frameBackground,
            matrixStack = context.pose(),
            x = x,
            y = y,
            width = width,
            height = height
        )

        context.enableScissor(
            x + 1,
            y + 2,
            x + 1 + width - 4,
            y + 2 + height - 4
        )
        context.pose().pushPose()
        context.pose().translate(x.toDouble() + width / 2, y.toDouble(), 0.0)
        face.render(context, delta)
        context.disableScissor()
        context.pose().popPose()

        context.pose().pushPose()
        context.pose().translate(0F, 0F, 100F)
        blitk(
            texture = frameResource,
            matrixStack = context.pose(),
            x = x,
            y = y,
            width = width,
            height = height
        )
        context.pose().popPose()
    }
}