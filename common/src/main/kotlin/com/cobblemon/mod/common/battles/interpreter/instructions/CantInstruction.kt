/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addStandardFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.getQueryStruct
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.moves.animations.TargetsProvider
import com.cobblemon.mod.common.api.moves.animations.UsersProvider
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.battles.dispatch.ActionEffectInstruction
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.battles.dispatch.UntilDispatch
import com.cobblemon.mod.common.battles.dispatch.WaitDispatch
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

/**
 * Formats: |cant|POKEMON|REASON and |cant|POKEMON|REASON|MOVE
 *
 * The POKEMON could not perform a move because of the indicated REASON.
 * @author Deltric
 * @since January 22nd, 2022
 */
class CantInstruction(val message: BattleMessage): ActionEffectInstruction {
    override var future: CompletableFuture<*> = CompletableFuture.completedFuture(Unit)
    override var holds = mutableSetOf<String>()
    override val id = cobblemonResource("cant")
    override fun preActionEffect(battle: PokemonBattle) {

    }

    override fun runActionEffect(battle: PokemonBattle, runtime: MoLangRuntime) {
        battle.dispatch {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatch GO
            val effectID = message.effectAt(1)?.id ?: return@dispatch GO
            val name = pokemon.getName()
            val status = Statuses.getStatus(effectID)
            val actionEffect = status?.getActionEffect() ?: return@dispatch GO
            val providers = mutableListOf<Any>(battle)
            pokemon.effectedPokemon.entity?.let { UsersProvider(it) }?.let(providers::add)

            val context = ActionEffectContext(
                actionEffect = actionEffect,
                runtime = runtime,
                providers = providers
            )
            this.future = actionEffect.run(context)
            holds = context.holds // Reference so future things can check on this action effect's holds
            future.thenApply { holds.clear() }
            GO
        }
    }

    override fun postActionEffect(battle: PokemonBattle) {
        battle.dispatch {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatch GO
            val effectID = message.effectAt(1)?.id ?: return@dispatch GO
            val name = pokemon.getName()
            // Move may be null as it's not always given
            val moveName = message.moveAt(2)?.displayName ?: run { "(Unrecognized: ${message.argumentAt(2)})".text() }

            val lang = when (effectID) {
                // TODO: in the games they use a generic image because there is a popup of the ability and the sprite of the mon, it may be good to have a similar system here
                "armortail", "damp", "dazzling", "queenlymajesty" -> battleLang("cant.generic", name, moveName)
                "par", "slp", "frz" -> {
                    val status = Statuses.getStatus(effectID)?.name?.path ?: return@dispatch GO
                    lang("status.$status.is", name)
                }
                else -> battleLang("cant.$effectID", name, moveName)
            }

            battle.broadcastChatMessage(lang.red())
            battle.minorBattleActions[pokemon.uuid] = message
            UntilDispatch { "effects" !in holds }
        }

    }


}