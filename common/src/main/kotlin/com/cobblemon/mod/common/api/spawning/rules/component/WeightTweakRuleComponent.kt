/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.rules.component

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.rules.selector.AllSpawnDetailSelector
import com.cobblemon.mod.common.api.spawning.rules.selector.AllSpawningContextSelector
import com.cobblemon.mod.common.api.spawning.rules.selector.SpawnDetailSelector
import com.cobblemon.mod.common.api.spawning.rules.selector.SpawningContextSelector
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveFloat

/**
 * A rule component that alters the weight of spawns at specific contexts.
 *
 * @author Hiroku
 * @since October 1st, 2023
 */
class WeightTweakRuleComponent : SpawnRuleComponent {
    val spawnSelector: SpawnDetailSelector = AllSpawnDetailSelector
    val contextSelector: SpawningContextSelector = AllSpawningContextSelector
    val weight = "v.weight".asExpressionLike()

    @Transient
    val runtime = MoLangRuntime().setup()

    override fun affectWeight(detail: SpawnDetail, ctx: SpawningContext, weight: Float): Float {
        return if (spawnSelector.selects(detail) && contextSelector.selects(ctx)) {
            runtime.environment.setSimpleVariable("spawn", detail.struct)
            runtime.environment.setSimpleVariable("weight", DoubleValue(weight.toDouble()))
            runtime.resolveFloat(this.weight)
        } else {
            weight
        }
    }
}