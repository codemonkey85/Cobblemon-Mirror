/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex.widgets

import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.drop.ItemDropEntry
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.ScrollingWidget
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.drawScaledTextJustifiedRight
import com.cobblemon.mod.common.client.render.renderScaledGuiItemIcon
import com.cobblemon.mod.common.util.itemRegistry
import com.cobblemon.mod.common.util.lang
import java.text.DecimalFormat
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.FastColor
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack

class DropsScrollingWidget(val pX: Int, val pY: Int): ScrollingWidget<DropsScrollingWidget.DropWidgetEntry>(
    width = PokedexGUIConstants.HALF_OVERLAY_WIDTH - 2,
    height = 42,
    left = pX,
    top = pY + 10,
    slotHeight = 10
) {

    var dropTable: DropTable = DropTable()

    companion object {
        val df = DecimalFormat("#.##")
    }

    init {
        setEntries()
    }

    override fun getX() = pX
    override fun getY() = pY

    fun setEntries() {
        dropTable.entries.forEach {
            if (it is ItemDropEntry) addEntry(DropWidgetEntry(it))
        }
    }

    override fun getScrollbarPosition(): Int {
        return left + width - scrollBarWidth - 7
    }

    override fun getBottom(): Int {
        return this.y + this.height - 1
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

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.pokedex.info.drops").bold(),
            x = pX,
            y = pY - 10,
            shadow = true
        )

        if (dropTable.entries.size == 0) {
            drawScaledText(
                context = context,
                text = lang("ui.pokedex.info.drops_empty"),
                x = pX + (width / 2) - 7,
                y = pY + (height / 2) - 3,
                shadow = false,
                colour = 0x606B6E,
                scale = PokedexGUIConstants.SCALE,
                centered = true
            )
        } else {
            super.renderWidget(context, mouseX, mouseY, delta)
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
            context, index, y + 2, x, entryWidth, entryHeight, mouseX, mouseY,
            hovered == entry, delta
        )
    }

    override fun getEntry(index: Int): DropWidgetEntry {
        return children()[index] as DropWidgetEntry
    }

    class DropWidgetEntry(val entry: ItemDropEntry): Slot<DropWidgetEntry>() {
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
            context.pose().pushPose()
            context.pose().translate(0f, 0f, 100f)
            val itemStack = Minecraft.getInstance().player?.level()?.itemRegistry?.get(entry.item)?.defaultInstance ?: ItemStack.EMPTY
            renderScaledGuiItemIcon(
                itemStack = itemStack,
                x = x.toDouble(),
                y = y.toDouble(),
                matrixStack = context.pose(),
                scale = PokedexGUIConstants.SCALE.toDouble()
            )
            context.pose().pushPose()

            val min = entry.quantityRange?.min() ?: entry.quantity
            val max = entry.quantityRange?.max() ?: entry.quantity

//            var itemName: String = Component.translatable(itemStack.hoverName).string
//            itemName = if (itemName.length > 24) {
//                itemName.substring(0, 24 - 3) + "..."
//            } else {
//                itemName
//            }

            val displayText: MutableComponent = if (entry.quantityRange != null) {
                lang(
                    "ui.pokedex.info.drops_display",
                    itemStack.hoverName.copy().withStyle(ChatFormatting.BOLD),
                    lang("ui.pokedex.info.drops_range", min, max)
                )
            } else {
                lang(
                    "ui.pokedex.info.drops_display",
                    itemStack.hoverName.copy().withStyle(ChatFormatting.BOLD),
                    lang("ui.pokedex.info.drops_amount", entry.quantity)
                )
            }

            drawScaledText(
                context = context,
                text = displayText,
                x = x + 10,
                y = y + 2,
                colour = 0x606B6E,
                scale = PokedexGUIConstants.SCALE
            )

            drawScaledTextJustifiedRight(
                context = context,
                text = lang("ui.pokedex.info.drops_percentage", df.format(entry.percentage)),
                x = x + 120,
                y = y + 2,
                colour = 0x606B6E,
                scale = PokedexGUIConstants.SCALE
            )

            context.pose().popPose()
            context.pose().popPose()
        }

        override fun getNarration(): Component {
            return Component.translatable(entry.item.toLanguageKey())
        }

    }

}