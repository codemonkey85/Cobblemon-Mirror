/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.modifiers

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.api.tags.CobblemonSpeciesTags
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.LivingEntity

/**
 * A [CatchRateModifier] based on the presence of [Species] [TagKey]s.
 * This is checked against [Pokemon.isTaggedBy].
 * 
 * @see [CobblemonSpeciesTags]
 *
 * @property multiplier The multiplier if the label is present.
 * @property matching Will this multiplier should be applied if the labels match?
 * @property tags The [TagKey]s being queried.
 */
class SpeciesTagModifier(
    val multiplier: Float,
    val matching: Boolean,
    vararg val tags: TagKey<Species>,
) : CatchRateModifier {

    override fun isGuaranteed(): Boolean = false

    override fun value(thrower: LivingEntity, pokemon: Pokemon): Float = this.multiplier

    override fun behavior(thrower: LivingEntity, pokemon: Pokemon): CatchRateModifier.Behavior = CatchRateModifier.Behavior.MULTIPLY

    override fun isValid(thrower: LivingEntity, pokemon: Pokemon): Boolean {
        val result = this.tags.any { pokemon.isTaggedBy(it) }
        return if (matching) result else !result
    }

    override fun modifyCatchRate(currentCatchRate: Float, thrower: LivingEntity, pokemon: Pokemon): Float = this.behavior(thrower, pokemon).mutator(currentCatchRate, this.value(thrower, pokemon))

}