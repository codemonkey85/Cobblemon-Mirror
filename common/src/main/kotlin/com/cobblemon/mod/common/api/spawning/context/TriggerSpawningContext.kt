/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context

import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos

/**
 * A spawning context relating to triggered spawning. Uses a single-point structure cache which is
 * the only contribution of this implementation.
 *
 * @author Hiroku
 * @since February 3rd, 2024
 */
open class TriggerSpawningContext(
    override val cause: SpawnCause,
    override val world: ServerLevel,
    override val position: BlockPos,
    override val light: Int,
    override val skyLight: Int,
    override val canSeeSky: Boolean,
    override val influences: MutableList<SpawningInfluence>
) : SpawningContext() {
    val triggerStructureCache = StructureChunkCache()
    override fun getStructureCache(pos: BlockPos) = triggerStructureCache
}