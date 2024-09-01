/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.info

import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component

class InfoOneLineWidget(
    pX: Int,
    pY: Int,
    width: Int,
    height: Int = ROW_HEIGHT,
    private val label: MutableComponent,
    private val value: MutableComponent,
) : SoundlessWidget(pX, pY, width, height, Component.literal("InfoOneLineWidget")) {
    companion object {
        private val FONT = CobblemonResources.DEFAULT_LARGE
        private const val ROW_HEIGHT = 15
        private const val WITHIN_ROW_VERTICAL_OFFSET = 6
        private const val LABEL_HORIZONTAL_OFFSET = 8
        private const val VALUE_HORIZONTAL_OFFSET = 53
    }


    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {

        // Label
        val label = InfoBlockWidget(
            pX = x + LABEL_HORIZONTAL_OFFSET,
            pY = y,
            blockWidth = Minecraft.getInstance().font.width(label),
            blockHeight = height,
            text = label,
            font = FONT,
            withinRowVerticalTextOffset = WITHIN_ROW_VERTICAL_OFFSET
        )
        label.render(context, pMouseX, pMouseY, pPartialTicks)

        // Value
        val value = InfoBlockWidget(
            pX = x + VALUE_HORIZONTAL_OFFSET,
            pY = y,
            blockWidth = Minecraft.getInstance().font.width(value),
            blockHeight = height,
            text = value,
            font = FONT,
            withinRowVerticalTextOffset = WITHIN_ROW_VERTICAL_OFFSET
        )
        value.render(context, pMouseX, pMouseY, pPartialTicks)
    }

}