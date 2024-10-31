/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.rules.selector

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveBoolean

class ExpressionSpawningContextSelector : SpawningContextSelector {
    @Transient
    val runtime = MoLangRuntime().setup()
    var expression = "true".asExpressionLike()

    override fun selects(ctx: SpawningContext): Boolean {
        runtime.environment.setSimpleVariable("context", ctx.getOrSetupStruct())
        return runtime.resolveBoolean(expression)
    }
}