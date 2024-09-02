/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import net.minecraft.world.phys.Vec3

object Vec3dAdapter : JsonDeserializer<Vec3> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): Vec3 {
        val array = json.asJsonArray
        return Vec3(array[0].asDouble, array[1].asDouble, array[2].asDouble)
    }
}