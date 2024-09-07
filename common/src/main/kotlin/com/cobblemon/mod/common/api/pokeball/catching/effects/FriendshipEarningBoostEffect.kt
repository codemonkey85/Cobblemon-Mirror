/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.effects

import com.cobblemon.mod.common.api.pokeball.catching.CaptureEffect
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.entity.LivingEntity

/**
 * A [CaptureEffect] meant to boost earnings of friendship.
 * This effect doesn't do anything during capture instead it will affect any positive friendship gain for the caught Pokémon.
 *
 * @property multiplier The multiplier for friendship gains
 */
class FriendshipEarningBoostEffect(val multiplier: Float) : CaptureEffect {
    override fun apply(thrower: LivingEntity, pokemon: Pokemon) {}
}