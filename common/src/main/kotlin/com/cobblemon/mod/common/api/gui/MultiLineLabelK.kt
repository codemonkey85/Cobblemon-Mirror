/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import java.util.stream.Collectors
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation

class MultiLineLabelK(
    private val comps: List<TextWithWidth>,
    private val font: ResourceLocation? = null
) {

    companion object {
        private val mcFont = Minecraft.getInstance().font

        fun create(component: Component, width: Number, maxLines: Number) = create(component, width, maxLines, null)

        fun create(component: Component, width: Number, maxLines: Number, font: ResourceLocation?): MultiLineLabelK {
            return MultiLineLabelK(
                mcFont.splitter.splitLines(component, width.toInt(), Style.EMPTY).stream()
                    .limit(maxLines.toLong())
                    .map {
                        TextWithWidth(it, mcFont.width(it))
                    }.collect(Collectors.toList()),
                font = font
            )
        }
    }

    fun renderLeftAligned(
        context: GuiGraphics,
        x: Number, y: Number,
        ySpacing: Number,
        colour: Int,
        shadow: Boolean = true
    ) {
        comps.forEachIndexed { index, textWithWidth ->
            drawString(
                context = context,
                x = x, y = y.toFloat() + ySpacing.toFloat() * index,
                colour = colour,
                shadow = shadow,
                text = textWithWidth.text.string,
                font = font
            )
        }
    }

    class TextWithWidth internal constructor(val text: FormattedText, val width: Int)
}