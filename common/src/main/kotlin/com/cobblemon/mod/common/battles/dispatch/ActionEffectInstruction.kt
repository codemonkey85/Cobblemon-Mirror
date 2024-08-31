/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.dispatch

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addStandardFunctions
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.CompletableFuture

/**
 * An [InterpreterInstruction] that triggers an [ActionEffectTimeline]
 * I think it might be better to have this as an abstract class so we can default the fields
 *
 * @author Apion
 * @since May 19, 2024
 */
interface ActionEffectInstruction : InterpreterInstruction {
    var future: CompletableFuture<*>
    var holds: MutableSet<String>
    //To expose via molang, so action effects can do different stuff in different instructions
    //e.g. "x is confused" vs "x hit itself in confusion"
    val id: ResourceLocation
    override fun invoke(battle: PokemonBattle) {
        preActionEffect(battle)
        val runtime = MoLangRuntime()
        battle.addQueryFunctions(runtime.environment.query)
        runtime.environment.query.addStandardFunctions()
        addMolangQueries(runtime)
        runActionEffect(battle, runtime)
        postActionEffect(battle)
    }

    fun preActionEffect(battle: PokemonBattle)
    fun runActionEffect(battle: PokemonBattle, runtime: MoLangRuntime)
    fun postActionEffect(battle: PokemonBattle)

    fun addMolangQueries(runtime: MoLangRuntime) {
        runtime.environment.query.addFunction("instruction_id") { StringValue(id.toString()) }
    }

}