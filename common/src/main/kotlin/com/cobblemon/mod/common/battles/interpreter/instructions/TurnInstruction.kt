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
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.dispatch.DispatchResult
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.net.messages.client.battle.BattleInitializePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleMusicPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleSetTeamPokemonPacket
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |turn|NUMBER
 *
 * It is now turn NUMBER.
 * @author Deltric
 * @since January 22nd, 2022
 */
class TurnInstruction(val message: BattleMessage) : InterpreterInstruction {
    override fun invoke(battle: PokemonBattle) {
        // TODO maybe tell the client that the turn number has changed
        val turnNumber = message.argumentAt(0)?.toInt() ?: return

        if (!battle.started) {
            battle.started = true
            battle.actors.filterIsInstance<PlayerBattleActor>().forEach { actor ->
                val initializePacket = BattleInitializePacket(battle, actor.getSide())
                actor.sendUpdate(initializePacket)
                actor.sendUpdate(BattleMusicPacket(actor.battleTheme))
            }
            battle.actors.forEach { actor ->
                actor.sendUpdate(BattleSetTeamPokemonPacket(actor.pokemonList.map { it.effectedPokemon }))
                val req = actor.request ?: return@forEach
                actor.sendUpdate(BattleQueueRequestPacket(req))
            }

            battle.dispatch {
                DispatchResult { !battle.side1.stillSendingOut() && !battle.side2.stillSendingOut() }
            }

            battle.dispatchGo {
                battle.side1.playCries()
                afterOnServer(seconds = 1.0F) { battle.side2.playCries() }
            }
        }

        battle.dispatch {
            battle.sendToActors(BattleMakeChoicePacket())
            battle.broadcastChatMessage(battleLang("turn", turnNumber).aqua())
            battle.turn(turnNumber)
            GO
        }
    }
}