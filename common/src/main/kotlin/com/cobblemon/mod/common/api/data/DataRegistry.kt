/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.data

import com.cobblemon.mod.common.api.reactive.SimpleObservable
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager

/**
 * A registry with data provided by a resource or data pack.
 *
 * @author Licious
 * @since August 1st, 2022
 */
interface DataRegistry {

    /**
     * The unique [ResourceLocation] of this registry.
     */
    val id: ResourceLocation

    /**
     * The expected [PackType].
     */
    val type: PackType

    /**
     * An observable that emits whenever this registry has finished reloading.
     */
    val observable: SimpleObservable<out DataRegistry>

    /**
     * Reloads this registry.
     *
     * @param manager The newly updated [ResourceManager]
     */
    fun reload(manager: ResourceManager)

    /**
     * Syncs this registry to a player when requested by the server.
     *
     * @param player The [ServerPlayer] being synchronized to the server.
     */
    fun sync(player: ServerPlayer)

}