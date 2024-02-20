package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-singlemove|POKEMON|MOVE
 *
 * POKEMON used MOVE which causes a temporary effect lasting the duration of the move.
 * @author Renaissance
 * @since March 24, 2023
 */
class SingleMoveInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting(1.5F) {
            val pokemon = message.getBattlePokemon(0, battle) ?: return@dispatchWaiting
            val pokemonName = pokemon.getName()
            val effectID = message.effectAt(1)?.id ?: return@dispatchWaiting
            val lang = battleLang("singlemove.$effectID", pokemonName)
            battle.broadcastChatMessage(lang)
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}