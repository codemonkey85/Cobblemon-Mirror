/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class FlyBehaviour(
    val canFly: Boolean = false,
    val flySpeedHorizontal: Float = 0.3F
) {

    companion object {

        @JvmField
        val CODEC: Codec<FlyBehaviour> = RecordCodecBuilder.create { builder ->
            builder.group(
                Codec.BOOL.optionalFieldOf("canFly", false).forGetter(FlyBehaviour::canFly),
                Codec.FLOAT.optionalFieldOf("flySpeedHorizontal", 0.3F).forGetter(FlyBehaviour::flySpeedHorizontal),
            ).apply(builder, ::FlyBehaviour)
        }

    }

}