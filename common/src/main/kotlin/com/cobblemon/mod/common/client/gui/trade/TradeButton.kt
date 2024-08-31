/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.trade

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component

class TradeButton(
    x: Int, y: Int,
    val parent: TradeGUI,
    onPress: OnPress
) : Button(x, y, WIDTH, HEIGHT, Component.literal("Trade"), onPress, DEFAULT_NARRATION) {

    companion object {
        private const val WIDTH = 53
        private const val HEIGHT = 14

        private val buttonResource = cobblemonResource("textures/gui/trade/trade_button.png")
        private val buttonDisabledResource = cobblemonResource("textures/gui/trade/trade_button_disabled.png")
        private val buttonActiveResource = cobblemonResource("textures/gui/trade/trade_button_active.png")
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val enabled = parent.offeredPokemon != null && parent.opposingOfferedPokemon != null && parent.protectiveTicks <= 0
        val active = parent.trade.acceptedOppositeOffer && !parent.trade.oppositeAcceptedMyOffer.get()

        val texture = if (!enabled) buttonDisabledResource
            else (if (active) buttonActiveResource else buttonResource)
        blitk(
            matrixStack = context.pose(),
            texture = texture,
            x = x,
            y = y,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (isHovered(mouseX.toDouble(), mouseY.toDouble())) HEIGHT else 0,
            textureHeight = HEIGHT * 2
        )


        val label = if (active) ".".repeat(parent.readyProgress).text() else lang("ui.trade")
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = label.bold(),
            x = x + (WIDTH / 2),
            y = y + 2.5,
            centered = true,
            shadow = true
        )
    }

    override fun playDownSound(pHandler: SoundManager) {
    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (x.toFloat()..(x.toFloat() + WIDTH)) && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + HEIGHT))
}