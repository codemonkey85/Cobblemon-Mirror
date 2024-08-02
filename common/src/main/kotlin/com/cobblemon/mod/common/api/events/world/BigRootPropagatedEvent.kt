/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.world

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.block.BigRootBlock
import com.cobblemon.mod.common.block.EnergyRootBlock
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState

/**
 * Event fired when a [BigRootBlock] attempts to spread. The event includes the original root position
 * as well as the position of the new root. [resultingSpread] controls whether the new root will be an
 * [EnergyRootBlock]. The new root position and whether it is an energy root is mutable.
 *
 * Cancelling this event will prevent propagation from occurring.
 *
 * @since June 17th, 2023
 * @author Hiroku
 */
class BigRootPropagatedEvent(
    val world: ServerLevel,
    val pos: BlockPos,
    var newRootPosition: BlockPos,
    var resultingSpread: BlockState
) : Cancelable()