/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.entity

import net.minecraft.world.entity.Entity

/**
 * Represents a logical despawner for some type of entity. It decides whether an entity should be despawned over time.
 *
 * @author Hiroku
 * @since March 19th, 2022
 */
interface Despawner<T : Entity> {
    fun beginTracking(entity: T)
    fun shouldDespawn(entity: T): Boolean
}