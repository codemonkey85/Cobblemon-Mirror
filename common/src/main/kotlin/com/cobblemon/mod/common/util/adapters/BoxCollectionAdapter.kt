/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.block.BerryBlock
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import net.minecraft.world.phys.AABB

object BoxCollectionAdapter : JsonDeserializer<Collection<AABB>> {
    val boxesByName = mutableMapOf<String, Collection<AABB>>()

    init {
        boxesByName["standard-sprout"] = BerryBlock.STANDARD_SPROUT
        boxesByName["standard-mature"] = BerryBlock.STANDARD_MATURE
        boxesByName["short-sprout"] = BerryBlock.SHORT_SPROUT
        boxesByName["short-mature"] = BerryBlock.SHORT_MATURE
        boxesByName["volcano-sprout"] = BerryBlock.VOLCANO_SPROUT
        boxesByName["volcano-mature"] = BerryBlock.VOLCANO_MATURE
        boxesByName["nest-sprout"] = BerryBlock.NEST_SPROUT
        boxesByName["nest-mature"] = BerryBlock.NEST_MATURE
        boxesByName["frill-sprout"] = BerryBlock.FRILL_SPROUT
        boxesByName["frill-mature"] = BerryBlock.FRILL_MATURE
        boxesByName["block-sprout"] = BerryBlock.BLOCK_SPROUT
        boxesByName["block-mature"] = BerryBlock.BLOCK_MATURE
        boxesByName["pyramid-sprout"] = BerryBlock.PYRAMID_SPROUT
        boxesByName["pyramid-mature"] = BerryBlock.PYRAMID_MATURE
        boxesByName["tail-sprout"] = BerryBlock.TAIL_SPROUT
        boxesByName["tail-mature"] = BerryBlock.TAIL_MATURE
        boxesByName["sword-sprout"] = BerryBlock.SWORD_SPROUT
        boxesByName["sword-mature"] = BerryBlock.SWORD_MATURE
        boxesByName["platform-sprout"] = BerryBlock.PLATFORM_SPROUT
        boxesByName["platform-mature"] = BerryBlock.PLATFORM_MATURE
        boxesByName["stand-sprout"] = BerryBlock.STAND_SPROUT
        boxesByName["stand-mature"] = BerryBlock.STAND_MATURE
        boxesByName["cone-sprout"] = BerryBlock.CONE_SPROUT
        boxesByName["cone-mature"] = BerryBlock.CONE_MATURE
        boxesByName["squat-sprout"] = BerryBlock.SQUAT_SPROUT
        boxesByName["squat-mature"] = BerryBlock.SQUAT_MATURE
        boxesByName["lantern-sprout"] = BerryBlock.LANTERN_SPROUT
        boxesByName["lantern-mature"] = BerryBlock.LANTERN_MATURE
        boxesByName["box-sprout"] = BerryBlock.BOX_SPROUT
        boxesByName["box-mature"] = BerryBlock.BOX_MATURE
        boxesByName["blossom-sprout"] = BerryBlock.BLOSSOM_SPROUT
        boxesByName["blossom-mature"] = BerryBlock.BLOSSOM_MATURE
        boxesByName["lilypad-sprout"] = BerryBlock.LILYPAD_SPROUT
        boxesByName["lilypad-mature"] = BerryBlock.LILYPAD_MATURE
        boxesByName["tall-sprout"] = BerryBlock.TALL_SPROUT
        boxesByName["tall-mature"] = BerryBlock.TALL_MATURE


    }

    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): Collection<AABB> {
        if (json.isJsonPrimitive)  {
            return boxesByName[json.asString] ?: throw IllegalArgumentException("Unrecognized box collection name: ${json.asString}")
        } else {
            return json.asJsonArray.map { ctx.deserialize<AABB>(it, AABB::class.java) }.toList()
        }
    }
}