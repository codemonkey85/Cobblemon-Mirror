/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.tags

import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.ExtraCodecs.TagOrElementLocation

sealed class RegistryBasedCondition<T> {

    abstract fun fits(element: T, registry: Registry<T>): Boolean

    protected abstract fun asTagOrElement(): TagOrElementLocation

    class ResourceLocationCondition<T : Any>(private val resourceLocation: ResourceLocation) : RegistryBasedCondition<T>() {

        override fun fits(element: T, registry: Registry<T>): Boolean = registry.getKey(element) == this.resourceLocation

        override fun asTagOrElement(): TagOrElementLocation = TagOrElementLocation(this.resourceLocation, false)

    }

    class TagCondition<T : Any>(private val tag: TagKey<T>) : RegistryBasedCondition<T>() {

        override fun fits(element: T, registry: Registry<T>): Boolean = registry.getResourceKey(element)
            .flatMap(registry::getHolder)
            .map { entry -> entry.`is`(tag) }
            .orElse(false)

        override fun asTagOrElement(): TagOrElementLocation = TagOrElementLocation(this.tag.location, true)

    }

    companion object {
        @JvmStatic
        fun <T : Any> codec(registryKey: ResourceKey<Registry<T>>): Codec<RegistryBasedCondition<T>> = ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(
            { base -> if (base.tag) TagCondition(TagKey.create(registryKey, base.id)) else ResourceLocationCondition(base.id) },
            { condition -> condition.asTagOrElement() }
        )
    }

}