package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-zbroken|POKEMON
 *
 * A z-move has broken through protect and hit POKEMON.
 * @author Segfault Guy
 * @since September 10, 2023
 */
class ZBrokenInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val battlePokemon = message.getBattlePokemon(0, battle) ?: return
        val pokemonName = battlePokemon.getName()
        battle.dispatchWaiting {
            battle.broadcastChatMessage(battleLang("zbroken", pokemonName).red())
            battle.minorBattleActions[battlePokemon.uuid] = message
        }
    }
}