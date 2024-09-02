/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.bedrockk.molang.Expression
import com.bedrockk.molang.MoLang
import com.bedrockk.molang.ast.NumberExpression
import com.bedrockk.molang.runtime.MoLangEnvironment
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.MoParams
import com.bedrockk.molang.runtime.struct.VariableStruct
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.ListExpression
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.molang.SingleExpression
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

val genericRuntime = MoLangRuntime().setup()

fun MoLangRuntime.resolve(expression: Expression, context: Map<String, MoValue> = emptyMap()): MoValue = try {
//    environment.structs["context"] = ContextStruct(context)
    execute(expression, context)
//    expression.evaluate(MoScope(), environment)
} catch (e: Exception) {
    throw IllegalArgumentException("Unable to parse expression: ${expression.getString()}", e)
}
fun MoLangRuntime.resolveDouble(expression: Expression, context: Map<String, MoValue> = emptyMap()): Double = resolve(expression, context).asDouble()
fun MoLangRuntime.resolveFloat(expression: Expression, context: Map<String, MoValue> = emptyMap()): Float = resolve(expression, context).asDouble().toFloat()
fun MoLangRuntime.resolveInt(expression: Expression, context: Map<String, MoValue> = emptyMap()): Int = resolveDouble(expression, context).toInt()
fun MoLangRuntime.resolveString(expression: Expression, context: Map<String, MoValue> = emptyMap()): String = resolve(expression, context).asString()
fun MoLangRuntime.resolveObject(expression: Expression, context: Map<String, MoValue> = emptyMap()): ObjectValue<*> = resolve(expression, context) as ObjectValue<*>
fun MoLangRuntime.resolveBoolean(expression: Expression, context: Map<String, MoValue> = emptyMap()): Boolean = resolve(expression, context).asDouble() != 0.0

fun MoLangRuntime.resolve(expression: ExpressionLike, context: Map<String, MoValue> = emptyMap()): MoValue = expression.resolve(this, context)
fun MoLangRuntime.resolveDouble(expression: ExpressionLike, context: Map<String, MoValue> = emptyMap()): Double = resolve(expression, context).asDouble()
fun MoLangRuntime.resolveFloat(expression: ExpressionLike, context: Map<String, MoValue> = emptyMap()): Float = resolve(expression, context).asDouble().toFloat()
fun MoLangRuntime.resolveInt(expression: ExpressionLike, context: Map<String, MoValue> = emptyMap()): Int = resolveDouble(expression, context).toInt()
fun MoLangRuntime.resolveString(expression: ExpressionLike, context: Map<String, MoValue> = emptyMap()): String = resolve(expression, context).asString()
fun MoLangRuntime.resolveObject(expression: ExpressionLike, context: Map<String, MoValue> = emptyMap()): ObjectValue<*> = resolve(expression, context) as ObjectValue<*>
fun MoLangRuntime.resolveBoolean(expression: ExpressionLike, context: Map<String, MoValue> = emptyMap()): Boolean = resolve(expression, context).asDouble() != 0.0


fun MoLangRuntime.resolveVec3d(triple: Triple<Expression, Expression, Expression>, context: Map<String, MoValue> = emptyMap()) =
    Vec3(
        resolveDouble(triple.first, context),
        resolveDouble(triple.second, context),
        resolveDouble(triple.third, context)
    )

fun MoLangRuntime.resolveBoolean(expression: Expression, pokemon: Pokemon, context: Map<String, MoValue> = emptyMap()): Boolean {
    environment.writePokemon(pokemon)
    return resolveBoolean(expression, context)
}

fun MoLangRuntime.resolveDouble(expression: Expression, pokemon: Pokemon, context: Map<String, MoValue> = emptyMap()): Double {
    environment.writePokemon(pokemon)
    return resolveDouble(expression, context)
}

fun MoLangRuntime.resolveInt(expression: Expression, pokemon: Pokemon, context: Map<String, MoValue> = emptyMap()): Int {
    environment.writePokemon(pokemon)
    return resolveInt(expression, context)
}

fun MoLangRuntime.resolveInt(expression: ExpressionLike, pokemon: Pokemon, context: Map<String, MoValue> = emptyMap()): Int {
    environment.writePokemon(pokemon)
    return resolveInt(expression, context)
}

fun MoLangRuntime.resolveFloat(expression: Expression, pokemon: Pokemon, context: Map<String, MoValue> = emptyMap()): Float {
    environment.writePokemon(pokemon)
    return resolveFloat(expression, context)
}


fun MoLangRuntime.resolveBoolean(expression: Expression, pokemon: BattlePokemon, context: Map<String, MoValue> = emptyMap()): Boolean {
    environment.writePokemon(pokemon)
    return resolveBoolean(expression, context)
}

fun MoLangRuntime.resolveDouble(expression: Expression, pokemon: BattlePokemon, context: Map<String, MoValue> = emptyMap()): Double {
    environment.writePokemon(pokemon)
    return resolveDouble(expression, context)
}

fun MoLangRuntime.resolveInt(expression: Expression, pokemon: BattlePokemon, context: Map<String, MoValue> = emptyMap()): Int {
    environment.writePokemon(pokemon)
    return resolveInt(expression, context)
}

fun MoLangRuntime.resolveInt(expression: ExpressionLike, pokemon: BattlePokemon, context: Map<String, MoValue> = emptyMap()): Int {
    environment.writePokemon(pokemon)
    return resolveInt(expression, context)
}

fun MoLangRuntime.resolveFloat(expression: Expression, pokemon: BattlePokemon, context: Map<String, MoValue> = emptyMap()): Float {
    environment.writePokemon(pokemon)
    return resolveFloat(expression, context)
}

fun MoLangRuntime.resolveFloat(expression: ExpressionLike, pokemon: Pokemon, context: Map<String, MoValue> = emptyMap()): Float {
    environment.writePokemon(pokemon)
    return resolveFloat(expression, context)
}


fun MoLangRuntime.resolveFloat(expression: ExpressionLike, pokemon: BattlePokemon, context: Map<String, MoValue> = emptyMap()): Float {
    environment.writePokemon(pokemon)
    return resolveFloat(expression, context)
}


fun Expression.getString() = originalString ?: "0"
fun Double.asExpressionLike() = SingleExpression(NumberExpression(this))
fun String.asExpressions() = try {
    MoLang.createParser(if (this == "") "0.0" else this).parse()
} catch (exc: Exception) {
    Cobblemon.LOGGER.error("Failed to parse MoLang expressions: $this")
    throw exc
}

fun String.asExpression() = try {
    MoLang.createParser(if (this == "") "0.0" else this).parseExpression()
} catch (exc: Exception) {
    Cobblemon.LOGGER.error("Failed to parse MoLang expressions: $this")
    throw exc
}

fun String.asExpressionLike() = try {
    val ls = MoLang.createParser(if (this == "") "0.0" else this).parse()
    if (ls.size == 1) {
        SingleExpression(ls[0])
    } else {
        ListExpression(ls)
    }
} catch (exc: Exception) {
    Cobblemon.LOGGER.error("Failed to parse MoLang expressions: $this")
    throw exc
}

fun Double.asExpression() = toString().asExpression() // Use the string route because it remembers the original string value for serialization

fun MoLangEnvironment.writePokemon(pokemon: Pokemon) {
    val pokemonStruct = VariableStruct()
    pokemon.writeVariables(pokemonStruct)
    setSimpleVariable("pokemon", pokemonStruct)
}

fun MoLangEnvironment.writePokemon(pokemon: BattlePokemon) {
    val pokemonStruct = VariableStruct()
    pokemon.writeVariables(pokemonStruct)
    setSimpleVariable("pokemon", pokemonStruct)
}

fun List<String>.asExpressionLike() = joinToString(separator = "\n").asExpressionLike()
fun List<Expression>.resolve(runtime: MoLangRuntime, context: Map<String, MoValue> = emptyMap()) = runtime.execute(this, context)
fun List<Expression>.resolveDouble(runtime: MoLangRuntime, context: Map<String, MoValue> = emptyMap()) = resolve(runtime, context).asDouble()
fun List<Expression>.resolveInt(runtime: MoLangRuntime, context: Map<String, MoValue> = emptyMap()) = resolveDouble(runtime, context).toInt()
fun List<Expression>.resolveBoolean(runtime: MoLangRuntime, context: Map<String, MoValue> = emptyMap()) = resolveDouble(runtime, context) == 1.0
fun List<Expression>.resolveObject(runtime: MoLangRuntime, context: Map<String, MoValue> = emptyMap()) = resolve(runtime, context) as ObjectValue<*>

fun MoParams.getStringOrNull(index: Int) = if (params.size > index) getString(index) else null
fun MoParams.getDoubleOrNull(index: Int) = if (params.size > index) getDouble(index) else null
fun MoParams.getBooleanOrNull(index: Int) = if (params.size > index) getDouble(index) == 1.0 else null

fun MoLangRuntime.withQueryValue(name: String, value: MoValue): MoLangRuntime {
    environment.query.functions.put(name) { value }
    return this
}

fun MoLangRuntime.withPlayerValue(name: String = "player", value: Player) = withQueryValue(name, value.asMoLangValue())

//fun MoLangRuntime.withPokemonValue(name: String = "pokemon", value: Pokemon) = withQueryValue(name, value.asMoLangValue())
fun MoLangRuntime.withNPCValue(name: String = "npc", value: NPCEntity) = withQueryValue(name, value.struct)