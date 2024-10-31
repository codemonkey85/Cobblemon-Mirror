/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokeball.catching.calculators

import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.BurnStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.FrozenStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.ParalysisStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.PoisonBadlyStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.PoisonStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.SleepStatus
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random
import net.minecraft.world.entity.LivingEntity

/**
 * An implementation of the capture calculator used in the generation 1 games.
 * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Catch_rate#Capture_method_.28Generation_II.29) page.
 *
 * @property bugsFixed Should the bug with the bonusStatus in the formula be fixed? Having this on false is how the games shipped out.
 *
 * @author Licious
 * @since January 29th, 2022
 */
class Gen2CaptureCalculator(val bugsFixed: Boolean) : CaptureCalculator {

    override fun id(): String = "generation_2" + if (this.bugsFixed) "_fixed" else ""

    override fun processCapture(thrower: LivingEntity, pokeBallEntity: EmptyPokeBallEntity, target: PokemonEntity): CaptureContext {
        val pokeBall = pokeBallEntity.pokeBall
        val pokemon = target.pokemon
        if (pokeBall.catchRateModifier.isGuaranteed()) {
            return CaptureContext.successful()
        }
        val catchRate = getCatchRate(thrower, pokeBallEntity, target, pokemon.form.catchRate.toFloat())
        val modifiedRate = if (pokeBall.catchRateModifier.isValid(thrower, pokemon)) pokeBall.catchRateModifier.modifyCatchRate(catchRate, thrower, pokemon) else catchRate
        val status = pokemon.status?.status
        val bonusStatus = when {
            status is SleepStatus || status is FrozenStatus -> 10
            this.bugsFixed && (status is ParalysisStatus || status is BurnStatus || status is PoisonStatus || status is PoisonBadlyStatus) -> 5
            else -> 1
        }
        val modifiedCatchRate = max((((3F * pokemon.maxHealth - 2F * pokemon.currentHealth) * modifiedRate) / (3F * pokemon.maxHealth)) + bonusStatus, 1F).coerceAtMost(255F).roundToInt()
        if (Random.nextInt(256) <= modifiedCatchRate) {
            return CaptureContext.successful()
        }
        val shakeProbability = when {
            modifiedCatchRate <= 1 -> 63
            modifiedCatchRate == 2 -> 75
            modifiedCatchRate == 3 -> 84
            modifiedCatchRate == 4 -> 90
            modifiedCatchRate == 5 -> 95
            modifiedCatchRate <= 7 -> 103
            modifiedCatchRate <= 10 -> 113
            modifiedCatchRate <= 15 -> 126
            modifiedCatchRate <= 20 -> 134
            modifiedCatchRate <= 30 -> 149
            modifiedCatchRate <= 40 -> 160
            modifiedCatchRate <= 50 -> 169
            modifiedCatchRate <= 60 -> 177
            modifiedCatchRate <= 80 -> 191
            modifiedCatchRate <= 100 -> 201
            modifiedCatchRate <= 120 -> 211
            modifiedCatchRate <= 140 -> 200
            modifiedCatchRate <= 160 -> 227
            modifiedCatchRate <= 180 -> 234
            modifiedCatchRate <= 200 -> 240
            modifiedCatchRate <= 220 -> 246
            modifiedCatchRate <= 240 -> 251
            modifiedCatchRate <= 254 -> 253
            else -> 255
        }
        var shakes = 0
        repeat(3) {
            if (Random.nextInt(256) >= shakeProbability) {
                return CaptureContext(numberOfShakes = shakes, isSuccessfulCapture = false, isCriticalCapture = false)
            }
            shakes++
        }
        return CaptureContext.successful()
    }

}