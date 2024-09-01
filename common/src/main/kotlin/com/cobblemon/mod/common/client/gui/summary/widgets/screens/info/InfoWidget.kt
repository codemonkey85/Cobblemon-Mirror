/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.info

import com.cobblemon.mod.common.api.gui.ColourLibrary
import com.cobblemon.mod.common.api.gui.MultiLineLabelK
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.*
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.common.reformatNatureTextIfMinted
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent


class InfoWidget(
    pX: Int,
    pY: Int,
    private val pokemon: Pokemon
) : SoundlessWidget(pX, pY, WIDTH, HEIGHT, Component.literal("InfoWidget")) {
    companion object {
        private const val WIDTH = 134
        private const val HEIGHT = 148
        private val infoBaseResource = cobblemonResource("textures/gui/summary/summary_info_base.png")
        private const val ROW_HEIGHT = 15
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val matrices = context.pose()
        // Base texture
        blitk(
            matrixStack = matrices,
            texture = infoBaseResource,
            x = x,
            y = y,
            width = width,
            height = height
        )


        // Pokédex Number
        // Add preceding zeroes if Pokédex number is less than 3 digits
        var dexNo = pokemon.species.nationalPokedexNumber.toString()
        while (dexNo.length < 3) {
            dexNo = "0$dexNo"
        }

        val pokedexNumberWidget = InfoOneLineWidget(
            pX = x,
            pY = y,
            width = width,
            label = lang("ui.info.pokedex_number"),
            value = dexNo.text()
        )
        pokedexNumberWidget.render(context, pMouseX, pMouseY, pPartialTicks)


        // Species
        val speciesWidget = InfoOneLineWidget(
            pX = x,
            pY = y + ROW_HEIGHT,
            width = width,
            label = lang("ui.info.species"),
            value = pokemon.species.translatedName
        )
        speciesWidget.render(context, pMouseX, pMouseY, pPartialTicks)

        // Type
        val type = pokemon.types.map { it.displayName.copy() }.reduce { acc, next -> acc.plus("/").plus(next) }
        val typeWidget = InfoOneLineWidget(
            pX = x,
            pY = y + 2 * ROW_HEIGHT,
            width = width,
            label = lang("ui.info.type"),
            value = type
        )
        typeWidget.render(context, pMouseX, pMouseY, pPartialTicks)

        // Original Trainer
        val otName: MutableComponent = Component.literal(pokemon.originalTrainerName ?: "")
        val otWidget = InfoOneLineWidget(
            pX = x,
            pY = y + 3 * ROW_HEIGHT,
            width = width,
            label = lang("ui.info.original_trainer"),
            value = otName
        )
        otWidget.render(context, pMouseX, pMouseY, pPartialTicks)


        // Nature
        // Get the name of the MintItem used to mint the Pokémon's nature
        val natureText = reformatNatureTextIfMinted(pokemon)

        val natureWidget = InfoOneLineWidget(
            pX = x,
            pY = y + 4 * ROW_HEIGHT,
            width = width,
            label = lang("ui.info.nature"),
            value = natureText,
        )
        natureWidget.render(context, pMouseX, pMouseY, pPartialTicks)


        // Ability
        val abilityWidget = InfoOneLineWidget(
            pX = x,
            pY = y + 5 * ROW_HEIGHT,
            width = width,
            label = lang("ui.info.ability").bold(),
            value = pokemon.ability.displayName.asTranslated().bold()
        )
        abilityWidget.render(context, pMouseX, pMouseY, pPartialTicks)

        val smallTextScale = 0.5F

        matrices.pushPose()
        matrices.scale(smallTextScale, smallTextScale, 1F)
        MultiLineLabelK.create(
            component = pokemon.ability.description.asTranslated(),
            width = 117 / smallTextScale,
            maxLines = 3
        ).renderLeftAligned(
            context = context,
            x = (x + 8) / smallTextScale,
            y = (y + 94.5) / smallTextScale,
            ySpacing = 5.5 / smallTextScale,
            colour = ColourLibrary.WHITE,
            shadow = true
        )
        matrices.popPose()

        drawScaledText(
            context = context,
            text = lang("ui.info.experience_points"),
            x = x + 72.5,
            y = y + 125,
            scale = smallTextScale,
            shadow = true
        )

        drawScaledText(
            context = context,
            text = lang("ui.info.to_next_level"),
            x = x + 72.5,
            y = y + 137,
            scale = smallTextScale,
            shadow = true
        )

        val mcFont = Minecraft.getInstance().font
        val experience = pokemon.experience.toString().text()
        val experienceForThisLevel =
            pokemon.experience - if (pokemon.level == 1) 0 else pokemon.experienceGroup.getExperience(pokemon.level)
        val experienceToNext =
            pokemon.experienceGroup.getExperience(pokemon.level + 1) - pokemon.experienceGroup.getExperience(pokemon.level)

        drawScaledText(
            context = context,
            text = experience,
            x = (x + 127) - (mcFont.width(experience) * smallTextScale),
            y = y + 125,
            scale = smallTextScale,
            shadow = true
        )

        drawScaledText(
            context = context,
            text = experienceToNext.toString().text(),
            x = (x + 127) - (mcFont.width(experienceToNext.toString().text()) * smallTextScale),
            y = y + 137,
            scale = smallTextScale,
            shadow = true
        )

        val expRatio = experienceForThisLevel.toFloat() / experienceToNext.toFloat()
        val expBarWidthMax = 55
        val expBarWidth = expRatio * expBarWidthMax

        blitk(
            matrixStack = matrices,
            texture = CobblemonResources.WHITE,
            x = x + 72,
            y = y + 131,
            width = expBarWidth,
            height = 1,
            textureWidth = expBarWidth / expRatio,
            uOffset = expBarWidthMax - expBarWidth,
            red = 0.2,
            green = 0.65,
            blue = 0.84
        )
    }
}