/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.text.gray
import com.cobblemon.mod.common.item.PokeBallItem
import com.cobblemon.mod.common.util.asTranslated
import net.minecraft.core.component.DataComponents
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object CobblemonTooltipGenerator : TooltipGenerator() {
    @Suppress("DEPRECATION")
    override fun generateTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        val resultLines = mutableListOf<Component>()

        if (stack.item.builtInRegistryHolder().unwrapKey().isPresent && stack.item.builtInRegistryHolder().unwrapKey().get().location().namespace == Cobblemon.MODID) {
            if (stack.get(DataComponents.HIDE_TOOLTIP) != null) {
                return null
            }
            val language = Language.getInstance()
            val key = this.baseLangKeyForItem(stack)
            if (language.has(key)) {
                resultLines.add(key.asTranslated().gray())
            }
            var i = 1
            var listKey = "${key}_$i"
            while(language.has(listKey)) {
                resultLines.add(listKey.asTranslated().gray())
                listKey = "${key}_${++i}"
            }
        }

        return resultLines
    }

    private fun baseLangKeyForItem(stack: ItemStack): String {
        if (stack.item is PokeBallItem) {
            val asPokeball = stack.item as PokeBallItem
            return "item.${asPokeball.pokeBall.name.namespace}.${asPokeball.pokeBall.name.path}.tooltip"
        }
        return "${stack.descriptionId}.tooltip"
    }
}