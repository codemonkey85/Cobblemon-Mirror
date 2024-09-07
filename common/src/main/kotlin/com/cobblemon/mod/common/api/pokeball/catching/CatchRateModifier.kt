/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching

import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.entity.LivingEntity

/**
 * A modifier responsible for handling any catch rate calculations a [PokeBall] may impact in a [CaptureCalculator].
 *
 * @author landonjw
 * @since  November 30, 2021
 */
interface CatchRateModifier {

    /**
     * Checks if this modifier guarantees a capture.
     *
     * @return True if this modifier guarantees a capture.
     */
    fun isGuaranteed() = false

    /**
     * Returns the value of this modifier.
     *
     * @param thrower The [LivingEntity] that threw the [PokeBall].
     * @param pokemon The target [Pokemon] attempting to be captured.
     * @return The value of this modifier.
     */
    fun value(thrower: LivingEntity, pokemon: Pokemon): Float

    /**
     * Returns the [Behavior] of this modifier.
     *
     * @param thrower The [LivingEntity] that threw the [PokeBall].
     * @param pokemon The target [Pokemon] attempting to be captured.
     * @return The [Behavior] of this modifier.
     */
    fun behavior(thrower: LivingEntity, pokemon: Pokemon): Behavior

    /**
     * Checks if this modifier can be used with the given params.
     *
     * @param thrower The [LivingEntity] that threw the [PokeBall].
     * @param pokemon The target [Pokemon] attempting to be captured.
     * @return If this multiplier can be used with the given params.
     */
    fun isValid(thrower: LivingEntity, pokemon: Pokemon): Boolean

    /**
     * Directly mutates a catch rate value with this modifier.
     * This is mostly unused across the different generations but Cobblemon custom formula relies on it.
     * It is not guaranteed that [isValid] is true.
     *
     * @param currentCatchRate The current value of the catch rate.
     * @param thrower The [LivingEntity] that threw the [PokeBall].
     * @param pokemon The target [Pokemon] attempting to be captured.
     * @return The modified catch rate.
     */
    fun modifyCatchRate(currentCatchRate: Float, thrower: LivingEntity, pokemon: Pokemon): Float

    /**
     * The behavior of a [CatchRateModifier] when being used in a [CaptureCalculator].
     *
     * @property mutator A lambda that takes an input and mutates it using the [CatchRateModifier.value].
     *
     * @author Licious
     * @since January 29th, 2022
     */
    enum class Behavior(val mutator: (input: Float, value: Float) -> Float) {

        ADD({ input, value -> input + value }),
        SUBTRACT({ input, value -> input - value }),
        MULTIPLY({ input, value -> input * value }),
        DIVIDE({ input, value -> input / value })

    }

    companion object {

        /**
         * A dummy implementation that never allows the application to succeed.
         * This is just used for a default value to work with GSON.
         */
        internal val DUMMY = object : CatchRateModifier {
            override fun value(thrower: LivingEntity, pokemon: Pokemon): Float = 1F
            override fun behavior(thrower: LivingEntity, pokemon: Pokemon): Behavior = Behavior.ADD
            override fun isValid(thrower: LivingEntity, pokemon: Pokemon): Boolean = false
            override fun modifyCatchRate(currentCatchRate: Float, thrower: LivingEntity, pokemon: Pokemon): Float = 1F
        }

    }

}