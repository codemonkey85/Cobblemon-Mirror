/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.berry

import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.api.fishing.PokeRods
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.text.blue
import com.cobblemon.mod.common.api.text.gray
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.item.interactive.PokerodItem
import com.cobblemon.mod.common.item.interactive.PokerodItem.Companion.getBaitEffects
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemNameBlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import java.math.BigDecimal
import java.text.DecimalFormat

open class BerryItem(private val berryBlock: BerryBlock) : ItemNameBlockItem(berryBlock, Properties()) {

    fun berry() = this.berryBlock.berry()
}