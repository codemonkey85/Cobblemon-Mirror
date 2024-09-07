/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.calculators

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent
import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokeball.PokeBall
import net.minecraft.world.entity.LivingEntity

/**
 * Used to process Pokémon captures.
 * This interface is here with the intention that several capture calculators can be created,
 * i.e. supporting an earlier generation capture system.
 *
 * To register a calculator in order to be used in the Cobblemon config use [CaptureCalculators.register].
 *
 * @author landonjw
 * @since November 30, 2021
 */
interface CaptureCalculator {
    /**
     * The literal ID of this calculator.
     * Used when registering the calculator to the registry in order to be used by the game rule.
     *
     * @return The literal ID of this calculator.
     */
    fun id(): String

    /**
     * Processes a capture attempt with the given params.
     *
     * @param thrower The [LivingEntity] that threw the [PokeBall].
     * @param pokeBallEntity The [EmptyPokeBallEntity] used.
     * @param target The target [PokemonEntity] attempting to be captured.
     * @return a [CaptureContext] that is the result of the capture attempt.
     */
    fun processCapture(thrower: LivingEntity, pokeBallEntity: EmptyPokeBallEntity, target: PokemonEntity) : CaptureContext

    fun getCatchRate(thrower: LivingEntity, pokeBallEntity: EmptyPokeBallEntity, target: PokemonEntity, catchRate: Float): Float {
        val event = PokemonCatchRateEvent(
            thrower = thrower,
            pokemonEntity = target,
            pokeBallEntity = pokeBallEntity,
            catchRate = catchRate
        )

        CobblemonEvents.POKEMON_CATCH_RATE.post(event)

        return event.catchRate
    }
}