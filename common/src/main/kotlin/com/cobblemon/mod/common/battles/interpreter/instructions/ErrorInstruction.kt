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
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.net.messages.client.battle.BattleMadeInvalidChoicePacket
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |error|(Invalid choice) ERROR
 *
 * The actor needs to send a different decision due to ERROR.
 * @author Yaseen
 * @since April 22nd, 2023
 */
class ErrorInstruction(val battleActor: BattleActor, val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.log("Error Instruction")
        battle.dispatchGo {
            //TODO: some lang stuff for the error messages (Whats the protocol for adding to other langs )
            //Also is it okay to ignore the team preview error for now? - You bet!
            val lang = when(message.rawMessage) {
                "|error|[Unavailable choice] Can't switch: The active Pokémon is trapped" -> battleLang("error.pokemon_is_trapped").red()
                "|error|[Invalid choice] Can't choose for Team Preview: You're not in a Team Preview phase" -> return@dispatchGo
                "|error|[Invalid choice] Can't do anything: It's not your turn" -> return@dispatchGo
                else -> battle.createUnimplemented(message)
            }
            battleActor.sendMessage(lang)
            battleActor.mustChoose = true
            battleActor.sendUpdate(BattleMadeInvalidChoicePacket())
        }
    }
}