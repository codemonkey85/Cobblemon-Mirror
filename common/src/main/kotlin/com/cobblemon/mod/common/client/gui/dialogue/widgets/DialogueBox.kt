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
import com.cobblemon.mod.common.client.gui.dialogue.DialogueScreen
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.net.messages.client.dialogue.dto.DialogueInputDTO
import com.cobblemon.mod.common.net.messages.server.dialogue.InputToDialoguePacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.locale.Language
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.FormattedCharSequence

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
    messages: List<MutableComponent>
): ObjectSelectionList<DialogueBox.DialogueLine>(
    Minecraft.getInstance(),
    frameWidth - 14,
    height, // height
    1, // top
    LINE_HEIGHT
) {
    val dialogue = dialogueScreen.dialogueDTO
    var opacity = 1F
    private var scrolling = false

    val appropriateX: Int
        get() = x
    val appropriateY: Int
        get() = y

    init {
        correctSize()

        val textRenderer = Minecraft.getInstance().font

        messages
            .flatMap { Language.getInstance().getVisualOrder(textRenderer.splitter.splitLines(it, LINE_WIDTH, it.style)) }
            .forEach { addEntry(DialogueLine(it)) }
    }

    override fun renderListBackground(context: GuiGraphics) {}

    override fun renderSelection(context: GuiGraphics, y: Int, entryWidth: Int, entryHeight: Int, borderColor: Int, fillColor: Int) {}

    override fun renderHeader(context: GuiGraphics, x: Int, y: Int) {
//        super.renderHeader(context, x, y)
    }

    override fun renderListSeparators(context: GuiGraphics) {}

    override fun renderDecorations(context: GuiGraphics, mouseX: Int, mouseY: Int) {
//        super.renderDecorations(context, mouseX, mouseY)
    }

    private fun correctSize() {
        val textBoxHeight = height
        setSize(width, textBoxHeight)
        x = listX + 8
        y = listY + 6
//        setDimensionsAndPosition(width, textBoxHeight, appropriateY + 6, appropriateY + 6 + textBoxHeight)
//        setX(appropriateX + 8)
    }

    companion object {
        const val LINE_HEIGHT = 10
        const val LINE_WIDTH = 142
        private val boxResource = cobblemonResource("textures/gui/dialogue/dialogue_box.png")
    }

    override fun addEntry(entry: DialogueLine): Int {
        return super.addEntry(entry)
    }

    override fun getRowWidth(): Int {
        return 80
    }

    override fun getScrollbarPosition(): Int {
        return this.x + 144
    }

    private fun scaleIt(i: Number): Int {
        return (minecraft.window.guiScale * i.toFloat()).toInt()
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        correctSize()
        blitk(
            matrixStack = context.pose(),
            texture = boxResource,
            x = x - 8,
            y = y - 7,
            height = height + 12,
            width = frameWidth,
            alpha = opacity
        )


        super.renderWidget(context, mouseX, mouseY, partialTicks)
//        context.disableScissor()
    }

    override fun enableScissor(context: GuiGraphics) {
        val textBoxHeight = height
        context.enableScissor(
            this.x,
            appropriateY,
            this.x + width - 10,
            appropriateY + textBoxHeight
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val toggleOffsetY = 92

        // TODO change this coordinate check to just be "anywhere the scroll bar isn't"
        if (!dialogueScreen.waitingForServerUpdate &&
            mouseX > (this.x) &&
            mouseX < (this.x + 160) &&
            mouseY > (appropriateY) &&
            mouseY < (appropriateY + height)
        ) {
            if (dialogue.dialogueInput.allowSkip && dialogue.dialogueInput.inputType in listOf(DialogueInputDTO.InputType.NONE, DialogueInputDTO.InputType.AUTO_CONTINUE)) {
                dialogueScreen.sendToServer(InputToDialoguePacket(dialogue.dialogueInput.inputId, "skip!"))
                return true
            }
        }

        updateScrollingState(mouseX, mouseY)
        if (scrolling) {
            focused = getEntryAtPosition(mouseX, mouseY)
            isDragging = true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (scrolling) {
            if (mouseY < this.y) {
                scrollAmount = 0.0
            } else if (mouseY > bottom) {
                scrollAmount = maxScroll.toDouble()
            } else {
                scrollAmount += deltaY
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    private fun updateScrollingState(mouseX: Double, mouseY: Double) {
        scrolling = mouseX >= this.scrollbarPosition.toDouble()
                && mouseX < (this.scrollbarPosition + 3).toDouble()
                && mouseY >= this.y
                && mouseY < bottom
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
                rowLeft - 38,
                rowTop - 2
            )
        }
    }
}