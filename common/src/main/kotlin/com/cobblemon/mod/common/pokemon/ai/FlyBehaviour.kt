/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.util.asExpressionLike
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class FlyBehaviour(
    val canFly: Boolean = false,
    val flySpeedHorizontal: ExpressionLike = "0.3".asExpressionLike()
) {
    companion object {
        @JvmStatic
        val CODEC: Codec<FlyBehaviour> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("canFly", false).forGetter(FlyBehaviour::canFly),
                ExpressionLike.CODEC.optionalFieldOf("flySpeedHorizontal", "0.3".asExpressionLike()).forGetter(FlyBehaviour::flySpeedHorizontal)
            ).apply(instance, ::FlyBehaviour)
        }
    }
}