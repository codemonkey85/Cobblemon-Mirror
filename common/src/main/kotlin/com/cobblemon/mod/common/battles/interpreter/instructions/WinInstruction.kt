/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.text.gold
import com.cobblemon.mod.common.api.text.plus
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.battles.dispatch.WaitDispatch
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.util.battleLang
import java.util.UUID

/**
 * Format: |win|USER
 *
 * USER has won the battle.
 * @author Deltric
 * @since January 22nd, 2022
 */
class WinInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val user = message.argumentAt(0) ?: return
        val ids = user.split("&").map { it.trim() }
        val winners = ids.map { battle.getActor(UUID.fromString(it))!! }
        val losers = battle.actors.filter { !winners.contains(it) }
        val winnersText = winners.map { it.getName() }.reduce { acc, next -> acc + " & " + next }
        val losersText = losers.map { it.getName() }.reduce { acc, next -> acc + " & " + next }
        val wasCaught = battle. showdownMessages.any { "capture" in it }

        battle.dispatch {
            // If the battle was a PvW battle, we need to set the killer of the wild Pokémon to the player
            if (battle.isPvW) {
                val nonPlayerActor = battle.actors.first { it.type == ActorType.WILD }
                val wildPokemon: BattlePokemon = nonPlayerActor.pokemonList.first()

                if (!wasCaught && losers.any { it.uuid == wildPokemon.uuid }) {
                    wildPokemon.effectedPokemon.entity?.killer = (battle.actors.firstOrNull { it.type == ActorType.PLAYER } as? PlayerBattleActor)?.entity
                }
            }

            // broadcast victory / defeat
            if (!wasCaught) {
                val blackedOut = battle.isPvW && losers.any { it is PlayerBattleActor }
                val lang = if (blackedOut) battleLang("lose", losersText).red() else battleLang("win", winnersText).gold()
                battle.broadcastChatMessage(lang)
                return@dispatch WaitDispatch(2F)
            }
            else {
                return@dispatch GO  // see BattleCaptureAction
            }
        }
        battle.dispatchGo {
            battle.end()
            CobblemonEvents.BATTLE_VICTORY.post(BattleVictoryEvent(battle, winners, losers, wasCaught))
            ShowdownInterpreter.lastCauser.remove(battle.battleId)
        }
    }
}