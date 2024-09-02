/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.mojang.datafixers.util.Either
import java.lang.reflect.Type
import net.minecraft.resources.ResourceLocation

object NPCScriptAdapter : JsonDeserializer<Either<ResourceLocation, ExpressionLike>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Either<ResourceLocation, ExpressionLike> {
        return if (json.isJsonPrimitive) {
            try {
                val identifier = json.asString.asIdentifierDefaultingNamespace()
                Either.left(identifier)
            } catch (exception: Exception) {
                Either.right(json.asString.asExpressionLike())
            }
        } else {
            Either.right(context.deserialize(json, ExpressionLike::class.java))
        }
    }
}