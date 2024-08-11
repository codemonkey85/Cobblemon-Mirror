/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.registry

import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import java.util.Optional

/**
 * Class containing utility methods for Cobblemon registries.
 *
 * @param T The type of the [Registry] element.
 *
 * @see [CobblemonRegistries].
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class CobblemonRegistry<T> : Iterable<T> {

    protected val registeredKeys = hashSetOf<ResourceKey<T>>()

    /**
     * @return The associated [Registry].
     */
    abstract fun registry(): Registry<T>

    /**
     * @return The associated [ResourceKey] of the registry.
     */
    abstract fun registryKey(): ResourceKey<Registry<T>>

    /**
     * @return The [ResourceKey]s for this registry provided by Cobblemon.
     */
    fun keys(): Set<ResourceKey<T>> = this.registeredKeys

    /**
     * Create a [ResourceKey] for this [registry].
     *
     * @param name The name aka the path of the [ResourceKey.location]
     * @return The generated [ResourceKey].
     */
    protected fun key(name: String): ResourceKey<T> {
        val key = this.createKey(name.asIdentifierDefaultingNamespace())
        check(this.registeredKeys.add(key)) { "Attempted to create key $key twice" }
        return key
    }

    override fun iterator(): Iterator<T> = this.registry().iterator()

    /**
     * @see [Registry.get]
     */
    fun get(resourceLocation: ResourceLocation?): T? = this.registry().get(resourceLocation)

    /**
     * @see [Registry.get]
     */
    fun get(resourceKey: ResourceKey<T>): T? = this.registry().get(resourceKey)

    /**
     * TODO
     *
     * @param name
     * @return
     */
    fun get(name: String): T? = this.get(name.asIdentifierDefaultingNamespace())

    /**
     * @see [Registry.getOptional]
     */
    fun getOptional(resourceLocation: ResourceLocation): Optional<T> = this.registry().getOptional(resourceLocation)

    /**
     * @see [Registry.getOptional]
     */
    fun getOptional(resourceKey: ResourceKey<T>): Optional<T> = this.registry().getOptional(resourceKey)

    /**
     * TODO
     *
     * @param name
     * @return
     */
    fun getOptional(name: String): Optional<T> = this.getOptional(name.asIdentifierDefaultingNamespace())

    /**
     * @see [Registry.getOrThrow]
     */
    fun getOrThrow(resourceLocation: ResourceLocation): T = this.getOrThrow(this.createKey(resourceLocation))

    /**
     * @see [Registry.getOrThrow]
     */
    fun getOrThrow(resourceKey: ResourceKey<T>): T = this.registry().getOrThrow(resourceKey)

    /**
     * TODO
     *
     * @param name
     * @return
     */
    fun getOrThrow(name: String): T = this.getOrThrow(name.asIdentifierDefaultingNamespace())

    /**
     * TODO
     *
     * @param resourceLocation
     * @return
     */
    fun createKey(resourceLocation: ResourceLocation): ResourceKey<T> = ResourceKey.create(this.registryKey(), resourceLocation)

}