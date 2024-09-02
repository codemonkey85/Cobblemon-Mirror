/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang
import net.minecraft.network.chat.Component

/**
 * Format: |-fieldstart|CONDITION
 *
 * The field CONDITION has started.
 * @author Xylopia
 * @since January 31st, 2023
 */
class FieldStartInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val effect = message.effectAt(0) ?: return
        val source = message.battlePokemonFromOptional(battle)
        source?.let { ShowdownInterpreter.broadcastOptionalAbility(battle, message.effect(), source) }

        battle.dispatchWaiting(1.5F) {
            // Note persistent is a CAP ability only we can ignore the flag
            val lang = battleLang("fieldstart.${effect.id}", source?.getName() ?: Component.literal("UNKNOWN"))
            battle.broadcastChatMessage(lang)

            val type = BattleContext.Type.valueOf(effect.rawData.substringAfterLast(" ").uppercase())
            battle.contextManager.add(ShowdownInterpreter.getContextFromAction(message, type, battle))
        }
    }
}