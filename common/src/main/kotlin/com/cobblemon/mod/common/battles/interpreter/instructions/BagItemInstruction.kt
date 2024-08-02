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
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |bagitem|POKEMON|ITEM
 *
 * POKEMON had ITEM used on it from the 'bag'.
 * @author landonjw
 * @since July 31st, 2023
 */
class BagItemInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchGo {
            val pokemon = message.pokemonByUuid(0, battle)!!
            val item = message.argumentAt(1)!!

            val ownerName = pokemon.actor.getName()
            val itemName = item.asTranslated()

            battle.broadcastChatMessage(battleLang("bagitem.use", ownerName, itemName, pokemon.getName()))
        }
    }
}