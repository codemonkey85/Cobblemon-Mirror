/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import java.lang.reflect.Type
import com.google.gson.JsonElement
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.schedule.Activity

object ActivityAdapter : JsonDeserializer<Activity> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Activity {
        val identifier = json.asString.asIdentifierDefaultingNamespace("minecraft")
        return BuiltInRegistries.ACTIVITY.get(identifier)
            ?: throw IllegalArgumentException("Unknown activity: $identifier")
    }
}