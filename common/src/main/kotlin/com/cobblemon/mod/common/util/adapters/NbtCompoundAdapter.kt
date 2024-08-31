/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.google.gson.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.SnbtPrinterTagVisitor
import java.lang.reflect.Type

/**
 * An adapter that handles an [CompoundTag] using string conversion.
 *
 * @author Hiroku
 * @since July 25th, 2022
 */
object NbtCompoundAdapter : JsonDeserializer<CompoundTag>, JsonSerializer<CompoundTag> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext) = NbtUtils.snbtToStructure(json.asString)
    override fun serialize(nbt: CompoundTag, type: Type, ctx: JsonSerializationContext): JsonElement {
        return JsonPrimitive(SnbtPrinterTagVisitor("", 0, mutableListOf()).visit(nbt))
    }
}