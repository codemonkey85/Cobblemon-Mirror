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
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.ShowdownActionRequest
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket

/**
 * Format: |request|REQUEST
 *
 * It's time for the actor to make a decision.
 * @author Deltric
 * @since January 22nd, 2022
 */
class RequestInstruction(val battleActor: BattleActor, val message: BattleMessage): InterpreterInstruction {
    override fun invoke(battle: PokemonBattle) {
        battle.log("Request Instruction")

        if (message.rawMessage.contains("teamPreview")) // TODO probably change when we're allowing team preview
            return

        // Parse Json message and update state info for actor
        val request = BattleRegistry.gson.fromJson(message.rawMessage.split("|request|")[1], ShowdownActionRequest::class.java)
        request.sanitize(battle, battleActor)
        battle.dispatchGo {
            // This request won't be acted on until the start of next turn
            battleActor.sendUpdate(BattleQueueRequestPacket(request))
            battleActor.request = request
            battleActor.responses.clear()
            // We need to send this out because 'upkeep' isn't received until the request is handled since the turn won't swap
            if (request.forceSwitch.contains(true)) {
                battle.doWhenClear {
                    battleActor.mustChoose = true
                    battleActor.sendUpdate(BattleMakeChoicePacket())
                }
            }
        }
    }

}