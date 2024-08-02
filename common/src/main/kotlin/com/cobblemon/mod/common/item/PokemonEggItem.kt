/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies.species
import com.cobblemon.mod.common.api.pokemon.breeding.Egg
import com.cobblemon.mod.common.api.pokemon.breeding.EggPokemon
import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.util.FastColor
import java.awt.Color

class PokemonEggItem() : Item(Item.Properties().stacksTo(1)) {

    companion object {
        fun from(egg: EggPokemon): ItemStack {
            //FIXME: Componentify
            val stack = ItemStack(CobblemonItems.POKEMON_EGG, 1)
            return stack
        }

        fun asEggPokemon(stack: ItemStack) : EggPokemon? {
            //FIXME: Componentify
            return null
        }

        fun getColor(stack: ItemStack, tintIndex: Int): Int {
            /*
            val nbt = BlockItem.getBlockEntityNbt(stack)  ?: return Integer.valueOf("FFFFFFFF", 16)
            val eggNbt = nbt.getCompound(DataKeys.EGG) ?:  return Integer.valueOf("FFFFFFFF", 16)
            val colors = Egg.getColorsFromNbt(eggNbt)
            return if (tintIndex == 0) {
                val color = Color.decode("#${colors.first}")
                Argb.getArgb(255, color.red, color.green, color.blue)
            } else {
                val overlayColor = colors.second ?: "FFFFFF"
                val color = Color.decode("#${overlayColor}")
                Argb.getArgb(255, color.red, color.green, color.blue)
            }

             */
            return FastColor.ARGB32.color(255, 255, 255)
        }
    }

}