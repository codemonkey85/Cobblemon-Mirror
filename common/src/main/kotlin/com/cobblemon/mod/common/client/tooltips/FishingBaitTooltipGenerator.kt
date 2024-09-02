/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.api.text.*
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.item.interactive.PokerodItem
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.lang
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import java.text.DecimalFormat

object FishingBaitTooltipGenerator : TooltipGenerator() {
    private val fishingBaitHeader by lazy { lang("fishing_bait_effect_header").blue() }
    private val fishingBaitItemClass by lazy { lang("item_class.fishing_bait").blue() }

    private val Genders = mapOf<Gender, Component>(
        Gender.MALE to lang("gender.male"),
        Gender.FEMALE to lang("gender.female"),
        Gender.GENDERLESS to lang("gender.genderless"),
    )

    override fun generateCategoryTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        if (!FishingBaits.isFishingBait(stack)) return null
        return mutableListOf(fishingBaitItemClass)
    }

    override fun generateAdditionalTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        val resultLines = mutableListOf<Component>()
        val bait =
            (if (stack.item is PokerodItem) PokerodItem.getBaitOnRod(stack) else FishingBaits.getFromBaitItemStack(stack))
                ?: return null
        resultLines.addLast(this.fishingBaitHeader)

        val formatter = DecimalFormat("0.##")

        bait.effects.forEach { effect ->
            val effectType = effect.type.path.toString()
            val effectSubcategory = effect.subcategory?.path
            val effectChance = effect.chance * 100
            var effectValue = when (effectType) {
                "bite_time" -> (effect.value * 100).toInt()
                else -> effect.value.toInt()
            }
            val subcategoryString: Component = if (effectSubcategory != null) {
                when (effectType) {
                    "nature", "ev", "iv" -> com.cobblemon.mod.common.api.pokemon.stats.Stats.getStat(
                        effectSubcategory
                    ).displayName

                    "gender_chance" -> Genders[Gender.valueOf(effectSubcategory.toUpperCase())]

                    "tera" -> ElementalTypes.get(effectSubcategory)?.displayName

                    else -> Component.empty()
                } ?: Component.literal("cursed").obfuscate()
            } else Component.literal("cursed").obfuscate()

            // handle reformatting of shiny chance effectChance
            if (effectType == "shiny_reroll") {
                effectValue++
            }

            resultLines.addLast(
                lang(
                    "fishing_bait_effects.$effectType.tooltip",
                    Component.literal(formatter.format(effectChance)).yellow(),
                    subcategoryString.copy().gold(),
                    Component.literal(formatter.format(effectValue)).green()
                )
            )
        }

        return resultLines
    }
}