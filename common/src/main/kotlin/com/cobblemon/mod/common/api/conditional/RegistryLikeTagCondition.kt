/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.conditional

import com.google.gson.JsonElement
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

/**
 * A condition for some registry type that asserts that the entry must be inside the given [TagKey]. This is presented
 * in JSONs as an identifier prefixed with a hash.
 *
 * @author Hiroku
 * @since July 16th, 2022
 */
open class RegistryLikeTagCondition<T : Any>(val tag: TagKey<T>) : RegistryLikeCondition<T> {

    companion object {
        const val PREFIX = "#"
        fun <T : Any> resolver(
            resourceKey: ResourceKey<Registry<T>>,
            constructor: (TagKey<T>) -> RegistryLikeTagCondition<T>
        ): (JsonElement) -> RegistryLikeTagCondition<T>? = {
            val firstSymbol = it.asString.substring(0, 1)
            if (firstSymbol == PREFIX) {
                val identifier = ResourceLocation.parse(it.asString.substring(1))
                constructor(TagKey.create(resourceKey, identifier))
            } else {
                null
            }
        }
    }

    override fun fits(t: T, registry: Registry<T>): Boolean {
//        val registryHasTag = registry.containsTag(tag)
//        if (!registryHasTag) {
////            LOGGER.warn("No tag in registry: ${tag.id}")
//            return false
//        }

        return registry.getResourceKey(t)
            .flatMap(registry::getHolder)
            .map { entry -> entry.`is`(tag) }
            .orElse(false)
    }
}