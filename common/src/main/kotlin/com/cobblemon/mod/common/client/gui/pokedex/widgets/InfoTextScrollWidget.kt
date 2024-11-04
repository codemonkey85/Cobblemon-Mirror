/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex.widgets

import com.cobblemon.mod.common.api.gui.MultiLineLabelK
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.ScrollingWidget
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.HALF_OVERLAY_WIDTH
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.POKEMON_DESCRIPTION_HEIGHT
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.POKEMON_DESCRIPTION_PADDING
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.SCALE
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.SCROLL_BAR_WIDTH
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.FastColor
import net.minecraft.util.Mth

abstract class InfoTextScrollWidget(val pX: Int, val pY: Int): ScrollingWidget<InfoTextScrollWidget.TextSlot>(
    left = pX,
    top = pY - POKEMON_DESCRIPTION_HEIGHT,
    width = HALF_OVERLAY_WIDTH,
    height = POKEMON_DESCRIPTION_HEIGHT
) {
    companion object {
        private val scrollBorder = cobblemonResource("textures/gui/pokedex/info_scroll_border.png")
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)

        blitk(
            matrixStack = context.pose(),
            texture = scrollBorder,
            x = (pX + 1) / SCALE,
            y = (pY + 40) / SCALE,
            width = 266,
            height = 4,
            textureHeight = 8,
            vOffset = 4,
            scale = SCALE
        )
    }
    override fun addEntry(entry: TextSlot): Int {
        return super.addEntry(entry)
    }

    fun setText(text: Collection<String>) {
        clearEntries()
        text.forEach {
            Minecraft.getInstance().font.splitter.splitLines(
                it.text(),
                ((width - SCROLL_BAR_WIDTH  - (POKEMON_DESCRIPTION_PADDING * 2)) / SCALE).toInt(),
                Style.EMPTY
            ).stream()
                .map { it.string }
                .forEach { addEntry(TextSlot(it)) }
        }
        scrollAmount = 0.0
    }

    override fun renderScrollbar(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val xLeft = this.scrollbarPosition
        val xRight = xLeft + 3
        val yStart = y + 1

        val barHeight = this.bottom - yStart

        var yBottom = ((barHeight * barHeight).toFloat() / this.maxPosition.toFloat()).toInt()
        yBottom = Mth.clamp(yBottom, 32, barHeight - 8)
        var yTop = scrollAmount.toInt() * (barHeight - yBottom) / this.maxScroll + yStart
        if (yTop < yStart) yTop = yStart

        context.fill(xLeft + 1, yStart, xRight - 1, this.bottom, FastColor.ARGB32.color(255, 126, 231, 229)) // track
        context.fill(xLeft, yTop, xRight, yTop + yBottom, FastColor.ARGB32.color(255, 58, 150, 182)) // bar
    }

    override fun getScrollbarPosition(): Int {
        return left + width - scrollBarWidth
    }

    override fun getBottom(): Int {
        return this.y + this.height - 1
    }

    class TextSlot(val text : String) : Slot<TextSlot>() {
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

            matrices.pushPose()
            MultiLineLabelK.create(
                component = text.text(),
                width = (139 - SCROLL_BAR_WIDTH - (POKEMON_DESCRIPTION_PADDING * 2)) / SCALE,
                maxLines = 30
            ).renderLeftAligned(
                context = context,
                x = x + POKEMON_DESCRIPTION_PADDING,
                y = y + 3,
                YStartOffset = 3,
                ySpacing = 0,
                colour = 0x606B6E,
                scale = SCALE,
                shadow = false
            )
            matrices.popPose()
        }

        override fun getNarration(): Component {
            return Component.literal(text)
        }
    }
}