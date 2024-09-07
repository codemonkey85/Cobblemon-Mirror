/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.dialogue.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.gui.drawCenteredText
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.gui.dialogue.DialogueScreen
import com.cobblemon.mod.common.net.messages.client.dialogue.dto.DialogueInputDTO
import com.cobblemon.mod.common.net.messages.server.dialogue.InputToDialoguePacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import org.lwjgl.glfw.GLFW

class DialogueTextInputWidget(
    val dialogueScreen: DialogueScreen,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    maxLength: Int = 100
) : EditBox(
    Minecraft.getInstance().font,
    x,
    y,
    width,
    height,
    "gui_dialogue_text_input".text()
) {
    init {
        setMaxLength(maxLength)
        isFocused = true
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (dialogueScreen.dialogueDTO.dialogueInput.inputType != DialogueInputDTO.InputType.TEXT || dialogueScreen.waitingForServerUpdate) {
            return false
        }

        return if (mouseX.toInt() in x..(x + width) && mouseY.toInt() in y..(y + height)) {
            isFocused = true
            true
        } else {
            false
        }
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (dialogueScreen.dialogueDTO.dialogueInput.inputType != DialogueInputDTO.InputType.TEXT || dialogueScreen.waitingForServerUpdate) {
            return
        }
        if (cursorPosition != value.length) {
            moveCursorToEnd(Screen.hasShiftDown())
        }

        blitk(
            matrixStack = context.pose(),
            x = x,
            y = y,
            width = width,
            height = height,
            texture = cobblemonResource("textures/gui/dialogue/dialogue_text_input.png")
        )

        drawCenteredText(
            context = context,
            text = (if (isFocused) "$value|" else value).text(),
            x = x + width / 2F,
            y = y + height / 2 - 4,
            shadow = true,
            colour = 0xFFFFFF
        )
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == InputConstants.KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.dialogueScreen.sendToServer(InputToDialoguePacket(dialogueScreen.dialogueDTO.dialogueInput.inputId, value.trim()))
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
}