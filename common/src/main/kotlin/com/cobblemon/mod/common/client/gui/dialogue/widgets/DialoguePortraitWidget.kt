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
import kotlin.math.ceil

class DialoguePortraitWidget(
    val dialogueScreen: DialogueScreen,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) : Renderable, GuiEventListener {
    companion object {
        val DIALOGUE_ARROW_HEIGHT = 11
        val DIALOGUE_ARROW_WIDTH = 6
        val frameLeftResource = cobblemonResource("textures/gui/dialogue/dialogue_portrait_left.png")
        val frameRightResource = cobblemonResource("textures/gui/dialogue/dialogue_portrait_right.png")
        val frameBackground = cobblemonResource("textures/gui/dialogue/dialogue_portrait_background.png")
    }
    override fun setFocused(focused: Boolean) {}
    override fun isFocused() = false
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val face = dialogueScreen.dialogueDTO.currentPageDTO.speaker?.let { dialogueScreen.speakers[it] }?.face ?: return
        val startX = if (face.isLeftSide) (x + 1) else (x + DialogueScreen.BOX_WIDTH + DialogueScreen.PORTRAIT_WIDTH - 1)
        blitk(
            texture = frameBackground,
            matrixStack = context.pose(),
            x = startX,
            y = y,
            width = width,
            height = height
        )

        context.enableScissor(
            startX + 3,
            y + 3,
            startX + 3 + width - 6,
            y + 3 + height - 6
        )

        context.pose().pushPose()
        context.pose().translate(startX.toDouble() + width / 2, y.toDouble(), 0.0)
        face.render(context, delta)
        context.disableScissor()
        context.pose().popPose()

        context.pose().pushPose()
        context.pose().translate(0F, 0F, 100F)
        blitk(
            texture = if (face.isLeftSide) frameLeftResource else frameRightResource,
            matrixStack = context.pose(),
            x = startX,
            y = y,
            width = width,
            height = height
        )
        context.pose().popPose()

        blitk(
            texture = DialogueBox.boxResource,
            matrixStack = context.pose(),
            x = startX + if (face.isLeftSide) (width - DIALOGUE_ARROW_WIDTH) else 0,
            y = y + height - ceil(DIALOGUE_ARROW_HEIGHT / 2.0),
            width = DIALOGUE_ARROW_WIDTH,
            height = DIALOGUE_ARROW_HEIGHT,
            uOffset = DialogueScreen.BOX_WIDTH,
            vOffset = if (face.isLeftSide) DIALOGUE_ARROW_HEIGHT else 0,
            textureHeight = DialogueScreen.BOX_HEIGHT,
            textureWidth = DialogueScreen.BOX_WIDTH + DIALOGUE_ARROW_WIDTH + DialogueBox.SCROLL_BAR_WIDTH + DialogueBox.SCROLL_TRACK_WIDTH
        )
    }
}