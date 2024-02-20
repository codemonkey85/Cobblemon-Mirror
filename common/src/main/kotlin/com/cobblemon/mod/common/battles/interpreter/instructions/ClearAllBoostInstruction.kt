package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-clearallboost|
 *
 * Clears all boosts from all Pokémon on both sides.
 * @author Segfault Guy
 * @since April 26, 2023
 */
class ClearAllBoostInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting(1.5F) {
            battle.activePokemon.forEach {
                it.battlePokemon?.contextManager?.clear(BattleContext.Type.BOOST, BattleContext.Type.UNBOOST)
            }
            battle.broadcastChatMessage(battleLang("clearallboost"))
        }
    }
}