/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.conditional

import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey

val ITEM_REGISTRY_LIKE_CODEC = makeCodecForRegistryLikeCondition("item", Registries.ITEM)

fun <T : Any> makeCodecForRegistryLikeCondition(name: String, registryKey: ResourceKey<Registry<T>>): Codec<RegistryLikeCondition<T>> {
    return CodecUtils.createByStringCodec(
        from = { str ->
            if (str.startsWith("#")) {
                return@createByStringCodec RegistryLikeTagCondition<T>(TagKey.create(registryKey, str.substring(1).asIdentifierDefaultingNamespace()))
            } else {
                return@createByStringCodec RegistryLikeIdentifierCondition<T>(str.asIdentifierDefaultingNamespace())
            }
        },
        to = { condition ->
            if (condition is RegistryLikeTagCondition) {
                "#${condition.tag.location}"
            } else {
                (condition as RegistryLikeIdentifierCondition).identifier.toString()
            }
        },
        errorSupplier = { "Unknown $name condition: $it. Should be # followed by a tag, or $name resource location e.g. #minecraft:ladders or minecraft:grass_block." }
    )
}