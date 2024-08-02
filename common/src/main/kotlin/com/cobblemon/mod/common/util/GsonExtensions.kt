/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.Reader

inline fun <reified T> Gson.fromJson(reader: Reader) = fromJson(reader, T::class.java)
inline fun <reified T> Gson.fromJson(element: JsonElement) = fromJson(element, T::class.java)
inline fun <reified T> Gson.fromJson(string: String) = fromJson(string, T::class.java)

/**
 * A simple trick function for allowing a JSON to have specified a single value or a list of values.
 * This function will look for the singular-named field and, if it is defined, copy its value into
 * the list value identified by the plural name. During runtime only the plural exists, but when
 * configuring the JSON the user can use the singular form for a cleaner document.
 */
fun JsonObject.singularToPluralList(rootName: String, pluralName: String = "${rootName}s"): JsonObject {
    if (has(rootName)) {
        if (!has(pluralName)) {
            add(pluralName, JsonArray())
        }
        get(pluralName).asJsonArray.add(get(rootName))
        remove(rootName)
    }
    return this
}

fun JsonElement.normalizeToArray(): JsonArray {
    if (this is JsonArray) {
        return this
    } else {
        val array = JsonArray()
        array.add(this)
        return array
    }
}