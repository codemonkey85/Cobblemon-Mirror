package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-zpower|POKEMON
 *
 * POKEMON has used the z-move variant of its move.
 * @author Segfault Guy
 * @since September 10, 2023
 */
class ZPowerInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val battlePokemon = message.getBattlePokemon(0, battle) ?: return
        val pokemonName = battlePokemon.getName()
        battle.dispatchWaiting {
            battle.broadcastChatMessage(battleLang("zpower", pokemonName).yellow())
            battle.minorBattleActions[battlePokemon.uuid] = message
        }
    }
}