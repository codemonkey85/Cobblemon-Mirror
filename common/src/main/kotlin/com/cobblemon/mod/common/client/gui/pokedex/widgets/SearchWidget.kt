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
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.HALF_OVERLAY_HEIGHT
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.HALF_OVERLAY_WIDTH
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.SCALE
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component

class SearchWidget(
    val posX: Number,
    val posY: Number,
    width: Int,
    height: Int,
    text: Component = "Search".text(),
    val update: () -> (Unit)
): EditBox(Minecraft.getInstance().font, posX.toInt(), posY.toInt(), width, height, text) {

    companion object {
        private val backgroundOverlay = cobblemonResource("textures/gui/pokedex/pokedex_screen_search_overlay.png")
        private val searchIcon = cobblemonResource("textures/gui/pokedex/search_icon.png")
    }

    init {
        this.setMaxLength(24)
        this.setResponder {
            update.invoke()
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (mouseX.toInt() in x..(x + width) && mouseY.toInt() in y..(y + height)) {
            isFocused = true
            true
        } else {
            false
        }
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val matrices = context.pose()

        blitk(
            matrixStack = matrices,
            texture = backgroundOverlay,
            x = posX, y = posY,
            width = HALF_OVERLAY_WIDTH,
            height = HALF_OVERLAY_HEIGHT
        )

        blitk(
            matrixStack = matrices,
            texture = searchIcon,
            x = (posX.toInt() + 3) / SCALE,
            y = (posY.toInt() + 2) / SCALE,
            width = 14,
            height = 14,
            scale = SCALE
        )

//        if(text.isEmpty() && !isFocused) {
//            drawScaledText(
//                context = context,
//                font = CobblemonResources.DEFAULT_LARGE,
//                text = Component.translatable("cobblemon.ui.pokedex.search").bold(),
//                x = posX.toInt() + 13,
//                y = posY.toInt() + 1,
//                shadow = true
//            )
//        }
        if (cursorPosition != value.length) moveCursorToEnd(false)

        val input = if (isFocused) "${value}_".text()
            else (if(value.isEmpty()) Component.translatable("cobblemon.ui.pokedex.search") else value.text())

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = input.bold(),
            // text = Component.translatable(if (isFocused) "${text}_" else text).bold(),
            x = posX.toInt() + 13,
            y = posY.toInt() + 1,
            shadow = true
        )
    }
}