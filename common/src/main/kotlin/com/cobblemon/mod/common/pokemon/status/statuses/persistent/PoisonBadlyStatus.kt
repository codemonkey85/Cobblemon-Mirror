/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.status.statuses.persistent

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.util.cobblemonResource
import kotlin.math.max
import kotlin.math.round
import kotlin.random.Random
import net.minecraft.server.level.ServerPlayer
class PoisonBadlyStatus : PersistentStatus(
    name = cobblemonResource("poisonbadly"),
    showdownName = "tox",
    applyMessage = "cobblemon.status.poisonbadly.apply",
    removeMessage = "cobblemon.status.poison.cure",
    defaultDuration = IntRange(180, 300)
) {
    override fun onSecondPassed(player: ServerPlayer, pokemon: Pokemon, random: Random) {
        // 1 in 15 chance to damage 10% of their HP with a minimum of 1
        if (!pokemon.isFainted() && random.nextInt(15) == 0) {
            pokemon.currentHealth -= max(1, round(pokemon.maxHealth * 0.1).toInt()) * (if (pokemon.ability.template.name == "poisonheal") -1 else 1)
            // Only way that's happened is if the Pokémon has poison heal
            if (pokemon.currentHealth == pokemon.maxHealth) {
                pokemon.status = null
            }
        }
    }
}