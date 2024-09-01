/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.util.server
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType

open class SpawnCause(
    val spawner: Spawner,
    val bucket: SpawnBucket,
    entity: Entity? = null
): SpawningInfluence {
    val entityWorldId = entity?.level()?.dimension()
    val entityId = entity?.id
    val entityUUID = entity?.uuid
    val entityType = entity?.type

    val entity: Entity?
        get() = if (entityType == EntityType.PLAYER) server()?.playerList?.getPlayer(entityUUID) else server()?.getLevel(entityWorldId)?.getEntity(entityId!!)
}