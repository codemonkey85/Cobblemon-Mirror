/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.brewing

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.brewing.ingredient.CobblemonIngredient
import com.cobblemon.mod.common.brewing.ingredient.CobblemonItemIngredient
import com.cobblemon.mod.common.brewing.ingredient.CobblemonPotionIngredient
import net.minecraft.world.item.Item
import net.minecraft.world.item.alchemy.Potions

object BrewingRecipes {

    val recipes: List<Triple<CobblemonIngredient, CobblemonIngredient, Item>> by lazy {
        listOf(
            Triple(CobblemonPotionIngredient(Potions.WATER), CobblemonItemIngredient(CobblemonItems.MEDICINAL_LEEK), CobblemonItems.MEDICINAL_BREW),
            convert(CobblemonItems.MEDICINAL_BREW, CobblemonItems.LEPPA_BERRY, CobblemonItems.ETHER),
            convert(CobblemonItems.MEDICINAL_BREW, CobblemonItems.HOPO_BERRY, CobblemonItems.ELIXIR),
            convert(CobblemonItems.MEDICINAL_BREW, CobblemonItems.ORAN_BERRY, CobblemonItems.POTION),
            convert(CobblemonItems.MEDICINAL_BREW, CobblemonItems.SITRUS_BERRY, CobblemonItems.HYPER_POTION),
            convert(CobblemonItems.MEDICINAL_BREW, CobblemonItems.LUM_BERRY, CobblemonItems.FULL_HEAL),
            convert(CobblemonItems.MEDICINAL_BREW, CobblemonItems.PECHA_BERRY, CobblemonItems.ANTIDOTE),
            convert(CobblemonItems.MEDICINAL_BREW, CobblemonItems.CHESTO_BERRY, CobblemonItems.AWAKENING),
            convert(CobblemonItems.MEDICINAL_BREW, CobblemonItems.RAWST_BERRY, CobblemonItems.BURN_HEAL),
            convert(CobblemonItems.MEDICINAL_BREW, CobblemonItems.ASPEAR_BERRY, CobblemonItems.ICE_HEAL),
            convert(CobblemonItems.MEDICINAL_BREW, CobblemonItems.CHERI_BERRY, CobblemonItems.PARALYZE_HEAL),
            convert(CobblemonItems.BURN_HEAL, CobblemonItems.LUM_BERRY, CobblemonItems.FULL_HEAL),
            convert(CobblemonItems.ANTIDOTE, CobblemonItems.LUM_BERRY, CobblemonItems.FULL_HEAL),
            convert(CobblemonItems.AWAKENING, CobblemonItems.LUM_BERRY, CobblemonItems.FULL_HEAL),
            convert(CobblemonItems.ICE_HEAL, CobblemonItems.LUM_BERRY, CobblemonItems.FULL_HEAL),
            convert(CobblemonItems.PARALYZE_HEAL, CobblemonItems.LUM_BERRY, CobblemonItems.FULL_HEAL),
            convert(CobblemonItems.ETHER, CobblemonItems.PEP_UP_FLOWER, CobblemonItems.MAX_ETHER),
            convert(CobblemonItems.ELIXIR, CobblemonItems.PEP_UP_FLOWER, CobblemonItems.MAX_ELIXIR),
            convert(CobblemonItems.POTION, CobblemonItems.ENERGY_ROOT, CobblemonItems.SUPER_POTION),
            convert(CobblemonItems.SUPER_POTION, CobblemonItems.FIGY_BERRY, CobblemonItems.HYPER_POTION),
            convert(CobblemonItems.SUPER_POTION, CobblemonItems.WIKI_BERRY, CobblemonItems.HYPER_POTION),
            convert(CobblemonItems.SUPER_POTION, CobblemonItems.MAGO_BERRY, CobblemonItems.HYPER_POTION),
            convert(CobblemonItems.SUPER_POTION, CobblemonItems.AGUAV_BERRY, CobblemonItems.HYPER_POTION),
            convert(CobblemonItems.SUPER_POTION, CobblemonItems.IAPAPA_BERRY, CobblemonItems.HYPER_POTION),
            convert(CobblemonItems.HYPER_POTION, CobblemonItems.VIVICHOKE, CobblemonItems.MAX_POTION),
            convert(CobblemonItems.MAX_POTION, CobblemonItems.LUM_BERRY, CobblemonItems.FULL_RESTORE),

            convert(CobblemonItems.POTION, CobblemonItems.PETAYA_BERRY, CobblemonItems.THROAT_SPRAY)
        )
    }
    
    private fun convert(input: Item, ingredient: Item, output: Item): Triple<CobblemonIngredient, CobblemonIngredient, Item> {
        return Triple(CobblemonItemIngredient(input), CobblemonItemIngredient(ingredient), output)
    }

}
