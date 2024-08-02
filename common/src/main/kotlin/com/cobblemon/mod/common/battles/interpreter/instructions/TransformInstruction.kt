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
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.battles.dispatch.UntilDispatch
import com.cobblemon.mod.common.entity.pokemon.effects.TransformEffect
import com.cobblemon.mod.common.net.messages.client.battle.BattleTransformPokemonPacket
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-transform|POKEMON|POKEMON
 *
 * POKEMON used Transform to turn into target POKEMON.
 * @author jeffw773
 * @since November 28th, 2023
 */
class TransformInstruction(val battle: PokemonBattle, val message: BattleMessage): InterpreterInstruction {

    val expectedTarget = message.battlePokemon(0, battle)

    override fun invoke(battle: PokemonBattle) {

        val (pnx, _) = message.pnxAndUuid(0) ?: return
        val (actor, _) = battle.getActorAndActiveSlotFromPNX(pnx)
        val pokemon = message.battlePokemon(0, battle) ?: return
        val targetPokemon = message.battlePokemon(1, battle) ?: return

        val effect = message.effect()
        ShowdownInterpreter.broadcastOptionalAbility(battle, effect, pokemon)

        battle.dispatch {
            val entity = pokemon.entity ?: return@dispatch GO
            val future = TransformEffect(targetPokemon.effectedPokemon, battle.started).start(entity)
            UntilDispatch { future?.isDone != false }
        }

        battle.dispatchWaiting {
            val mock = pokemon.entity?.effects?.mockEffect?.mock
            val pokemonName = pokemon.getName()
            val targetPokemonName = targetPokemon.getName()

            mock?.let {
                battle.sendSidedUpdate(
                    source = actor,
                    allyPacket = BattleTransformPokemonPacket(pnx, pokemon, it, true),
                    opponentPacket = BattleTransformPokemonPacket(pnx, pokemon, it, false)
                )
            }

            val lang = battleLang("transform", pokemonName, targetPokemonName)
            battle.broadcastChatMessage(lang)
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}