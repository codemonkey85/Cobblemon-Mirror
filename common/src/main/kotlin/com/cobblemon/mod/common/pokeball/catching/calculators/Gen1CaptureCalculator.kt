/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokeball.catching.calculators

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.BurnStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.FrozenStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.ParalysisStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.PoisonBadlyStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.PoisonStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.SleepStatus
import kotlin.math.roundToInt
import kotlin.random.Random
import net.minecraft.world.entity.LivingEntity

/**
 * An implementation of the capture calculator used in the generation 1 games.
 * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_I.29) page.
 *
 * @author Licious
 * @since January 29th, 2022
 */
object Gen1CaptureCalculator : CaptureCalculator {

    private const val FRZ_SLEEP_THRESHOLD = 25
    private const val PARA_BRN_PSN_THRESHOLD = 12

    override fun id(): String = "generation_1"

    override fun processCapture(thrower: LivingEntity, pokeBallEntity: EmptyPokeBallEntity, target: PokemonEntity): CaptureContext {
        val pokeBall = pokeBallEntity.pokeBall
        val pokemon = target.pokemon
        if (pokeBall.catchRateModifier.isGuaranteed()) {
            return CaptureContext.successful()
        }
        // This value can be reused when calculating the shakes in case of breakout.
        val nBound = when (pokeBall) {
            PokeBalls.POKE_BALL -> 255
            PokeBalls.GREAT_BALL -> 200
            else -> 150
        }
        val n = Random.nextInt(nBound + 1)
        // Hiro note: usedThreshold makes no sense here, it's never actually used because
        // we return in both places that it was set. What the hell was the point? Someone figure this out.
        var usedThreshold = 0
        val status = pokemon.status?.status
        if ((status is FrozenStatus || status is SleepStatus) && n < FRZ_SLEEP_THRESHOLD) {
            usedThreshold = FRZ_SLEEP_THRESHOLD
            return CaptureContext.successful()
        }
        else if ((status is ParalysisStatus || status is BurnStatus || status is PoisonStatus || status is PoisonBadlyStatus) && n < PARA_BRN_PSN_THRESHOLD) {
            usedThreshold = PARA_BRN_PSN_THRESHOLD
            return CaptureContext.successful()
        }
        if (n - usedThreshold > pokemon.form.catchRate) {
            return CaptureContext(numberOfShakes = 0, isSuccessfulCapture = false, isCriticalCapture = false)
        }
        val m = Random.nextInt(256)
        val ballValue = when (pokeBall) {
            PokeBalls.GREAT_BALL -> 8F
            else -> 12F
        }
        val f = ((pokemon.maxHealth * 255F * 4F) / (pokemon.currentHealth * ballValue)).coerceIn(1F, 255F).roundToInt()
        if (f >= m) {
            return CaptureContext.successful()
        }
        return CaptureContext(numberOfShakes = this.calculateShakes(pokemon, getCatchRate(thrower, pokeBallEntity, target, pokemon.form.catchRate.toFloat()), nBound, f), isSuccessfulCapture = false, isCriticalCapture = false)
    }

    private fun calculateShakes(pokemon: Pokemon, catchRate: Float, ballValue: Int, f: Int): Int {
        val d = (catchRate * 100F) / ballValue
        if (d >= 256) {
            return 3
        }
        val s = when (pokemon.status?.status) {
            is FrozenStatus, is SleepStatus -> 10
            is ParalysisStatus, is BurnStatus, is PoisonStatus, is PoisonBadlyStatus -> 5
            else -> 0
        }
        val x = ((d * f) / 255) + s
        return when {
            x < 10 -> 0
            x < 30 -> 1
            x < 70 -> 2
            else -> 3
        }
    }

}