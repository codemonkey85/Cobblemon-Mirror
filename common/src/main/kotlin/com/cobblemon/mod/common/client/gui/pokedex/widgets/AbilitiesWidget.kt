/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex.widgets

import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.pokedex.ScaledButton
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

class AbilitiesWidget(x: Int, y: Int): InfoTextScrollWidget(pX = x, pY = y) {
    companion object {
        private val arrowLeft = cobblemonResource("textures/gui/pokedex/info_arrow_left.png")
        private val arrowRight = cobblemonResource("textures/gui/pokedex/info_arrow_right.png")
    }

    val leftButton: ScaledButton = ScaledButton(
        pX + 2.5F,
        pY - 8F,
        7,
        10,
        arrowLeft,
        clickAction = { switchAbility(false) }
    )

    val rightButton: ScaledButton = ScaledButton(
        pX + 133F,
        pY - 8F,
        7,
        10,
        arrowRight,
        clickAction = { switchAbility(true) }
    )

    var abilitiesList: List<AbilityTemplate> = mutableListOf()
    var selectedAbilitiesIndex: Int = 0

    init { setAbility() }

    private fun switchAbility(nextIndex: Boolean) {
        if (nextIndex) {
            if (selectedAbilitiesIndex < abilitiesList.lastIndex) selectedAbilitiesIndex++
            else selectedAbilitiesIndex = 0
        } else {
            if (selectedAbilitiesIndex > 0) selectedAbilitiesIndex--
            else selectedAbilitiesIndex = abilitiesList.lastIndex
        }
        setAbility()
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (!abilitiesAvailable()) return

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = Component.translatable("cobblemon.ui.pokedex.info.ability", abilitiesList[selectedAbilitiesIndex].displayName.asTranslated()).bold(),
            x = pX + 9,
            y = pY - 10,
            shadow = true
        )

        super.renderWidget(context, mouseX, mouseY, delta)
    }

    fun setAbility() {
        if (abilitiesAvailable()) setText(listOf(abilitiesList[selectedAbilitiesIndex].description))
    }

    fun abilitiesAvailable(): Boolean {
        return abilitiesList.isNotEmpty() && selectedAbilitiesIndex < abilitiesList.size
    }
}