/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.molang

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.util.getString
import com.cobblemon.mod.common.util.resolve

/**
 * An [ExpressionLike] that produces MoValues by evaluating a series of [Expression]s.
 *
 * @author Hiroku
 * @since October 22nd, 2023
 */
class ListExpression(val exprs: List<Expression>): ExpressionLike {
    override fun toString() = exprs.joinToString("\n") { it.getString().let { if (it.endsWith(";")) it else "$it;" } }
    override fun resolve(runtime: MoLangRuntime, context: Map<String, MoValue>) = exprs.resolve(runtime, context)
}
