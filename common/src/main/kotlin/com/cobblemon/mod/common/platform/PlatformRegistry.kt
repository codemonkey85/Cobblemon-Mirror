/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.platform

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.battles.BagItems
import com.cobblemon.mod.common.item.battle.BagItemLike
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

/**
 * A registry meant to hold values that will be later resolved on each platform implementation.
 *
 * @param R The type of the vanilla [Registry].
 * @param K The type of the vanilla [resourceKey].
 * @param T The type of the entries in the registry.
 *
 * @author Licious
 * @since February 11th, 2023
 */
abstract class PlatformRegistry<R : Registry<T>, K : ResourceKey<R>, T> {

    /**
     * The vanilla [Registry].
     */
    abstract val registry: R

    /**
     * The vanilla [ResourceKey].
     */
    abstract val resourceKey: K

    protected val queue = hashMapOf<ResourceLocation, T>()

    /**
     * Creates a new entry in this registry.
     *
     * @param E The type of the entry.
     * @param name The name of the entry, this will be an [ResourceLocation.path].
     * @param entry The entry being added.
     * @return The entry created.
     */
    open fun <E : T> create(name: String, entry: E): E {
        val resourceLocation = cobblemonResource(name)
        return this.create(resourceLocation, entry)
    }

    /**
     * Creates a new entry in this registry. Introduced for sidemod usage
     *
     * @param E The type of the entry.
     * @param name The name of the entry, this will be an [ResourceLocation.path].
     * @param entry The entry being added.
     * @return The entry created.
     */
    open fun <E : T> create(resourceLocation: ResourceLocation, entry: E): E {
        this.queue[resourceLocation] = entry
        if (entry is BagItemLike) {
            BagItems.bagItems.add(
                priority = Priority.NORMAL,
                value = entry
            )
        }
        return entry
    }

    /**
     * Handles the registration of this registry into the platform specific one.
     *
     * @param consumer The consumer that will handle the logic for registering every entry in this registry into the platform specific one.
     */
    open fun register(consumer: (ResourceLocation, T) -> Unit) {
        this.queue.forEach(consumer)
    }

    /**
     * Returns a collection of every entry in this registry.
     *
     * @return The entries of this registry.
     */
    open fun all(): Collection<T> = this.queue.values.toList()

}