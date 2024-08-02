/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.preset

import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.SpawnDetailPresets
import com.cobblemon.mod.common.api.spawning.SpawnLoader
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition
import com.cobblemon.mod.common.api.spawning.context.RegisteredSpawningContext
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.multiplier.WeightMultiplier
import com.cobblemon.mod.common.util.MergeMode
import com.cobblemon.mod.common.util.adapters.SpawnDetailAdapter
import com.google.gson.JsonObject

/**
 * Base class for spawn detail presets. Presets are a spawn loading mechanism that allows various properties to be
 * defined in a preset that will then be inserted into any spawn details that apply this preset. Presets can be used
 * to shortcut the process of commonly used conditions and other [SpawnDetail] properties as well as make those
 * commonly used properties very easy to maintain.
 *
 * A subclass of this base must be registered using [SpawnDetailPresets.registerPresetType].
 *
 * Preset loading occurs during initialization and first will load the internal presets. Then the external
 * config/cobblemon/spawning/presets directory and its child directories will be searched for presets.
 * If a preset is loaded internally that has the same name as an external one, the external preset will take
 * precedence.
 *
 * Most of the logic for presets occurs inside the [SpawnDetailAdapter].
 *
 * It is worth understanding that these presets are purely a loading mechanism and don't exist from then on.
 *
 * @author Hiroku
 * @since July 8th, 2022
 */
abstract class SpawnDetailPreset {
    var bucket: SpawnBucket? = null
    var spawnDetailType: String? = null
    var context: RegisteredSpawningContext<*>? = null
    var condition: JsonObject? = null
    var anticondition: JsonObject? = null
    var weightMultipliers: MutableList<WeightMultiplier>? = null
    var weight: Float? = null
    var percentage: Float? = null
    var mergeMode = MergeMode.INSERT

    open fun apply(spawnDetail: SpawnDetail) {
        bucket?.let { spawnDetail.bucket = it }
        context?.let { spawnDetail.context = it }
        weight?.let { spawnDetail.weight = it }
        percentage?.let { spawnDetail.percentage = it }
        mergeMode.merge(spawnDetail.weightMultipliers, weightMultipliers)

        applyToConditionList(spawnDetail.conditions, condition?.let { resolveCondition(spawnDetail, it) })
        anticondition?.let { spawnDetail.anticonditions.add(resolveCondition(spawnDetail, it)) }
    }

    fun applyToConditionList(conditions: MutableList<SpawningCondition<*>>, resolvedCondition: SpawningCondition<*>?) {
        resolvedCondition ?: return
        conditions.forEach { it.copyFrom(resolvedCondition, mergeMode) }
        if (conditions.isEmpty()) {
            conditions.add(resolvedCondition)
        }
    }

    fun resolveCondition(spawnDetail: SpawnDetail, conditionJson: JsonObject): SpawningCondition<*> {
        SpawnLoader.deserializingConditionClass = SpawningCondition.getByName(spawnDetail.context.defaultCondition)
        return SpawnLoader.gson.fromJson(conditionJson, SpawningCondition::class.java)
    }
}