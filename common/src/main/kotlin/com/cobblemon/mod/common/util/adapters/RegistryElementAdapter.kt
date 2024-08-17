/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.google.gson.*
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type

/**
 * A type adapter to process elements of a registry.
 *
 * @param T The type of the registry element.
 * @property registryProvider A supplier for the registry that gets used. This is used to ensure the registry is only used once it's safe.
 */
class RegistryElementAdapter<T : Any>(val registryProvider: () -> Registry<T>) : JsonDeserializer<T>, JsonSerializer<T> {

    override fun deserialize(jElement: JsonElement, type: Type, context: JsonDeserializationContext): T {
        val identifier = context.deserialize<ResourceLocation>(jElement, ResourceLocation::class.java)
        val registry = this.registryProvider()
        return registry.get(identifier) ?: throw IllegalArgumentException("Cannot resolve element '$identifier' from ${registry.key().location()}")
    }

    override fun serialize(element: T, type: Type, context: JsonSerializationContext): JsonElement {
        val registry = this.registryProvider()
        val identifier = registry.getKey(element) ?: throw IllegalArgumentException("Cannot resolve the identifier from the registry ${registry.key().location()} for $element")
        return JsonPrimitive(identifier.toString())
    }

}