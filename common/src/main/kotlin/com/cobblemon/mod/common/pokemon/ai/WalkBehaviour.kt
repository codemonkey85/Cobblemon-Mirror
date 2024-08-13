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

data class WalkBehaviour(
    val canWalk: Boolean = true,
    val avoidsLand: Boolean = false,
    var walkSpeed: ExpressionLike = "0.35".asExpressionLike()
) {
    companion object {
        @JvmStatic
        val CODEC: Codec<WalkBehaviour> =  RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("canWalk", true).forGetter(WalkBehaviour::avoidsLand),
                Codec.BOOL.optionalFieldOf("avoidsLand", false).forGetter(WalkBehaviour::avoidsLand),
                ExpressionLike.CODEC.optionalFieldOf("walkSpeed", "0.35".asExpressionLike()).forGetter(WalkBehaviour::walkSpeed)
            ).apply(instance, ::WalkBehaviour)
        }
    }
}