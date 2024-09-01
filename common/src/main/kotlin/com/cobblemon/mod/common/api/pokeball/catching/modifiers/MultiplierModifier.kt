/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.modifiers

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.entity.LivingEntity

/**
 * A [CatchRateModifier] that just applies a basic multiplier if a [condition] is met.
 *
 * @property multiplier The value of the multiplier.
 * @property condition A lambda that checks if a [Pokemon] can have this multiplier applied.
 *
 * @author landonjw
 * @since  December 1st, 2021
 */
class MultiplierModifier(private val multiplier: Float, private val condition: (thrower: LivingEntity, pokemon: Pokemon) -> Boolean = { _, _ -> true }) : CatchRateModifier {

    override fun isGuaranteed(): Boolean = false

    override fun value(thrower: LivingEntity, pokemon: Pokemon): Float = this.multiplier

    override fun behavior(thrower: LivingEntity, pokemon: Pokemon): CatchRateModifier.Behavior = CatchRateModifier.Behavior.MULTIPLY

    override fun isValid(thrower: LivingEntity, pokemon: Pokemon): Boolean = this.condition(thrower, pokemon)

    override fun modifyCatchRate(currentCatchRate: Float, thrower: LivingEntity, pokemon: Pokemon): Float {
        return if(this.isValid(thrower, pokemon)) {
            this.behavior(thrower, pokemon).mutator(currentCatchRate, this.value(thrower, pokemon))
        } else {
            currentCatchRate
        }
    }
}