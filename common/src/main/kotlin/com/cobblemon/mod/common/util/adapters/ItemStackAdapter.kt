/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.google.gson.*
import net.minecraft.world.item.ItemStack

/**
 * Saves and loads an [ItemStack] with JSON.
 * The JSON can be either a [JsonPrimitive] or a [JsonObject].
 * If it is a [JsonPrimitive] the expected value is simply the [Identifier] of the item.
 * If it is a [JsonObject] the entire [CompoundTag] behind the [ItemStack] is expected.
 *
 * When serializing it will always convert the equivalent [CompoundTag] into a [JsonObject]
 *
 * @author Licious
 * @since March 20th, 2022
 */
//object ItemStackAdapter : JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {
//
//    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ItemStack = (if (json.isJsonPrimitive) ItemStack(Registry.ITEM.get(Identifier.of(json.asString.lowercase()))) else ItemStack.fromNbt(json.asNbt() as NbtCompound))!!
//
//    override fun serialize(src: ItemStack, typeOfSrc: Type, context: JsonSerializationContext) = src.saveToJson()
//
//}