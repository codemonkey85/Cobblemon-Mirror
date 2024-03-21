package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |replace|POKEMON|DETAILS|HP STATUS
 *
 * Illusion has ended for POKEMON. Syntax is the same as [SwitchInstruction].
 * @author Segfault Guy
 * @since March 18th, 2024
 */
class ReplaceInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting(1F) {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchWaiting
            val entity = pokemon.entity
            entity?.let { it.effects.mockEffect?.end(it) }
        }
    }
}