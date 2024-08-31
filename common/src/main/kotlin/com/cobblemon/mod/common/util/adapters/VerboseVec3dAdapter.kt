/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.google.gson.*
import net.minecraft.world.phys.Vec3
import java.lang.reflect.Type

object VerboseVec3dAdapter : JsonDeserializer<Vec3>, JsonSerializer<Vec3> {

    private const val X = "x"
    private const val Y = "y"
    private const val Z = "z"

    override fun deserialize(jElement: JsonElement, type: Type, context: JsonDeserializationContext): Vec3 {
        val json = jElement.asJsonObject
        val x = json.get(X).asDouble
        val y = json.get(Y).asDouble
        val z = json.get(Z).asDouble
        return Vec3(x, y, z)
    }

    override fun serialize(vec: Vec3, type: Type, context: JsonSerializationContext) = JsonObject().apply {
        addProperty(X, vec.x)
        addProperty(Y, vec.y)
        addProperty(Z, vec.z)
    }

}