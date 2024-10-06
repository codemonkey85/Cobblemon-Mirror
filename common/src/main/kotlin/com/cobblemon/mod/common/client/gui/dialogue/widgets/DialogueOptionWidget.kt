/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.dialogue.widgets

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.ParentWidget
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.gui.drawCenteredText
import com.cobblemon.mod.common.client.gui.dialogue.DialogueScreen
import com.cobblemon.mod.common.net.messages.server.dialogue.InputToDialoguePacket
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

class DialogueOptionWidget(
    val dialogueScreen: DialogueScreen,
    val text: MutableComponent,
    val value: String,
    val selectable: Boolean,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val texture: ResourceLocation
) : ParentWidget(x, y, width, height, text) {
    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        blitk(
            texture = texture,
            matrixStack = context.pose(),
            x = x,
            y = y,
            width = width,
            height = height,
            vOffset = if (selectable) (if (isHovered) height else 0) else (height * 2),
            textureHeight = height * 3,
        )

        drawCenteredText(
            context = context,
            text = text,
            x = x + width / 2,
            y = y + height / 2 - 3,
            shadow = true,
            colour = if (selectable) 0xFFFFFF else 0x8D8D8D
        )

    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (!isHovered) return false

        if (!selectable || dialogueScreen.waitingForServerUpdate) return true

        dialogueScreen.sendToServer(InputToDialoguePacket(dialogueScreen.dialogueDTO.dialogueInput.inputId, value))
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 0.6F))
        return true
    }
}