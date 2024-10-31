/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.progress

import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgress
import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgressType
import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgressTypes
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.DamageTakenRequirement
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder

/**
 * An [EvolutionProgress] meant to keep track of damage taken in battle without fainting.
 *
 * @author Licious
 * @since January 28th, 2022
 */
class DamageTakenEvolutionProgress : EvolutionProgress<DamageTakenEvolutionProgress.Progress> {

    private var progress = Progress(0)

    override fun id(): ResourceLocation = ID

    override fun currentProgress(): Progress = this.progress

    override fun updateProgress(progress: Progress) {
        this.progress = progress
    }

    override fun reset() {
        this.updateProgress(Progress(0))
    }

    override fun shouldKeep(pokemon: Pokemon): Boolean = supports(pokemon)

    override fun type(): EvolutionProgressType<*> = EvolutionProgressTypes.DAMAGE_TAKEN

    data class Progress(val amount: Int)

    companion object {

        val ID = cobblemonResource(DamageTakenRequirement.ADAPTER_VARIANT)
        private const val AMOUNT = "amount"

        @JvmStatic
        val CODEC: MapCodec<DamageTakenEvolutionProgress> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.intRange(1, Int.MAX_VALUE).fieldOf(AMOUNT).forGetter { it.progress.amount }
            ).apply(instance) { amount -> DamageTakenEvolutionProgress().apply { updateProgress(Progress(amount)) } }
        }

        fun supports(pokemon: Pokemon): Boolean {
            return pokemon.form.evolutions.any { evolution ->
                evolution.requirements.any { requirement ->
                    requirement is DamageTakenRequirement
                }
            }
        }

    }

}