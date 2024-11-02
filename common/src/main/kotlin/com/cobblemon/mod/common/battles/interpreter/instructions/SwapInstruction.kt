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
import com.cobblemon.mod.common.battles.dispatch.InstructionSet
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.entity.pokemon.effects.IllusionEffect
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwapPokemonPacket
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.setPositionSafely
import com.cobblemon.mod.common.util.swap


/**
 * Format:
 * |swap|POKEMON|(from)EFFECT
 *
 * Indicates that a pokemon has swapped its field position with an ally.
 * @author JazzMcNade
 */
class SwapInstruction(val message: BattleMessage, val instructionSet: InstructionSet): InterpreterInstruction {
    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting {
            val battlePokemonA = message.battlePokemon(0, battle) ?: return@dispatchWaiting
            val pnxA = message.argumentAt(0)?.substring(0, 3)
            val (actor, activePokemonA) = battle.getActorAndActiveSlotFromPNX(pnxA!!)


            val activeBattlePokemonB = activePokemonA.getAdjacentAllies().firstOrNull()
            if (activeBattlePokemonB != null) {
                val pnxB = activeBattlePokemonB.getPNX()
                val (actorB, activePokemonB) = battle.getActorAndActiveSlotFromPNX(pnxB)
                // Swap references of the 2 pokemon
                actor.activePokemon.swap((pnxA[2] - 'a'), (pnxB[2] - 'a'))

                val posA = ShowdownInterpreter.getSendoutPosition(battle, activePokemonA, battlePokemonA.actor) ?: activePokemonA.position?.second
                if (posA != null && battlePokemonA.entity != null) {
                    battlePokemonA.entity?.setPositionSafely(battlePokemonA.entity!!.getAjustedSendoutPosition(posA))
                }

                val posB = ShowdownInterpreter.getSendoutPosition(battle, activePokemonB, battlePokemonA.actor) ?: activePokemonB.position?.second
                val battlePokemonB = activePokemonB.battlePokemon
                if (posB != null && battlePokemonB?.entity != null) {
                    activePokemonB.battlePokemon?.entity?.setPositionSafely(battlePokemonB.entity!!.getAjustedSendoutPosition(posB))
                }

                // Notify clients of the swap
                battle.sendUpdate(BattleSwapPokemonPacket(pnxA))

                if (!message.hasOptionalArgument("silent")) {
                    // Send battle message
                    val lastCauser = instructionSet.getMostRecentCauser(comparedTo = this)
                    val lang = if (lastCauser is MoveInstruction && lastCauser.move.name == "allyswitch") {
                        // Ally Switch
                        battleLang("activate.allyswitch", battlePokemonA.getName(), activePokemonB.battlePokemon?.getName() ?: "")
                    } else {
                        // Triple battle shift
                        battleLang("shift", battlePokemonA.getName())
                    }
                    battle.broadcastChatMessage(lang)
                }
            }
        }

    }
}