/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.molang

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.util.asExpressionLike
import com.mojang.serialization.Codec

/**
 * An object that can be given a [MoLangRuntime] to produce a single [MoValue]. This abstracts
 * the use of simple and complex [Expression]s as MoLang can be a single line or multiple.
 *
 * @author Hiroku
 * @since October 22nd, 2023
 */
interface ExpressionLike {
    override fun toString(): String
    /** Produces a [MoValue] for a [MoLangRuntime] to supply an environment. */
    fun resolve(runtime: MoLangRuntime, context: Map<String, MoValue> = runtime.environment.context?.map ?: hashMapOf()): MoValue
    fun getString(): String = toString()

    fun resolveDouble(runtime: MoLangRuntime) = resolve(runtime).asDouble()
    fun resolveFloat(runtime: MoLangRuntime) = resolveDouble(runtime).toFloat()
    fun resolveString(runtime: MoLangRuntime) = resolve(runtime).asString()
    fun resolveInt(runtime: MoLangRuntime) = resolveDouble(runtime).toInt()
    fun resolveBoolean(runtime: MoLangRuntime) = resolveDouble(runtime) == 1.0
    fun resolveObject(runtime: MoLangRuntime) = resolve(runtime) as ObjectValue<*>

    companion object {
        val CODEC: Codec<ExpressionLike> = Codec.STRING.xmap({
            it.asExpressionLike()
        }) {
            it.getString()
        }
    }
}
