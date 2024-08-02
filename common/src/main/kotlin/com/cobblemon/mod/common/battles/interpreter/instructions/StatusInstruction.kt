/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.net.messages.client.battle.BattlePersistentStatusPacket
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.util.asTranslated

/**
 * Format: |-status|POKEMON|STATUS
 *
 * POKEMON has been inflicted with STATUS.
 * @author Hiroku
 * @since October 3rd, 2022
 */
class StatusInstruction( val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val (pnx, _) = message.pnxAndUuid(0) ?: return
        val pokemon = message.battlePokemon(0, battle) ?: return
        val otherPokemon = message.actorAndActivePokemonFromOptional(battle, "of")?.second?.battlePokemon
        val statusLabel = message.argumentAt(1) ?: return
        val status = Statuses.getStatus(statusLabel) ?: return Cobblemon.LOGGER.error("Unrecognized status: $statusLabel")

        ShowdownInterpreter.broadcastOptionalAbility(battle, message.effect(), otherPokemon ?: pokemon)

        battle.dispatchWaiting {
            if (status is PersistentStatus) {
                pokemon.effectedPokemon.applyStatus(status)
                battle.sendUpdate(BattlePersistentStatusPacket(pnx, status))
            }

            battle.broadcastChatMessage(status.applyMessage.asTranslated(pokemon.getName()))
            pokemon.contextManager.add(ShowdownInterpreter.getContextFromAction(message, BattleContext.Type.STATUS, battle))
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}