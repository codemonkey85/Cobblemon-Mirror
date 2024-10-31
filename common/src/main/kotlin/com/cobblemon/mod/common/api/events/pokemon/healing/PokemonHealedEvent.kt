/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon.healing

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.item.HealingSource
import com.cobblemon.mod.common.item.berry.HealingBerryItem
import com.cobblemon.mod.common.item.interactive.PotionItem
import com.cobblemon.mod.common.item.interactive.PotionType
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Event that is fired when a Pokemon is healed.
 * @param pokemon The Pokemon that is being healed.
 * @param amount The amount of healing that is being applied. If -1, the Pokemon is fully healed.
 * @param source The HealingSource that is being used to heal the Pokemon.
 *
 * @see HealingSource
 */
class PokemonHealedEvent(
    val pokemon: Pokemon,
    var amount: Int = -1,
    val source: HealingSource = HealingSource.Force
) : Cancelable() {
    fun isFullHeal() = amount == -1
    fun isHealed() = amount > 0
}