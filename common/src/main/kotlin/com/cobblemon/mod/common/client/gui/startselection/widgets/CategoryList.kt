/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.startselection.widgets

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.startselection.StarterSelectionScreen
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.config.starter.RenderableStarterCategory
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component

class CategoryList(
    private val paneWidth: Int,
    private val paneHeight: Int,
    topOffset: Int,
    private val entryWidth: Int,
    entryHeight: Int,
    private val categories: List<RenderableStarterCategory>,
    val listX: Int,
    val listY: Int,
    private val minecraft: Minecraft = Minecraft.getInstance(),
    private val starterSelectionScreen: StarterSelectionScreen
) : ObjectSelectionList<CategoryList.Category>(
    minecraft,
    paneWidth,
    paneHeight,
    topOffset,
    entryHeight
) {

    companion object {
        private const val CATEGORY_BUTTON_WIDTH = 51.5f
        private const val CATEGORY_BUTTON_HEIGHT = 16f
        private const val ENTRY_X_OFFSET = 10f
        private val categoryResource = cobblemonResource("textures/gui/starterselection/starterselection_slot.png")
    }

    init {
        this.x = listX
        this.y = listY
        this.correctSize()
        createEntries().forEach { addEntry(it) }
        //this.setRenderBackground(false)
    }

    private fun createEntries() = categories.map {
        Category(it)
    }

    override fun renderListBackground(context: GuiGraphics) {}

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        correctSize()
    }

    private fun correctSize() {
        this.setRectangle(this.paneWidth, this.paneHeight, this.listX, this.listY)
    }

    private fun scale(n: Int): Int = (this.minecraft.window.guiScale * n).toInt()
    override fun getRowWidth() = this.entryWidth
    override fun getScrollbarPosition(): Int {
        return this.listX + this.width - 5
    }


    inner class Category(private val category: RenderableStarterCategory) : Entry<Category>() {

        override fun render(
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
        ) {
            val matrices = context.pose()
            val isHovered = mouseX >= x && mouseY >= y && mouseX < x + entryWidth && mouseY < y + (entryHeight - 1)
            if (isHovered) {
                blitk(
                    matrixStack = matrices,
                    x = x + 2.5f, y = y,
                    texture = categoryResource,
                    width = CATEGORY_BUTTON_WIDTH, height = CATEGORY_BUTTON_HEIGHT,
                    red = 0.75f, green = 0.75f, blue = 0.75f
                )
            } else
                blitk(
                    matrixStack = matrices,
                    x = x + 2.5f, y = y,
                    texture = categoryResource,
                    width = CATEGORY_BUTTON_WIDTH, height = CATEGORY_BUTTON_HEIGHT
                )
            drawScaledText(
                context = context,
                text = category.displayNameText,
                x = x + 28,
                y = y + 4.5F,
                scale = 1F,
                maxCharacterWidth = 50,
                shadow = true,
                centered = true
            )
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            starterSelectionScreen.changeCategory(category = category)
            minecraft.soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
            return true
        }

        override fun getNarration(): Component {
            return Component.literal("Yes")
        }
    }
}