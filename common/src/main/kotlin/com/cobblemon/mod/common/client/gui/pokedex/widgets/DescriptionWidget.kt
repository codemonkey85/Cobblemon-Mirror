/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

class DescriptionWidget(descX: Int, descY: Int): InfoTextScrollWidget(pX = descX, pY = descY) {
    companion object {
        private val unknownIcon = cobblemonResource("textures/gui/pokedex/pokedex_slot_unknown.png")
    }

    var showPlaceholder: Boolean = true

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (showPlaceholder) {
            blitk(
                matrixStack = context.pose(),
                texture = unknownIcon,
                x = pX + 65.5,
                y = pY + 16,
                width = 8,
                height = 10
            )
        } else {
            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = Component.translatable("cobblemon.ui.pokedex.info.entry").bold(),
                x = pX + 9,
                y = pY - 10,
                shadow = true
            )

            super.renderWidget(context, mouseX, mouseY, delta)
        }
    }
}