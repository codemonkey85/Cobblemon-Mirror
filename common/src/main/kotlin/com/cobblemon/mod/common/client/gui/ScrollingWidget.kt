/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.mixin.accessor.EntryListWidgetAccessor
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.util.Mth

abstract class ScrollingWidget<T : ObjectSelectionList.Entry<T>>(
    top: Int = 0,
    val left: Int = 0,
    width: Int = 10,
    height: Int = 10,
    slotHeight: Int = 10,
    val scrollBarWidth: Int = 5
) : ObjectSelectionList<T>(
    Minecraft.getInstance(),
    width, // Width
    height, // Height
    top, // Top
    slotHeight // Slot Height
) {
    override fun renderListBackground(guiGraphics: GuiGraphics) {}
    override fun renderSelection(context: GuiGraphics, y: Int, entryWidth: Int, entryHeight: Int, borderColor: Int, fillColor: Int) {}
    override fun renderDecorations(context: GuiGraphics, mouseX: Int, mouseY: Int) {}


    init {
        setRectangle(width, height, top, top + height)
        setLeft(left)
    }

    final override fun setRectangle(width: Int, height: Int, top: Int, bottom: Int) {
        this.width = width
        this.height = height
        this.y = bottom
        this.x = left + width
    }

    fun setLeft(left: Int){
        this.x = left
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val asAccessor = this as EntryListWidgetAccessor
        this.focused = if (this.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) this.getEntryAtPosition(
            mouseX.toDouble(),
            mouseY.toDouble()
        ) else null
        val renderBackground = false
        if (renderBackground) {
            context.setColor(0.125f, 0.125f, 0.125f, 1.0f)
            context.blit(
                Screen.MENU_BACKGROUND,
                this.left,
                this.y,
                right.toFloat(),
                (this.bottom + scrollAmount.toInt()).toFloat(),
                this.right - this.left,
                this.bottom - this.y, 32, 32
            )
            context.setColor(1.0f, 1.0f, 1.0f, 1.0f)
        }

        val l1 = this.rowLeft
        val l = this.y + 4 - scrollAmount.toInt()

        this.enableScissor(context)
        if (asAccessor.renderHeader) {
            this.renderHeader(context, l1, l)
        }

        this.renderListItems(context, mouseX, mouseY, delta)
        context.disableScissor()

        val renderHorizontalShadows = false
        if (renderHorizontalShadows) {
            this.renderHorizontalShadows(context, mouseX, mouseY, delta)
        }

        val maxScroll = this.maxScroll
        if (maxScroll > 0) {
            renderScrollbar(context, mouseX, mouseY, delta)
        }

        this.renderDecorations(context, mouseX, mouseY)
        RenderSystem.disableBlend()
    }

    open fun renderScrollbar(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float){
        val xLeft = this.scrollbarPosition
        val xRight = xLeft + scrollBarWidth

        val barHeight = this.bottom - this.y

        var j2 = ((barHeight * barHeight).toFloat() / this.maxPosition.toFloat()).toInt()
        j2 = Mth.clamp(j2, 32, barHeight - 8)
        var k1 = scrollAmount.toInt() * (barHeight - j2) / this.maxScroll + this.y
        if (k1 < this.y) {
            k1 = this.y
        }

        context.fill(xLeft, this.y, xRight, this.bottom, -16777216)
        context.fill(xLeft, k1, xRight, k1 + j2, -8355712)
        context.fill(xLeft ,k1, xRight - 1, k1 + j2 - 1, -4144960)
    }

    open fun renderHorizontalShadows(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float){
        context.setColor(0.25f, 0.25f, 0.25f, 1.0f)
        context.blit(
            Screen.MENU_BACKGROUND,
            this.left, 0, 0.0f, 0.0f,
            this.width,
            this.y, 32, 32
        )
        context.blit(
            Screen.MENU_BACKGROUND,
            this.left,
            this.bottom, 0.0f,
            bottom.toFloat(),
            this.width,
            this.height - this.bottom, 32, 32
        )
        context.setColor(1.0f, 1.0f, 1.0f, 1.0f)
        context.fillGradient(
            RenderType.guiOverlay(),
            this.left,
            this.y,
            this.right,
            this.y + 4, -16777216, 0, 0
        )
        context.fillGradient(
            RenderType.guiOverlay(),
            this.left,
            this.bottom - 4,
            this.right,
            this.bottom, 0, -16777216, 0
        )
    }

    override fun renderListItems(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val i = this.rowLeft
        val j = this.rowWidth
        val k = this.itemHeight
        val l = this.itemCount

        for (m in 0 until l) {
            val n = this.getRowTop(m)
            val o = this.getRowBottom(m)
            if (o >= this.y && n <= this.bottom) {
                this.renderItem(context!!, mouseX, mouseY, delta, m, i, n, j, k)
            }
        }
    }

    override fun renderItem(
        context: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
        index: Int,
        x: Int,
        y: Int,
        entryWidth: Int,
        entryHeight: Int
    ) {
        val entry =  this.getEntry(index)
        entry.render(
            context, index, y, x, entryWidth, entryHeight, mouseX, mouseY,
            focused == entry, delta
        )
    }

    override fun getRowLeft(): Int {
        return this.left
    }

    override fun getRowRight(): Int {
        return this.rowLeft + this.rowWidth
    }

    override fun getRowWidth(): Int {
        return this.width
    }

    override fun getRowTop(index: Int): Int {
        return this.y - scrollAmount.toInt() + (index * this.itemHeight)
    }

    override fun getRowBottom(index: Int): Int {
        return this.getRowTop(index) + this.itemHeight
    }

    override fun getScrollbarPosition(): Int = this.left + this.width - this.scrollBarWidth

    abstract class Slot<T : Slot<T>>(): Entry<T>() {
        // Override render to show each individual element
    }
}