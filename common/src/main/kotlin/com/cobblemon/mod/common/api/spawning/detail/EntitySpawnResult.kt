/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import net.minecraft.world.entity.Entity

/**
 * A spawn result for spawn actions which involves some number of entities. This is used to maintain
 * spawned entity lists for Spawner implementations that cap the number of entities that may be spawned.
 *
 * @author Hiroku
 * @since January 13th, 2024
 */
class EntitySpawnResult(val entities: List<Entity>)