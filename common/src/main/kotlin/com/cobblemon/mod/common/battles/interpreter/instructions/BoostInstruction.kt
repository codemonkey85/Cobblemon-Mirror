/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.moves.animations.ActionEffects
import com.cobblemon.mod.common.api.moves.animations.UsersProvider
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.ActionEffectInstruction
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.battles.dispatch.InstructionSet
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.battles.dispatch.UntilDispatch
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

/**
 * Format: |-boost|POKEMON|STAT|AMOUNT or |-unboost|POKEMON|STAT|AMOUNT
 *
 * POKEMON has gained or lost AMOUNT in STAT, using the standard rules for stat changes in-battle.
 * STAT is a standard three-letter abbreviation fot the stat in question.
 * @author Hiroku
 * @since August 20th, 2022
 */
class BoostInstruction(val instructionSet: InstructionSet, val message: BattleMessage, val remainingLines: Iterator<BattleMessage>, val isBoost: Boolean = true): ActionEffectInstruction {
    override var future: CompletableFuture<*> = CompletableFuture.completedFuture(Unit)
    override var holds = mutableSetOf<String>()
    override val id = cobblemonResource("boost")
    override fun preActionEffect(battle: PokemonBattle) {

    }

    override fun runActionEffect(battle: PokemonBattle, runtime: MoLangRuntime) {
        battle.dispatch {
            val actionEffect = if (isBoost) BOOST_EFFECT else UNBOOST_EFFECT
            val providers = mutableListOf<Any>(battle)
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatch GO
            pokemon.effectedPokemon.entity?.let { UsersProvider(it) }?.let(providers::add)
            val context = ActionEffectContext(
                actionEffect = actionEffect,
                runtime = runtime,
                providers = providers
            )
            this.future = actionEffect.run(context)
            holds = context.holds // Reference so future things can check on this action effect's holds
            future.thenApply { holds.clear() }
            return@dispatch GO
        }
    }

    override fun postActionEffect(battle: PokemonBattle) {
        val pokemon = message.battlePokemon(0, battle) ?: return
        val statKey = message.argumentAt(1) ?: return
        val stages = message.argumentAt(2)?.toInt() ?: return
        val stat = Stats.getStat(statKey).displayName
        val severity = Stats.getSeverity(stages)
        val rootKey = if (isBoost) "boost" else "unboost"

        battle.dispatch {
            val lang = when {
                message.hasOptionalArgument("zeffect") -> battleLang("$rootKey.$severity.zeffect", pokemon.getName(), stat)
                else -> battleLang("$rootKey.$severity", pokemon.getName(), stat)
            }
            battle.broadcastChatMessage(lang)

            val boostBucket = if (isBoost) BattleContext.Type.BOOST else BattleContext.Type.UNBOOST
            val context = ShowdownInterpreter.getContextFromAction(message, boostBucket, battle)
            // TODO: replace with context that tracks detailed information such as # of stages
            repeat(stages) { pokemon.contextManager.add(context) }
            battle.minorBattleActions[pokemon.uuid] = message
            return@dispatch UntilDispatch { "effects" !in holds }
        }
    }

    companion object {
        val BOOST_EFFECT = ActionEffects.actionEffects[cobblemonResource("boost")]!!
        val UNBOOST_EFFECT = ActionEffects.actionEffects[cobblemonResource("unboost")]!!
    }

}