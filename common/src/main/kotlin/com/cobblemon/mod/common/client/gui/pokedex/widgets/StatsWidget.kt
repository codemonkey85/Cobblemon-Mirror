/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.HALF_OVERLAY_WIDTH
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.POKEMON_DESCRIPTION_HEIGHT
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.SCALE
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.drawScaledTextJustifiedRight
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

class StatsWidget(val pX: Int, val pY: Int) : SoundlessWidget(
    pX,
    pY,
    HALF_OVERLAY_WIDTH,
    POKEMON_DESCRIPTION_HEIGHT,
    lang("ui.pokedex.pokemon_info")
) {

    companion object {
        private val statLabels = arrayOf(
            lang("ui.stats.hp"),
            lang("ui.stats.atk"),
            lang("ui.stats.def"),
            lang("ui.stats.sp_atk"),
            lang("ui.stats.sp_def"),
            lang("ui.stats.speed")
        )

        private val stats = arrayOf(
            Stats.HP,
            Stats.ATTACK,
            Stats.DEFENCE,
            Stats.SPECIAL_ATTACK,
            Stats.SPECIAL_DEFENCE,
            Stats.SPEED
        )
    }

    var baseStats: Map<Stat, Int>? = null

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (baseStats == null) return

        val matrices = context.pose()

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = Component.translatable("cobblemon.ui.pokedex.info.stats").bold(),
            x = pX + 9,
            y = pY - 10,
            shadow = true
        )

        // Divider
        blitk(
            matrixStack = matrices,
            texture = CobblemonResources.WHITE,
            x = pX + 50.5,
            y = pY + 4,
            height = 33.5,
            width = 1,
            red = 0.71F,
            green = 0.95F,
            blue = 0.95F
        )

        for (i in statLabels.indices) {
            drawScaledText(
                context = context,
                text = statLabels[i].bold(),
                x = pX + 9,
                y = pY + 4 + (6 * i),
                colour = 0x606B6E,
                scale = SCALE
            )

            val statValue = baseStats!![stats[i]]

            if (statValue != null) {
                drawScaledTextJustifiedRight(
                    context = context,
                    text = statValue.toString().text(),
                    x = pX + 48,
                    y = pY + 4 + (6 * i),
                    colour = 0x3A96B6,
                    scale = SCALE
                )

                val (red, green, blue) = getStatValueRGB(statValue)
                blitk(
                    matrixStack = matrices,
                    texture = CobblemonResources.WHITE,
                    x = pX + 54.5,
                    y = pY + 4 + (6 * i),
                    height = 3.5F,
                    width = (statValue / 255F) * 77,
                    red = red,
                    green = green,
                    blue = blue
                )
            }
        }
    }

    private fun getStatValueRGB(value: Int): Triple<Float, Float, Float> {
        when {
            value >= 150 -> return Triple(0.647F, 0.929F, 0.647F)
            value >= 120 -> return Triple(0.8F, 0.937F, 0.423F)
            value >= 100 -> return Triple(0.93F, 0.91F, 0.415F)
            value >= 80 -> return Triple(0.965F, 0.812F, 0.423F)
        }
        return Triple(0.964F, 0.64F, 0.502F)
    }
}