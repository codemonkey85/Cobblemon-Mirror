/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.actor

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.battles.ai.RandomBattleAI
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import java.util.UUID
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component

class MultiPokemonBattleActor(
    pokemonList: List<BattlePokemon>,
    artificialDecider: BattleAI = RandomBattleAI(),
    uuid: UUID = UUID.randomUUID()
) : AIBattleActor(uuid, pokemonList, artificialDecider) {
    override fun getName(): MutableComponent = "Wild Pokémon".text() // TODO probably remove by making it nullable
    override fun nameOwned(name: String): MutableComponent = Component.literal(name)
    override val type = ActorType.WILD
}