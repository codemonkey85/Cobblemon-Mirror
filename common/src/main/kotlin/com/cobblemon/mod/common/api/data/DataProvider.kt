/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.data

import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation

/**
 * Provides a general listener for resource and data pack updates notifying the [DataRegistry] listening.
 *
 * @author Licious
 * @since August 5th, 2022
 */
interface DataProvider {

    /**
     * Registers a [DataRegistry] to listen for updates.
     * The updates will automatically happen on the correct sides based on [DataRegistry.type].
     *
     * @param registry The [DataRegistry] being registered.
     */
    fun <T : DataRegistry> register(registry: T): T

    /**
     * Attempts to find a [DataRegistry] with the given [ResourceLocation].
     * See [DataRegistry.id].
     *
     * @param registryIdentifier The [ResourceLocation]
     * @return The [DataRegistry] if existing.
     */
    fun fromIdentifier(registryIdentifier: ResourceLocation): DataRegistry?

    /**
     * Syncs all of [DataRegistry]s in this provider to a player when requested from the server.
     * This should not be invoked in a single player game instance, the default implementation already makes this check.
     *
     * @param player The [ServerPlayer] being synchronized to the server.
     */
    fun sync(player: ServerPlayer)

    fun doAfterSync(player: ServerPlayer, action: () -> Unit)
}