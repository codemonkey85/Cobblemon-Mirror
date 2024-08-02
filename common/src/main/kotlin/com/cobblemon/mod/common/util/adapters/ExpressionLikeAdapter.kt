/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.ListExpression
import com.cobblemon.mod.common.api.molang.SingleExpression
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.asExpressions
import com.cobblemon.mod.common.util.endWith
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * An adapter that can take a single string or an array of strings can created an [ExpressionLike], something that
 * can be given a MoLangRuntime to produce MoValues.
 *
 * @author Hiroku
 * @since October 22nd, 2023
 */
object ExpressionLikeAdapter : JsonDeserializer<ExpressionLike> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): ExpressionLike {
        return if (json.isJsonPrimitive) {
            json.asString.asExpressionLike()
        } else if (json.isJsonArray) {
            json.asJsonArray.asExpressionLike()
        } else {
            throw IllegalArgumentException("Invalid expression JSON: $json")
        }
    }
}
