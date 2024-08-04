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
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.battleLang
import java.util.UUID
import net.minecraft.network.chat.MutableComponent

class TrainerBattleActor(
    val trainerName: String,
    uuid: UUID,
    pokemonList: List<BattlePokemon>,
    artificialDecider: BattleAI
) : AIBattleActor(uuid, pokemonList, artificialDecider) {
    override fun getName() = trainerName.asTranslated()
    override fun nameOwned(name: String): MutableComponent = battleLang("owned_pokemon", this.getName(), name)
    override val type = ActorType.NPC
}