/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.fishing

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import net.minecraft.world.item.ItemStack

/**
 * Event that is fired when a fishing rod is cast.
 */
interface PokerodCastEvent {
    val rod: ItemStack

    /**
     * Event that is fired before a fishing rod is cast.
     * @param rod The ItemStack of the rod that is being cast.
     * @param bobber The PokeRodFishingBobberEntity that is being cast.
     * @param bait The ItemStack of the bait that is set on the rod.
     */
    data class Pre(
        override var rod: ItemStack,
        var bobber: PokeRodFishingBobberEntity,
        var bait: ItemStack
    ) : PokerodCastEvent, Cancelable()

    /**
     * Event that is fired after a fishing rod is cast.
     * @param rod The ItemStack of the rod that is being cast.
     * @param bobber The PokeRodFishingBobberEntity that is being cast.
     * @param bait The ItemStack of the bait that is set on the rod.
     */
    data class Post(
        override var rod: ItemStack,
        var bobber: PokeRodFishingBobberEntity,
        var bait: ItemStack
    ) : PokerodCastEvent
}