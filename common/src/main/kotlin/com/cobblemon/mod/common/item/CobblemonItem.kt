/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.api.text.blue
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.item.interactive.PokerodItem.Companion.getBaitEffects
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.lang
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import java.math.BigDecimal

/**
 * Base for custom items in Cobblemon.
 *
 * Containing common shared code.
 */
open class CobblemonItem(settings : Properties) : Item(settings)