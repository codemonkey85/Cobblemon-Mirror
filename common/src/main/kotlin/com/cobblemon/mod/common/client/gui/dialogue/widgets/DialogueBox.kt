/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.dialogue.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.gui.ScrollingWidget
import com.cobblemon.mod.common.client.gui.dialogue.DialogueScreen
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.net.messages.client.dialogue.dto.DialogueInputDTO
import com.cobblemon.mod.common.net.messages.server.dialogue.InputToDialoguePacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.locale.Language
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.Mth

/**
 * UI element for showing the lines of dialogue text.
 *
 * @author Hiroku
 * @since December 29th, 2023
 */
class DialogueBox(
    val dialogueScreen: DialogueScreen,
    val listX: Int = 0,
    val listY: Int = 0,
    val frameWidth: Int,
    height: Int,
    messages: MutableList<MutableComponent>
): ScrollingWidget<DialogueBox.DialogueLine>(
    left = listX,
    top = listY,
    width = frameWidth,
    height = height,
    slotHeight = LINE_HEIGHT,
    scrollBarWidth = SCROLL_BAR_WIDTH
) {
    companion object {
        const val SCROLL_TRACK_WIDTH = 2
        const val SCROLL_BAR_WIDTH = 4
        const val LINE_HEIGHT = 12
        const val LINE_WIDTH = 168
        val boxResource = cobblemonResource("textures/gui/dialogue/dialogue_box.png")
    }

    val dialogue = dialogueScreen.dialogueDTO

    init {
        correctSize()

        val textRenderer = Minecraft.getInstance().font

        messages.flatMap { Language.getInstance().getVisualOrder(textRenderer.splitter.splitLines(it, LINE_WIDTH, it.style)) }
            .forEach { addEntry(DialogueLine(it)) }

        // Add empty line for bottom padding if text area height is larger than box height
        if (maxPosition > (height - 2)) addEntry(DialogueLine(FormattedCharSequence.EMPTY))
    }

    override fun renderScrollbar(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val xLeft = this.scrollbarPosition
        val yStart = y + 2

        val barHeight = this.bottom - yStart

        var yBottom = ((barHeight * barHeight).toFloat() / this.maxPosition.toFloat()).toInt()
        yBottom = Mth.clamp(yBottom, 32, barHeight - 8)
        var yTop = scrollAmount.toInt() * (barHeight - yBottom) / this.maxScroll + yStart
        if (yTop < yStart) yTop = yStart

        // Scroll Track
        blitk(
            texture = boxResource,
            matrixStack = context.pose(),
            x = xLeft + 1,
            y = yStart,
            width = SCROLL_TRACK_WIDTH,
            height = height - 4,
            uOffset = DialogueScreen.BOX_WIDTH + DialoguePortraitWidget.DIALOGUE_ARROW_WIDTH,
            textureHeight = DialogueScreen.BOX_HEIGHT,
            textureWidth = frameWidth + DialoguePortraitWidget.DIALOGUE_ARROW_WIDTH + SCROLL_BAR_WIDTH + SCROLL_TRACK_WIDTH
        )

        // Scroll Bar
        blitk(
            texture = boxResource,
            matrixStack = context.pose(),
            x = xLeft,
            y = yTop,
            width = SCROLL_BAR_WIDTH,
            height = yBottom,
            uOffset = DialogueScreen.BOX_WIDTH + DialoguePortraitWidget.DIALOGUE_ARROW_WIDTH + SCROLL_TRACK_WIDTH,
            textureHeight = DialogueScreen.BOX_HEIGHT,
            textureWidth = frameWidth + DialoguePortraitWidget.DIALOGUE_ARROW_WIDTH + SCROLL_BAR_WIDTH + SCROLL_TRACK_WIDTH
        )
    }

    private fun correctSize() {
        setSize(width, height)
        x = listX
        y = listY
    }

    override fun addEntry(entry: DialogueLine): Int {
        return super.addEntry(entry)
    }

    override fun getRowWidth(): Int {
        return LINE_WIDTH
    }

    override fun getScrollbarPosition(): Int {
        return this.x + 186
    }

    override fun getBottom(): Int {
        return this.y + this.height - 2
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        correctSize()
        blitk(
            matrixStack = context.pose(),
            texture = boxResource,
            x = x,
            y = y,
            height = height,
            width = frameWidth,
            textureWidth = frameWidth + DialoguePortraitWidget.DIALOGUE_ARROW_WIDTH + SCROLL_BAR_WIDTH + SCROLL_TRACK_WIDTH
        )

        super.renderWidget(context, mouseX, mouseY, partialTicks)
    }

    override fun enableScissor(context: GuiGraphics) {
        val textBoxHeight = height
        context.enableScissor(
            this.x,
            this.y + 1,
            this.x + width - 1,
            this.y + textBoxHeight - 1
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        // TODO change this coordinate check to just be "anywhere the scroll bar isn't"
        if (!dialogueScreen.waitingForServerUpdate &&
            mouseX > this.x && mouseX < this.scrollbarPosition &&
            mouseY > this.y && mouseY < this.bottom
        ) {
            if (dialogue.dialogueInput.allowSkip && dialogue.dialogueInput.inputType in listOf(DialogueInputDTO.InputType.NONE, DialogueInputDTO.InputType.AUTO_CONTINUE)) {
                dialogueScreen.sendToServer(InputToDialoguePacket(dialogue.dialogueInput.inputId, "skip!"))
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    class DialogueLine(val line: FormattedCharSequence) : Entry<DialogueLine>() {
        override fun getNarration() = "".text()

        override fun renderBack(
            context: GuiGraphics,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {}

        override fun render(
            context: GuiGraphics,
            index: Int,
            rowTop: Int,
            rowLeft: Int,
            rowWidth: Int,
            rowHeight: Int,
            mouseX: Int,
            mouseY: Int,
            isHovered: Boolean,
            partialTicks: Float
        ) {
            drawScaledText(
                context,
                line,
                rowLeft + 14,
                rowTop + 7,
                colour = 0x4C4C4C
            )
        }
    }
}