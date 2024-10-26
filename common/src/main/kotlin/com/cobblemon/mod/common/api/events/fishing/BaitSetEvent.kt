/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.fishing

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.reactive.CancelableObservable
import net.minecraft.world.item.ItemStack

/**
 * Event that is fired when a bait is set on a fishing rod.
 * @param rod The ItemStack of the rod that the bait is set on.
 * @param bait The ItemStack of the bait that is set on the rod.
 */
class BaitSetEvent(val rod: ItemStack, val bait: ItemStack) : Cancelable()