/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag

@JvmName("toJsonArrayString")
fun Collection<String>.toJsonArray(): JsonArray {
    val array = JsonArray()
    if (isEmpty())
        return array
    forEach { array.add(it) }
    return array
}

@JvmName("toJsonArrayBoolean")
fun Collection<Boolean>.toJsonArray(): JsonArray {
    val array = JsonArray()
    if (isEmpty())
        return array
    forEach { array.add(it) }
    return array
}

@JvmName("toJsonArrayNumber")
fun Collection<Number>.toJsonArray(): JsonArray {
    val array = JsonArray()
    if (isEmpty())
        return array
    forEach { array.add(it) }
    return array
}

@JvmName("toJsonArrayJsonElement")
fun Collection<JsonElement>.toJsonArray(): JsonArray {
    val array = JsonArray()
    if (isEmpty())
        return array
    forEach { array.add(it) }
    return array
}

fun JsonObject.isEmpty() = size() <= 0

fun JsonObject.isNotEmpty() = size() > 0

fun JsonElement.asNbt(): Tag = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, this)

fun <T : Enum<T>> Array<T>.getFromJSON(element: JsonElement, name: String): T {
    val type = (element as JsonObject).get(name).asString
    return first { type.equals(it.name, ignoreCase = true) }
}

fun JsonObject.getFirst(vararg names: String): JsonElement? {
    for (name in names) {
        val element = get(name)
        if (element != null) {
            return element
        }
    }
    return null
}

fun JsonArray.asExpressionLike(): ExpressionLike {
    return map { it.asString }.asExpressionLike()
}