/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.battles.interpreter.ContextManager
import net.minecraft.network.chat.Component

/**
 * Unlike the Showdown side.ts, this can represent multiple actors.
 *
 * @author Hiroku
 * @since March 9th, 2022
 */
class BattleSide(vararg val actors: BattleActor) {
    val activePokemon: List<ActiveBattlePokemon>
        get() = actors.flatMap { it.activePokemon }

    lateinit var battle: PokemonBattle
    val contextManager = ContextManager()
    fun getOppositeSide() = if (this == battle.side1) battle.side2 else battle.side1

    fun broadcastChatMessage(component: Component) {
        return this.actors.forEach { it.sendMessage(component) }
    }

    fun stillSendingOut() = actors.any { it.stillSendingOutCount > 0 }

    fun playCries() {
        activePokemon.forEach {
            val entity = it.battlePokemon?.entity ?: return@forEach
            entity.cry()
        }
    }
}