package com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

class MoveDescriptionScrollList(
    private val listX: Int,
    private val listY: Int,
    slotHeight: Int
) : ObjectSelectionList<MoveDescriptionEntry>(
    Minecraft.getInstance(),
    60, // width
    28, // height
    0, // top
    slotHeight
) {
    companion object {
        const val WIDTH = 108
        const val HEIGHT = 111
    }

    init {
        this.y = this.listY
        this.x = this.listX
        this.setRenderHeader(false, 0)
    }

    override fun getScrollbarPosition(): Int {
        return x + width - 3
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, f: Float) {
        isHovered = pMouseX >= x && pMouseY >= y && pMouseX < x + width && pMouseY < y + height
        context.enableScissor(
            x,
            y - 1,
            x + width,
            y + height
        )
        super.renderWidget(context, pMouseX, pMouseY, f)
        context.disableScissor()
    }

    override fun renderListBackground(guiGraphics: GuiGraphics) {
    }

    fun setMoveDescription(moveDescription: MutableComponent) {
        clearEntries()
        val splitWidth = 100
        val text = moveDescription.string
        val words = text.split(" ")
        val splitText = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (Minecraft.getInstance().font.width(currentLine.toString() + word) > splitWidth) {
                splitText.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }
                currentLine.append(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            splitText.add(currentLine.toString())
        }

        for (part in splitText) {
            addEntry(MoveDescriptionEntry(Component.literal(part)))
        }
    }
}