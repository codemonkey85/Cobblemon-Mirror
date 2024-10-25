/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.featurerenderers

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.stats.StatWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

/**
 * Renders an [IntSpeciesFeature] as a bar in the summary screen.
 *
 * @author Hiroku
 * @since November 13th, 2023
 */
class BarSummarySpeciesFeatureRenderer(
    override val name: String,
    val displayName: MutableComponent,
    val min: Int,
    val max: Int,
    val colour: Vec3,
    val underlay: ResourceLocation,
    val overlay: ResourceLocation,
    val pokemon: Pokemon
) : SummarySpeciesFeatureRenderer<IntSpeciesFeature> {
    override fun render(GuiGraphics: GuiGraphics, x: Float, y: Float, pokemon: Pokemon, feature: IntSpeciesFeature) {
        val value = feature.value
        val barRatio = (value - min) / (max - min).toFloat()
        val barWidth = Mth.ceil(barRatio * 108)

        blitk(
            matrixStack = GuiGraphics.pose(),
            texture = underlay,
            x = x,
            y = y,
            height = 28,
            width = 124
        )

        val red = colour.x / 255F
        val green = colour.y / 255F
        val blue = colour.z / 255F

        blitk(
            matrixStack = GuiGraphics.pose(),
            texture = CobblemonResources.WHITE,
            x = x + 8,
            y = y + 16,
            height = 10,
            width = barWidth,
            red = red,
            green = green,
            blue = blue
        )

        blitk(
            matrixStack = GuiGraphics.pose(),
            texture = overlay,
            x = x / StatWidget.SCALE,
            y = (y + 16) / StatWidget.SCALE,
            height = 20,
            width = 248,
            scale = StatWidget.SCALE
        )

        // Label
        drawScaledText(
            context = GuiGraphics,
            font = CobblemonResources.DEFAULT_LARGE,
            text = displayName.bold(),
            x = x + 62,
            y = y + 2.5,
            centered = true,
            shadow = true
        )

        drawScaledText(
            context = GuiGraphics,
            text = value.toString().text(),
            x = x + 11,
            y = y + 6,
            scale = StatWidget.SCALE,
            centered = true
        )

        drawScaledText(
            context = GuiGraphics,
            text = "${Mth.floor(barRatio * 100)}%".text(),
            x = x + 113,
            y = y + 6,
            scale = StatWidget.SCALE,
            centered = true
        )
    }
}