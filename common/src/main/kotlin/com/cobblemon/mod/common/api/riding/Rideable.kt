/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding

import net.minecraft.world.entity.PlayerRideableJumping

/**
 * Represents an entity that supports riding.
 *
 * @since 1.5.0
 */
interface Rideable : PlayerRideableJumping {

    /**
     * Denotes the manager responsible for handling any instance of riding taking place on an entity
     */
    val riding: RidingManager

}