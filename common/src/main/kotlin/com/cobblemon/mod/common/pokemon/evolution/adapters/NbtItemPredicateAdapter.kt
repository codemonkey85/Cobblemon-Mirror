/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.adapters

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.pokemon.evolution.predicate.NbtItemPredicate
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.mojang.serialization.JsonOps
import net.minecraft.advancements.critereon.NbtPredicate
import net.minecraft.world.item.Item
import java.lang.reflect.Type

object NbtItemPredicateAdapter : JsonDeserializer<NbtItemPredicate>, JsonSerializer<NbtItemPredicate> {

    private const val ITEM = "item"
    private const val NBT = "nbt"
    private val CONDITION_TYPE = TypeToken.getParameterized(RegistryLikeCondition::class.java, Item::class.java).type

    override fun deserialize(jElement: JsonElement, type: Type, context: JsonDeserializationContext): NbtItemPredicate {
        if (jElement.isJsonPrimitive) {
            return NbtItemPredicate(context.deserialize(jElement, CONDITION_TYPE))
        }
        val jObject = jElement.asJsonObject
        val itemCondition = context.deserialize<RegistryLikeCondition<Item>>(jObject.get(ITEM), CONDITION_TYPE)
        val nbtPredicate = NbtPredicate.CODEC.decode(JsonOps.INSTANCE, jObject.get(NBT)).result().get().first
        return NbtItemPredicate(itemCondition, nbtPredicate)
    }

    override fun serialize(predicate: NbtItemPredicate, type: Type, context: JsonSerializationContext): JsonElement {
        val serializedItemCondition = context.serialize(predicate.item, CONDITION_TYPE)
        if (predicate.nbt == null) {
            return serializedItemCondition
        }
        return JsonObject().apply {
            add(ITEM, serializedItemCondition)
            add(NBT, NbtPredicate.CODEC.encode(predicate.nbt, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).result().get())
        }
    }

}