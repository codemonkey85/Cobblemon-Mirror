/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.api.interaction.PokemonEntityInteraction
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.pokemon.evolution.variants.TradeEvolution
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer

class LinkCableItem : CobblemonItem(Properties()), PokemonEntityInteraction {
    override val accepted = setOf(PokemonEntityInteraction.Ownership.OWNER)
    override fun processInteraction(player: ServerPlayer, entity: PokemonEntity, stack: ItemStack): Boolean {
        val pokemon = entity.pokemon
        pokemon.lockedEvolutions.filterIsInstance<TradeEvolution>().forEach { evolution ->
            // If an evolution is possible non-optional or has been successfully queued we will consume the item and stop
            // validate requirements to respect required held items and such.
            if (evolution.requirements.all { it.check(pokemon) } && evolution.evolve(pokemon)) {
                this.consumeItem(player, stack)
                return true
            }
        }
        return false
    }

}