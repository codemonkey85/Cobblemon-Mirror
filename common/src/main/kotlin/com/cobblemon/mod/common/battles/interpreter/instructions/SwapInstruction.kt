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
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.InstructionSet
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwapPokemonPacket
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.setPositionSafely
import com.cobblemon.mod.common.util.swap
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import java.util.logging.Level


/**
 * Format:
 * |swap|POKEMON|(from)EFFECT
 *
 * Indicates that a pokemon has swapped its field position with an ally.
 * @author JazzMcNade
 */
class SwapInstruction(val message: BattleMessage, val instructionSet: InstructionSet): InterpreterInstruction {
    override fun invoke(battle: PokemonBattle) {
        // TODO: more error checks
        battle.dispatchWaiting {
            val battlePokemonA = message.battlePokemon(0, battle) ?: return@dispatchWaiting
            val pnxA = message.argumentAt(0)?.substring(0, 3)
            var posA: Vec3? = null
            if (battlePokemonA.entity == null) {
                posA = ShowdownInterpreter.getSendoutPosition(battle, pnxA!!, battlePokemonA.actor)
            } else {
                posA = battlePokemonA.entity?.position()
            }

            val (actor, activePokemonA) = battle.getActorAndActiveSlotFromPNX(pnxA!!)
            val activeBattlePokemonB = activePokemonA.getAdjacentAllies().firstOrNull()
            if(activeBattlePokemonB != null) {
                val pnxB = activeBattlePokemonB.getPNX()
                val (actorB, activePokemonB) = battle.getActorAndActiveSlotFromPNX(pnxB)

                // Swap the position of the 2 on the field
                var posB: Vec3? = null
                if (activePokemonB.battlePokemon?.entity == null) {
                    // target slot is likely fainted
                    posB = ShowdownInterpreter.getSendoutPosition(battle, activePokemonB.getPNX(), actorB)
                } else {
                    posB = activePokemonB.battlePokemon?.entity?.position()
                }
                if (posB != null && battlePokemonA.entity != null) {
                    battlePokemonA.entity?.setPositionSafely(posB)
                } else if(posB != null) {
                    if (activePokemonA.battlePokemon?.entity != null) {
                        activePokemonA.battlePokemon?.entity?.setPositionSafely(posB)
                    } else {
                        activePokemonB.position.let {
                            if (it != null) {
                                activePokemonB.position = Pair<ServerLevel, Vec3>(it.first, posB)
                            }
                        }
                    }
                }
                if(posA != null) {
                    if (activePokemonB.battlePokemon?.entity != null) {
                        activePokemonB.battlePokemon?.entity?.setPositionSafely(posA)
                    } else {
                        activePokemonA.position.let {
                            if (it != null) {
                                activePokemonB.position = Pair<ServerLevel, Vec3>(it.first, posA)
                            }
                        }
                    }
                }
                // Swap references of the 2 pokemon
                actor.activePokemon.swap((pnxA[2] - 'a'), (pnxB[2] - 'a'))

                // Notify clients of the swap
                battle.sendUpdate(BattleSwapPokemonPacket(pnxA))

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