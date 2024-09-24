/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.variation

import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.weightedSelection

/**
 * An element of a [WeightedNPCVariationProvider] that provides a set of aspects with a weight.
 *
 * @author Hiroku
 * @since August 11th, 2024
 */
class WeightedAspect(val aspects: Set<String>, val weight: Double)

/**
 * A variation provider that provides a set of aspects with a weight. When providing for an entity,
 * a simple weighted selection is done to determine which aspects to apply.
 *
 * @author Hiroku
 * @since August 11th, 2024
 */
class WeightedNPCVariationProvider : NPCVariationProvider {
    var options: List<WeightedAspect> = emptyList()

    override val aspects: Set<String>
        get() = options.flatMap(WeightedAspect::aspects).toSet()

    override fun provideAspects(npcEntity: NPCEntity) = options.weightedSelection(WeightedAspect::weight)?.aspects ?: emptySet()
}