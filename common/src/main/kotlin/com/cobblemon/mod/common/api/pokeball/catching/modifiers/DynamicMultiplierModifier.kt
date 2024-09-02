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
 * @property multiplier A lambda that determines value of the multiplier.
 * @property condition A lambda that checks if a [Pokemon] can have this multiplier applied.
 *
 * @author Licious
 * @since January 29th, 2023
 */
class DynamicMultiplierModifier(private val multiplier: (thrower: LivingEntity, pokemon: Pokemon) -> Float, private val condition: (thrower: LivingEntity, pokemon: Pokemon) -> Boolean) : CatchRateModifier {

    override fun isGuaranteed(): Boolean = false

    override fun value(thrower: LivingEntity, pokemon: Pokemon): Float = this.multiplier(thrower, pokemon)

    override fun behavior(thrower: LivingEntity, pokemon: Pokemon): CatchRateModifier.Behavior = CatchRateModifier.Behavior.MULTIPLY

    override fun isValid(thrower: LivingEntity, pokemon: Pokemon): Boolean = this.condition(thrower, pokemon)

    override fun modifyCatchRate(currentCatchRate: Float, thrower: LivingEntity, pokemon: Pokemon): Float {
        return if(this.isValid(thrower, pokemon)) {
            currentCatchRate * this.behavior(thrower, pokemon).mutator(currentCatchRate, this.value(thrower, pokemon))
        } else {
            currentCatchRate
        }
    }
}