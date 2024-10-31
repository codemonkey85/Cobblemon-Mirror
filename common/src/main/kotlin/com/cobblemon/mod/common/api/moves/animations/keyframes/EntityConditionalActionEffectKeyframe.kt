/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.animations.keyframes

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.entity.PosableEntity
import net.minecraft.world.entity.Entity

/**
 * An action effect keyframe that plays for all entities for which the condition is true.
 *
 * @author Hiroku
 * @since January 21st, 2024
 */
interface EntityConditionalActionEffectKeyframe {
    val entityCondition: ExpressionLike
    fun test(context: ActionEffectContext, entity: Entity, isUser: Boolean): Boolean {
        context.runtime.environment.query
            .addFunction("entity") {
                if (entity is PosableEntity) {
                    entity.struct
                } else {
                    QueryStruct(hashMapOf())
                }.addFunction("is_user") { DoubleValue(isUser) } // Deprecated tbh
            }
            .addFunction("is_user") { DoubleValue(isUser) }

        return entityCondition.resolveBoolean(context.runtime)
    }
}