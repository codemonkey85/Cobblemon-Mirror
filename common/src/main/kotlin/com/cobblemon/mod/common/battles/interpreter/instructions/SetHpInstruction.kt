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
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.net.messages.client.battle.BattleHealthChangePacket
import com.cobblemon.mod.common.util.battleLang
import kotlin.math.roundToInt

/**
 * Format: |-sethp|POKEMON|HP
 *
 * POKEMON now has HP hit points.
 * @author Licious
 * @since February 8th, 2023
 */
class SetHpInstruction(val actor: BattleActor, val publicMessage: BattleMessage, val privateMessage: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting {
            val (pnx, _) = privateMessage.pnxAndUuid(0) ?: return@dispatchWaiting
            val flatHp = privateMessage.argumentAt(1)?.split("/")?.getOrNull(0)?.toFloatOrNull() ?: return@dispatchWaiting
            val ratioHp = publicMessage.argumentAt(1)?.split("/")?.getOrNull(0)?.toFloatOrNull()?.times(0.01F) ?: return@dispatchWaiting
            val battlePokemon = privateMessage.battlePokemon(0, battle) ?: return@dispatchWaiting
            battlePokemon.effectedPokemon.currentHealth = flatHp.roundToInt()
            battle.sendSidedUpdate(actor, BattleHealthChangePacket(pnx, flatHp), BattleHealthChangePacket(pnx, ratioHp))
            // It doesn't matter which we check when silent both have it
            if (!publicMessage.hasOptionalArgument("silent")) {
                val effectID = publicMessage.effect()?.id ?: return@dispatchWaiting
                val lang = battleLang("sethp.$effectID")
                battle.broadcastChatMessage(lang)
            }
            battle.minorBattleActions[battlePokemon.uuid] = publicMessage
        }
    }
}