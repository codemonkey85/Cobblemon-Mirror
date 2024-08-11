/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.effect

import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.Codec
import net.minecraft.server.level.ServerPlayer

/**
 * Interface for all ShoulderEffects
 *
 * @author Qu
 * @since 2022-01-26
 */
interface ShoulderEffect {
    fun applyEffect(pokemon: Pokemon, player: ServerPlayer, isLeft: Boolean)
    fun removeEffect(pokemon: Pokemon, player: ServerPlayer, isLeft: Boolean)
    fun type(): ShoulderEffectType<*>

    companion object {
        @JvmStatic
        val CODEC: Codec<ShoulderEffect> = ShoulderEffectType.REGISTRY
            .byNameCodec()
            .dispatch(ShoulderEffect::type) { it.codec() }
    }
}