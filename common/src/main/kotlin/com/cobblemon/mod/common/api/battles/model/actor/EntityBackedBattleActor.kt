/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battles.model.actor

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

/**
 * Allows a [BattleActor] to attach a [LivingEntity] to itself.
 *
 * @param T The type of [LivingEntity].
 */
interface EntityBackedBattleActor<T : LivingEntity> {

    /**
     * The [LivingEntity] attached to the [BattleActor].
     */
    val entity: T?
    val initialPos: Vec3?

}