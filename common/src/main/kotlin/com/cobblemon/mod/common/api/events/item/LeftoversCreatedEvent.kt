/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.item

import com.cobblemon.mod.common.api.events.Cancelable
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/**
 * Fired when eating an apple triggers the creation of a Leftovers. The stack that will be given
 * can be changed via [leftovers]. Cancelling the event will prevent a Leftovers from being given.
 *
 * @author Hiroku
 * @since March 11th, 2023
 */
class LeftoversCreatedEvent(val playerEntity: ServerPlayer, var leftovers: ItemStack) : Cancelable()